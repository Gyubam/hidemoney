package com.hiddensubsidy.app.data.model

import kotlinx.serialization.Serializable

/**
 * 원격 앱 설정 (GitHub Pages `app-config.json`).
 * 출시 후 서버 코드 없이 강제 업데이트 등을 제어. 0원 운영 — 정적 JSON 호스팅.
 */
@Serializable
data class AppConfig(
    val minVersionCode: Int = 0,        // 이 값 미만 versionCode = 강제 업데이트
    val latestVersionCode: Int = 0,     // 최신 버전 (선택 업데이트 안내용, 추후)
    val updateMessage: String = "",     // 강제 업데이트 화면 안내 문구
)
