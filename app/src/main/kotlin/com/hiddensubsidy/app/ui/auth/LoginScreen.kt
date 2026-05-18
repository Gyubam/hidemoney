package com.hiddensubsidy.app.ui.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.hiddensubsidy.app.data.AuthRepository
import com.hiddensubsidy.app.ui.theme.AppTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        loading = true
        if (result.resultCode != Activity.RESULT_OK) {
            loading = false
            Toast.makeText(context, "로그인이 취소됐어요", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).await()
                val idToken = account.idToken ?: error("idToken 없음")
                val signInResult = AuthRepository.signInWithIdToken(idToken)
                signInResult.fold(
                    onSuccess = {
                        Toast.makeText(context, "환영해요, ${it.displayName ?: "사용자"}님 🎉", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    },
                    onFailure = {
                        Toast.makeText(context, "로그인 실패: ${it.message}", Toast.LENGTH_LONG).show()
                    },
                )
            } catch (e: ApiException) {
                Toast.makeText(context, "Google 로그인 실패: ${e.statusCode}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "로그인 오류: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                loading = false
            }
        }
    }

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onBack = onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(colors.accentBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "🔐", style = TextStyle(fontSize = 48.sp))
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Google 계정으로 로그인",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "받을 예정·신청한·받은 지원금을\n폰을 바꿔도 유지해드려요",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(40.dp))

                if (loading) {
                    CircularProgressIndicator(color = colors.accent)
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.textPrimary)
                            .clickable {
                                loading = true
                                launcher.launch(AuthRepository.getSignInIntent(context))
                            }
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "G", style = TextStyle(fontSize = 22.sp), color = colors.background)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Google로 계속하기",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.background,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    text = "로그인 없이 계속 사용해도 돼요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    val colors = AppTheme.colors
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topInset)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "뒤로",
                tint = colors.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

