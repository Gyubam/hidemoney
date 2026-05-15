"""정책 데이터 빌드 오케스트레이터.

흐름:
  1. docs/policies.json 로드 (기준 데이터)
  2. 각 정책에 대해:
     - daysLeft 재계산 (today vs deadline)
     - difficultyScore 휴리스틱 계산 (필요 서류 + 절차 단계 + 자격 항목)
     - roiScore 계산 (amount / difficulty 정규화)
     - --enrich 플래그 시 Gemini Flash로 summary 정련
  3. Pydantic으로 스키마 검증
  4. 변경이 있을 때만 파일 저장 (exit 0). 변경 없으면 그대로 (exit 0).

R2 단계에서는 SampleData export(docs/policies.json) 자체를 입력으로 사용.
R2.5 이후 정부24 OpenAPI / 복지로 fetch가 추가되면 1단계만 교체.
"""

from __future__ import annotations

import argparse
import json
import logging
import os
import sys
from dataclasses import dataclass
from datetime import date, datetime
from pathlib import Path
from typing import Any, Dict, List, Optional

from dateutil import parser as date_parser

from schema import Policy
from summarize import GeminiClient, build_client_from_env, enrich_policy

log = logging.getLogger("build-policies")


ROOT = Path(__file__).resolve().parent.parent
OUTPUT_PATH = ROOT / "docs" / "policies.json"


# ────────────────────────────────────────────────────────────────────────────────
# 보강 휴리스틱
# ────────────────────────────────────────────────────────────────────────────────


def compute_days_left(deadline: Optional[str], today: date) -> Optional[int]:
    if not deadline:
        return None
    try:
        d = date_parser.parse(deadline).date()
        return (d - today).days
    except (ValueError, TypeError):
        return None


def compute_difficulty(policy: Dict[str, Any]) -> int:
    """1~10. 서류·절차·자격 항목이 많을수록 어려움."""
    docs = len(policy.get("documents") or [])
    procs = len(policy.get("procedure") or [])
    elig = len(policy.get("eligibility") or [])
    raw = docs * 1.0 + procs * 0.8 + elig * 0.5
    # 0~10 normalize, 최소 1 (완전 무서류여도 1)
    score = max(1, min(10, round(raw)))
    return score


def compute_roi(amount: int, difficulty: int) -> int:
    """0~100. 금액 / 난이도 로그 스케일."""
    if amount <= 0:
        return 0
    # 100만원 amount, 난이도 1일 때 ~80점. 1000만원 + 난이도 5 → ~85점.
    import math

    base = math.log10(max(1, amount)) * 15  # 100만원 → 90, 1000만원 → 105
    penalty = (difficulty - 1) * 3  # 난이도 10이면 -27
    score = base - penalty
    return max(0, min(100, round(score)))


def enrich_local(policy: Dict[str, Any], today: date) -> Dict[str, Any]:
    """LLM 없이도 동작하는 결정론적 보강. daysLeft·difficulty·roi 계산."""
    out = dict(policy)
    days = compute_days_left(out.get("deadline"), today)
    if days is not None:
        out["daysLeft"] = days
    difficulty = compute_difficulty(out)
    out["difficultyScore"] = difficulty
    out["roiScore"] = compute_roi(int(out.get("amount") or 0), difficulty)
    return out


# ────────────────────────────────────────────────────────────────────────────────
# 검증 + 저장
# ────────────────────────────────────────────────────────────────────────────────


def validate_all(policies: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """Pydantic 검증. 실패하면 ValueError. 검증 통과한 dict 리스트 반환."""
    validated = []
    for raw in policies:
        try:
            p = Policy.model_validate(raw)
        except Exception as e:
            raise ValueError(f"validation failed for id={raw.get('id')!r}: {e}") from e
        # by_alias 미사용 — 필드명 그대로
        validated.append(p.model_dump(exclude_none=True))
    return validated


def write_if_changed(policies: List[Dict[str, Any]], path: Path) -> bool:
    """파일 내용이 다를 때만 쓴다. 변경 여부 반환."""
    new_text = json.dumps(policies, ensure_ascii=False, indent=4) + "\n"
    if path.exists():
        old_text = path.read_text(encoding="utf-8")
        if old_text == new_text:
            return False
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(new_text, encoding="utf-8")
    return True


# ────────────────────────────────────────────────────────────────────────────────
# 메인
# ────────────────────────────────────────────────────────────────────────────────


@dataclass
class RunResult:
    total: int
    enriched_by_llm: int
    changed: bool


def run(*, enrich: bool, today: Optional[date] = None) -> RunResult:
    today = today or date.today()
    log.info("Loading %s", OUTPUT_PATH)
    base = json.loads(OUTPUT_PATH.read_text(encoding="utf-8"))
    if not isinstance(base, list):
        raise ValueError("policies.json must be a JSON array")

    llm_client: Optional[GeminiClient] = None
    if enrich:
        llm_client = build_client_from_env()
        if llm_client is None:
            log.warning("GEMINI_API_KEY not set — skipping LLM enrich")

    out: List[Dict[str, Any]] = []
    llm_count = 0
    for raw in base:
        local = enrich_local(raw, today=today)
        if llm_client is not None:
            refined = enrich_policy(local, client=llm_client)
            if refined is not local:
                llm_count += 1
            out.append(refined)
        else:
            out.append(local)

    validated = validate_all(out)
    changed = write_if_changed(validated, OUTPUT_PATH)
    return RunResult(total=len(validated), enriched_by_llm=llm_count, changed=changed)


def main(argv: Optional[List[str]] = None) -> int:
    ap = argparse.ArgumentParser(description="숨은지원금 정책 데이터 빌드")
    ap.add_argument(
        "--enrich",
        action="store_true",
        help="Gemini Flash로 summary 정련 (GEMINI_API_KEY 필요)",
    )
    ap.add_argument(
        "--today",
        default=None,
        help="기준일 (YYYY-MM-DD). 미지정 시 시스템 today. CI 재현성용.",
    )
    ap.add_argument(
        "-v", "--verbose", action="store_true", help="DEBUG 로그"
    )
    args = ap.parse_args(argv)

    logging.basicConfig(
        level=logging.DEBUG if args.verbose else logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s — %(message)s",
    )

    today: Optional[date] = None
    if args.today:
        today = date_parser.parse(args.today).date()

    try:
        result = run(enrich=args.enrich, today=today)
    except Exception as e:
        log.exception("build failed: %s", e)
        return 1

    log.info(
        "done — total=%d, llm_enriched=%d, file_changed=%s",
        result.total,
        result.enriched_by_llm,
        result.changed,
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
