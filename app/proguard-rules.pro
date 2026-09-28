# FluxBoard ProGuard/R8 rules

# Keep InputMethodService entry points referenced only from the manifest
-keep class com.fluxboard.app.presentation.keyboard.** { *; }

# Supabase / Ktor / kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.fluxboard.app.**$$serializer { *; }
-keepclassmembers class com.fluxboard.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.fluxboard.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# RevenueCat
-keep class com.revenuecat.purchases.** { *; }

# PostHog
-keep class com.posthog.** { *; }
