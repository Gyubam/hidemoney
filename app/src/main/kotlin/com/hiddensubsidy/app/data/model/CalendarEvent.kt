package com.hiddensubsidy.app.data.model

import kotlinx.serialization.Serializable

enum class CalendarEventKind(val label: String) {
    ApplicationOpen("신청 시작"),
    Deadline("신청 마감"),
    Announcement("발표"),
    Disbursement("지급"),
}

@Serializable
data class PolicyCalendarEvent(
    val date: String,           // yyyy-MM-dd
    val kindName: String,       // CalendarEventKind.name
    val policyId: String,
    val policyTitle: String,
) {
    val kind: CalendarEventKind get() = CalendarEventKind.valueOf(kindName)
}
