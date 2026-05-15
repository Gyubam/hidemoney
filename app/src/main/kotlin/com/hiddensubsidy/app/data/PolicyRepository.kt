package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.Policy

/**
 * 정책 데이터 소스 추상화.
 * 현재는 InMemory(SampleData), 다음 단계에서 Remote(Ktor) + 로컬 캐시로 확장.
 */
interface PolicyRepository {
    suspend fun loadAll(): List<Policy>
    suspend fun findById(id: String): Policy?
}

class InMemoryPolicyRepository(
    private val policies: List<Policy>,
) : PolicyRepository {
    override suspend fun loadAll(): List<Policy> = policies
    override suspend fun findById(id: String): Policy? = policies.firstOrNull { it.id == id }
}
