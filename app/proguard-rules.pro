# Add project specific ProGuard rules here.

-dontobfuscate
-dontwarn **
-ignorewarnings
-dontoptimize

-keepattributes *

-keep class de.m4lik.burningseries.** { *; }

# keeps views and other stuff. Support library fails without this.
-keep class android.support.v7.widget.SearchView { *; }

# this is for butterknife
-keep class **$$ViewBinder { *; }

# keep enums!
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}