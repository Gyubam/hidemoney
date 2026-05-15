package com.hiddensubsidy.app.data.model

import kotlinx.serialization.Serializable

enum class LifeEvent(
    val id: String,
    val label: String,
    val emoji: String,
) {
    Move("move", "이사", "🏠"),
    Resign("resign", "퇴사", "💼"),
    Pregnancy("pregnancy", "임신·출산", "👶"),
    Marriage("marriage", "결혼", "💍"),
    Startup("startup", "창업", "🚀"),
    Employment("employment", "취업", "🎓");

    companion object {
        fun fromId(id: String): LifeEvent? = entries.firstOrNull { it.id == id }
    }
}

@Serializable
data class TimelineGroup(
    val label: String,
    val emoji: String,
    val policies: List<Policy>,
)

@Serializable
data class EventBundle(
    val eventId: String,                  // LifeEvent.id
    val tagline: String,                  // "정부 + 지자체"
    val maxAmountLabel: String,           // "최대 4,800,000원" — 직접 노출용 (대출은 별도 표기 가능)
    val groups: List<TimelineGroup>,
) {
    val count: Int get() = groups.sumOf { it.policies.size }
    val event: LifeEvent? get() = LifeEvent.fromId(eventId)
}
