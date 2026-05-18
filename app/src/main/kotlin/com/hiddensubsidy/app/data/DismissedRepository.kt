package com.hiddensubsidy.app.data

import android.content.Context

/**
 * "관심 없음" 정책 저장소.
 *
 * 사용자가 자기 상황과 맞지 않는 정책(예: 6.5억 missed 안에 들어간 6억짜리 무관한 정책)을
 * 직접 dismiss하면, 홈 임팩트 카드 + missed sheet에서 제외됨.
 *
 * 다른 상태(favorites/applied/received)와는 무관 — 별도 set으로 관리.
 * 검색 화면에서는 그대로 표시 (사용자가 검색해서 찾을 때까지 진짜 숨김 X).
 */
object DismissedRepository {

    private const val PREFS = "hs_prefs"
    private const val KEY = "dismissed"

    fun load(context: Context): Set<String> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getStringSet(KEY, emptySet()) ?: emptySet()
    }

    fun save(context: Context, ids: Set<String>) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().putStringSet(KEY, ids.toSet()).apply()
    }

    fun toggle(context: Context, id: String): Set<String> {
        val current = load(context).toMutableSet()
        if (id in current) current.remove(id) else current.add(id)
        save(context, current)
        return current
    }
}
