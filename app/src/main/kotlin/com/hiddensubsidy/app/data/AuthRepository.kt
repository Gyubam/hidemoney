package com.hiddensubsidy.app.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Auth (Google 로그인) wrapper.
 *
 * MVP 흐름:
 *  1. [getSignInIntent] → ActivityResultLauncher로 Google 선택 UI
 *  2. 결과의 idToken을 [signInWithIdToken]에 전달 → Firebase Auth 로그인
 *  3. [currentUser] / [authState] 로 상태 관찰
 *  4. [signOut] 으로 양쪽(Firebase + Google) 모두 로그아웃
 *
 * Firebase user 정보 (uid/displayName/email/photoUrl) 만 클라에 노출.
 * Firestore 동기화는 별도 라운드(R9.5).
 */
object AuthRepository {

    /** Firebase 현재 사용자. 미로그인이면 null. */
    val currentUser get() = Firebase.auth.currentUser

    /** Firebase Auth 상태 flow. 로그인/로그아웃 시 자동 emit. */
    val authState: Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        Firebase.auth.addAuthStateListener(listener)
        awaitClose { Firebase.auth.removeAuthStateListener(listener) }
    }

    /** google-services.json의 default_web_client_id 자동 매핑. */
    private fun googleSignInClient(context: Context): GoogleSignInClient {
        val webClientId = context.getString(
            context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        )
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    /** Google 계정 선택 UI 띄울 Intent. ActivityResultLauncher에 전달. */
    fun getSignInIntent(context: Context) = googleSignInClient(context).signInIntent

    /** Google idToken으로 Firebase Auth 로그인. */
    suspend fun signInWithIdToken(idToken: String): Result<com.google.firebase.auth.FirebaseUser> {
        return runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = Firebase.auth.signInWithCredential(credential).await()
            result.user ?: error("Firebase user null after sign-in")
        }
    }

    /** 양쪽(Firebase + Google 계정 캐시) 다 sign-out. */
    suspend fun signOut(context: Context) {
        Firebase.auth.signOut()
        runCatching { googleSignInClient(context).signOut().await() }
    }
}
