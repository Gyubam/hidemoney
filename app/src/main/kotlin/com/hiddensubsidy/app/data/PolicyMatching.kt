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

/** sentinel 무시 후 의미 있는 조건이 0개면 룰 사실상 없음 — 매칭 불가로 처리. */
private fun EligibilityRule.hasEffectiveCondition(): Boolean {
    if (effectiveMinAge() != null) return true
    if (effectiveMaxAge() != null) return true
    if (regions != null) return true
    if (effectiveRequiresOccupation() != null) return true
    if (requiresMarried != null) return true
    if (requiresChildren != null) return true
    if (minChildCount != null) return true
    if (requiresMulticultural == true) return true
    if (requiresDefector == true) return true
    if (requiresSingleParent == true) return true
    if (requiresDisabled == true) return true
    if (requiresVeteran == true) return true
    if (maxIncomeMonthly != null) return true
    if (maxIncomePercent != null) return true
    if (maxHouseholdSize != null) return true
    if (minHouseholdSize != null) return true
    if (requiresEducation != null) return true
    if (requiresHousingType != null) return true
    return false
}

/**
 * 사용자 프로필 + 정책 자격 룰 strict 매칭.
 *
 * "정확하지 않으면 매칭 X" 원칙 (행동 유도):
 * - 룰의 어떤 조건이든 사용자 정보 부재 → false
 * - 룰 자체가 의미 없는 sentinel만 가짐 → false
 *
 * 더 많은 매칭 원하면 프로필 입력 유도 (My 탭). 모름=true는 사용자에게 거짓 임팩트.
 */
fun EligibilityRule.matches(profile: UserProfile): Boolean {
    if (!hasEffectiveCondition()) return false

    effectiveMinAge()?.let {
        val age = profile.age ?: return false
        if (age < it) return false
    }
    effectiveMaxAge()?.let {
        val age = profile.age ?: return false
        if (age > it) return false
    }
    regions?.let { allowed ->
        val region = profile.region ?: return false
        if (region !in allowed) return false
    }
    effectiveRequiresOccupation()?.let { allowed ->
        val occ = profile.occupation ?: return false
        if (occ !in allowed) return false
    }
    requiresMarried?.let { req ->
        val m = profile.married ?: return false
        if (m != req) return false
    }
    requiresChildren?.let { req ->
        val c = profile.hasChildren ?: return false
        if (c != req) return false
    }
    minChildCount?.let { min ->
        // 다자녀 정책 — childCount 부재 시 false (strict). hasChildren=false면 자동 fail.
        val cc = profile.childCount ?: return false
        if (cc < min) return false
    }
    // Sensitive — UserProfile에 정보 없음 → strict 자동 false
    if (requiresMulticultural == true) return false
    if (requiresDefector == true) return false
    if (requiresSingleParent == true) return false
    if (requiresDisabled == true) return false
    if (requiresVeteran == true) return false
    maxIncomeMonthly?.let { max ->
        val inc = profile.incomeMonthly ?: return false
        if (inc > max) return false
    }
    maxIncomePercent?.let { maxPct ->
        val inc = profile.incomeMonthly ?: return false
        // 가구원수 미입력 시 1인 가정 fallback — strict로 다 제외하면 정책 21.5% 사라짐
        val hs = (profile.householdSize ?: 1).coerceAtMost(MEDIAN_INCOME_2026.keys.max())
        val median = MEDIAN_INCOME_2026[hs] ?: return false
        val userPct = (inc * 100 / median).toInt()
        if (userPct > maxPct) return false
    }
    maxHouseholdSize?.let { max ->
        val hs = profile.householdSize ?: return false
        if (hs > max) return false
    }
    minHouseholdSize?.let { min ->
        val hs = profile.householdSize ?: return false
        if (hs < min) return false
    }
    requiresEducation?.let { allowed ->
        val edu = profile.education ?: return false
        if (edu !in allowed) return false
    }
    requiresHousingType?.let { allowed ->
        val house = profile.housingType ?: return false
        if (house !in allowed) return false
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
