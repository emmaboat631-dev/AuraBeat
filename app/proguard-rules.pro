# Keep Media3 session classes
-keep class androidx.media3.** { *; }
-keepclassmembers class * extends androidx.media3.session.MediaSessionService { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
