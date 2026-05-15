package com.hiddensubsidy.app.data

import android.content.Context

private const val PREFS = "hs_prefs"
private const val KEY = "favorites"

object FavoritesRepository {

    fun load(context: Context): Set<String> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getStringSet(KEY, emptySet()) ?: emptySet()
    }

    fun save(context: Context, ids: Set<String>) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // SharedPreferences는 StringSet 참조를 캐시할 수 있어 새 Set으로 복사해서 저장
        p.edit().putStringSet(KEY, ids.toSet()).apply()
    }

    fun toggle(context: Context, id: String): Set<String> {
        val current = load(context).toMutableSet()
        if (id in current) current.remove(id) else current.add(id)
        save(context, current)
        return current
    }
}
