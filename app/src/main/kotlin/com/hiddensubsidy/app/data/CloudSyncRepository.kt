package com.hiddensubsidy.app.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.hiddensubsidy.app.data.model.UserProfile
import kotlinx.coroutines.tasks.await

/**
 * Firestore `users/{uid}` 문서 동기화 wrapper.
 *
 * 흐름:
 *  - **로그인 시** [pullFromCloud] — 클라우드 → SharedPreferences (덮어쓰기)
 *    클라우드 비어있으면 (신규 사용자) 현재 로컬 데이터 [pushToCloud]로 백업
 *  - **로컬 변경 시** [pushToCloud] — SharedPreferences → 클라우드 (last-write-wins)
 *  - **로그아웃 시** 동기화 X (로컬 SharedPreferences 그대로 유지)
 *
 * Spark 무료 티어: 1GB 저장 / 일 5만 읽기 / 일 2만 쓰기. 1인당 ~1KB라 1000명까지 0원.
 */
object CloudSyncRepository {

    private const val TAG = "cloud-sync"
    private const val COLLECTION = "users"

    private fun userDoc(uid: String) = Firebase.firestore.collection(COLLECTION).document(uid)

    /**
     * 클라우드 → 로컬. uid 문서 있으면 모든 상태 갱신.
     * 문서 없으면 (신규 사용자) 현재 로컬 데이터를 클라우드에 백업.
     *
     * @return 로컬 갱신됐는지 (true=클라우드에서 받음, false=신규 → 로컬 그대로)
     */
    suspend fun pullFromCloud(context: Context, uid: String): Boolean {
        return try {
            val snap = userDoc(uid).get().await()
            if (!snap.exists()) {
                Log.i(TAG, "no cloud doc for $uid — push local backup")
                pushToCloud(context, uid)
                return false
            }
            val cloud = snap.toObject<CloudUserData>() ?: run {
                Log.w(TAG, "cloud doc deserialize failed")
                return false
            }
            // 로컬 SharedPreferences 갱신
            FavoritesRepository.save(context, cloud.favorites.toSet())
            DismissedRepository.save(context, cloud.dismissed.toSet())
            // applied/received는 한 번에 저장 (toggleApplied/Received 내부 로직과 일관)
            val prefs = context.getSharedPreferences("hs_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putStringSet("applied", cloud.applied.toSet())
                .putStringSet("received", cloud.received.toSet())
                .apply()
            // triggers
            val prefsEdit = prefs.edit()
            // 기존 trigger 다 제거 후 새로 set
            prefs.all.keys
                .filter { it.startsWith("event_trigger_") }
                .forEach { prefsEdit.remove(it) }
            cloud.triggers.forEach { (eventId, ts) ->
                prefsEdit.putLong("event_trigger_$eventId", ts)
            }
            prefsEdit.apply()
            // profile
            cloud.profile?.let { p ->
                UserPrefs.save(
                    context,
                    UserProfile(
                        age = p.age,
                        region = p.region,
                        gender = p.gender,
                        occupation = p.occupation,
                        married = p.married,
                        hasChildren = p.hasChildren,
                        childCount = p.childCount,
                        incomeMonthly = p.incomeMonthly,
                        householdSize = p.householdSize,
                        education = p.education,
                        housingType = p.housingType,
                        isMulticultural = p.isMulticultural,
                        isSingleParent = p.isSingleParent,
                        isDisabled = p.isDisabled,
                        isVeteran = p.isVeteran,
                        isDefector = p.isDefector,
                        isLowIncome = p.isLowIncome,
                        includeLoanGrants = p.includeLoanGrants,
                    )
                )
            }
            Log.i(TAG, "pull OK: fav=${cloud.favorites.size}, applied=${cloud.applied.size}, received=${cloud.received.size}, triggers=${cloud.triggers.size}")
            true
        } catch (e: Exception) {
            Log.w(TAG, "pull failed: ${e.message}")
            false
        }
    }

    /**
     * 로컬 → 클라우드. 현재 SharedPreferences 상태를 그대로 푸시. last-write-wins.
     */
    suspend fun pushToCloud(context: Context, uid: String): Boolean {
        return try {
            val prefs = context.getSharedPreferences("hs_prefs", Context.MODE_PRIVATE)
            val triggers = prefs.all.entries
                .filter { it.key.startsWith("event_trigger_") }
                .mapNotNull {
                    val ts = it.value as? Long ?: return@mapNotNull null
                    it.key.removePrefix("event_trigger_") to ts
                }
                .toMap()
            val profile = UserPrefs.load(context).let { p ->
                CloudUserProfile(
                    age = p.age, region = p.region, gender = p.gender,
                    occupation = p.occupation, married = p.married,
                    hasChildren = p.hasChildren, childCount = p.childCount,
                    incomeMonthly = p.incomeMonthly, householdSize = p.householdSize,
                    education = p.education, housingType = p.housingType,
                    isMulticultural = p.isMulticultural, isSingleParent = p.isSingleParent,
                    isDisabled = p.isDisabled, isVeteran = p.isVeteran,
                    isDefector = p.isDefector, isLowIncome = p.isLowIncome,
                    includeLoanGrants = p.includeLoanGrants,
                )
            }
            val data = CloudUserData(
                favorites = FavoritesRepository.load(context).toList(),
                applied = ApplicationStatusRepository.loadApplied(context).toList(),
                received = ApplicationStatusRepository.loadReceived(context).toList(),
                dismissed = DismissedRepository.load(context).toList(),
                triggers = triggers,
                profile = profile,
                updatedAt = System.currentTimeMillis(),
            )
            userDoc(uid).set(data).await()
            Log.i(TAG, "push OK")
            true
        } catch (e: Exception) {
            Log.w(TAG, "push failed: ${e.message}")
            false
        }
    }
}

/** Firestore에 직렬화될 사용자 데이터. Firestore 자동 변환을 위해 모든 필드 default 값 + var X (data class OK). */
data class CloudUserData(
    val favorites: List<String> = emptyList(),
    val applied: List<String> = emptyList(),
    val received: List<String> = emptyList(),
    val dismissed: List<String> = emptyList(),
    val triggers: Map<String, Long> = emptyMap(),
    val profile: CloudUserProfile? = null,
    val updatedAt: Long = 0L,
)

data class CloudUserProfile(
    val age: Int? = null,
    val region: String? = null,
    val gender: String? = null,
    val occupation: String? = null,
    val married: Boolean? = null,
    val hasChildren: Boolean? = null,
    val childCount: Int? = null,
    val incomeMonthly: Long? = null,
    val householdSize: Int? = null,
    val education: String? = null,
    val housingType: String? = null,
    val isMulticultural: Boolean = false,
    val isSingleParent: Boolean = false,
    val isDisabled: Boolean = false,
    val isVeteran: Boolean = false,
    val isDefector: Boolean = false,
    val isLowIncome: Boolean = false,
    val includeLoanGrants: Boolean = false,
)
