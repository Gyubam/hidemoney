package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.EventBundle
import com.hiddensubsidy.app.data.model.LifeEvent
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.data.model.TimelineGroup
import com.hiddensubsidy.app.data.model.UserProfile

/**
 * 9,923개 풀데이터에서 LifeEvent 6개별로 자동 분류된 EventBundle 생성.
 *
 * 분류 방식 — title/summary 키워드 매칭. 정부 데이터에 LifeEvent 분류 필드 없음 → 클라 키워드 fallback.
 * 자격 충족 + PolicyRelevance 통과 정책만 (홈 missed와 동일 기준).
 *
 * 영구 한계: 정부 raw에 "이사 전 / 이사 후" 같은 시점 메타데이터 없음 → 타임라인 분기 X.
 *   단일 group "${event.label}할 때 받는 지원금" 으로 묶음.
 *   기존 SampleData의 timeline 구조는 유지(모델), 단지 자동 분류는 단일 group으로 만듦.
 */
object EventAggregator {

    /** LifeEvent별 분류 키워드. title/summary substring 매칭 (or). */
    private val EVENT_KEYWORDS: Map<LifeEvent, List<String>> = mapOf(
        LifeEvent.Move to listOf(
            "이사", "전입", "전세자금", "월세 지원", "월세지원", "임차", "임대주택",
            "주거안정", "주택 이사", "주거이전", "보증금",
        ),
        LifeEvent.Resign to listOf(
            "실업", "실업급여", "구직", "고용보험", "재취업", "이직", "퇴직",
            "구직활동", "구직자",
        ),
        LifeEvent.Pregnancy to listOf(
            "임신", "출산", "산모", "산후", "산전", "신생아", "영아", "영유아",
            "임산부", "출생아", "산후조리", "출산장려",
            "임신·출산", "임신출산", "첫만남", "부모급여",
        ),
        LifeEvent.Marriage to listOf(
            "신혼", "결혼", "혼인", "신혼부부", "신혼희망", "혼인신고",
        ),
        LifeEvent.Startup to listOf(
            "창업", "스타트업", "사업화", "벤처", "기업가", "K-스타트업", "K스타트업",
            "예비창업", "초기창업",
        ),
        LifeEvent.Employment to listOf(
            "취업", "신입", "청년채용", "내일채움", "도약계좌", "청년수당",
            "청년취업", "취업준비", "청년 자산",
        ),
    )

    /** 각 이벤트별 cap (너무 많으면 카드 산만). */
    private const val PER_EVENT_CAP = 50

    fun compute(
        allPolicies: List<Policy>,
        profile: UserProfile,
    ): List<EventBundle> {
        if (allPolicies.isEmpty()) return emptyList()

        val matched = allPolicies.matchedWith(profile)
        val eligible = matched.filter { PolicyRelevance.isEligibleForUser(it, profile) }

        return LifeEvent.entries.map { event ->
            buildBundleFor(event, eligible, profile)
        }
    }

    private fun buildBundleFor(event: LifeEvent, eligible: List<Policy>, profile: UserProfile): EventBundle {
        val keywords = EVENT_KEYWORDS[event].orEmpty()
        val matched = eligible
            .filter { p ->
                val text = "${p.title} ${p.summary}"
                keywords.any { kw -> text.contains(kw) }
            }
            .sortedByRelevance(profile)
            .take(PER_EVENT_CAP)

        val maxAmount = matched.maxOfOrNull { it.amount } ?: 0L
        val groupLabel = "${event.label}할 때 받는 지원금"

        return EventBundle(
            eventId = event.id,
            tagline = if (matched.isEmpty()) "조건 더 채워보세요" else "자격 충족",
            maxAmountLabel = formatMaxLabel(maxAmount, matched.size),
            groups = listOf(
                TimelineGroup(
                    label = groupLabel,
                    emoji = event.emoji,
                    policies = matched,
                )
            ),
        )
    }

    private fun formatMaxLabel(maxAmount: Long, count: Int): String {
        if (count == 0) return "곧 등록될 예정"
        if (maxAmount <= 0) return "$count 건"
        // 토스 톤 — 큰 금액은 "최대 N억원" / 작은 건 "최대 N만원"
        val labeled = when {
            maxAmount >= 100_000_000L -> "최대 ${maxAmount / 100_000_000L}억원"
            maxAmount >= 10_000L -> "최대 ${maxAmount / 10_000L}만원"
            else -> "최대 ${maxAmount}원"
        }
        return labeled
    }
}
