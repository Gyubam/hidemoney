package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.CategoryStats
import com.hiddensubsidy.app.data.model.HomeData
import com.hiddensubsidy.app.data.model.MissedGrant
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.data.model.UserProfile
import java.time.LocalDate

object HomeAggregator {

    /**
     * "받을 수 있는 지원금" 카운트에 포함하는 grantType.
     * 현금성·바우처만. 융자(갚아야 함)·서비스(신청 부담 큼)·기타(불명확)는 제외.
     */
    private val MISSED_GRANT_TYPES = setOf(
        "현금", "현금(감면)", "현금(장학금)",
        "현물", "이용권",
    )

    /** 명시적 제외 — 융자(대출, 갚아야 함). */
    private val LOAN_GRANT_TYPES = setOf("현금(융자)")

    // region/gender/business 키워드 매칭은 PolicyRelevance로 이동 (검색 화면과 공통 사용).


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
        dismissed: Set<String> = emptySet(),
    ): HomeData {
        if (allPolicies.isEmpty()) return SampleData.home

        // 자격 매칭 결과 + daysLeft 재계산 + 키워드 정밀화 + 관심 없음 제외 = "사용자에게 의미 있는"
        val matched = allPolicies.matchedWith(profile).withFreshDaysLeft(today)
        val eligible = matched
            .filter { it.id !in dismissed }
            .filter { PolicyRelevance.isEligibleForUser(it, profile) }
        val withDeadline = eligible.filter { it.deadline.isNotBlank() }

        // 이번 주 (7일 이내). deadline 있는 정책만 — fallback 제거.
        // 이전 fallback(eligible.maxByOrNull)은 deadline 빈 정책을 D-0으로 표시하는 사고 원인.
        // 이번 주 정책 없으면 카드 자체를 안 그림 (HomeScreen이 firstOrNull?.let로 분기).
        val thisWeek = withDeadline
            .filter { it.daysLeft in 0..7 }
            .maxByOrNull { it.relevanceScore(profile) }

        // 마감 임박 (30일 이내, 이번 주 중복 제외)
        val deadlineSoon = withDeadline
            .filter { it.daysLeft in 0..30 && it.id != thisWeek?.id }
            .sortedBy { it.daysLeft }
            .take(5)

        // 놓친 돈 후보 — eligible(PolicyRelevance 통과)에서 amount/grantType 추가 필터.
        // 풀빌드 후 grantType 100% 채워졌으니 strict — 빈 grantType은 매칭 X.
        // 사용자가 융자 포함 토글 켜면 융자도 포함.
        val missedCandidates = eligible.filter { p ->
            if (p.amount <= 0) return@filter false
            if (p.grantType.isEmpty()) return@filter false
            val isLoan = p.grantType.any { it in LOAN_GRANT_TYPES }
            if (isLoan && !profile.includeLoanGrants) return@filter false
            isLoan || p.grantType.any { it in MISSED_GRANT_TYPES }
        }
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

        // 카테고리별 자격 충족 통계 — 홈 6 매트릭스 카드용
        val categorySummary = eligible
            .groupBy { it.category }
            .filter { it.key.isNotBlank() }
            .mapValues { (_, list) ->
                CategoryStats(
                    count = list.size,
                    amount = list.sumOf { it.amount.coerceAtLeast(0L) },
                )
            }

        return HomeData(
            missedTotalAmount = missedTotalAmount,
            missedCount = missedCount,
            missedGrants = missedGrants,
            thisWeekPolicies = listOfNotNull(thisWeek),
            deadlineSoon = deadlineSoon,
            categorySummary = categorySummary,
        )
    }
}
