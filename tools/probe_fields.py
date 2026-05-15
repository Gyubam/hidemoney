"""정부24 serviceList 응답에서 핵심 필드 unique 값 정찰.

워크플로우 실행 후 Actions 로그에서 결과 확인:
- 사용자구분: 어떤 값을 가져야 cond[사용자구분::LIKE]가 매치되나
- 서비스분야: 우리 카테고리 매핑 dict와 실제 값 일치하나
- 소관기관유형: 중앙부처/지자체 등 어떤 값들이 있나
- 지원유형: 분류 가능한가

사용자 액션 0. Claude가 결과 보고 다음 라운드 결정.
"""

from __future__ import annotations

import logging
import os
import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from crawl import GovApiClient  # noqa: E402

log = logging.getLogger("probe-fields")


def main() -> int:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s — %(message)s",
    )

    api_key = os.environ.get("DATA_GO_KR_API_KEY", "").strip()
    if not api_key:
        log.error("DATA_GO_KR_API_KEY 환경변수 미설정")
        return 1

    client = GovApiClient(service_key=api_key)

    log.info("Fetching serviceList page=1 perPage=200 ...")
    body = client.list_services(page=1, per_page=200)
    data = body.get("data") or []
    log.info("got %d rows | totalCount=%s matchCount=%s",
             len(data), body.get("totalCount"), body.get("matchCount"))

    if not data:
        log.error("응답 data가 비어있음. 키 또는 API 동작 확인 필요.")
        return 1

    fields = [
        "사용자구분",
        "서비스분야",
        "지원유형",
        "소관기관유형",
        "소관기관명",
    ]

    for f in fields:
        counter = Counter(str(r.get(f) or "(empty)").strip() for r in data)
        print(f"\n=== {f} unique values (총 {len(counter)}종, top 20) ===")
        for v, c in counter.most_common(20):
            print(f"  {c:>4}x  {v}")

    print("\n=== Sample raw row (1st) ===")
    sample = data[0]
    for k, v in sample.items():
        sv = str(v or "").replace("\n", " / ")
        if len(sv) > 100:
            sv = sv[:100] + "..."
        print(f"  {k}: {sv}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
