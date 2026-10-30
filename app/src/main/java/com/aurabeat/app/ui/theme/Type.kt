package com.aurabeat.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Sans = FontFamily.SansSerif

val AuraTypography = Typography(
    displayLarge   = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold,     fontSize = 40.sp, letterSpacing = (-0.5).sp),
    displayMedium  = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold,     fontSize = 32.sp, letterSpacing = (-0.25).sp),
    headlineLarge  = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold,     fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    headlineSmall  = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleLarge     = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium    = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium,   fontSize = 16.sp),
    titleSmall     = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    bodyLarge      = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal,   fontSize = 16.sp),
    bodyMedium     = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    bodySmall      = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal,   fontSize = 12.sp),
    labelLarge     = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium,   fontSize = 14.sp, letterSpacing = 0.4.sp),
    labelMedium    = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium,   fontSize = 12.sp),
    labelSmall     = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium,   fontSize = 11.sp),
)
