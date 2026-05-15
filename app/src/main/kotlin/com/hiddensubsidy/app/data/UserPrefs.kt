package com.hiddensubsidy.app.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.hiddensubsidy.app.data.model.UserProfile

private const val PREFS = "hs_prefs"

object UserPrefs {
    fun load(context: Context): UserProfile {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return UserProfile(
            age = if (p.contains("age")) p.getInt("age", 0) else null,
            region = p.getString("region", null),
            occupation = p.getString("occupation", null),
            married = if (p.contains("married")) p.getBoolean("married", false) else null,
            hasChildren = if (p.contains("has_children")) p.getBoolean("has_children", false) else null,
            incomeMonthly = if (p.contains("income_monthly")) p.getLong("income_monthly", 0L) else null,
        )
    }
}

@Composable
fun rememberUserProfile(): UserProfile {
    val context = LocalContext.current
    return remember { UserPrefs.load(context) }
}
