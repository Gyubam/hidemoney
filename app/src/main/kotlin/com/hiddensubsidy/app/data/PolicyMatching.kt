package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.EligibilityRule
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.data.model.UserProfile

/**
 * 사용자 프로필 + 정책 자격 룰 매칭.
 *
 * 정보 부재 처리:
 * - 필수(age, region) 부재인데 룰이 그 조건 요구 → false
 * - 선택(occupation, married, hasChildren) 부재 → 관대(통과)
 */
fun EligibilityRule.matches(profile: UserProfile): Boolean {
    minAge?.let {
        val age = profile.age ?: return false
        if (age < it) return false
    }
    maxAge?.let {
        val age = profile.age ?: return false
        if (age > it) return false
    }
    regions?.let { allowed ->
        val region = profile.region ?: return false
        if (region !in allowed) return false
    }
    requiresOccupation?.let { allowed ->
        // 선택 정보 — 미입력은 통과
        val occ = profile.occupation ?: return@let
        if (occ !in allowed) return false
    }
    requiresMarried?.let { req ->
        val m = profile.married ?: return@let
        if (m != req) return false
    }
    requiresChildren?.let { req ->
        val c = profile.hasChildren ?: return@let
        if (c != req) return false
    }
    return true
}

/** 정책에 매칭 결과를 inject. 룰 없으면 그대로(기본 isEligible 유지). */
fun Policy.matchedWith(profile: UserProfile): Policy {
    val rule = eligibilityRule ?: return this
    return copy(isEligible = rule.matches(profile))
}

fun List<Policy>.matchedWith(profile: UserProfile): List<Policy> =
    map { it.matchedWith(profile) }

/** 자격 충족 정책만 필터. */
fun List<Policy>.eligibleOnly(profile: UserProfile): List<Policy> =
    matchedWith(profile).filter { it.isEligible }
