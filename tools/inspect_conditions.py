"""supportConditions API의 JA 코드 정찰.

30개 정책 sample 받아서:
- JA 코드별 활성 빈도
- 각 코드가 가지는 값 종류
- 정책 제목/지원대상 함께 보여서 코드 의미 추론 가능

목적: normalize.py에서 추가 매핑할 JA 코드 결정.
출력: 콘솔 + tools/inspect-conditions.json (artifact 업로드용).
"""

from __future__ import annotations

import json
import logging
import sys

# Windows cp949 콘솔 인코딩 fix — 한글 출력 시 UnicodeEncodeError 방지
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

from crawl import build_client_from_env

log = logging.getLogger("inspect-conditions")


def main() -> int:
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

    client = build_client_from_env()
    if client is None:
        print("ERROR: DATA_GO_KR_API_KEY missing", file=sys.stderr)
        return 1

    log.info("Fetching 100 policy IDs (개인,가구)...")
    rows = []
    for row in client.iter_services(
        limit=100,
        per_page=100,
        client_filter_user_type="개인,가구",
    ):
        rows.append(row)
    log.info("Got %d rows", len(rows))

    log.info("Fetching supportConditions for each...")
    samples = []
    for i, row in enumerate(rows, 1):
        sid = str(row.get("서비스ID") or "")
        title = row.get("서비스명")
        cond = None
        try:
            cond = client.get_conditions(sid)
        except Exception as e:
            log.warning("conditions fetch failed for %s: %s", sid, e)
        samples.append({
            "id": sid,
            "title": title,
            "target": (row.get("지원대상") or "")[:200],
            "criteria": (row.get("선정기준") or "")[:200],
            "conditions": cond,
        })
        log.info("[%d/%d] %s — %s", i, len(rows), sid, title)

    # JA 코드 빈도 + 값 종류 집계
    code_freq = {}
    code_values: dict[str, set] = {}
    for s in samples:
        cond = s.get("conditions") or {}
        for k, v in cond.items():
            if not k.startswith("JA"):
                continue
            # 빈 값/0/공백은 미활성으로 간주
            sv = str(v).strip()
            if sv in ("", "0", "N", "F", "False", "None"):
                continue
            code_freq[k] = code_freq.get(k, 0) + 1
            code_values.setdefault(k, set()).add(sv)

    print("\n=== JA Code Frequency (active in 30 samples) ===")
    for k, c in sorted(code_freq.items(), key=lambda x: -x[1]):
        vals = sorted(code_values.get(k, set()))[:10]
        print(f"  {k}: {c} times | values: {vals}")

    # 정책별 활성 코드 매핑 (의미 추론에 핵심)
    print("\n=== Per-policy active codes (for semantic inference) ===")
    for s in samples:
        cond = s.get("conditions") or {}
        active = [
            f"{k}={v}" for k, v in cond.items()
            if k.startswith("JA") and str(v).strip() not in ("", "0", "N", "F", "False", "None")
        ]
        if not active:
            continue
        print(f"\n[{s['id']}] {s['title']}")
        print(f"  target: {s['target']}")
        print(f"  codes: {', '.join(active[:20])}")

    # artifact 업로드용 dump
    output = {
        "samples": samples,
        "code_freq": code_freq,
        "code_values": {k: sorted(v) for k, v in code_values.items()},
    }
    with open("inspect-conditions.json", "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    log.info("Wrote inspect-conditions.json (artifact)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
