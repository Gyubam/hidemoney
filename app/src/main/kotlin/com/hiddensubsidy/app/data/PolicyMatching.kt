package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.EligibilityRule
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.data.model.UserProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// 정부 API sentinel — "전 연령 대상"을 maxAge=120/100/99 같은 값으로 표기. 무의미한 룰.
private const val MAX_AGE_SENTINEL = 100
// minAge=0/1은 "조건 없음" 사실상 의미. 신생아 정책 같은 게 minAge=0이라도 다른 룰로 판별돼야.
private const val MIN_AGE_VALID_FLOOR = 2

/** 2026년 기준 가구원수별 월 중위소득 (원). 5인 이상은 4인 기준 cap. */
private val MEDIAN_INCOME_2026 = mapOf(
    1 to 2_228_445L,
    2 to 3_682_609L,
    3 to 4_714_657L,
    4 to 5_729_913L,
)

private fun EligibilityRule.effectiveMinAge(): Int? {
    val mi = minAge?.takeIf { it >= MIN_AGE_VALID_FLOOR } ?: return null
    // "minAge=18/19 + maxAge sentinel" 패턴 = 사실상 "성인 누구나" → broad
    val maxIsSentinel = maxAge == null || maxAge >= MAX_AGE_SENTINEL
    if (maxIsSentinel && mi <= 19) return null
    return mi
}

private fun EligibilityRule.effectiveMaxAge(): Int? =
    maxAge?.takeIf { it < MAX_AGE_SENTINEL }

// 정부 supportConditions에서 normalize 단계가 만드는 모든 직업 코드 — 다 있으면 "직업 무관".
// 이전 normalize.py(broad detection 전)로 만든 어제 데이터에서 즉시 효과.
private val ALL_OCCUPATIONS_FROM_DATA = setOf("학생", "직장인", "구직 중")

/** occupation 룰이 모든 직업 카테고리 포함 = 사실상 무관 = null 처리. */
private fun EligibilityRule.effectiveRequiresOccupation(): List<String>? {
    val req = requiresOccupation ?: return null
    val set = req.toSet()
    return if (ALL_OCCUPATIONS_FROM_DATA.all { it in set }) null else req
}

/**
 * 사용자 프로필 + 정책 자격 룰 soft 매칭.
 *
 * "명백히 안 맞는 것만 제외" 원칙 (A: 개인화 랭킹 + B: 보편 정책 구제):
 * - 입력된 정보로 **명백히 위반**하는 조건이 있을 때만 false (나이 초과, 확정 타지역 등)
 * - 사용자 정보 **미입력** 조건 → 통과 (false 아님). 부적합은 PolicyRelevance 키워드/지역이 컷
 * - 조건 없는 **보편 정책**(sentinel만) → 통과. 전국민 대상 좋은 정책이 숨지 않도록
 *
 * 노출 순서는 [relevanceScore]가 "내게 특화된 정책"을 위로 올려 결정.
 * (이전 strict 버전: 정보 부재/sentinel = false. 다 입력해도 매칭 급감하던 문제로 완화.)
 */
fun EligibilityRule.matches(profile: UserProfile): Boolean {
    // 나이 — 입력돼 있고 명시 범위를 벗어날 때만 제외
    profile.age?.let { age ->
        effectiveMinAge()?.let { if (age < it) return false }
        effectiveMaxAge()?.let { if (age > it) return false }
    }
    // 지역 — 입력돼 있고 룰의 허용 지역에 없을 때만 제외
    profile.region?.let { region ->
        regions?.let { allowed -> if (region !in allowed) return false }
    }
    // 직업 — 입력돼 있고 명시 직업군에 없을 때만 제외
    profile.occupation?.let { occ ->
        effectiveRequiresOccupation()?.let { allowed -> if (occ !in allowed) return false }
    }
    // 혼인 — 입력돼 있고 요구 상태와 다를 때만 제외
    profile.married?.let { m ->
        requiresMarried?.let { req -> if (m != req) return false }
    }
    // requiresChildren / maxHouseholdSize / requiresHousingType / sensitive 5종은
    // 데이터 sentinel inflate(76~86%)로 신뢰도 낮아 매칭에서 무시. 키워드 기반은 PolicyRelevance에서.
    // 소득 — 입력돼 있고 상한 초과할 때만 제외
    profile.incomeMonthly?.let { inc ->
        maxIncomeMonthly?.let { max -> if (inc > max) return false }
        maxIncomePercent?.let { maxPct ->
            val hs = (profile.householdSize ?: 1).coerceAtMost(MEDIAN_INCOME_2026.keys.max())
            val median = MEDIAN_INCOME_2026[hs]
            if (median != null) {
                val userPct = (inc * 100 / median).toInt()
                if (userPct > maxPct) return false
            }
        }
    }
    // 학력 — 입력돼 있고 명시 학력군에 없을 때만 제외
    profile.education?.let { edu ->
        requiresEducation?.let { allowed -> if (edu !in allowed) return false }
    }
    return true
}

/** 정책에 매칭 결과를 inject. 룰 없으면 매칭 불가(false). */
fun Policy.matchedWith(profile: UserProfile): Policy {
    val rule = eligibilityRule ?: return copy(isEligible = false)
    return copy(isEligible = rule.matches(profile))
}

fun List<Policy>.matchedWith(profile: UserProfile): List<Policy> =
    map { it.matchedWith(profile) }

/** 자격 충족 정책만 필터. */
fun List<Policy>.eligibleOnly(profile: UserProfile): List<Policy> =
    matchedWith(profile).filter { it.isEligible }

/**
 * 개인화 노출 점수 (높을수록 사용자에게 특화 → 위로 정렬).
 *
 * base = roiScore(0~100). 룰의 명시 조건이 내 프로필과 들어맞을 때마다 가점.
 * → "내게 딱 맞는 정책"이 위로, 조건 없는 보편 정책은 roiScore만으로 아래.
 * 금액순 정렬을 대체 (이전엔 roiScore가 정렬에 안 쓰이고 amount만 사용).
 */
fun Policy.relevanceScore(profile: UserProfile): Int {
    var score = roiScore ?: 0
    val rule = eligibilityRule ?: return score
    // 내 지역 특화(지자체 맞춤) — 가장 강한 신호. 우리 구까지 일치하면 추가 가점
    if (PolicyRelevance.isRegionSpecificMatch(this, profile.region)) score += 35
    if (PolicyRelevance.isDistrictSpecificMatch(this, profile.region, profile.district)) score += 20
    profile.region?.let { region ->
        if (rule.regions?.contains(region) == true) score += 20
    }
    profile.age?.let { age ->
        val mi = rule.effectiveMinAge()
        val ma = rule.effectiveMaxAge()
        if ((mi != null && age >= mi) || (ma != null && age <= ma)) score += 20
    }
    profile.occupation?.let { occ ->
        if (rule.effectiveRequiresOccupation()?.contains(occ) == true) score += 15
    }
    profile.married?.let { m -> if (rule.requiresMarried == m) score += 10 }
    profile.incomeMonthly?.let {
        if (rule.maxIncomePercent != null || rule.maxIncomeMonthly != null) score += 10
    }
    profile.education?.let { edu ->
        if (rule.requiresEducation?.contains(edu) == true) score += 10
    }
    return score
}

/** 관련도 순 정렬 (동점 시 금액 큰 순). */
fun List<Policy>.sortedByRelevance(profile: UserProfile): List<Policy> =
    sortedWith(compareByDescending<Policy> { it.relevanceScore(profile) }.thenByDescending { it.amount })

private val ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE

/**
 * 빌드 시점 stale daysLeft를 today 기준으로 재계산.
 * deadline 빈 값/파싱 실패면 daysLeft=0 + 그대로 둠 (UI는 deadline.isNotBlank()로 분기).
 */
fun Policy.withFreshDaysLeft(today: LocalDate): Policy {
    if (deadline.isBlank()) return copy(daysLeft = 0)
    return try {
        val d = LocalDate.parse(deadline, ISO_DATE)
        copy(daysLeft = ChronoUnit.DAYS.between(today, d).toInt())
    } catch (_: Exception) {
        copy(daysLeft = 0)
    }
}

fun List<Policy>.withFreshDaysLeft(today: LocalDate): List<Policy> =
    map { it.withFreshDaysLeft(today) }
