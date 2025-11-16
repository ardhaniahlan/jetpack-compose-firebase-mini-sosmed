# =========================================
# 🔒 ProGuard Rules for Mini Sosmed Project
# =========================================

# ---------- Firebase ----------
# Keep all Firebase classes (Auth, Firestore, Storage)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ---------- Hilt / Dagger ----------
# Keep Hilt/Dagger generated code
-keep class dagger.hilt.** { *; }
-keep class *Hilt_* { *; }
-keep class *Injector { *; }
-dontwarn dagger.hilt.**

# ---------- Jetpack Compose ----------
# Keep all Compose-related classes and avoid warnings
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-dontwarn androidx.activity.compose.**
-dontwarn androidx.lifecycle.compose.**

# ---------- ViewModel & Data Models ----------
# Keep all ViewModels and data classes (important for reflection)
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class **.datamodel.** { *; }
-keepclassmembers class **.model.** { *; }
-dontwarn androidx.lifecycle.**

# ---------- Kotlin Coroutines ----------
-dontwarn kotlinx.coroutines.**

# ---------- Prevent Removal of Kotlin Metadata ----------
-keep class kotlin.Metadata { *; }

# ---------- Keep App Classes and Entry Points ----------
-keep class org.apps.minisosmed.** { *; }

# ---------- Preserve attributes needed for reflection ----------
-keepattributes InnerClasses,EnclosingMethod,Signature

# ---------- Optional: Keep line numbers for crash logs ----------
-keepattributes SourceFile,LineNumberTable

# =========================================
# End of Rules
# =========================================
