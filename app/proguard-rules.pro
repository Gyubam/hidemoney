# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.hiddensubsidy.app.**$$serializer { *; }
-keepclassmembers class com.hiddensubsidy.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.hiddensubsidy.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}