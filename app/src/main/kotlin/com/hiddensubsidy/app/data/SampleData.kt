package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.CalendarEventKind
import com.hiddensubsidy.app.data.model.DocumentRequirement
import com.hiddensubsidy.app.data.model.EventBundle
import com.hiddensubsidy.app.data.model.HomeData
import com.hiddensubsidy.app.data.model.LifeEvent
import com.hiddensubsidy.app.data.model.MissedGrant
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.data.model.PolicyCalendarEvent
import com.hiddensubsidy.app.data.model.TimelineGroup

object SampleData {

    // =====================================================
    // 정책 풀 — 상세 정보 풍부한 정책들
    // =====================================================
    private val youthRent = Policy(
        id = "youth-rent",
        title = "청년 월세 지원",
        amount = 600_000L,
        deadline = "2026-05-27",
        daysLeft = 12,
        category = "주거",
        summary = "만 19~34세 청년 무주택자에게 월 최대 20만원, 12개월간 지원해드려요.",
        period = "최대 12개월 · 월 20만원",
        eligibility = listOf(
            "만 19~34세 청년",
            "무주택자",
            "본인 소득 중위 100% 이하",
            "가구 중위소득 60% 이하",
        ),
        documents = listOf(
            DocumentRequirement("주민등록등본", "https://www.gov.kr"),
            DocumentRequirement("임대차계약서"),
            DocumentRequirement("소득금액증명원", "https://www.hometax.go.kr"),
            DocumentRequirement("무주택 확인서", "https://www.gov.kr"),
        ),
        procedure = listOf(
            "복지로 회원가입 / 로그인",
            "복지서비스 → 주거지원 → 청년월세 메뉴 선택",
            "신청서 작성 + 서류 업로드",
            "지자체 심사 (평균 30일)",
            "선정 시 본인 계좌로 매월 입금",
        ),
        applicationOrg = "복지로",
        applicationUrl = "https://www.bokjiro.go.kr",
        isEligible = true,
    )

    private val birth = Policy(
        id = "birth",
        title = "출산장려금",
        amount = 2_000_000L,
        deadline = "2026-05-18",
        daysLeft = 3,
        category = "출산",
        summary = "첫 자녀 출산 시 일시금 200만원을 지자체에서 지급해요.",
        period = "출생일 기준 1년 이내 신청",
        eligibility = listOf(
            "출산일 기준 해당 지자체 6개월 이상 거주",
            "출생신고 완료",
            "부모 중 1인 주민등록 유지",
        ),
        documents = listOf(
            DocumentRequirement("출생증명서"),
            DocumentRequirement("주민등록등본", "https://www.gov.kr"),
            DocumentRequirement("통장 사본"),
        ),
        procedure = listOf(
            "주민센터 방문 또는 정부24 온라인 신청",
            "구비서류 제출",
            "지자체 확인 후 일시금 지급 (평균 14일)",
        ),
        applicationOrg = "정부24",
        applicationUrl = "https://www.gov.kr",
        isEligible = true,
    )

    private val telecom = Policy(
        id = "telecom",
        title = "통신비 감면",
        amount = 180_000L,
        deadline = "2026-05-22",
        daysLeft = 7,
        category = "생활",
        summary = "기초생활수급자·차상위계층 대상 이동통신 요금 월 최대 1.5만원 감면.",
        period = "월 최대 15,000원 · 상시",
        eligibility = listOf(
            "기초생활수급자 또는 차상위계층",
            "본인 명의 이동통신 가입",
        ),
        documents = listOf(
            DocumentRequirement("수급자 증명서", "https://www.gov.kr"),
        ),
        procedure = listOf(
            "통신사 고객센터 또는 대리점 방문",
            "수급자 증명서 제출",
            "다음 달 청구서부터 자동 감면",
        ),
        applicationOrg = "통신3사",
        applicationUrl = "https://www.welfare.go.kr",
        isEligible = false,
    )

    private val scholarship = Policy(
        id = "scholarship",
        title = "주거안정 장학금",
        amount = 800_000L,
        deadline = "2026-05-27",
        daysLeft = 12,
        category = "교육",
        summary = "지방 출신 대학생의 수도권 거주 부담을 줄이기 위한 장학금이에요.",
        period = "학기당 80만원",
        eligibility = listOf(
            "재학 중인 대학생",
            "원거주지가 수도권 외 지역",
            "성적 평점 3.0 이상",
        ),
        documents = listOf(
            DocumentRequirement("재학증명서"),
            DocumentRequirement("성적증명서"),
            DocumentRequirement("주민등록초본", "https://www.gov.kr"),
        ),
        procedure = listOf(
            "한국장학재단 홈페이지 접속",
            "주거안정 장학금 신청",
            "서류 업로드",
            "심사 후 학기 시작 전 지급",
        ),
        applicationOrg = "한국장학재단",
        applicationUrl = "https://www.kosaf.go.kr",
        isEligible = true,
    )

    // =====================================================
    // 이벤트 묶음용 정책들 — 간단 요약 위주 (디테일 일부 비움)
    // =====================================================
    private fun lite(
        id: String,
        title: String,
        amount: Long,
        category: String,
        summary: String,
        period: String? = null,
        org: String = "복지로",
        url: String = "https://www.bokjiro.go.kr",
    ) = Policy(
        id = id,
        title = title,
        amount = amount,
        deadline = "상시",
        daysLeft = 90,
        category = category,
        summary = summary,
        period = period,
        applicationOrg = org,
        applicationUrl = url,
        isEligible = true,
    )

    // ── 이사
    private val youthRentLoan = lite(
        id = "youth-rent-loan",
        title = "청년 전월세 보증금 대출",
        amount = 200_000_000L,
        category = "주거",
        summary = "만 34세 이하 청년에게 최대 2억원, 연 1.5%대 저리 대출.",
        period = "최대 2억원 · 연 1.5%",
        org = "주택도시기금",
        url = "https://nhuf.molit.go.kr",
    )
    private val newlywedLoan = lite(
        id = "newlywed-loan",
        title = "신혼부부 전세자금 대출",
        amount = 300_000_000L,
        category = "주거",
        summary = "결혼 7년 이내 부부에게 최대 3억원, 연 1.2%대 전세대출.",
        period = "최대 3억원 · 연 1.2%",
        org = "주택도시기금",
        url = "https://nhuf.molit.go.kr",
    )
    private val mapoMoveIn = lite(
        id = "mapo-movein",
        title = "마포구 청년 정착지원금",
        amount = 300_000L,
        category = "주거",
        summary = "마포구 전입 청년에게 일시금 30만원 지급 (1년 이상 거주 조건).",
        period = "1회 30만원",
        org = "마포구청",
        url = "https://www.mapo.go.kr",
    )

    // ── 퇴사
    private val unemployment = lite(
        id = "unemployment-benefit",
        title = "실업급여 (구직급여)",
        amount = 8_640_000L,
        category = "생활",
        summary = "비자발적 퇴직자에게 평균임금 60%를 최대 270일간 지급.",
        period = "최대 9개월",
        org = "고용24",
        url = "https://www.work24.go.kr",
    )
    private val jobseekerSupport = lite(
        id = "jobseeker-support",
        title = "국민취업지원제도",
        amount = 3_000_000L,
        category = "생활",
        summary = "구직자에게 6개월 동안 월 50만원 + 취업활동비 지원.",
        period = "월 50만원 · 6개월",
        org = "고용24",
        url = "https://www.work24.go.kr",
    )
    private val trainingCard = lite(
        id = "training-card",
        title = "국민내일배움카드",
        amount = 5_000_000L,
        category = "교육",
        summary = "5년간 최대 500만원, 직업훈련 수강료 지원.",
        period = "5년 최대 500만원",
        org = "HRD-Net",
        url = "https://www.hrd.go.kr",
    )

    // ── 임신·출산
    private val pregnancyVoucher = lite(
        id = "pregnancy-voucher",
        title = "임신·출산 진료비",
        amount = 1_000_000L,
        category = "출산",
        summary = "임산부에게 100만원 (다태아 140만원) 국민행복카드 지원.",
        period = "100만원 · 카드 형태",
        org = "정부24",
        url = "https://www.gov.kr",
    )
    private val firstMeet = lite(
        id = "first-meet",
        title = "첫만남 이용권",
        amount = 2_000_000L,
        category = "출산",
        summary = "출생아 1인당 200만원 바우처 지급.",
        period = "1회 200만원",
        org = "정부24",
        url = "https://www.gov.kr",
    )
    private val parentSubsidy = lite(
        id = "parent-subsidy",
        title = "부모급여",
        amount = 8_400_000L,
        category = "출산",
        summary = "만 0세 월 100만원, 만 1세 월 50만원 현금 지급.",
        period = "월 100만원 · 0~1세",
        org = "복지로",
        url = "https://www.bokjiro.go.kr",
    )

    // ── 결혼
    private val newlywedHope = lite(
        id = "newlywed-hope",
        title = "신혼희망타운",
        amount = 0L,
        category = "주거",
        summary = "신혼·예비신혼부부 대상 분양·임대 우선공급.",
        period = "분양가 시세 70%",
        org = "LH 청약센터",
        url = "https://apply.lh.or.kr",
    )
    private val didimdolLoan = lite(
        id = "didimdol-loan",
        title = "신혼부부 디딤돌 대출",
        amount = 400_000_000L,
        category = "주거",
        summary = "결혼 7년 이내 부부에게 최대 4억원 주택구입자금 대출.",
        period = "최대 4억원 · 연 1.9%",
        org = "주택도시기금",
        url = "https://nhuf.molit.go.kr",
    )

    // ── 창업
    private val startupSchool = lite(
        id = "startup-school",
        title = "청년창업사관학교",
        amount = 100_000_000L,
        category = "창업",
        summary = "만 39세 이하 청년창업가에게 최대 1억원 + 1년 보육.",
        period = "1억원 + 1년 보육",
        org = "K-Startup",
        url = "https://www.k-startup.go.kr",
    )
    private val youthStartupFund = lite(
        id = "youth-startup-fund",
        title = "청년창업지원금",
        amount = 10_000_000L,
        category = "창업",
        summary = "예비창업자·초기 창업자에게 사업화 자금 1000만원.",
        period = "1회 1000만원",
        org = "K-Startup",
        url = "https://www.k-startup.go.kr",
    )

    // ── 취업
    private val nextMutualAid = lite(
        id = "next-mutual-aid",
        title = "청년내일채움공제",
        amount = 12_000_000L,
        category = "청년",
        summary = "중소기업 취업 청년이 2년 적립 시 1200만원 + 기업·정부 매칭.",
        period = "2년 1200만원",
        org = "고용24",
        url = "https://www.work24.go.kr",
    )
    private val ydaAccount = lite(
        id = "yda-account",
        title = "청년 도약 계좌",
        amount = 50_000_000L,
        category = "청년",
        summary = "5년간 매월 70만원 납입 시 정부 매칭 + 비과세.",
        period = "5년 최대 5천만원",
        org = "은행 (취급기관)",
        url = "https://www.fss.or.kr",
    )

    // =====================================================
    // 이벤트 묶음
    // =====================================================
    private val moveBundle = EventBundle(
        eventId = LifeEvent.Move.id,
        tagline = "정부 + 지자체",
        maxAmountLabel = "최대 5억원 + 90만원",
        groups = listOf(
            TimelineGroup("이사 전 · 3개월 안", "📍", listOf(youthRentLoan, newlywedLoan)),
            TimelineGroup("이사 직후 · 1개월 안", "🏠", listOf(youthRent, mapoMoveIn)),
        ),
    )

    private val resignBundle = EventBundle(
        eventId = LifeEvent.Resign.id,
        tagline = "고용보험",
        maxAmountLabel = "최대 1,664만원",
        groups = listOf(
            TimelineGroup("퇴사 직후 · 3개월 안", "📤", listOf(unemployment, jobseekerSupport)),
            TimelineGroup("재취업 준비 · 6개월 안", "📚", listOf(trainingCard)),
        ),
    )

    private val pregnancyBundle = EventBundle(
        eventId = LifeEvent.Pregnancy.id,
        tagline = "정부 + 지자체",
        maxAmountLabel = "최대 1,340만원",
        groups = listOf(
            TimelineGroup("임신 중", "🤰", listOf(pregnancyVoucher)),
            TimelineGroup("출산 직후 · 1년 안", "👶", listOf(firstMeet, birth)),
            TimelineGroup("육아 · 1~2년", "🍼", listOf(parentSubsidy)),
        ),
    )

    private val marriageBundle = EventBundle(
        eventId = LifeEvent.Marriage.id,
        tagline = "주택 + 청약",
        maxAmountLabel = "최대 7억원 (대출)",
        groups = listOf(
            TimelineGroup("결혼 전 · 6개월 안", "💌", listOf(newlywedLoan)),
            TimelineGroup("결혼 직후 · 1년 안", "🏡", listOf(newlywedHope, didimdolLoan)),
        ),
    )

    private val startupBundle = EventBundle(
        eventId = LifeEvent.Startup.id,
        tagline = "예비 + 초기 창업",
        maxAmountLabel = "최대 1.1억원",
        groups = listOf(
            TimelineGroup("창업 전 · 준비기", "🧪", listOf(youthStartupFund)),
            TimelineGroup("창업 1년차", "🚀", listOf(startupSchool)),
        ),
    )

    private val employmentBundle = EventBundle(
        eventId = LifeEvent.Employment.id,
        tagline = "청년 자산형성",
        maxAmountLabel = "최대 6,200만원",
        groups = listOf(
            TimelineGroup("취업 직후 · 1년 안", "💼", listOf(nextMutualAid, ydaAccount)),
        ),
    )

    val events: List<EventBundle> = listOf(
        moveBundle, resignBundle, pregnancyBundle,
        marriageBundle, startupBundle, employmentBundle,
    )

    fun findEvent(id: String): EventBundle? = events.firstOrNull { it.eventId == id }

    val mySummary = com.hiddensubsidy.app.data.model.MySummary(
        savedCount = 3,
        savedAmount = 1_500_000L,
        appliedCount = 1,
        appliedAmount = 600_000L,
        receivedCount = 5,
        receivedAmount = 2_300_000L,
    )

    // =====================================================
    // 전체 정책 풀 (정책 상세 lookup 용)
    // =====================================================
    private val allPolicies: List<Policy> = listOf(
        youthRent, birth, telecom, scholarship,
        youthRentLoan, newlywedLoan, mapoMoveIn,
        unemployment, jobseekerSupport, trainingCard,
        pregnancyVoucher, firstMeet, parentSubsidy,
        newlywedHope, didimdolLoan,
        startupSchool, youthStartupFund,
        nextMutualAid, ydaAccount,
    )

    fun findPolicy(id: String): Policy? = allPolicies.firstOrNull { it.id == id }

    // =====================================================
    // 캘린더 일정 (자격 충족 정책만)
    // =====================================================
    private fun cal(
        date: String,
        kind: CalendarEventKind,
        policy: Policy,
    ) = PolicyCalendarEvent(date, kind.name, policy.id, policy.title)

    val calendarEvents: List<PolicyCalendarEvent> = listOf(
        // 청년 월세 지원 — 신청 마감 D-12 (today=2026-05-15)
        cal("2026-05-01", CalendarEventKind.ApplicationOpen, youthRent),
        cal("2026-05-27", CalendarEventKind.Deadline, youthRent),
        cal("2026-06-20", CalendarEventKind.Announcement, youthRent),
        cal("2026-07-05", CalendarEventKind.Disbursement, youthRent),

        // 출산장려금 — 신청 마감 임박 D-3
        cal("2026-05-18", CalendarEventKind.Deadline, birth),
        cal("2026-06-01", CalendarEventKind.Disbursement, birth),

        // 주거안정 장학금
        cal("2026-05-10", CalendarEventKind.ApplicationOpen, scholarship),
        cal("2026-05-27", CalendarEventKind.Deadline, scholarship),
        cal("2026-06-15", CalendarEventKind.Announcement, scholarship),

        // 이벤트 정책들 — 상시지만 캘린더에 일부 표시 (자격 충족 가정)
        cal("2026-05-22", CalendarEventKind.ApplicationOpen, youthStartupFund),
        cal("2026-06-10", CalendarEventKind.Deadline, youthStartupFund),

        cal("2026-05-30", CalendarEventKind.ApplicationOpen, nextMutualAid),
        cal("2026-06-25", CalendarEventKind.Deadline, nextMutualAid),

        cal("2026-05-15", CalendarEventKind.ApplicationOpen, ydaAccount),
        cal("2026-06-30", CalendarEventKind.Deadline, ydaAccount),
    )

    // =====================================================
    // 홈 데이터
    // =====================================================
    val home = HomeData(
        missedTotalAmount = 2_400_000L,
        missedCount = 12,
        missedGrants = listOf(
            MissedGrant(
                id = "yda-2024",
                title = "청년 도약 계좌",
                amount = 1_200_000L,
                eligibleFrom = "2024-07",
                year = 2024,
                summary = "5년간 매월 70만원 납입 시 정부 매칭 지원",
            ),
            MissedGrant(
                id = "youth-rent-2024",
                title = "청년 월세 지원",
                amount = 600_000L,
                eligibleFrom = "2024-03",
                year = 2024,
                summary = "월 최대 20만원 12개월 지원",
            ),
            MissedGrant(
                id = "comm-discount-2023",
                title = "통신비 감면",
                amount = 180_000L,
                eligibleFrom = "2023-09",
                year = 2023,
                summary = "기초생활수급자·차상위계층 월 1.5만원 감면",
            ),
        ),
        thisWeekPolicies = listOf(youthRent),
        deadlineSoon = listOf(birth, telecom, scholarship),
    )
}
