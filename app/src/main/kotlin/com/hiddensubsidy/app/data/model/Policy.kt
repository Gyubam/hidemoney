package com.hiddensubsidy.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Policy(
    val id: String,
    val title: String,
    val amount: Long,                  // 원 단위
    val deadline: String,              // ISO yyyy-MM-dd
    val daysLeft: Int,
    val category: String,              // 청년·주거·창업 등
    val summary: String,
    val region: String? = null,
    // === 정책 상세용 ===
    val period: String? = null,                       // "최대 12개월"
    val eligibility: List<String> = emptyList(),      // 자격 조건 텍스트
    val documents: List<DocumentRequirement> = emptyList(),
    val procedure: List<String> = emptyList(),        // 신청 절차
    val applicationOrg: String? = null,               // "복지로", "정부24"
    val applicationUrl: String? = null,               // 외부 신청 URL
    val isEligible: Boolean = true,                   // 자격 충족 여부 — 동적 계산 결과
    // === 자격 매칭 룰 (선언적) ===
    val eligibilityRule: EligibilityRule? = null,
)

/**
 * 정책 자격 매칭 룰. null 필드는 "조건 없음".
 * - 필수 정보(age/region) 부재 시 해당 조건은 미충족 처리.
 * - 선택 정보(occupation/married/hasChildren) 부재 시 관대하게 통과.
 */
@Serializable
data class EligibilityRule(
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val regions: List<String>? = null,          // null=전국, 비어있으면 특정 지역들
    val requiresOccupation: List<String>? = null,
    val requiresMarried: Boolean? = null,
    val requiresChildren: Boolean? = null,
)

@Serializable
data class DocumentRequirement(
    val name: String,
    val sourceUrl: String? = null,                    // 발급처 딥링크
)

@Serializable
data class MissedGrant(
    val id: String,
    val title: String,
    val amount: Long,
    val eligibleFrom: String,          // 자격이 충족됐던 시점 yyyy-MM
    val year: Int,
    val summary: String,
)

@Serializable
data class HomeData(
    val missedTotalAmount: Long,
    val missedCount: Int,
    val missedGrants: List<MissedGrant>,
    val thisWeekPolicies: List<Policy>,
    val deadlineSoon: List<Policy>,
)
