package com.hiddensubsidy.app.data

import android.content.Context

/**
 * 생애 이벤트 트리거 — 사용자가 "내가 이사했어요" 같은 시점을 마크하면 저장.
 *
 * 저장: SharedPreferences `event_trigger_<eventId>` = epoch millis
 * 자동 만료: 6개월 (180일) 지나면 무시 (만료 시각도 비교용으로 epoch 그대로 둠 — 새로 토글 시 갱신)
 *
 * 활용: 트리거 활성 시 해당 이벤트 정책을 홈/missed에 가중 표시.
 * MVP는 시점 저장 + 상태 표시만. 알림 강화는 추후 (R7-A 알림과 연결).
 */
object EventTriggerRepository {

    private const val PREFS = "hs_prefs"
    private const val KEY_PREFIX = "event_trigger_"
    private const val EXPIRY_MILLIS = 180L * 24 * 60 * 60 * 1000  // 180일

    /** 활성 트리거 이벤트 id 집합. 만료된 건 제외. */
    fun loadActive(context: Context, now: Long = System.currentTimeMillis()): Set<String> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.all.entries
            .mapNotNull { (k, v) ->
                if (!k.startsWith(KEY_PREFIX)) return@mapNotNull null
                val ts = (v as? Long) ?: return@mapNotNull null
                if (now - ts > EXPIRY_MILLIS) return@mapNotNull null
                k.removePrefix(KEY_PREFIX)
            }
            .toSet()
    }

    fun isActive(context: Context, eventId: String, now: Long = System.currentTimeMillis()): Boolean {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ts = p.getLong(KEY_PREFIX + eventId, 0L)
        if (ts == 0L) return false
        return now - ts <= EXPIRY_MILLIS
    }

    /** 토글: 비활성이면 현재 시각으로 마크, 활성이면 해제. */
    fun toggle(context: Context, eventId: String, now: Long = System.currentTimeMillis()): Boolean {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = KEY_PREFIX + eventId
        val current = p.getLong(key, 0L)
        val active = current != 0L && now - current <= EXPIRY_MILLIS
        if (active) {
            p.edit().remove(key).apply()
            return false
        } else {
            p.edit().putLong(key, now).apply()
            return true
        }
    }
}
