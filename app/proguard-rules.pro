# Add project specific ProGuard rules here.
# Keep JavascriptInterface annotations for WebView bridge
-keepattributes JavascriptInterface
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.example.BiometricInterface {
    public <methods>;
}

# Keep Security and Updater classes
-keep class com.example.security.** { *; }
-keep class com.example.updater.** { *; }

# Keep line numbers for Play Console crash diagnostics
-keepattributes SourceFile,LineNumberTable
