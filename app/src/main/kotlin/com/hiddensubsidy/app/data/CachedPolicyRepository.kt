package com.hiddensubsidy.app.data

import android.content.Context
import com.hiddensubsidy.app.data.model.Policy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 캐시 우선 정책 저장소.
 * - loadAll(): 캐시 있으면 즉시 반환 → 없으면 remote → 실패 시 fallback
 * - refresh(): remote 강제 호출 후 캐시 갱신 (UI에서 background 트리거)
 */
class CachedPolicyRepository(
    context: Context,
    private val remote: PolicyRepository,
    private val fallback: PolicyRepository,
) : PolicyRepository {

    private val cacheFile: File = File(context.filesDir, "policies-cache.json")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
    }

    override suspend fun loadAll(): List<Policy> = withContext(Dispatchers.IO) {
        // 1) 캐시 파일이 있으면 즉시 반환
        runCatching {
            if (cacheFile.exists()) {
                return@withContext json.decodeFromString<List<Policy>>(cacheFile.readText())
            }
        }
        // 2) Remote 시도
        runCatching {
            val fresh = remote.loadAll()
            cacheFile.writeText(json.encodeToString(fresh))
            return@withContext fresh
        }
        // 3) Fallback (InMemory/SampleData)
        fallback.loadAll()
    }

    override suspend fun findById(id: String): Policy? = loadAll().firstOrNull { it.id == id }

    /** Remote 강제 fetch + 캐시 갱신. 실패 시 기존 캐시 그대로. */
    suspend fun refresh(): List<Policy> = withContext(Dispatchers.IO) {
        val fresh = remote.loadAll()
        cacheFile.writeText(json.encodeToString(fresh))
        fresh
    }
}
