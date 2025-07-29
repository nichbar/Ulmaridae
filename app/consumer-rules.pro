# Consumer ProGuard rules for Ulmaridae
# These rules will be applied to any module that depends on this one

# Keep public API classes
-keep public class now.link.** {
    public protected *;
}

# Keep Service classes that might be accessed externally
-keep class now.link.service.NezhaAgentService { *; }
-keep class now.link.service.NezhaAgentTileService { *; }

# Keep model classes that might be serialized
-keep class now.link.model.** { *; }

# Keep any classes with native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
