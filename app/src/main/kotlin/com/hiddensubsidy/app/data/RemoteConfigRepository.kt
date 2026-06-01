package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/** GitHub Pages의 app-config.json 조회. 실패 시 호출부에서 통과 처리(앱 못 켜는 사고 방지). */
class RemoteConfigRepository(
    private val client: HttpClient,
    private val url: String,
) {
    suspend fun fetch(): AppConfig = client.get(url).body()
}
