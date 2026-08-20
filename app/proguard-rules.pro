# Keep data/dto classes used with kotlinx.serialization reflection
-keepattributes *Annotation*, InnerClasses
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.linguatranslate.app.**$$serializer { *; }
-keepclassmembers class com.linguatranslate.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.linguatranslate.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
