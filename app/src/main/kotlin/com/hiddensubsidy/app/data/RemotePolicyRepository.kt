package com.hiddensubsidy.app.data

import com.hiddensubsidy.app.data.model.Policy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RemotePolicyRepository(
    private val client: HttpClient,
    private val url: String,
) : PolicyRepository {
    override suspend fun loadAll(): List<Policy> = client.get(url).body()
    override suspend fun findById(id: String): Policy? =
        loadAll().firstOrNull { it.id == id }
}
