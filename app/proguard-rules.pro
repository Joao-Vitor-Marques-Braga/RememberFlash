# Add project specific ProGuard rules here.
-keepattributes *Annotation*

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Gemini SDK
-keep class com.google.ai.client.generativeai.** { *; }

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class com.rememberflash.app.domain.model.** { *; }
