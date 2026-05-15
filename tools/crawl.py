"""data.go.kr 공공서비스(혜택) 정보 API 클라이언트 — 행정안전부/정부24.

엔드포인트 (Base: https://api.odcloud.kr/api):
- GET /gov24/v3/serviceList      — 페이징 목록 (서비스명/소관기관/사용자구분/서비스분야 필터)
- GET /gov24/v3/serviceDetail    — 단건 상세 (cond[서비스ID::EQ]=...)
- GET /gov24/v3/supportConditions — 구조화 자격 조건 (JA0xxx 코드)

인증:
- 환경변수 DATA_GO_KR_API_KEY (Decoding 원본 키 권장)
- requests 라이브러리가 params 패턴에서 자동 URL 인코딩 → 이중인코딩 방지

설계 원칙:
- requests.Session으로 keep-alive (페이징 다중 호출 효율)
- 보수적 retry (3회, 지수 백오프)
- per-call 0.3s sleep (rate limit 매너)
"""

from __future__ import annotations

import logging
import os
import time
from dataclasses import dataclass
from typing import Any, Dict, Iterable, List, Optional

import requests

log = logging.getLogger(__name__)


class GovApiError(RuntimeError):
    """정부 API 호출 실패. retry 후에도 못 되면 raise."""


@dataclass(frozen=True)
class RawPolicy:
    """단일 정책의 원본 3종 묶음 — normalize 단계 입력."""

    list_row: Dict[str, Any]
    detail: Optional[Dict[str, Any]]
    conditions: Optional[Dict[str, Any]]

    @property
    def service_id(self) -> str:
        return str(self.list_row.get("서비스ID") or "")


class GovApiClient:
    BASE_URL = "https://api.odcloud.kr/api"
    REQUEST_GAP_SEC = 0.3

    def __init__(
        self,
        service_key: str,
        *,
        timeout: int = 15,
        max_retry: int = 3,
    ):
        if not service_key:
            raise ValueError("DATA_GO_KR_API_KEY is empty")
        self._key = service_key
        self._timeout = timeout
        self._max_retry = max_retry
        self._session = requests.Session()
        self._session.headers.update({"Accept": "application/json"})

    def _get(self, path: str, params: Dict[str, Any]) -> Dict[str, Any]:
        url = f"{self.BASE_URL}{path}"
        merged: Dict[str, Any] = {
            "serviceKey": self._key,
            "returnType": "JSON",
        }
        merged.update(params)

        last_exc: Optional[Exception] = None
        for attempt in range(1, self._max_retry + 1):
            try:
                resp = self._session.get(url, params=merged, timeout=self._timeout)
                resp.raise_for_status()
                body = resp.json()
                if not isinstance(body, dict):
                    raise ValueError(f"unexpected response type: {type(body).__name__}")
                return body
            except (requests.RequestException, ValueError) as e:
                log.warning(
                    "attempt %d/%d failed for %s (%s): %s",
                    attempt,
                    self._max_retry,
                    path,
                    params,
                    e,
                )
                last_exc = e
                if attempt < self._max_retry:
                    time.sleep(1.5 * attempt)
        raise GovApiError(f"GET {path} failed after {self._max_retry} attempts: {last_exc}")

    # ────────────────────────────────────────────────────────────────────────
    # serviceList
    # ────────────────────────────────────────────────────────────────────────
    def list_services(
        self,
        *,
        page: int = 1,
        per_page: int = 50,
        service_field: Optional[str] = None,
        user_type: Optional[str] = None,
    ) -> Dict[str, Any]:
        """페이징 목록 조회. 응답: {page, perPage, totalCount, matchCount, data: [...]}"""
        params: Dict[str, Any] = {"page": page, "perPage": per_page}
        if service_field:
            params["cond[서비스분야::LIKE]"] = service_field
        if user_type:
            params["cond[사용자구분::LIKE]"] = user_type
        return self._get("/gov24/v3/serviceList", params)

    def iter_services(
        self,
        *,
        limit: Optional[int] = None,
        per_page: int = 100,
        user_type: Optional[str] = None,
        service_field: Optional[str] = None,
        client_filter_user_type: Optional[str] = None,
    ) -> Iterable[Dict[str, Any]]:
        """모든 서비스를 페이징으로 yield. limit 도달 시 조기 종료.

        user_type: 서버 측 cond[사용자구분::LIKE] 필터. 한국어 키 URL 인코딩 문제로
        실제로는 동작 안 할 가능성 있음(R2.7.5 검증).
        client_filter_user_type: 응답 받은 후 클라이언트 측에서 필터링 — 확실히 동작.
        예: '개인' → '사용자구분' 필드에 '개인' 포함된 행만 yield.
        둘 다 동시 사용 가능 (서버 필터 시도 + 클라 보강).
        """
        fetched = 0
        scanned = 0
        page = 1
        while True:
            body = self.list_services(
                page=page,
                per_page=per_page,
                user_type=user_type,
                service_field=service_field,
            )
            data = body.get("data") or []
            if not data:
                break
            for row in data:
                scanned += 1
                if client_filter_user_type:
                    row_value = str(row.get("사용자구분") or "")
                    if client_filter_user_type not in row_value:
                        continue
                yield row
                fetched += 1
                if limit is not None and fetched >= limit:
                    log.info("client filter: %d yielded / %d scanned", fetched, scanned)
                    return
            total_count = int(body.get("totalCount") or 0)
            if scanned >= total_count:
                break
            page += 1
            time.sleep(self.REQUEST_GAP_SEC)
        if client_filter_user_type:
            log.info("client filter done: %d yielded / %d scanned", fetched, scanned)

    # ────────────────────────────────────────────────────────────────────────
    # serviceDetail
    # ────────────────────────────────────────────────────────────────────────
    def get_detail(self, service_id: str) -> Optional[Dict[str, Any]]:
        body = self._get(
            "/gov24/v3/serviceDetail",
            {
                "page": 1,
                "perPage": 1,
                "cond[서비스ID::EQ]": service_id,
            },
        )
        data = body.get("data") or []
        return data[0] if data else None

    # ────────────────────────────────────────────────────────────────────────
    # supportConditions
    # ────────────────────────────────────────────────────────────────────────
    def get_conditions(self, service_id: str) -> Optional[Dict[str, Any]]:
        body = self._get(
            "/gov24/v3/supportConditions",
            {
                "page": 1,
                "perPage": 1,
                "cond[서비스ID::EQ]": service_id,
            },
        )
        data = body.get("data") or []
        return data[0] if data else None

    # ────────────────────────────────────────────────────────────────────────
    # 묶음 fetch
    # ────────────────────────────────────────────────────────────────────────
    def fetch_full(self, service_id: str, *, list_row: Dict[str, Any]) -> RawPolicy:
        """단일 서비스ID에 대해 list_row + detail + conditions 묶어서 반환."""
        detail = self.get_detail(service_id)
        time.sleep(self.REQUEST_GAP_SEC)
        cond = self.get_conditions(service_id)
        time.sleep(self.REQUEST_GAP_SEC)
        return RawPolicy(list_row=list_row, detail=detail, conditions=cond)


def fetch_policies(
    client: GovApiClient,
    *,
    limit: int = 30,
    per_page: int = 50,
    user_type: Optional[str] = None,
) -> List[RawPolicy]:
    """limit 개수의 정책에 대해 list+detail+conditions 묶음을 받아 리스트로 반환.

    user_type: 사용자구분 필터. 서버 측 cond[사용자구분::LIKE]와 클라이언트 측 필터
    둘 다 적용 — 서버 필터가 한국어 키 인코딩 문제로 무력해도 클라 필터가 잡아냄.
    """
    raws: List[RawPolicy] = []
    iterator = client.iter_services(
        limit=limit,
        per_page=max(per_page, 100),  # 클라 필터로 많이 걸러질 수 있어 더 큰 페이지
        user_type=user_type,
        client_filter_user_type=user_type,
    )
    for row in iterator:
        svc_id = str(row.get("서비스ID") or "").strip()
        if not svc_id:
            log.warning("skipped row without 서비스ID: %r", row)
            continue
        try:
            raw = client.fetch_full(svc_id, list_row=row)
        except GovApiError as e:
            log.warning("skip %s — fetch_full failed: %s", svc_id, e)
            continue
        raws.append(raw)
        log.info("fetched %d/%d — %s (%s)", len(raws), limit, row.get("서비스명"), svc_id)
    return raws


def build_client_from_env() -> Optional[GovApiClient]:
    key = os.environ.get("DATA_GO_KR_API_KEY", "").strip()
    if not key:
        return None
    return GovApiClient(service_key=key)
