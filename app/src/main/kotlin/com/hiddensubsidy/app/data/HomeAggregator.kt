package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.HomeData
import com.hiddensubsidy.app.data.model.MissedGrant
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.data.model.UserProfile
import java.time.LocalDate

object HomeAggregator {

    /**
     * allPolicies(정부 풀 데이터) + profile → HomeData 동적 계산.
     *
     * 휴리스틱:
     * - thisWeek: 자격 충족 + 7일 이내 마감 + amount 큰 순 1개 (없으면 자격 충족 중 amount 큰 1개)
     * - deadlineSoon: 자격 충족 + 30일 이내 마감, daysLeft 오름차순, 상위 5개 (thisWeek 중복 제외)
     * - missedTotal/Count: 자격 충족 + amount > 0 (지금이라도 받을 수 있는 모든 정책)
     *   * Firebase Auth로 신청 이력 받으면 정확해짐. 지금은 "자격 충족 = 미신청" 가정.
     * - missedGrants 샘플: amount 큰 순 상위 3개
     *
     * 정책이 비어있으면 SampleData.home fallback (앱 첫 진입 직후 네트워크 실패 케이스).
     */
    fun computeHome(
        allPolicies: List<Policy>,
        profile: UserProfile,
        today: LocalDate = LocalDate.now(),
    ): HomeData {
        if (allPolicies.isEmpty()) return SampleData.home

        // 자격 매칭 결과 + daysLeft 재계산을 미리 적용
        val matched = allPolicies.matchedWith(profile).withFreshDaysLeft(today)
        val eligible = matched.filter { it.isEligible }
        val withDeadline = eligible.filter { it.deadline.isNotBlank() }

        // 이번 주 (7일 이내)
        val thisWeek = withDeadline
            .filter { it.daysLeft in 0..7 }
            .maxByOrNull { it.amount }
            ?: eligible.maxByOrNull { it.amount }

        // 마감 임박 (30일 이내, 이번 주 중복 제외)
        val deadlineSoon = withDeadline
            .filter { it.daysLeft in 0..30 && it.id != thisWeek?.id }
            .sortedBy { it.daysLeft }
            .take(5)

        // 놓친 돈 = 자격 충족 + amount > 0 (휴리스틱)
        val missedCandidates = eligible.filter { it.amount > 0 }
        val missedTotalAmount = missedCandidates.sumOf { it.amount }
        val missedCount = missedCandidates.size

        // 자격 충족 정책 전체를 amount 큰 순으로 MissedGrant 변환 (LazyColumn 스크롤).
        // 100개 cap — 그 이상은 사용자가 스크롤 안 함 + 토스 톤 UX.
        val missedGrants = missedCandidates
            .sortedByDescending { it.amount }
            .take(100)
            .map { p ->
                MissedGrant(
                    id = p.id,
                    title = p.title,
                    amount = p.amount,
                    eligibleFrom = "",
                    year = today.year - 1,
                    summary = p.summary,
                )
            }

        return HomeData(
            missedTotalAmount = missedTotalAmount,
            missedCount = missedCount,
            missedGrants = missedGrants,
            thisWeekPolicies = listOfNotNull(thisWeek),
            deadlineSoon = deadlineSoon,
        )
    }
}
