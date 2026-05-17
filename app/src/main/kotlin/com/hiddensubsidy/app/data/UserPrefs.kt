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
            gender = p.getString("gender", null),
            occupation = p.getString("occupation", null),
            married = if (p.contains("married")) p.getBoolean("married", false) else null,
            hasChildren = if (p.contains("has_children")) p.getBoolean("has_children", false) else null,
            incomeMonthly = if (p.contains("income_monthly")) p.getLong("income_monthly", 0L) else null,
            householdSize = if (p.contains("household_size")) p.getInt("household_size", 0) else null,
            education = p.getString("education", null),
            housingType = p.getString("housing_type", null),
        )
    }

    fun save(context: Context, profile: UserProfile) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit().apply {
            if (profile.age != null) putInt("age", profile.age) else remove("age")
            if (profile.region != null) putString("region", profile.region) else remove("region")
            if (profile.gender != null) putString("gender", profile.gender) else remove("gender")
            if (profile.occupation != null) putString("occupation", profile.occupation) else remove("occupation")
            if (profile.married != null) putBoolean("married", profile.married) else remove("married")
            if (profile.hasChildren != null) putBoolean("has_children", profile.hasChildren) else remove("has_children")
            if (profile.incomeMonthly != null) putLong("income_monthly", profile.incomeMonthly) else remove("income_monthly")
            if (profile.householdSize != null) putInt("household_size", profile.householdSize) else remove("household_size")
            if (profile.education != null) putString("education", profile.education) else remove("education")
            if (profile.housingType != null) putString("housing_type", profile.housingType) else remove("housing_type")
        }.apply()
    }
}

@Composable
fun rememberUserProfile(): UserProfile {
    val context = LocalContext.current
    return remember { UserPrefs.load(context) }
}
