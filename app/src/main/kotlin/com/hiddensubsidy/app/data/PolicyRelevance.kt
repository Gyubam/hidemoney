package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.Districts
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

    /**
     * 도 단위 풀네임 → 약칭. 데이터의 applicationOrg는 "충청북도"·"경상남도" 같은 풀네임을 씀.
     * "충북"·"경남" 약칭은 풀네임의 부분문자열이 아니라(서울특별시⊃서울과 달리) 매칭 실패 →
     * 도 정책이 "전국"으로 오인돼 전 지역에 노출되던 버그 방지. 풀네임을 먼저 검사.
     */
    private val FULL_NAME_REGIONS = mapOf(
        "충청북도" to "충북", "충청남도" to "충남",
        "경상북도" to "경북", "경상남도" to "경남",
        "전라북도" to "전북", "전라남도" to "전남",
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

    /**
     * Sensitive 카테고리 — 일반 사용자(이런 정보 입력 안 함) 가정으로 default 제외.
     * 향후 UserProfile에 옵션 추가하면 사용자가 해당이면 노출 가능.
     *
     * - 정부 특정 신분(북한이탈/다문화/한부모/장애/보훈) → 해당자만 받음
     * - 행정 정책(범죄수익/환수/압수) → 일반 지원금 아님
     */
    private val SENSITIVE_KEYWORDS = listOf(
        "북한이탈주민", "탈북", "새터민",
        "다문화가족", "다문화가정",
        "한부모", "조손",
        "장애인", "장애아동", "장애학생",
        "국가유공자", "보훈", "참전유공",
        "범죄수익", "범죄피해", "환수", "압수", "벌금",
        "재소자", "출소자", "보호관찰",
        "독립유공", "위안부",
    )

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
        // 0) 도 단위 풀네임 우선 ("충청북도"→"충북"). 약칭이 부분문자열로 안 잡히는 케이스.
        FULL_NAME_REGIONS.forEach { (full, short) -> if (org.contains(full)) return short }
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

    /** applicationOrg에서 추출한 지역이 내 지역과 정확히 일치 = 지자체 특화 매칭 (랭킹 가점용). */
    fun isRegionSpecificMatch(policy: Policy, userRegion: String?): Boolean {
        if (userRegion == null) return false
        return extractRegionFromOrg(policy.applicationOrg) == userRegion
    }

    /** applicationOrg에서 해당 광역의 시군구를 추출. 없으면 null. */
    private fun extractDistrictFromOrg(org: String?, region: String): String? {
        if (org.isNullOrBlank()) return null
        val districts = Districts.byRegion[region] ?: return null
        // 긴 이름 우선 매칭 — "강서구"가 "서구"보다 먼저 잡히도록
        return districts.sortedByDescending { it.length }.firstOrNull { org.contains(it) }
    }

    /** 내 구와 정책 명시 구가 일치 = 우리 동네 정책 (랭킹 가점용). */
    fun isDistrictSpecificMatch(policy: Policy, userRegion: String?, userDistrict: String?): Boolean {
        if (userRegion == null || userDistrict == null) return false
        if (extractRegionFromOrg(policy.applicationOrg) != userRegion) return false
        return extractDistrictFromOrg(policy.applicationOrg, userRegion) == userDistrict
    }

    fun isRegionRelevant(policy: Policy, userRegion: String?, userDistrict: String? = null): Boolean {
        val org = policy.applicationOrg
        val extractedRegion = extractRegionFromOrg(org)
        if (extractedRegion != null) {
            if (userRegion == null) return false
            if (extractedRegion != userRegion) return false
            // 광역 일치 — 구까지 입력돼 있으면 다른 구 정책 제외 (정책에 구 명시된 경우만)
            if (userDistrict != null) {
                val policyDistrict = extractDistrictFromOrg(org, extractedRegion)
                if (policyDistrict != null && policyDistrict != userDistrict) return false
            }
            return true
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

    // 항상 제외 — 일반 사용자에게 의미 0 (사용자 토글 X)
    private val ADMIN_KEYWORDS = listOf(
        "범죄수익", "범죄피해", "환수", "압수", "벌금", "징수",
        "재소자", "출소자", "보호관찰", "교정",
    )
    // 세무 행정 서비스 — 세금 분쟁/상담 도움. 지원금 아니고 일반인 거의 무관.
    // "지방세 감면"·"한옥 감면" 같은 실제 혜택은 이 키워드에 안 걸려 유지됨.
    private val TAX_SERVICE_KEYWORDS = listOf(
        "불복청구", "마을세무사", "과세전적부심사", "심사청구 대리", "이의신청 대리",
        "납세자보호", "세무 상담", "세무상담",
    )
    private val VICTIM_KEYWORDS = listOf(
        "가정폭력", "성폭력", "성매매", "학교폭력", "데이트폭력", "디지털성폭력",
        "스토킹", "아동학대", "노인학대", "인신매매",
        "피해자 보호", "피해 가구",
    )
    private val FACILITY_KEYWORDS = listOf(
        "LPG용기", "LPG 용기", "보일러 교체", "지하수", "정화조",
        "축사 시설", "온실 시설", "양식장", "어선 시설",
    )
    private val DISEASE_KEYWORDS = listOf(
        "C형간염", "B형간염", "결핵", "한센", "에이즈", "HIV", "투석",
        "희귀질환", "암환자", "치매", "조현병", "발달장애 진단", "지적장애 진단",
        "정신질환자",
    )
    private val SPECIAL_OCCUPATION_KEYWORDS = listOf(
        "광업 종사", "광부", "광산",
        "원양어업", "어선원", "선원", "양식 어업",
        "임업 종사", "산림조합",
        "축산농가", "축산업자",
        "농민회", "어민회",
        "건축주", "건설업자", "시공사",
        "운수업", "택시사업자", "화물차주", "버스기사",
    )
    private val ADOPTION_KEYWORDS = listOf("입양", "위탁가정", "유기동물", "유기견", "유기묘")

    /** 농어업·축산·임업 종사자 전용 — 일반 직장인에게 의미 X. */
    private val AGRI_FORESTRY_KEYWORDS = listOf(
        "농업인", "어업인", "축산농가", "축산업자", "임업 종사", "임업인",
        "농가소득", "어가소득", "농촌 정착", "어촌 정착",
        "농어가", "어가", "농어업", "농촌체험", "농가 부업",
        "양식어업", "농업법인", "어업법인", "수산업법",
        "농지", "농기계", "어선원",
    )

    /** 특정 차량/장비 보유자 전용. */
    private val VEHICLE_EQUIPMENT_KEYWORDS = listOf(
        "노후 경유차", "노후경유차", "경유차 조기",
        "이륜차 운전자", "이륜차 보유",
        "전기차 보조", "수소차 보조", "전기이륜차",
        "어선 보유", "어선 안전", "어선 기반",
        "농기계 임대",
    )

    /** 주택·시설물 개량 — 자가/주택 보유자만. */
    private val FACILITY_OWNER_KEYWORDS = listOf(
        "태양광 설치", "옥상녹화", "노후주택", "단열 시공", "지붕 개량",
        "주거환경 개선", "집수리", "주택 개보수", "노후 보일러 교체",
        "미니태양광",
    )

    /** 반려동물 — 보유자만. */
    private val PET_KEYWORDS = listOf(
        "반려동물", "반려견", "반려묘", "광견병", "동물보호", "반려인",
    )

    /** 학교/교사 — 교원만. */
    private val SCHOOL_STAFF_KEYWORDS = listOf(
        "교원", "교사 대상", "학교장", "사립학교 교직원", "강사 채용",
        "학교 운영 지원", "교직원",
    )

    /** 재난·이재민 — 재난 당사자만 (시기적). */
    private val DISASTER_VICTIM_KEYWORDS = listOf(
        "이재민", "재난피해 가구", "수해 복구", "한파 피해", "재해 위로금",
        "재난지원금", "이재 가구",
    )

    /** R&D / 특허 / 연구원 — 일반 사용자 X. */
    private val RND_KEYWORDS = listOf(
        "R&D 과제", "연구개발 과제", "특허 출원", "특허 분쟁",
        "연구장비", "연구과제 지원",
    )

    /** 예술인 — occupation="예술인"만. */
    private val ARTIST_KEYWORDS = listOf(
        "예술인 지원", "공연예술 종사", "전통예술 종사",
        "예술인 패스", "예술인 복지", "창작 지원금",
    )

    /** 소상공인/사업체/법인 — occupation="사업자"만. */
    private val BUSINESS_OWNER_KEYWORDS = listOf(
        "소상공인", "자영업", "중소기업", "법인 대상", "사업체 대상",
        "협동조합 지원", "전통시장 상인", "상점가", "프랜차이즈",
    )

    /** 청소년/학교밖 — 만 18세 이하만. */
    private val YOUTH_KEYWORDS = listOf(
        "청소년", "학교밖", "학교 밖 청소년", "중도탈락",
    )

    /** 저소득/수급자/차상위 — isLowIncome 토글 켰을 때만. */
    private val LOW_INCOME_KEYWORDS = listOf(
        "수급자", "수급권자", "차상위", "기초생활", "저소득",
        "취약계층", "긴급복지",
    )

    /**
     * 키워드 기반 sensitive 정책 판단.
     * 사용자 마크에 따라 해당 카테고리 키워드는 통과시킴.
     * 일부 카테고리(행정/피해자/시설/질병/특수직업)는 사용자 토글 X — 일반 사용자에게 의미 0.
     */
    fun isSensitiveExclusion(policy: Policy, profile: UserProfile): Boolean {
        val text = "${policy.title} ${policy.summary}"

        // 항상 제외 카테고리 (사용자 토글 X)
        if (ADMIN_KEYWORDS.any { text.contains(it) }) return true
        if (TAX_SERVICE_KEYWORDS.any { text.contains(it) }) return true
        if (VICTIM_KEYWORDS.any { text.contains(it) }) return true
        if (FACILITY_KEYWORDS.any { text.contains(it) }) return true
        if (DISEASE_KEYWORDS.any { text.contains(it) }) return true
        if (SPECIAL_OCCUPATION_KEYWORDS.any { text.contains(it) }) return true
        if (ADOPTION_KEYWORDS.any { text.contains(it) }) return true
        // 자동 제외 — 특정 보유자/대상만 받는 정책
        if (VEHICLE_EQUIPMENT_KEYWORDS.any { text.contains(it) }) return true
        if (FACILITY_OWNER_KEYWORDS.any { text.contains(it) }) return true
        if (PET_KEYWORDS.any { text.contains(it) }) return true
        if (SCHOOL_STAFF_KEYWORDS.any { text.contains(it) }) return true
        if (DISASTER_VICTIM_KEYWORDS.any { text.contains(it) }) return true
        if (RND_KEYWORDS.any { text.contains(it) }) return true

        // occupation 기반 분기 — 농어업/예술인/사업자만 통과
        if (AGRI_FORESTRY_KEYWORDS.any { text.contains(it) } && profile.occupation != "농어업") return true
        if (ARTIST_KEYWORDS.any { text.contains(it) } && profile.occupation != "예술인") return true
        if (BUSINESS_OWNER_KEYWORDS.any { text.contains(it) } && profile.occupation != "사업자") return true

        // 청소년/학교밖 — 만 18세 이하만
        val age = profile.age
        if (YOUTH_KEYWORDS.any { text.contains(it) } && (age == null || age > 18)) return true

        // 저소득/수급자/차상위 — 본인 토글 켰을 때만 통과
        if (LOW_INCOME_KEYWORDS.any { text.contains(it) } && !profile.isLowIncome) return true

        // 사용자 마크에 따라 분기
        val femaleSensitive = listOf("위안부")
        val veteran = listOf("국가유공자", "보훈", "참전유공", "독립유공")
        val multicultural = listOf("다문화가족", "다문화가정")
        val defector = listOf("북한이탈주민", "탈북", "새터민")
        val singleParent = listOf("한부모", "조손")
        val disabled = listOf("장애인", "장애아동", "장애학생")

        if (femaleSensitive.any { text.contains(it) } && profile.gender != "여") return true
        if (veteran.any { text.contains(it) } && !profile.isVeteran) return true
        if (multicultural.any { text.contains(it) } && !profile.isMulticultural) return true
        if (defector.any { text.contains(it) } && !profile.isDefector) return true
        if (singleParent.any { text.contains(it) } && !profile.isSingleParent) return true
        if (disabled.any { text.contains(it) } && !profile.isDisabled) return true
        return false
    }

    // ─────────────────────────────────────────────────────────────────
    // 종합 판정 — 자격 + 키워드 정밀화 모두 통과해야 true.
    // 홈/검색 양쪽이 같은 정의로 "자격 충족"을 카운트하도록.
    // ─────────────────────────────────────────────────────────────────
    /** 자녀 관련 정책 키워드 — 자녀 없는 사용자에게 제외. */
    private val CHILD_KEYWORDS = listOf(
        "다자녀", "둘째", "셋째", "넷째", "두 자녀", "세 자녀",
        "자녀가", "자녀를", "자녀의", "자녀 양육",
        "유아", "영유아", "어린이", "아이",
        "초등학생", "유치원", "어린이집", "유치원생",
        "영아", "신생아", "출생아",
    )

    fun isChildPolicyMismatched(policy: Policy, profile: UserProfile): Boolean {
        if (profile.hasChildren == true) return false
        val text = "${policy.title} ${policy.summary}"
        return CHILD_KEYWORDS.any { text.contains(it) }
    }

    /** 주거 (무주택) 정책 — 자가 사용자에게 제외. */
    private val HOMELESS_KEYWORDS = listOf(
        "무주택", "전세자금", "월세 지원", "월세지원", "임차", "임대주택",
        "전월세", "주택 매입", "주택 구입",
    )

    fun isHousingPolicyMismatched(policy: Policy, profile: UserProfile): Boolean {
        if (profile.housingType == null) return false  // 미입력 시 통과 (관대)
        if (profile.housingType == "자가") {
            val text = "${policy.title} ${policy.summary}"
            return HOMELESS_KEYWORDS.any { text.contains(it) }
        }
        return false
    }

    /** 1인가구 전용 정책 — 1인 사용자만 통과. */
    private val SINGLE_HOUSEHOLD_KEYWORDS = listOf("1인가구", "1인 가구", "일인가구", "독거")

    fun isSingleHouseholdMismatched(policy: Policy, profile: UserProfile): Boolean {
        val text = "${policy.title} ${policy.summary}"
        val isSingleOnly = SINGLE_HOUSEHOLD_KEYWORDS.any { text.contains(it) }
        if (!isSingleOnly) return false
        return profile.householdSize != null && profile.householdSize != 1
    }

    /** 노인 (60세+) 정책 — 30~50대 사용자에게 의미 X. age sentinel 보강용. */
    private val SENIOR_KEYWORDS = listOf(
        "노인", "어르신", "고령자", "장년",
        "노년", "치매 어르신",
    )

    fun isSeniorPolicyMismatched(policy: Policy, profile: UserProfile): Boolean {
        val age = profile.age ?: return false
        if (age >= 60) return false  // 노인 사용자면 통과
        val text = "${policy.title} ${policy.summary}"
        return SENIOR_KEYWORDS.any { text.contains(it) }
    }

    fun isEligibleForUser(policy: Policy, profile: UserProfile): Boolean {
        if (!policy.isEligible) return false
        if (isSensitiveExclusion(policy, profile)) return false
        if (isChildPolicyMismatched(policy, profile)) return false
        if (isHousingPolicyMismatched(policy, profile)) return false
        if (isSingleHouseholdMismatched(policy, profile)) return false
        if (isSeniorPolicyMismatched(policy, profile)) return false
        if (!isCategoryRelevant(policy, profile.occupation)) return false
        if (!isRegionRelevant(policy, profile.region, profile.district)) return false
        if (!isGenderRelevant(policy, profile.gender)) return false
        return true
    }
}
