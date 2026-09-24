# NOVA v1 intentionally keeps the native shell small and unobfuscated.
-keepclassmembers class com.nova.messenger.MainActivity$NovaAndroidBridge {
    @android.webkit.JavascriptInterface <methods>;
}
