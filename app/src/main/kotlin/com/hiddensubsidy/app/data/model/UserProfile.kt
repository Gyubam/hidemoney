package com.hiddensubsidy.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val age: Int? = null,
    val region: String? = null,
    val district: String? = null,      // 시군구 (선택). 입력 시 광역 내 정밀 매칭. 미입력=광역 단위
    val gender: String? = null,        // "남" / "여"
    val occupation: String? = null,    // 직장인 / 학생 / 사업자 / 프리랜서
    val married: Boolean? = null,
    val hasChildren: Boolean? = null,
    val childCount: Int? = null,         // 자녀 수. 다자녀(JA0411) 매칭용. hasChildren=true일 때만 의미
    val incomeMonthly: Long? = null,        // 월 소득 (원). 카테고리 입력의 대표값으로 저장
    val householdSize: Int? = null,         // 가구원수. 4+ 는 4로 캡 저장
    val education: String? = null,          // 학력 ("고졸 미만"/"고졸"/"대학 재학"/"대졸 이상")
    val housingType: String? = null,        // 거주 형태 ("자가"/"전세"/"월세"/"기타")

    // Sensitive 카테고리 — 해당 사용자만 활성화. default false = 해당 정책 제외.
    val isMulticultural: Boolean = false,
    val isSingleParent: Boolean = false,
    val isDisabled: Boolean = false,
    val isVeteran: Boolean = false,
    val isDefector: Boolean = false,        // 북한이탈주민
    val isLowIncome: Boolean = false,       // 저소득/수급자/차상위
    // 표시 옵션 — 융자 정책 포함 (default false: 현금성/바우처만)
    val includeLoanGrants: Boolean = false,
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
            region?.let { r -> parts += if (district != null) "$r $district" else r }
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

/**
 * 광역별 시군구 목록 (표준 행정구역, 2024 기준).
 * 사용자가 광역 선택 후 구/시군까지 고르면 정밀 매칭. 세종은 단일 행정구역이라 제외.
 * 데이터(applicationOrg)의 "서울특별시 동작구" 형태와 매칭.
 */
object Districts {
    val byRegion: Map<String, List<String>> = mapOf(
        "서울" to listOf(
            "종로구", "중구", "용산구", "성동구", "광진구", "동대문구", "중랑구",
            "성북구", "강북구", "도봉구", "노원구", "은평구", "서대문구", "마포구",
            "양천구", "강서구", "구로구", "금천구", "영등포구", "동작구", "관악구",
            "서초구", "강남구", "송파구", "강동구",
        ),
        "부산" to listOf(
            "중구", "서구", "동구", "영도구", "부산진구", "동래구", "남구", "북구",
            "해운대구", "사하구", "금정구", "강서구", "연제구", "수영구", "사상구", "기장군",
        ),
        "대구" to listOf(
            "중구", "동구", "서구", "남구", "북구", "수성구", "달서구", "달성군", "군위군",
        ),
        "인천" to listOf(
            "중구", "동구", "미추홀구", "연수구", "남동구", "부평구", "계양구", "서구",
            "강화군", "옹진군",
        ),
        "광주" to listOf("동구", "서구", "남구", "북구", "광산구"),
        "대전" to listOf("동구", "중구", "서구", "유성구", "대덕구"),
        "울산" to listOf("중구", "남구", "동구", "북구", "울주군"),
        "경기" to listOf(
            "수원시", "성남시", "의정부시", "안양시", "부천시", "광명시", "평택시", "동두천시",
            "안산시", "고양시", "과천시", "구리시", "남양주시", "오산시", "시흥시", "군포시",
            "의왕시", "하남시", "용인시", "파주시", "이천시", "안성시", "김포시", "화성시",
            "광주시", "양주시", "포천시", "여주시", "연천군", "가평군", "양평군",
        ),
        "강원" to listOf(
            "춘천시", "원주시", "강릉시", "동해시", "태백시", "속초시", "삼척시",
            "홍천군", "횡성군", "영월군", "평창군", "정선군", "철원군", "화천군",
            "양구군", "인제군", "고성군", "양양군",
        ),
        "충북" to listOf(
            "청주시", "충주시", "제천시", "보은군", "옥천군", "영동군", "증평군",
            "진천군", "괴산군", "음성군", "단양군",
        ),
        "충남" to listOf(
            "천안시", "공주시", "보령시", "아산시", "서산시", "논산시", "계룡시", "당진시",
            "금산군", "부여군", "서천군", "청양군", "홍성군", "예산군", "태안군",
        ),
        "전북" to listOf(
            "전주시", "군산시", "익산시", "정읍시", "남원시", "김제시", "완주군", "진안군",
            "무주군", "장수군", "임실군", "순창군", "고창군", "부안군",
        ),
        "전남" to listOf(
            "목포시", "여수시", "순천시", "나주시", "광양시", "담양군", "곡성군", "구례군",
            "고흥군", "보성군", "화순군", "장흥군", "강진군", "해남군", "영암군", "무안군",
            "함평군", "영광군", "장성군", "완도군", "진도군", "신안군",
        ),
        "경북" to listOf(
            "포항시", "경주시", "김천시", "안동시", "구미시", "영주시", "영천시", "상주시",
            "문경시", "경산시", "의성군", "청송군", "영양군", "영덕군", "청도군", "고령군",
            "성주군", "칠곡군", "예천군", "봉화군", "울진군", "울릉군",
        ),
        "경남" to listOf(
            "창원시", "진주시", "통영시", "사천시", "김해시", "밀양시", "거제시", "양산시",
            "의령군", "함안군", "창녕군", "고성군", "남해군", "하동군", "산청군", "함양군",
            "거창군", "합천군",
        ),
        "제주" to listOf("제주시", "서귀포시"),
    )

    fun forRegion(region: String?): List<String> = region?.let { byRegion[it] } ?: emptyList()
}

object Occupations {
    val all = listOf("직장인", "학생", "사업자", "프리랜서", "구직 중", "농어업", "예술인")
}

object Genders {
    val all = listOf("남", "여")
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
