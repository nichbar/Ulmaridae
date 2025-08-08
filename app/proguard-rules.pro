# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# ======================================
# Android Framework and Core Rules
# ======================================

# Keep all Activities, Services, Receivers, and Providers
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# Keep TileService classes (for Quick Settings Tile)
-keep public class * extends android.service.quicksettings.TileService
-keep class now.link.service.UnifiedAgentTileService { *; }

# Keep Service classes
-keep class now.link.service.UnifiedAgentService { *; }

# Keep ViewModels
-keep class now.link.viewmodel.** { *; }

# Keep data classes and configuration classes
-keep class now.link.model.** { *; }

# Keep all native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep all classes that have special methods (Parcelable, Serializable)
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ======================================
# Android Components and Views
# ======================================

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep view binding classes
-keep class **.databinding.** { *; }
-keep class now.link.databinding.** { *; }

# Keep annotation classes
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }

# ======================================
# AndroidX and Material Components
# ======================================

# Keep Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep AndroidX classes
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep Lifecycle components
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Keep Fragment classes
-keep class * extends androidx.fragment.app.Fragment { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# ======================================
# Kotlin specific rules
# ======================================

# Keep Kotlin metadata
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Kotlin reflection (if used)
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# Keep Kotlin intrinsics
-keep class kotlin.jvm.internal.** { *; }

# ======================================
# Networking and Security (if applicable)
# ======================================

# Keep SSL classes (if using HTTPS)
-keep class javax.net.ssl.** { *; }
-dontwarn javax.net.ssl.**

# Keep OkHttp (if using networking)
-dontwarn okhttp3.**
-dontwarn okio.**

# ======================================
# Project specific rules
# ======================================

# Keep main application class
-keep class now.link.MainActivity { *; }

# Keep all public methods in application classes
-keepclassmembers class now.link.** {
    public *;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep SharedPreferences keys (if using reflection)
-keepclassmembers class * {
    public static final java.lang.String PREF_*;
}

# ======================================
# Debugging and Development
# ======================================

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable

# Keep parameter names for debugging
-keepparameternames

# Don't warn about missing classes
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.**
-dontwarn com.google.errorprone.**

# Keep error prone annotations if used
-keep class com.google.errorprone.** { *; }
-dontwarn com.google.errorprone.**

# Keep javax model classes
-keep class javax.lang.model.** { *; }
-dontwarn javax.lang.model.**

# ======================================
# General optimization rules
# ======================================

# Allow aggressive optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Remove debug logs in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}