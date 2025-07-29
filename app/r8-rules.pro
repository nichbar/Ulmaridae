# R8 specific optimization rules for Ulmaridae
# This file contains R8-specific optimizations and rules

# ======================================
# R8 Full Mode Optimizations
# ======================================

# Enable more aggressive optimizations
-repackageclasses ''

# ======================================
# Code Shrinking Optimizations
# ======================================

# Keep important classes from being removed
-keep class now.link.MainActivity
-keep class now.link.service.**
-keep class now.link.viewmodel.**

# Keep only used methods in utility classes
-keepclassmembers class now.link.util.** {
    public static <methods>;
}

# ======================================
# Obfuscation Rules
# ======================================

# Keep class names for crash reporting
-keepnames class now.link.**

# Obfuscate but keep debugging info
-keepattributes SourceFile,LineNumberTable,*Annotation*

# Keep method signatures for reflection
-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

# ======================================
# Performance Optimizations
# ======================================

# Optimize field access
-optimizations field/removal/writeonly,field/marking/private

# Class merging optimizations
-optimizations class/merging/vertical,class/merging/horizontal

# ======================================
# Advanced R8 Features
# ======================================

# Enable string optimization
-optimizations code/simplification/string

# Enable branch optimization
-optimizations code/removal/simple,code/removal/advanced

# Enable constant folding
-optimizations code/simplification/arithmetic

# ======================================
# Debugging Support
# ======================================

# Keep meaningful stack traces
-keepattributes SourceFile,LineNumberTable

# ======================================
# Platform Specific
# ======================================

# Android specific optimizations
-dontpreverify
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Keep Android system classes
-keep class android.** { *; }
-keep class androidx.** { *; }
-keep class com.google.android.** { *; }

# ======================================
# Final Output Optimization
# ======================================

# Rename packages for smaller APK
-repackageclasses 'a'
-allowaccessmodification
-mergeinterfacesaggressively

# Optimize for size
-optimizationpasses 5
