package com.hiddensubsidy.app.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * 알림 스케줄링.
 * - 매일 1회 PeriodicWork (KST 09시쯤 — flexInterval로 자유도 줘서 OS가 적절히)
 * - 즐겨찾기 toggle 시 OneTime 즉시 1회 (당일 D-0/D-1/D-3 즉시 알림)
 */
object NotificationScheduler {

    private const val PERIODIC_WORK_NAME = "deadline_periodic"
    private const val IMMEDIATE_WORK_NAME = "deadline_immediate"

    fun schedulePeriodic(context: Context) {
        NotificationHelper.ensureChannel(context)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        // 24시간마다 1회. flex 4시간(앞쪽) — OS가 배터리/네트워크 보고 적절히 발화.
        val request = PeriodicWorkRequestBuilder<PolicyDeadlineWorker>(
            24, TimeUnit.HOURS,
            4, TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,  // 기존 거 유지
            request,
        )
    }

    /** 즐겨찾기 toggle 또는 앱 진입 시 즉시 1회 검사 (당일 알림 빠진 거 채움). */
    fun runOnce(context: Context) {
        NotificationHelper.ensureChannel(context)
        val request = OneTimeWorkRequestBuilder<PolicyDeadlineWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
