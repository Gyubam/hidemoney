# ─────────────────────────────────────────────────────────────────
# kotlinx-serialization (Policy / EligibilityRule / HomeData / CloudUserData 등)
# ─────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.hiddensubsidy.app.**$$serializer { *; }
-keepclassmembers class com.hiddensubsidy.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.hiddensubsidy.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ─────────────────────────────────────────────────────────────────
# Ktor (CachedPolicyRepository, RemotePolicyRepository, PolicyDeadlineWorker)
# ─────────────────────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**

# ─────────────────────────────────────────────────────────────────
# Firebase (Auth + Firestore + FCM)
# ─────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firestore reflection — CloudUserData / CloudUserProfile data class
-keepclassmembers class com.hiddensubsidy.app.data.** {
    public <init>(...);
    *;
}
-keep class com.hiddensubsidy.app.data.CloudUserData { *; }
-keep class com.hiddensubsidy.app.data.CloudUserProfile { *; }

# ─────────────────────────────────────────────────────────────────
# AdMob (com.google.android.gms.ads)
# ─────────────────────────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ─────────────────────────────────────────────────────────────────
# WorkManager
# ─────────────────────────────────────────────────────────────────
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# ─────────────────────────────────────────────────────────────────
# Compose / Material3 (대부분 자동, R8 friendly)
# ─────────────────────────────────────────────────────────────────
# 별도 keep 룰 불필요 — Compose는 R8 기본 룰로 동작

# ─────────────────────────────────────────────────────────────────
# 일반 — 디버그 정보 보존 (Crashlytics·스택트레이스 위해)
# ─────────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
