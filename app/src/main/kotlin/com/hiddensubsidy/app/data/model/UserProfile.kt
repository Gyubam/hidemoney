package com.hiddensubsidy.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val age: Int? = null,
    val region: String? = null,
    val occupation: String? = null,    // 직장인 / 학생 / 사업자 / 프리랜서
    val married: Boolean? = null,
    val hasChildren: Boolean? = null,
    val incomeMonthly: Long? = null,        // 월 소득 (원). 카테고리 입력의 대표값으로 저장
    val householdSize: Int? = null,         // 가구원수. 4+ 는 4로 캡 저장
    val education: String? = null,          // 학력 ("고졸 미만"/"고졸"/"대학 재학"/"대졸 이상")
    val housingType: String? = null,        // 거주 형태 ("자가"/"전세"/"월세"/"기타")
) {
    /** 프로필 정확도 0.0~1.0. 필수 2개에 30%, 선택 7개에 70% (각 10%). */
    val completeness: Float
        get() {
            var score = 0f
            if (age != null) score += 0.15f
            if (region != null) score += 0.15f
            if (occupation != null) score += 0.10f
            if (married != null) score += 0.10f
            if (hasChildren != null) score += 0.10f
            if (incomeMonthly != null) score += 0.10f
            if (householdSize != null) score += 0.10f
            if (education != null) score += 0.10f
            if (housingType != null) score += 0.10f
            return score
        }

    val summary: String
        get() {
            val parts = mutableListOf<String>()
            age?.let { parts += "만 ${it}세" }
            region?.let { parts += it }
            return if (parts.isEmpty()) "프로필 미입력" else parts.joinToString(" · ")
        }
}

@Serializable
data class MySummary(
    val savedCount: Int,
    val savedAmount: Long,
    val appliedCount: Int,
    val appliedAmount: Long,
    val receivedCount: Int,
    val receivedAmount: Long,
)

object Regions {
    val all = listOf(
        "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
        "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주",
    )
}

object Occupations {
    val all = listOf("직장인", "학생", "사업자", "프리랜서", "구직 중")
}

object Educations {
    val all = listOf("고졸 미만", "고졸", "대학 재학", "대졸 이상")
}

object HousingTypes {
    val all = listOf("자가", "전세", "월세", "기타")
}

object HouseholdSizes {
    /** UI 표시 옵션. value = 저장값 (4+는 4로 캡) */
    data class Option(val label: String, val value: Int)
    val all = listOf(
        Option("1인 가구", 1),
        Option("2인 가구", 2),
        Option("3인 가구", 3),
        Option("4인 이상", 4),
    )
    fun labelFor(value: Int?): String? = value?.let { v -> all.firstOrNull { it.value == v }?.label }
}

/**
 * 월 소득 구간. 정부 정책의 "중위소득 N% 이하" 기준은 가구원수마다 달라서
 * 우리는 사용자에게 월 소득 카테고리 받고, 저장은 대표값(구간 중앙값)으로 함.
 *
 * 2026년 기준 1인 가구 중위소득 약 230만, 2인 약 380만. 카테고리는 보수적으로 광범위.
 */
object IncomeBrackets {
    data class Option(val label: String, val midValue: Long)
    val all = listOf(
        Option("100만원 이하", 800_000L),
        Option("100~200만원", 1_500_000L),
        Option("200~300만원", 2_500_000L),
        Option("300~400만원", 3_500_000L),
        Option("400~500만원", 4_500_000L),
        Option("500만원 이상", 6_000_000L),
    )
    fun labelFor(value: Long?): String? {
        if (value == null) return null
        // 저장된 midValue 또는 가까운 범주 찾기
        return all.firstOrNull { it.midValue == value }?.label
            ?: all.minByOrNull { kotlin.math.abs(it.midValue - value) }?.label
    }
}
