package com.hiddensubsidy.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val age: Int? = null,
    val region: String? = null,
    val occupation: String? = null,    // 직장인 / 학생 / 사업자 / 프리랜서
    val married: Boolean? = null,
    val hasChildren: Boolean? = null,
    val incomeMonthly: Long? = null,
) {
    /** 프로필 정확도 0.0~1.0 — 필수 2개에 50%, 선택 4개에 50% */
    val completeness: Float
        get() {
            var score = 0f
            if (age != null) score += 0.25f
            if (region != null) score += 0.25f
            if (occupation != null) score += 0.125f
            if (married != null) score += 0.125f
            if (hasChildren != null) score += 0.125f
            if (incomeMonthly != null) score += 0.125f
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
