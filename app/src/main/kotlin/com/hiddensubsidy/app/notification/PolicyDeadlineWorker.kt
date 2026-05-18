package com.hiddensubsidy.app.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hiddensubsidy.app.data.FavoritesRepository
import com.hiddensubsidy.app.data.PolicyRelevance
import com.hiddensubsidy.app.data.UserPrefs
import com.hiddensubsidy.app.data.matchedWith
import com.hiddensubsidy.app.data.model.Policy
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 매일 한 번 즐겨찾기 정책 마감 검사 + D-day 알림.
 *
 * 알림 트리거: daysLeft가 3, 1, 0 (오늘) 인 정책.
 * 정책 데이터는 캐시 또는 remote에서 로드. 캐시 우선 (오프라인 OK).
 */
class PolicyDeadlineWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val policies = loadPolicies(context)
        if (policies.isEmpty()) {
            Log.w(TAG, "no policies loaded — skip")
            return Result.success()
        }

        // ── 1) 즐겨찾기 정책 마감 알림 (D-3/D-1/D-0)
        val favorites = FavoritesRepository.load(context)
        val today = LocalDate.now()
        val byId = policies.associateBy { it.id }
        var deadlineNotified = 0
        for (fid in favorites) {
            val policy = byId[fid] ?: continue
            if (policy.deadline.isBlank()) continue
            val daysLeft = daysBetween(today, policy.deadline) ?: continue
            if (daysLeft in setOf(0, 1, 3)) {
                NotificationHelper.notifyDeadline(context, policy, daysLeft)
                deadlineNotified++
            }
        }

        // ── 2) 자격 충족 신규 정책 알림 (이전 known set과 diff)
        val profile = UserPrefs.load(context)
        val eligibleIds = policies
            .asSequence()
            .map { it.matchedWith(profile) }
            .filter { it.isEligible }
            .filter { PolicyRelevance.isEligibleForUser(it, profile) }
            .filter { it.amount > 0 }
            .map { it.id }
            .toSet()

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val baselineEstablished = prefs.getBoolean(KEY_BASELINE_ESTABLISHED, false)
        val knownIds: Set<String> = prefs.getStringSet(KEY_KNOWN_IDS, emptySet()) ?: emptySet()

        val newIds = eligibleIds - knownIds
        var newNotified = 0
        if (!baselineEstablished) {
            // 첫 실행 — baseline만 잡고 알림 X (앱 첫 진입 시 폭격 방지)
            prefs.edit()
                .putStringSet(KEY_KNOWN_IDS, eligibleIds.toSet())
                .putBoolean(KEY_BASELINE_ESTABLISHED, true)
                .apply()
            Log.i(TAG, "baseline established: ${eligibleIds.size} ids")
        } else if (newIds.isNotEmpty()) {
            val topPolicy = newIds
                .mapNotNull { byId[it] }
                .maxByOrNull { it.amount }
            NotificationHelper.notifyNewEligible(context, newIds.size, topPolicy)
            newNotified = newIds.size
            prefs.edit().putStringSet(KEY_KNOWN_IDS, eligibleIds.toSet()).apply()
        } else {
            // 변화 없음 — known 갱신만 (사용자 프로필 변경 등으로 set 변할 수 있음)
            prefs.edit().putStringSet(KEY_KNOWN_IDS, eligibleIds.toSet()).apply()
        }

        Log.i(TAG, "favorites=${favorites.size}, deadlineNotified=$deadlineNotified, eligible=${eligibleIds.size}, newNotified=$newNotified")
        return Result.success()
    }

    private suspend fun loadPolicies(context: Context): List<Policy> {
        // 신규 정책 알림을 의미있게 잡으려면 최신 데이터 필요 → remote 우선.
        // remote 성공 시 캐시도 갱신 (앱 다음 진입 시 최신 보장).
        val cacheFile = java.io.File(context.filesDir, "policies-cache.json")
        val client = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(jsonFmt) }
        }
        try {
            val text = client.get(REMOTE_URL).bodyAsText()
            val list = jsonFmt.decodeFromString<List<Policy>>(text)
            if (list.isNotEmpty()) {
                // remote 성공 — 캐시 갱신
                runCatching { cacheFile.writeText(text, Charsets.UTF_8) }
                Log.i(TAG, "remote OK: ${list.size} policies")
                return list
            }
        } catch (e: Exception) {
            Log.w(TAG, "remote fetch failed: ${e.message} — fallback to cache")
        } finally {
            client.close()
        }
        // remote 실패 시 캐시 fallback
        if (cacheFile.exists()) {
            return try {
                jsonFmt.decodeFromString<List<Policy>>(cacheFile.readText(Charsets.UTF_8))
            } catch (e: Exception) {
                Log.w(TAG, "cache parse failed: ${e.message}")
                emptyList()
            }
        }
        return emptyList()
    }

    private fun daysBetween(today: LocalDate, isoDeadline: String): Int? = try {
        val d = LocalDate.parse(isoDeadline, DateTimeFormatter.ISO_LOCAL_DATE)
        ChronoUnit.DAYS.between(today, d).toInt()
    } catch (_: Exception) {
        null
    }

    companion object {
        private const val TAG = "deadline-worker"
        private const val REMOTE_URL = "https://gyubam.github.io/hidemoney/policies.json"
        private val jsonFmt = Json { ignoreUnknownKeys = true; explicitNulls = false }
        private const val PREFS = "hs_prefs"
        private const val KEY_KNOWN_IDS = "known_eligible_ids"
        private const val KEY_BASELINE_ESTABLISHED = "known_eligible_baseline"
    }
}
