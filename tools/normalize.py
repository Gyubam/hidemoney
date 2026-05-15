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

LLM_NORMALIZE_PROMPT = """너는 정부 공공서비스(혜택) 데이터를 모바일 앱 '숨은지원금'용으로 정규화하는 전문가다.
앱은 사용자가 본인이 받을 수 있는 정부 지원금을 빠르게 발견하도록 돕는 토스 톤 앱이다.

[입력]
SERVICE_LIST:
{list_row}

SERVICE_DETAIL:
{detail}

[너의 임무]
원문에 있는 정보를 **적극 추출**해 아래 JSON 스키마로 정규화. 모든 키 출력 필수.

{{
  "summary": "1~2문장 60자 이내. 토스 톤으로 자연스럽게 **재작성** (raw text 복붙 절대 금지). 누가·얼마·언제가 한눈에. 어미는 '~해드려요/~지원해요/~받을 수 있어요' 같은 친근한 존댓말.",
  "category": "정확히 다음 6개 중 하나: 주거 | 출산 | 생활 | 교육 | 청년 | 창업. 가장 가까운 것을 반드시 골라라.",
  "amount": 정수. 원 단위. 텍스트에서 금액 추출. '월 N만원'은 12개월 가정해 N*120000. '최대 N억'은 N*100000000. 범위면 상한값. 정말 비금전이면 0.,
  "period": "지원 기간/주기 한 줄. 예: '월 20만원 · 최대 12개월' 또는 '연 1회' 또는 '상시'. 빈 문자열 허용.",
  "eligibility": ["자격 체크리스트 3~6개. 각 항목 25자 이내. 지원대상+선정기준에서 추출. 가급적 빈 배열 피하기."],
  "documents": [{{"name": "서류명"}}, ...],
  "procedure": ["1단계", "2단계", "..."]
}}

[카테고리 매핑 가이드]
- 주거: 월세/전세/주택/보증금/임대/이사/주거 관련
- 출산: 출산/임신/육아/보육/어린이/유아/다자녀
- 생활: 의료/통신/공과금/생필품/장애/돌봄/노인/기초생활/저소득
- 교육: 학비/장학금/직업훈련/교재/학자금/유아학비
- 청년: 청년/대학생/취업/구직/자격증/일자리 (위 4개에 안 맞을 때만)
- 창업: 창업/사업/소상공인/벤처/어업/축산/임업/농업

[좋은 정규화 예시]
원문 detail: "만 19~34세 청년 무주택자에게 월 최대 20만원, 12개월간 주거 안정 지원. 신청서류: 주민등록등본, 임대차계약서, 소득금액증명원. 절차: 복지로 회원가입 → 메뉴 선택 → 신청서 작성 → 심사 → 지급"
좋은 결과:
{{"summary":"만 19~34세 청년 무주택자에게 월 최대 20만원을 12개월간 지원해드려요.","category":"주거","amount":2400000,"period":"월 20만원 · 최대 12개월","eligibility":["만 19~34세 청년","무주택자","월세 거주"],"documents":[{{"name":"주민등록등본"}},{{"name":"임대차계약서"}},{{"name":"소득금액증명원"}}],"procedure":["복지로 회원가입","메뉴 선택","신청서 작성 + 서류 업로드","지자체 심사 후 지급"]}}

규칙:
- 출력은 JSON 객체 하나. ```json 마크다운 펜스 금지.
- 모든 키 반드시 출력.
- summary는 raw text 복붙 금지. 반드시 자연스러운 토스 톤으로 재작성.
- category는 빈 문자열 금지. 6개 중 가장 가까운 것 무조건 선택.
- 정보 부족해도 적극 추론. eligibility는 최소 2~3개 항목.
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


# 한국어 금액 표기 정규식: "월 20만원", "최대 2억원", "연 1,200,000원" 등
_AMOUNT_PATTERN = re.compile(
    r"(\d{1,4}(?:,\d{3})*|\d+)\s*(억|만|천)?\s*원"
)
_UNIT_MULT = {"억": 100_000_000, "만": 10_000, "천": 1_000, None: 1}


def guess_amount_from_text(text: str) -> int:
    """텍스트에서 가장 큰 금액 추정 — LLM 추출 실패 시 fallback."""
    if not text:
        return 0
    candidates: List[int] = []
    for m in _AMOUNT_PATTERN.finditer(text):
        try:
            n = int(m.group(1).replace(",", ""))
        except ValueError:
            continue
        unit = m.group(2)
        mult = _UNIT_MULT.get(unit, 1)
        value = n * mult
        # 너무 작은 값(잔돈 단위) 또는 비현실적 큰 값 필터
        if 1_000 <= value <= 10_000_000_000:
            candidates.append(value)
    return max(candidates) if candidates else 0


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
        for key in ("summary", "category", "period"):
            val = extracted.get(key)
            if isinstance(val, str) and val.strip():
                policy[key] = val.strip()
        amt_val = extracted.get("amount")
        if isinstance(amt_val, (int, float)) and amt_val > 0:
            policy["amount"] = int(amt_val)
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

    # amount fallback — LLM 실패해도 텍스트에서 정규식 추출
    if not policy.get("amount"):
        fallback_text = " ".join(
            str(v) for v in (
                detail.get("지원내용"),
                detail.get("서비스목적"),
                list_row.get("서비스목적요약"),
                list_row.get("지원내용"),
            ) if v
        )
        guessed = guess_amount_from_text(fallback_text)
        if guessed > 0:
            policy["amount"] = guessed

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
