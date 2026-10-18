# BozorGo — R8 / ProGuard qoidalari

# --- Umumiy ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Room ---
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep class uz.bozorgo.data.local.db.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontnote androidx.room.**

# --- Gson (seed va tarmoq modellari) ---
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class uz.bozorgo.data.remote.dto.** { *; }
-keep class uz.bozorgo.data.seed.dto.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# --- Retrofit / OkHttp ---
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers class * {
    @retrofit2.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# --- Kotlin ---
-dontwarn kotlinx.coroutines.**
-keepclassmembers class **$WhenMappings { <methods>; }
-keepclassmembers class kotlin.Metadata { public <methods>; }

# --- Compose (R8 bilan Compose ishlashi uchun) ---
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# --- Ma'lumotlar bazasi migratsiyalari ---
-keep class androidx.room.migration.Migration { *; }
