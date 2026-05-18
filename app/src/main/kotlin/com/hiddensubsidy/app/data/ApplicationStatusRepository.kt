package com.hiddensubsidy.app.data

import android.content.Context

/**
 * 정책 진척 상태 저장소.
 *
 * 세 가지 상태가 SharedPreferences에 독립 Set으로 저장됨:
 *  - **SAVED** = 받을 예정 (즐겨찾기 ⭐) — [FavoritesRepository] 가 관리
 *  - **APPLIED** = 신청한 지원금 — 본 저장소 `applied` Set
 *  - **RECEIVED** = 받은 지원금 — 본 저장소 `received` Set
 *
 * 상태는 느슨 — 사용자가 ⭐ 안 누르고 바로 "신청했어요" OK. 단계 강제 X.
 * "받음" 마크 시 자동으로 "신청" 해제 (받았으면 더 이상 신청 진행 중 아님).
 */
object ApplicationStatusRepository {

    private const val PREFS = "hs_prefs"
    private const val KEY_APPLIED = "applied"
    private const val KEY_RECEIVED = "received"

    private fun load(context: Context, key: String): Set<String> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getStringSet(key, emptySet()) ?: emptySet()
    }

    private fun save(context: Context, key: String, ids: Set<String>) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putStringSet(key, ids.toSet()).apply()
    }

    fun loadApplied(context: Context): Set<String> = load(context, KEY_APPLIED)
    fun loadReceived(context: Context): Set<String> = load(context, KEY_RECEIVED)

    /** 신청 상태 토글. 신청 마크하면 받음에선 빠짐 (다시 진행 중 의미). */
    fun toggleApplied(context: Context, id: String): Pair<Set<String>, Set<String>> {
        val applied = loadApplied(context).toMutableSet()
        val received = loadReceived(context).toMutableSet()
        if (id in applied) {
            applied.remove(id)
        } else {
            applied.add(id)
            received.remove(id)
        }
        save(context, KEY_APPLIED, applied)
        save(context, KEY_RECEIVED, received)
        return applied to received
    }

    /** 받음 상태 토글. 받음 마크하면 신청에선 빠짐 (받았으니 진행 중 아님). */
    fun toggleReceived(context: Context, id: String): Pair<Set<String>, Set<String>> {
        val applied = loadApplied(context).toMutableSet()
        val received = loadReceived(context).toMutableSet()
        if (id in received) {
            received.remove(id)
        } else {
            received.add(id)
            applied.remove(id)
        }
        save(context, KEY_APPLIED, applied)
        save(context, KEY_RECEIVED, received)
        return applied to received
    }
}
