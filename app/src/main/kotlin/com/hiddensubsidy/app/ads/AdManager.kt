package com.hiddensubsidy.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * AdMob 전면 광고(Interstitial) 관리자.
 *
 * 노출 지점:
 *  - 정책 상세 진입 카운터: **3회마다 1번** 시도
 *  - 외부 신청 링크 클릭 시: **매번** 시도 (이탈 직전 타이밍)
 *  - 광고 로드 실패 / 미준비 시 조용히 skip (UX 흐름 안 끊김)
 *
 * 광고 단위 ID:
 *  - 현재 Google 공식 **테스트 ID**. 출시 직전 사용자 AdMob 콘솔에서 발급 받은 실 ID로 교체.
 *  - 테스트 ID는 합법적으로 작동 검증 가능 (실제 수익 X, 테스트 광고 표시).
 */
object AdManager {

    private const val TAG = "ad-manager"

    // 디버그 빌드 → 테스트 광고 단위 (안전, 자기 폰에서 클릭해도 무해)
    // 릴리스 빌드 → 실 광고 단위 (출시용, 진짜 수익)
    private val INTERSTITIAL_UNIT_ID: String
        get() = if (com.hiddensubsidy.app.BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"  // Google 공식 테스트 ID
        } else {
            "ca-app-pub-2968584390793166/8781576983"  // 실 ID (Policy_Detail_Interstitial)
        }

    private const val DETAIL_VIEWS_PER_AD = 3              // 정책 상세 3번마다 시도

    @Volatile private var detailViewCounter: Int = 0
    @Volatile private var loadedAd: InterstitialAd? = null
    @Volatile private var loading: Boolean = false

    /**
     * 앱 시작 시 1회 호출. SDK 초기화 + 첫 광고 미리 로드.
     * HiddenSubsidyApp.onCreate() 또는 MainActivity.onCreate()에서.
     */
    fun initialize(context: Context) {
        MobileAds.initialize(context) { Log.i(TAG, "MobileAds initialized") }
        preload(context)
    }

    /** 백그라운드에서 광고 미리 로드. 빈도 제한과 무관. */
    private fun preload(context: Context) {
        if (loadedAd != null || loading) return
        loading = true
        InterstitialAd.load(
            context.applicationContext,
            INTERSTITIAL_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadedAd = ad
                    loading = false
                    Log.i(TAG, "interstitial loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadedAd = null
                    loading = false
                    Log.w(TAG, "interstitial load failed: ${error.message}")
                }
            },
        )
    }

    /**
     * 정책 상세 진입 시 호출. 빈도 정책 통과하면 광고 표시.
     * 통과 안 하면 카운터만 증가하고 조용히 skip.
     */
    fun onPolicyDetailEntered(activity: Activity) {
        detailViewCounter++
        // 3번에 1번 트리거 (쿨다운/진입보호 없음)
        if (detailViewCounter % DETAIL_VIEWS_PER_AD != 0) return
        // 통과 — 광고 시도
        showInterstitial(activity)
    }

    /**
     * 외부 신청 링크 클릭 시 호출. 광고를 띄우고 **닫힌 뒤** [onProceed] 실행(=링크 열기).
     * 광고가 준비 안 됐으면 흐름을 끊지 않도록 즉시 [onProceed] 실행.
     */
    fun onApplyLinkClicked(activity: Activity, onProceed: () -> Unit) {
        showInterstitial(activity, onProceed)
    }

    private fun showInterstitial(activity: Activity, onProceed: (() -> Unit)? = null) {
        val ad = loadedAd
        if (ad == null) {
            Log.i(TAG, "no ad ready — skip + preload")
            preload(activity)
            onProceed?.invoke()  // 광고 없으면 바로 진행
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadedAd = null
                preload(activity)    // 다음 광고 미리 로드
                onProceed?.invoke()  // 광고 닫힌 뒤 진행
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                loadedAd = null
                Log.w(TAG, "show failed: ${error.message}")
                preload(activity)
                onProceed?.invoke()  // 표시 실패해도 진행
            }
        }
        ad.show(activity)
    }
}
