"""정부 API 원본(RawPolicy) → 우리 Policy 스키마 정규화.

두 단계 합성:
- 결정론 단계: supportConditions의 JA0xxx 구조화 코드 → EligibilityRule 직매핑
- LLM 단계: 비정형 텍스트(지원내용/구비서류/신청방법/지원대상) → eligibility/documents/procedure/amount/category/period 추출

LLM 단계 실패해도 결정론 단계 결과만으로 valid Policy 생산. 빌드 절대 안 깨짐.
"""

from __future__ import annotations

import json
import logging
import re
from typing import Any, Dict, List, Optional

from crawl import RawPolicy

log = logging.getLogger(__name__)


# ────────────────────────────────────────────────────────────────────────────────
# 결정론적 매핑
# ────────────────────────────────────────────────────────────────────────────────


def _yn(val: Any) -> bool:
    """API 응답의 'Y'/'N'/공백을 boolean으로. truthy 값 다양함에 관용."""
    if val is None:
        return False
    s = str(val).strip().upper()
    return s in {"Y", "TRUE", "1", "O"}


def conditions_to_eligibility_rule(cond: Optional[Dict[str, Any]]) -> Optional[Dict[str, Any]]:
    """supportConditions 응답 → EligibilityRule 딕셔너리. 비어있으면 None."""
    if not cond:
        return None
    rule: Dict[str, Any] = {}

    # 연령
    min_age = cond.get("JA0110")
    max_age = cond.get("JA0111")
    try:
        if min_age not in (None, "", 0, "0"):
            rule["minAge"] = int(min_age)
    except (TypeError, ValueError):
        pass
    try:
        if max_age not in (None, "", 0, "0"):
            rule["maxAge"] = int(max_age)
    except (TypeError, ValueError):
        pass

    # 직업/신분
    occupations: List[str] = []
    student_codes = ("JA0317", "JA0318", "JA0319", "JA0320")
    if any(_yn(cond.get(c)) for c in student_codes):
        occupations.append("학생")
    if _yn(cond.get("JA0326")):
        occupations.append("직장인")
    if _yn(cond.get("JA0327")):
        occupations.append("구직 중")
    if occupations:
        # 중복 제거 + 순서 보존
        seen: List[str] = []
        for o in occupations:
            if o not in seen:
                seen.append(o)
        rule["requiresOccupation"] = seen

    # 출산/육아
    if _yn(cond.get("JA0303")):
        rule["requiresChildren"] = True

    # 가족 구성 — 한부모/조손은 우리 모델에 대응 없음, 일단 무시
    # 결혼 여부도 supportConditions에 명시 코드 없어 LLM에 위임

    return rule or None


# ────────────────────────────────────────────────────────────────────────────────
# slug / id
# ────────────────────────────────────────────────────────────────────────────────


_NON_SLUG = re.compile(r"[^a-z0-9]+")


def _slugify(service_id: str) -> str:
    """서비스ID(영숫자 혼합)를 우리 id 슬러그로. 충돌 가능성 낮으니 그대로 lower."""
    base = (service_id or "").strip().lower()
    base = _NON_SLUG.sub("-", base).strip("-")
    return base or "unknown"


# ────────────────────────────────────────────────────────────────────────────────
# LLM 보강
# ────────────────────────────────────────────────────────────────────────────────


OUR_CATEGORIES = ["주거", "출산", "생활", "교육", "청년", "창업"]

LLM_NORMALIZE_PROMPT = """너는 정부 공공서비스(혜택) 데이터를 모바일 앱용으로 정규화하는 전문가다.
정부24 API 원본 데이터를 보고, 아래 JSON 스키마로 변환해라.

[원본 데이터]
SERVICE_LIST:
{list_row}

SERVICE_DETAIL:
{detail}

[출력 스키마 — 이 6개 키만 출력. 다른 키 절대 만들지 마.]
{{
  "summary": "1~2문장 60자 이내. 토스 톤 존댓말 (~해드려요). 누가/얼마/언제 한눈에.",
  "category": "주거|출산|생활|교육|청년|창업 중 하나 (정확히 일치). 모호하면 빈 문자열.",
  "amount": 정수. 원 단위. 텍스트에서 금액 추출. 모호하면 0.
  "period": "지원 기간 한 줄. 예: '월 20만원 · 최대 12개월'. 없으면 빈 문자열.",
  "eligibility": ["자격 체크리스트", "..."],
  "documents": [{{"name": "서류명"}}, ...],
  "procedure": ["1단계", "2단계", ...]
}}

규칙:
- 출력은 JSON 객체 하나. ```json 마크다운 펜스 금지.
- amount는 숫자만 (단위·콤마 없음). 범위면 최대값. 모호하면 0.
- category는 6개 중 정확한 한 단어. 못 정하면 빈 문자열.
- eligibility 3~6개. 사용자가 빠르게 체크할 수 있는 짧은 항목으로.
- documents 항목은 서류명만. 발급처 URL은 별도 처리.
- procedure는 1단계당 한 문장 이내.
- 원문 정보 없는 필드는 합리적 추론보다 빈 값 우선 (eligibility/documents/procedure는 빈 배열, period는 빈 문자열).
"""


def _llm_extract(
    raw: RawPolicy,
    *,
    llm_client: Any,
) -> Dict[str, Any]:
    """Gemini로 비정형 필드 추출. 실패 시 빈 dict 반환."""
    list_row = dict(raw.list_row or {})
    detail = dict(raw.detail or {})
    # 토큰 절약 — 우리가 안 쓰는 필드 제거
    for noise_key in ("조회수", "등록일시", "수정일시"):
        list_row.pop(noise_key, None)
        detail.pop(noise_key, None)

    prompt = LLM_NORMALIZE_PROMPT.format(
        list_row=json.dumps(list_row, ensure_ascii=False, indent=2),
        detail=json.dumps(detail, ensure_ascii=False, indent=2),
    )

    try:
        resp = llm_client._model.generate_content(  # noqa: SLF001
            prompt,
            generation_config={
                "temperature": 0.2,
                "response_mime_type": "application/json",
            },
        )
        text = (resp.text or "").strip()
        return _parse_json_loose(text) or {}
    except Exception as e:
        log.warning("LLM normalize failed for %s: %s", raw.service_id, e)
        return {}


def _parse_json_loose(text: str) -> Optional[Dict[str, Any]]:
    if not text:
        return None
    fence = re.match(r"^```(?:json)?\s*([\s\S]*?)\s*```$", text.strip())
    if fence:
        text = fence.group(1)
    try:
        obj = json.loads(text)
        return obj if isinstance(obj, dict) else None
    except json.JSONDecodeError:
        return None


# ────────────────────────────────────────────────────────────────────────────────
# 메인 변환
# ────────────────────────────────────────────────────────────────────────────────


def normalize(
    raw: RawPolicy,
    *,
    llm_client: Any = None,
) -> Dict[str, Any]:
    """RawPolicy → 우리 Policy dict. 검증은 build_policies.validate_all에서."""
    list_row = raw.list_row or {}
    detail = raw.detail or {}

    # 결정론 단계 — 정부 데이터에서 직접 매핑되는 필드
    policy: Dict[str, Any] = {
        "id": _slugify(raw.service_id),
        "title": str(list_row.get("서비스명") or detail.get("서비스명") or "").strip(),
        "amount": 0,
        "category": "",
        "summary": str(detail.get("서비스목적") or list_row.get("서비스목적요약") or "").strip(),
        "period": "",
        "eligibility": [],
        "documents": [],
        "procedure": [],
        "applicationOrg": str(list_row.get("소관기관명") or detail.get("접수기관명") or "").strip(),
        "applicationUrl": str(detail.get("온라인신청사이트URL") or list_row.get("상세조회URL") or "").strip(),
    }

    # 자격 룰 (구조화 데이터 → 우리 모델 직매핑)
    rule = conditions_to_eligibility_rule(raw.conditions)
    if rule:
        policy["eligibilityRule"] = rule

    # LLM 단계 — 비정형 필드 정련
    if llm_client is not None:
        extracted = _llm_extract(raw, llm_client=llm_client)
        # 화이트리스트 머지
        for key in ("summary", "category", "amount", "period"):
            val = extracted.get(key)
            if isinstance(val, str) and val.strip():
                policy[key] = val.strip()
            elif isinstance(val, (int, float)) and key == "amount":
                policy[key] = int(val)
        for key in ("eligibility", "procedure"):
            val = extracted.get(key)
            if isinstance(val, list):
                policy[key] = [str(x).strip() for x in val if str(x).strip()]
        docs = extracted.get("documents")
        if isinstance(docs, list):
            cleaned: List[Dict[str, Any]] = []
            for d in docs:
                if isinstance(d, dict) and d.get("name"):
                    item = {"name": str(d["name"]).strip()}
                    if d.get("sourceUrl"):
                        item["sourceUrl"] = str(d["sourceUrl"]).strip()
                    cleaned.append(item)
                elif isinstance(d, str) and d.strip():
                    cleaned.append({"name": d.strip()})
            policy["documents"] = cleaned

    # category 정합성 — OUR_CATEGORIES에 없으면 빈 문자열
    if policy.get("category") and policy["category"] not in OUR_CATEGORIES:
        log.debug("dropping non-canonical category %r for %s", policy["category"], policy["id"])
        policy["category"] = ""

    return policy


def normalize_all(
    raws: List[RawPolicy],
    *,
    llm_client: Any = None,
) -> List[Dict[str, Any]]:
    out = []
    for i, raw in enumerate(raws, 1):
        log.info("normalizing %d/%d — %s", i, len(raws), raw.service_id)
        out.append(normalize(raw, llm_client=llm_client))
    return out
