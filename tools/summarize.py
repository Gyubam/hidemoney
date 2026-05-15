"""Gemini Flash로 정책 요약 정련.

비용·속도 모두 보수적으로:
- 정책 1개당 1회 호출 (배치 가능하지만 디버깅 편의 우선)
- 응답은 JSON only, 기존 필드는 보존, 새/정련 필드만 머지
- 실패하면 원본 그대로 반환 (절대 빌드 깨뜨리지 않음)
"""

from __future__ import annotations

import json
import logging
import os
import re
from typing import Any, Dict, Optional

log = logging.getLogger(__name__)

# 출력 키 화이트리스트 — Gemini가 다른 필드 건드려도 적용 안 함
ALLOWED_LLM_KEYS = {"summary"}


_PROMPT_TEMPLATE = """너는 정부 지원금 앱 '숨은지원금'의 카피라이터다.
아래 정책 JSON을 보고, **summary 한 줄만** 자연스러운 한국어로 정련해라.

규칙:
- summary는 1~2문장, 60자 이내. 누가/얼마/언제 한눈에 보이게.
- 톤: 토스 앱처럼 담백하고 친근한 존댓말. "~해드려요" 같은 부드러운 어미.
- 숫자(금액·연령)는 절대 바꾸지 마라. 원문 그대로.
- 출력은 반드시 JSON 한 객체. ```json 같은 마크다운 펜스 금지.
- 형식: {{"summary": "..."}}

[정책 JSON]
{policy_json}
"""


class GeminiClient:
    """지연 import로 google-generativeai 의존성을 옵션화."""

    def __init__(self, api_key: str, model: str = "gemini-2.0-flash"):
        import google.generativeai as genai

        genai.configure(api_key=api_key)
        self._model = genai.GenerativeModel(model)
        self._model_name = model

    def refine(self, policy: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        prompt = _PROMPT_TEMPLATE.format(
            policy_json=json.dumps(policy, ensure_ascii=False, indent=2)
        )
        try:
            resp = self._model.generate_content(
                prompt,
                generation_config={
                    "temperature": 0.4,
                    "response_mime_type": "application/json",
                },
            )
            text = (resp.text or "").strip()
            return _parse_json_loose(text)
        except Exception as e:
            log.warning("Gemini refine failed for %s: %s", policy.get("id"), e)
            return None


def _parse_json_loose(text: str) -> Optional[Dict[str, Any]]:
    """모델이 마크다운 펜스를 끼워도 관용적으로 파싱."""
    if not text:
        return None
    # ```json ... ``` 펜스 제거
    fence = re.match(r"^```(?:json)?\s*([\s\S]*?)\s*```$", text.strip())
    if fence:
        text = fence.group(1)
    try:
        obj = json.loads(text)
        return obj if isinstance(obj, dict) else None
    except json.JSONDecodeError:
        return None


def enrich_policy(
    policy: Dict[str, Any],
    client: Optional[GeminiClient] = None,
) -> Dict[str, Any]:
    """정책 1개에 Gemini 정련 적용. 클라이언트 없으면 무변경."""
    if client is None:
        return policy
    refined = client.refine(policy)
    if not refined:
        return policy
    merged = dict(policy)
    for key, val in refined.items():
        if key in ALLOWED_LLM_KEYS and isinstance(val, str) and val.strip():
            merged[key] = val.strip()
    return merged


def build_client_from_env() -> Optional[GeminiClient]:
    """환경변수에서 API key를 읽어 클라이언트 구성. 키 없으면 None."""
    api_key = os.environ.get("GEMINI_API_KEY", "").strip()
    if not api_key:
        return None
    model = os.environ.get("GEMINI_MODEL", "gemini-2.0-flash").strip() or "gemini-2.0-flash"
    return GeminiClient(api_key=api_key, model=model)
