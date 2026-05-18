package com.hiddensubsidy.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hiddensubsidy.app.MainActivity
import com.hiddensubsidy.app.R
import com.hiddensubsidy.app.data.model.Policy

/**
 * 알림 발송 helper.
 * - "신청 마감 임박" 채널 (default importance)
 * - 클릭 시 MainActivity로 이동. extra로 policy id 전달 (향후 deep-link로 정책 detail 직진)
 */
object NotificationHelper {

    private const val CHANNEL_ID = "deadline_alerts"
    private const val CHANNEL_NAME = "신청 마감 알림"
    private const val CHANNEL_DESC = "받을 예정 정책의 마감 임박 알림"

    const val EXTRA_POLICY_ID = "policy_id"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 정책 마감 임박 알림 발송. policyId를 notificationId로 stable 사용 (중복 방지).
     */
    fun notifyDeadline(context: Context, policy: Policy, daysLeft: Int) {
        ensureChannel(context)
        val title = if (daysLeft <= 0) "오늘 마감!" else "D-${daysLeft}, 곧 마감돼요"
        val body = "${policy.title} — 신청 안 하면 못 받아요"

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_POLICY_ID, policy.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            policy.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context)
                .notify(notificationId(policy.id, daysLeft), builder.build())
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS 권한 없음 — 무시
        }
    }

    /** 정책별 + daysLeft 별로 unique한 id. 같은 D-day 알림 중복 발송 방지. */
    private fun notificationId(policyId: String, daysLeft: Int): Int {
        return (policyId.hashCode() * 31 + daysLeft).and(Int.MAX_VALUE)
    }

    /**
     * 자격 충족 신규 정책 알림.
     * @param count 신규 정책 개수
     * @param topPolicy 그 중 amount 가장 큰 정책 (대표로 본문에 표시)
     */
    fun notifyNewEligible(context: Context, count: Int, topPolicy: Policy?) {
        ensureChannel(context)
        val title = if (count == 1) {
            "받을 수 있는 정책이 새로 등록됐어요"
        } else {
            "받을 수 있는 정책 ${count}개가 새로 등록됐어요"
        }
        val body = topPolicy?.let { "${it.title} 등 — 자세히 확인해보세요" }
            ?: "새로 추가된 정부 지원금을 확인해보세요"

        val intent = Intent(context, MainActivity::class.java).apply {
            // 정책 id 없이 열면 홈 탭으로 진입 (기본 행동)
            putExtra(EXTRA_OPEN_HOME, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NEW_ELIGIBLE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        try {
            NotificationManagerCompat.from(context).notify(NEW_ELIGIBLE_NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // 권한 없음 무시
        }
    }

    const val EXTRA_OPEN_HOME = "open_home"
    private const val NEW_ELIGIBLE_NOTIFICATION_ID = 9000_001
    private const val NEW_ELIGIBLE_REQUEST_CODE = 9001
}
