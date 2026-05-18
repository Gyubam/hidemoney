package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.CalendarEventKind
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.data.model.PolicyCalendarEvent
import com.hiddensubsidy.app.data.model.UserProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 9,923개 풀데이터에서 캘린더에 표시할 PolicyCalendarEvent 생성.
 *
 * 정책에 있는 신뢰 가능한 시점 = **deadline 하나뿐**.
 * 정부 API에 신청시작/발표/지급 일자 필드 없음 → 마감만 점 찍음.
 *
 * 양 정책 풀:
 *  - 자격 충족 + PolicyRelevance 통과 + deadline 있는 정책 (자동 노출)
 *  - 즐겨찾기 정책 (자격 무관 — 사용자가 직접 마크함)
 *
 * 표시 범위: today 기준 ±6개월 (1년 윈도우). 너무 멀거나 지난 건 제외.
 * cap: 200개 (캘린더 산만 방지).
 */
object CalendarAggregator {

    private val ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE
    private const val MAX_EVENTS = 200
    private const val WINDOW_MONTHS = 6L

    fun compute(
        allPolicies: List<Policy>,
        favorites: Set<String>,
        profile: UserProfile,
        today: LocalDate = LocalDate.now(),
    ): List<PolicyCalendarEvent> {
        if (allPolicies.isEmpty()) return emptyList()
        val windowStart = today.minusMonths(WINDOW_MONTHS)
        val windowEnd = today.plusMonths(WINDOW_MONTHS)

        val matched = allPolicies.matchedWith(profile)
        val byId = matched.associateBy { it.id }

        val eligibleDeadlines = matched.asSequence()
            .filter { it.deadline.isNotBlank() }
            .filter { PolicyRelevance.isEligibleForUser(it, profile) }

        // 즐겨찾기 정책의 마감일도 노출 (자격 무관 — 사용자 의지로 마크)
        val favoriteDeadlines = favorites.asSequence()
            .mapNotNull { byId[it] }
            .filter { it.deadline.isNotBlank() }

        val combined = (eligibleDeadlines + favoriteDeadlines)
            .distinctBy { it.id }
            .mapNotNull { policy ->
                val date = parseDate(policy.deadline) ?: return@mapNotNull null
                if (date.isBefore(windowStart) || date.isAfter(windowEnd)) return@mapNotNull null
                PolicyCalendarEvent(
                    date = policy.deadline,
                    kindName = CalendarEventKind.Deadline.name,
                    policyId = policy.id,
                    policyTitle = policy.title,
                )
            }
            .toList()
            .sortedBy { it.date }
            .take(MAX_EVENTS)

        return combined
    }

    private fun parseDate(iso: String): LocalDate? = try {
        LocalDate.parse(iso, ISO_DATE)
    } catch (_: Exception) {
        null
    }
}
