package com.hiddensubsidy.app.data

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

    /** 사업자/창업가 외에는 거짓 임팩트가 될 키워드 (title + summary 검사). */
    private val BUSINESS_KEYWORDS = listOf(
        "창업", "스타트업", "사업화", "벤처", "기업가", "K-스타트업",
        "경진대회", "공모전", "콘테스트", "사업자",
    )

    /**
     * 사업자/예비창업가 전용 정책 여부 판별.
     * 1차: category == "창업"
     * 2차: title/summary 키워드 (정부 매핑이 "고용·창업"을 "청년"으로 분류한 거 잡기)
     */
    private fun isBusinessPolicy(policy: com.hiddensubsidy.app.data.model.Policy): Boolean {
        if (policy.category == "창업") return true
        val text = "${policy.title} ${policy.summary}"
        return BUSINESS_KEYWORDS.any { text.contains(it) }
    }

    private fun isCategoryRelevant(policy: com.hiddensubsidy.app.data.model.Policy, occupation: String?): Boolean {
        if (isBusinessPolicy(policy) && occupation != "사업자") return false
        return true
    }

    /**
     * 성별 매칭 — title 키워드 기반.
     * 정부 supportConditions JA0101/JA0102는 거의 100% 둘 다 Y(sentinel)이라
     * 데이터로는 매칭 불가. title에 명시된 케이스만 잡음 (false positive 최소).
     */
    private val FEMALE_KEYWORDS = listOf("여성", "여학생", "여자", "임산부", "산모", "산후", "출산", "임신")
    private val MALE_KEYWORDS = listOf("남성", "남학생", "남자")

    private fun isGenderRelevant(policy: com.hiddensubsidy.app.data.model.Policy, gender: String?): Boolean {
        val text = "${policy.title} ${policy.summary}"
        val femaleTargeted = FEMALE_KEYWORDS.any { text.contains(it) }
        val maleTargeted = MALE_KEYWORDS.any { text.contains(it) }
        // 양쪽 키워드 다 등장 = 일반 정책(남녀 비교 등) → 통과
        if (femaleTargeted && maleTargeted) return true
        if (femaleTargeted && gender != "여") return false
        if (maleTargeted && gender != "남") return false
        return true
    }

    /** 17 광역 시·도. applicationOrg에서 substring 검색 우선. */
    private val PROVINCIAL_REGIONS = listOf(
        "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
        "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주",
    )

    /** applicationOrg 텍스트에서 광역 region 추출. 못 찾으면 null. */
    private fun extractRegionFromOrg(org: String?): String? {
        if (org.isNullOrBlank()) return null
        return PROVINCIAL_REGIONS.firstOrNull { org.contains(it) }
    }

    /** 소관기관명이 시/군/구청 형태면 지자체 정책 (광역 추출 실패 시 strict 처리). */
    private val LOCAL_GOV_PATTERN = Regex("(시|군|구)청?$")
    private fun isLocalGovOrg(org: String?): Boolean {
        if (org.isNullOrBlank()) return false
        return LOCAL_GOV_PATTERN.containsMatchIn(org.trim())
    }

    /**
     * 지역 매칭 — 사용자 region에 안 맞는 지자체 정책 제외.
     * - applicationOrg에서 광역 추출되면 → 사용자 region과 일치해야
     * - 시군구청인데 광역 추출 실패 → strict로 제외 (여수시·곡성군 같은 케이스)
     * - 중앙부처(특정 광역 안 들어감 + "시/군/구" 패턴 없음) → 전국 대상 → 통과
     */
    private fun isRegionRelevant(policy: com.hiddensubsidy.app.data.model.Policy, userRegion: String?): Boolean {
        val org = policy.applicationOrg
        val extractedRegion = extractRegionFromOrg(org)
        if (extractedRegion != null) {
            if (userRegion == null) return false
            return extractedRegion == userRegion
        }
        if (isLocalGovOrg(org)) return false
        return true
    }


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

        // 놓친 돈 후보 — 자격 충족 + amount > 0 + 현금성/바우처 + 카테고리 적합
        // 융자 제외(갚아야 함), 사업자 아니면 창업 제외(거짓 임팩트 방지).
        // grantType이 비어있으면(어제 데이터 호환) 일단 포함.
        val missedCandidates = eligible.filter { p ->
            if (p.amount <= 0) return@filter false
            if (!isCategoryRelevant(p, profile.occupation)) return@filter false
            if (!isRegionRelevant(p, profile.region)) return@filter false
            if (!isGenderRelevant(p, profile.gender)) return@filter false
            if (p.grantType.isEmpty()) return@filter true  // 풀빌드 전 데이터 호환
            if (p.grantType.any { it in LOAN_GRANT_TYPES }) return@filter false
            // 현금성/바우처 하나라도 포함되면 OK (multi-value)
            p.grantType.any { it in MISSED_GRANT_TYPES }
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

        return HomeData(
            missedTotalAmount = missedTotalAmount,
            missedCount = missedCount,
            missedGrants = missedGrants,
            thisWeekPolicies = listOfNotNull(thisWeek),
            deadlineSoon = deadlineSoon,
        )
    }
}
