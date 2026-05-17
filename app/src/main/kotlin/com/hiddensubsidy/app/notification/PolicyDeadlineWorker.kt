package com.hiddensubsidy.app.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hiddensubsidy.app.data.FavoritesRepository
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
        val favorites = FavoritesRepository.load(context)
        if (favorites.isEmpty()) {
            Log.i(TAG, "no favorites — skip")
            return Result.success()
        }

        val policies = loadPolicies(context)
        if (policies.isEmpty()) {
            Log.w(TAG, "no policies loaded — skip")
            return Result.success()
        }

        val today = LocalDate.now()
        val byId = policies.associateBy { it.id }
        var notified = 0
        for (fid in favorites) {
            val policy = byId[fid] ?: continue
            if (policy.deadline.isBlank()) continue
            val daysLeft = daysBetween(today, policy.deadline) ?: continue
            // D-3, D-1, D-0 알림 (그 외는 skip)
            if (daysLeft in setOf(0, 1, 3)) {
                NotificationHelper.notifyDeadline(context, policy, daysLeft)
                notified++
            }
        }
        Log.i(TAG, "favorites=${favorites.size}, notified=$notified")
        return Result.success()
    }

    private suspend fun loadPolicies(context: Context): List<Policy> {
        // 1) 디스크 캐시 우선 (CachedPolicyRepository와 동일 경로)
        val cacheFile = java.io.File(context.filesDir, "policies-cache.json")
        if (cacheFile.exists()) {
            try {
                val text = cacheFile.readText(Charsets.UTF_8)
                val list = jsonFmt.decodeFromString<List<Policy>>(text)
                if (list.isNotEmpty()) return list
            } catch (e: Exception) {
                Log.w(TAG, "cache parse failed: ${e.message}")
            }
        }
        // 2) remote fallback
        val client = HttpClient(OkHttp) {
            install(ContentNegotiation) { json(jsonFmt) }
        }
        return try {
            val text = client.get(REMOTE_URL).bodyAsText()
            jsonFmt.decodeFromString<List<Policy>>(text)
        } catch (e: Exception) {
            Log.w(TAG, "remote fetch failed: ${e.message}")
            emptyList()
        } finally {
            client.close()
        }
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
    }
}
