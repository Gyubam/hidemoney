"""정부 API 원본(RawPolicy) → 우리 Policy 스키마 정규화.

3 단계 합성 (LLM 부담 분산):
- 결정론 1: supportConditions의 JA0xxx 구조화 코드 → EligibilityRule 직매핑
- 결정론 2: serviceList의 '서비스분야' → 우리 6 카테고리 직매핑 (LLM 우회)
- 결정론 3: 모든 텍스트 필드 정규식 → amount 추출 (LLM 우회)
- LLM 1: summary 정련 + period + amount 보강 (단일 작업, 부담 적음)
- LLM 2: eligibility/documents/procedure 추출 (단일 작업, 부담 적음)

LLM 어느 단계 실패해도 결정론 단계만으로 valid Policy 생산. 빌드 절대 안 깨짐.
"""

from __future__ import annotations

import json
import logging
import re
from typing import Any, Dict, List, Optional

from crawl import RawPolicy

log = logging.getLogger(__name__)


OUR_CATEGORIES = ["주거", "출산", "생활", "교육", "청년", "창업"]


# ────────────────────────────────────────────────────────────────────────────────
# 결정론 1 — supportConditions의 JA0xxx → EligibilityRule
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
        seen: List[str] = []
        for o in occupations:
            if o not in seen:
                seen.append(o)
        rule["requiresOccupation"] = seen

    # 출산/육아
    if _yn(cond.get("JA0303")):
        rule["requiresChildren"] = True

    return rule or None


# ────────────────────────────────────────────────────────────────────────────────
# 결정론 2 — 정부24 '서비스분야' → 우리 6 카테고리 매핑
# ────────────────────────────────────────────────────────────────────────────────


SERVICE_FIELD_TO_CATEGORY: Dict[str, str] = {
    # 정부24 실제 표기는 가운뎃점(·, U+00B7)을 사용. 정찰 결과 기반.
    "주거·자립": "주거",
    "보육·교육": "교육",
    "임신·출산": "출산",
    "입양·위탁": "출산",
    "고용·창업": "청년",
    "보호·돌봄": "생활",
    "안전·위기": "생활",
    "문화·여가": "생활",
    # 정찰에서 추가 발견된 분야
    "농림축산어업": "창업",  # 어업/축산업/임업 — 1인 사업자 카테고리로
    "보건·의료": "생활",
    "행정·안전": "생활",
    "생활안정": "생활",
    "문화·환경": "생활",
    # swagger엔 있지만 정찰 200건에서 안 보였던 — 안전 대비
    "신체건강": "생활",
    "정신건강": "생활",
    "생활지원": "생활",
}

# 하이픈/공백 변형 표기 → 가운뎃점 정식 표기로 정규화
_FIELD_ALIASES: Dict[str, str] = {
    "주거-자립": "주거·자립",
    "보육-교육": "보육·교육",
    "임신-출산": "임신·출산",
    "입양-위탁": "입양·위탁",
    "고용-창업": "고용·창업",
    "보호-돌봄": "보호·돌봄",
    "안전-위기": "안전·위기",
    "문화-여가": "문화·여가",
    "보건-의료": "보건·의료",
    "행정-안전": "행정·안전",
    "문화-환경": "문화·환경",
    "주거자립": "주거·자립",
    "보육교육": "보육·교육",
    "임신출산": "임신·출산",
}


def category_from_field(service_field: Optional[str]) -> str:
    """정부24 '서비스분야' 값 → 우리 6 카테고리. 매핑 못 찾으면 빈 문자열."""
    if not service_field:
        return ""
    key = str(service_field).strip()
    key = _FIELD_ALIASES.get(key, key)
    return SERVICE_FIELD_TO_CATEGORY.get(key, "")


# ────────────────────────────────────────────────────────────────────────────────
# slug / id
# ────────────────────────────────────────────────────────────────────────────────


_NON_SLUG = re.compile(r"[^a-z0-9]+")


def _slugify(service_id: str) -> str:
    """서비스ID(영숫자 혼합)를 우리 id 슬러그로."""
    base = (service_id or "").strip().lower()
    base = _NON_SLUG.sub("-", base).strip("-")
    return base or "unknown"


# ────────────────────────────────────────────────────────────────────────────────
# 결정론 3 — 한국어 금액 정규식
# ────────────────────────────────────────────────────────────────────────────────


_AMOUNT_PATTERN = re.compile(r"(\d{1,4}(?:,\d{3})*|\d+)\s*(억|만|천)?\s*원")
_UNIT_MULT = {"억": 100_000_000, "만": 10_000, "천": 1_000, None: 1}


def guess_amount_from_text(text: str) -> int:
    """텍스트에서 가장 큰 금액 추정. LLM 추출 실패 시 fallback."""
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
        # 너무 작은 값 또는 비현실적 큰 값 필터
        if 1_000 <= value <= 10_000_000_000:
            candidates.append(value)
    return max(candidates) if candidates else 0


def _gather_text(*sources: Optional[str]) -> str:
    return " ".join(str(s) for s in sources if s)


# ────────────────────────────────────────────────────────────────────────────────
# LLM 단계 — 두 작업으로 분리 (한 번에 5필드 추출하면 모델이 압도됨)
# ────────────────────────────────────────────────────────────────────────────────


LLM_SUMMARY_PROMPT = """너는 정부 공공서비스를 모바일 앱 '숨은지원금'에 표시할 카피라이터다.
아래 정부 정책 원문을 보고 토스 톤 한 줄 요약과 기간·금액을 추출하라.

[원문]
서비스명: {title}
서비스목적: {purpose}
지원대상: {target}
지원내용: {content}
신청기한: {deadline}

[출력 — JSON 객체 하나, 마크다운 펜스 금지]
{{
  "summary": "1~2문장 60자 이내. '~해드려요/지원해요/받을 수 있어요' 같은 친근한 존댓말로 **재작성**. raw 복붙 금지. 누가·얼마·언제 한눈에.",
  "period": "지원 기간/주기 한 줄. 예: '월 20만원 · 12개월', '연 1회', '상시'. 없으면 빈 문자열.",
  "amount": 정수. 원 단위. '월 N만원'은 12개월 가정해 N*120000. '최대 N억'은 N*100000000. 비금전이면 0.
}}

[좋은 예]
입력: title='청년 월세 지원' / content='만 19~34세 청년 무주택자에게 월 20만원을 12개월간 지원'
출력: {{"summary":"만 19~34세 청년 무주택자에게 월 20만원을 12개월간 지원해드려요.","period":"월 20만원 · 12개월","amount":2400000}}
"""


LLM_ITEMS_PROMPT = """너는 정부 정책 raw text를 모바일 앱용 체크리스트로 분해하는 전문가다.

[원문]
서비스명: {title}
지원대상: {target}
선정기준: {criteria}
구비서류: {documents}
신청방법: {procedure}

[출력 — JSON 객체 하나, 마크다운 펜스 금지]
{{
  "eligibility": ["자격 체크리스트 3~6개. 각 항목 25자 이내. 지원대상+선정기준에서 추출."],
  "documents": [{{"name": "서류명"}}],
  "procedure": ["1단계", "2단계"]
}}

규칙:
- 모든 키 반드시 출력. 정보 없으면 빈 배열.
- 서류명은 콤마/줄바꿈으로 분리해 각각 1항목.
- 신청방법은 단계별 분리, 각 단계 30자 이내.

[좋은 예]
입력: target='만 19~34세 청년 무주택자' / documents='주민등록등본, 임대차계약서, 소득금액증명원' / procedure='복지로 회원가입 후 신청서 작성, 지자체 심사'
출력: {{"eligibility":["만 19~34세 청년","무주택자"],"documents":[{{"name":"주민등록등본"}},{{"name":"임대차계약서"}},{{"name":"소득금액증명원"}}],"procedure":["복지로 회원가입","신청서 작성","지자체 심사"]}}
"""


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


def _call_llm(client: Any, prompt: str, *, service_id: str, task: str) -> Dict[str, Any]:
    try:
        resp = client._model.generate_content(  # noqa: SLF001
            prompt,
            generation_config={
                "temperature": 0.3,
                "response_mime_type": "application/json",
            },
        )
        text = (resp.text or "").strip()
        return _parse_json_loose(text) or {}
    except Exception as e:
        log.warning("LLM %s failed for %s: %s", task, service_id, e)
        return {}


def _llm_summary(raw: RawPolicy, *, client: Any) -> Dict[str, Any]:
    detail = raw.detail or {}
    list_row = raw.list_row or {}
    prompt = LLM_SUMMARY_PROMPT.format(
        title=list_row.get("서비스명") or detail.get("서비스명") or "",
        purpose=detail.get("서비스목적") or list_row.get("서비스목적요약") or "",
        target=detail.get("지원대상") or list_row.get("지원대상") or "",
        content=detail.get("지원내용") or list_row.get("지원내용") or "",
        deadline=detail.get("신청기한") or list_row.get("신청기한") or "",
    )
    return _call_llm(client, prompt, service_id=raw.service_id, task="summary")


def _llm_items(raw: RawPolicy, *, client: Any) -> Dict[str, Any]:
    detail = raw.detail or {}
    list_row = raw.list_row or {}
    prompt = LLM_ITEMS_PROMPT.format(
        title=list_row.get("서비스명") or detail.get("서비스명") or "",
        target=detail.get("지원대상") or list_row.get("지원대상") or "",
        criteria=detail.get("선정기준") or list_row.get("선정기준") or "",
        documents=detail.get("구비서류") or "",
        procedure=detail.get("신청방법") or list_row.get("신청방법") or "",
    )
    return _call_llm(client, prompt, service_id=raw.service_id, task="items")


# ────────────────────────────────────────────────────────────────────────────────
# 메인 변환
# ────────────────────────────────────────────────────────────────────────────────


def normalize(
    raw: RawPolicy,
    *,
    llm_client: Any = None,
) -> Dict[str, Any]:
    """RawPolicy → 우리 Policy dict."""
    list_row = raw.list_row or {}
    detail = raw.detail or {}

    # ── 결정론 단계 — 정부 데이터 직매핑
    policy: Dict[str, Any] = {
        "id": _slugify(raw.service_id),
        "title": str(list_row.get("서비스명") or detail.get("서비스명") or "").strip(),
        "amount": 0,
        "category": category_from_field(list_row.get("서비스분야")),
        "summary": str(detail.get("서비스목적") or list_row.get("서비스목적요약") or "").strip(),
        "period": "",
        "eligibility": [],
        "documents": [],
        "procedure": [],
        "applicationOrg": str(list_row.get("소관기관명") or detail.get("접수기관명") or "").strip(),
        "applicationUrl": str(detail.get("온라인신청사이트URL") or list_row.get("상세조회URL") or "").strip(),
    }

    # eligibilityRule (JA0xxx 직매핑)
    rule = conditions_to_eligibility_rule(raw.conditions)
    if rule:
        policy["eligibilityRule"] = rule

    # amount 정규식 — 모든 텍스트 필드 통합
    all_text = _gather_text(
        list_row.get("서비스목적요약"),
        list_row.get("지원내용"),
        list_row.get("지원대상"),
        list_row.get("선정기준"),
        detail.get("서비스목적"),
        detail.get("지원내용"),
        detail.get("지원대상"),
        detail.get("선정기준"),
    )
    guessed = guess_amount_from_text(all_text)
    if guessed > 0:
        policy["amount"] = guessed

    # ── LLM 단계 1: summary + period + amount 정련
    if llm_client is not None:
        s_out = _llm_summary(raw, client=llm_client)
        sm = s_out.get("summary")
        if isinstance(sm, str) and sm.strip():
            policy["summary"] = sm.strip()
        pr = s_out.get("period")
        if isinstance(pr, str) and pr.strip():
            policy["period"] = pr.strip()
        am = s_out.get("amount")
        if isinstance(am, (int, float)) and am > policy["amount"]:
            policy["amount"] = int(am)

    # ── LLM 단계 2: eligibility + documents + procedure 추출
    if llm_client is not None:
        i_out = _llm_items(raw, client=llm_client)
        elig = i_out.get("eligibility")
        if isinstance(elig, list):
            policy["eligibility"] = [str(x).strip() for x in elig if str(x).strip()][:6]
        proc = i_out.get("procedure")
        if isinstance(proc, list):
            policy["procedure"] = [str(x).strip() for x in proc if str(x).strip()][:8]
        docs = i_out.get("documents")
        if isinstance(docs, list):
            cleaned: List[Dict[str, Any]] = []
            for d in docs:
                if isinstance(d, dict) and d.get("name"):
                    item: Dict[str, Any] = {"name": str(d["name"]).strip()}
                    if d.get("sourceUrl"):
                        item["sourceUrl"] = str(d["sourceUrl"]).strip()
                    cleaned.append(item)
                elif isinstance(d, str) and d.strip():
                    cleaned.append({"name": d.strip()})
            policy["documents"] = cleaned[:10]

    # category 정합성 — OUR_CATEGORIES에 없으면 빈 문자열 (방어)
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
