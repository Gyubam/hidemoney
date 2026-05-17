package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.data.model.UserProfile

/**
 * 정책이 사용자에게 "의미 있게 노출 가능"한지 판단.
 *
 * `Policy.matchedWith(profile).isEligible`은 supportConditions JA 코드 기반 자격 매칭만.
 * 그 외 region/gender/business 키워드 정보는 데이터에 분리 필드가 없어 클라이언트 키워드로 보강.
 *
 * 홈 missed 카드와 검색 화면의 "자격 충족" 카운트를 동일하게 맞추기 위해 공통 사용.
 */
object PolicyRelevance {

    /** 사업자/창업가 단독 키워드 (false positive 적음). */
    private val BUSINESS_STRONG_KEYWORDS = listOf(
        "창업", "스타트업", "사업화", "벤처", "기업가", "K-스타트업", "사업자",
    )

    /**
     * "공모전", "콘테스트"는 단독으로는 미술/문학 등 일반 공모전 잡힘 (false positive).
     * 다음 키워드 중 하나와 같이 등장할 때만 사업 관련으로 판정.
     */
    private val BUSINESS_COMPETITION_BIGRAMS = listOf(
        "창업 공모전", "사업화 공모전", "스타트업 공모전", "벤처 공모전",
        "창업 경진대회", "사업화 경진대회", "스타트업 경진대회",
        "K-스타트업", "K스타트업",
    )

    /** 17 광역 시·도. applicationOrg에서 substring 검색 우선. */
    private val PROVINCIAL_REGIONS = listOf(
        "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
        "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주",
    )

    /** 시군구 → 광역 매핑. 자주 등장하는 서울/경기/광역시 중심. 점진 확장. */
    private val MUNICIPALITY_TO_REGION = buildMap<String, String> {
        // 서울 25개 구
        listOf(
            "종로구", "중구", "용산구", "성동구", "광진구", "동대문구", "중랑구",
            "성북구", "강북구", "도봉구", "노원구", "은평구", "서대문구", "마포구",
            "양천구", "강서구", "구로구", "금천구", "영등포구", "동작구", "관악구",
            "서초구", "강남구", "송파구", "강동구",
        ).forEach { put(it, "서울") }
        // 경기 주요 시
        listOf(
            "수원시", "성남시", "고양시", "용인시", "부천시", "안산시", "안양시",
            "남양주시", "화성시", "평택시", "의정부시", "시흥시", "파주시", "광명시",
            "김포시", "군포시", "광주시", "이천시", "양주시", "오산시", "구리시",
            "안성시", "포천시", "의왕시", "하남시", "여주시", "양평군", "동두천시",
            "과천시", "가평군", "연천군",
        ).forEach { put(it, "경기") }
        // 광역시·지방 대표 시군 (대표 일부만, 점진 확장)
        put("여수시", "전남"); put("순천시", "전남"); put("목포시", "전남"); put("광양시", "전남"); put("나주시", "전남")
        put("곡성군", "전남"); put("구례군", "전남"); put("고흥군", "전남"); put("보성군", "전남"); put("화순군", "전남")
        put("강진군", "전남"); put("해남군", "전남"); put("영암군", "전남"); put("무안군", "전남"); put("함평군", "전남")
        put("영광군", "전남"); put("장성군", "전남"); put("완도군", "전남"); put("진도군", "전남"); put("신안군", "전남")
        put("전주시", "전북"); put("군산시", "전북"); put("익산시", "전북"); put("정읍시", "전북"); put("남원시", "전북")
        put("김제시", "전북")
        put("창원시", "경남"); put("진주시", "경남"); put("통영시", "경남"); put("사천시", "경남"); put("김해시", "경남")
        put("밀양시", "경남"); put("거제시", "경남"); put("양산시", "경남")
        put("포항시", "경북"); put("경주시", "경북"); put("김천시", "경북"); put("안동시", "경북"); put("구미시", "경북")
        put("영주시", "경북"); put("영천시", "경북"); put("상주시", "경북"); put("문경시", "경북"); put("경산시", "경북")
        put("청주시", "충북"); put("충주시", "충북"); put("제천시", "충북")
        put("천안시", "충남"); put("공주시", "충남"); put("보령시", "충남"); put("아산시", "충남"); put("서산시", "충남")
        put("논산시", "충남"); put("계룡시", "충남"); put("당진시", "충남")
        put("춘천시", "강원"); put("원주시", "강원"); put("강릉시", "강원"); put("동해시", "강원"); put("태백시", "강원")
        put("속초시", "강원"); put("삼척시", "강원")
        put("제주시", "제주"); put("서귀포시", "제주")
    }

    /** 시군구청 패턴 — 광역 추출 실패 시 strict 제외 기준. */
    private val LOCAL_GOV_PATTERN = Regex("(시|군|구)청?$")

    /** 성별 키워드 — "출산"은 부부/가족 대상이므로 여성 한정 X. 임신/임산부/산모/산후만 여성 한정. */
    private val FEMALE_KEYWORDS = listOf("여성", "여학생", "여자", "임산부", "산모", "산후", "임신")
    private val MALE_KEYWORDS = listOf("남성", "남학생", "남자")

    // ─────────────────────────────────────────────────────────────────
    // 개별 검사
    // ─────────────────────────────────────────────────────────────────

    fun isBusinessPolicy(policy: Policy): Boolean {
        if (policy.category == "창업") return true
        val text = "${policy.title} ${policy.summary}"
        // 강한 단독 키워드
        if (BUSINESS_STRONG_KEYWORDS.any { text.contains(it) }) return true
        // "공모전/콘테스트"는 결합어로만 (일반 미술/문학 공모전 false positive 회피)
        return BUSINESS_COMPETITION_BIGRAMS.any { text.contains(it) }
    }

    fun isCategoryRelevant(policy: Policy, occupation: String?): Boolean {
        if (isBusinessPolicy(policy) && occupation != "사업자") return false
        return true
    }

    private fun extractRegionFromOrg(org: String?): String? {
        if (org.isNullOrBlank()) return null
        // 1) 광역 직접 매칭
        PROVINCIAL_REGIONS.firstOrNull { org.contains(it) }?.let { return it }
        // 2) 시군구 → 광역 매핑 (서울 25개 구 / 경기 / 일부 지방)
        MUNICIPALITY_TO_REGION.forEach { (muni, region) ->
            if (org.contains(muni)) return region
        }
        return null
    }

    private fun isLocalGovOrg(org: String?): Boolean {
        if (org.isNullOrBlank()) return false
        return LOCAL_GOV_PATTERN.containsMatchIn(org.trim())
    }

    fun isRegionRelevant(policy: Policy, userRegion: String?): Boolean {
        val org = policy.applicationOrg
        val extractedRegion = extractRegionFromOrg(org)
        if (extractedRegion != null) {
            if (userRegion == null) return false
            return extractedRegion == userRegion
        }
        if (isLocalGovOrg(org)) return false
        return true
    }

    fun isGenderRelevant(policy: Policy, gender: String?): Boolean {
        val text = "${policy.title} ${policy.summary}"
        val femaleTargeted = FEMALE_KEYWORDS.any { text.contains(it) }
        val maleTargeted = MALE_KEYWORDS.any { text.contains(it) }
        if (femaleTargeted && maleTargeted) return true
        if (femaleTargeted && gender != "여") return false
        if (maleTargeted && gender != "남") return false
        return true
    }

    // ─────────────────────────────────────────────────────────────────
    // 종합 판정 — 자격 + 키워드 정밀화 모두 통과해야 true.
    // 홈/검색 양쪽이 같은 정의로 "자격 충족"을 카운트하도록.
    // ─────────────────────────────────────────────────────────────────
    fun isEligibleForUser(policy: Policy, profile: UserProfile): Boolean {
        if (!policy.isEligible) return false
        if (!isCategoryRelevant(policy, profile.occupation)) return false
        if (!isRegionRelevant(policy, profile.region)) return false
        if (!isGenderRelevant(policy, profile.gender)) return false
        return true
    }
}
