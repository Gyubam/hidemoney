package com.hiddensubsidy.app.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.hiddensubsidy.app.ui.components.formatAmount

object ShareHelper {

    private const val FEEDBACK_EMAIL = "sgb8154@gmail.com"
    private const val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.hiddensubsidy.app"

    /** 놓친 돈 시트 공유 — 바이럴 핵심 카피 */
    fun shareMissed(context: Context, missedAmount: Long, missedCount: Int) {
        val text = buildString {
            append("나 정부 지원금 ${formatAmount(missedAmount)}이나 놓쳤대 ㅋㅋ\n")
            append("최근 3년 ${missedCount}건. 너도 한번 봐봐 👇\n\n")
            append(PLAY_STORE_URL)
        }
        share(context, text = text, title = "정부 지원금 놓친 돈 확인")
    }

    /** 친구 초대 */
    fun inviteFriends(context: Context) {
        val text = buildString {
            append("정부 지원금 추천 앱이래, 못 받은 돈 알려준대 ㅋㅋ\n")
            append("진짜 받을 수 있는 것만 골라줘서 편함\n\n")
            append(PLAY_STORE_URL)
        }
        share(context, text = text, title = "숨은지원금 추천")
    }

    /** 의견 보내기 (mailto) */
    fun sendFeedback(context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:$FEEDBACK_EMAIL".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "[숨은지원금] 의견 보내기")
            putExtra(
                Intent.EXTRA_TEXT,
                "사용해보고 느낀 점, 추가됐으면 하는 정책 등 자유롭게 적어주세요.\n\n— "
            )
        }
        runCatching {
            context.startActivity(intent)
        }.onFailure {
            // 이메일 앱 없을 때 fallback — 일반 텍스트 공유 시트
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "숨은지원금 의견 → $FEEDBACK_EMAIL")
            }
            context.startActivity(Intent.createChooser(fallback, "의견 보내기"))
        }
    }

    /** 개인정보 처리방침 — placeholder */
    fun openPrivacyPolicy(context: Context) {
        // TODO: Firebase Hosting 등에 호스팅 후 URL 교체
        val intent = Intent(Intent.ACTION_VIEW, PLAY_STORE_URL.toUri())
        runCatching { context.startActivity(intent) }
    }

    private fun share(context: Context, text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
        }
        context.startActivity(Intent.createChooser(intent, "공유"))
    }
}
