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
 * 빈도 정책 (토스 톤 유지 + 사용자 인내력 보호):
 *  - 정책 상세 진입 카운터: **5회마다 1번** 시도
 *  - 마지막 광고 후 **5분 쿨다운**
 *  - 앱 진입 후 **30초 안에는 X** (온보딩/첫 인상 보호)
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

    private const val APP_OPEN_GRACE_MS = 30_000L          // 앱 진입 후 30초 보호
    private const val COOLDOWN_MS = 5L * 60 * 1000         // 마지막 광고 후 5분 쿨다운
    private const val DETAIL_VIEWS_PER_AD = 5              // 정책 상세 5번마다 시도

    private val appStartTime = System.currentTimeMillis()
    private var lastShownAt: Long = 0L
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
        val now = System.currentTimeMillis()
        // 1) 앱 시작 직후 30초 보호
        if (now - appStartTime < APP_OPEN_GRACE_MS) return
        // 2) 마지막 광고 후 5분 쿨다운
        if (lastShownAt > 0 && now - lastShownAt < COOLDOWN_MS) return
        // 3) 5번에 1번 트리거
        if (detailViewCounter % DETAIL_VIEWS_PER_AD != 0) return
        // 통과 — 광고 시도
        showInterstitial(activity)
    }

    private fun showInterstitial(activity: Activity) {
        val ad = loadedAd
        if (ad == null) {
            Log.i(TAG, "no ad ready — skip + preload")
            preload(activity)
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadedAd = null
                lastShownAt = System.currentTimeMillis()
                preload(activity)  // 다음 광고 미리 로드
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                loadedAd = null
                Log.w(TAG, "show failed: ${error.message}")
                preload(activity)
            }
        }
        ad.show(activity)
    }
}
