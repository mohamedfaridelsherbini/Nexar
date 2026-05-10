package com.mohamedfaridelsherbini.nexar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Typography scale from Nexar.pen
val NexarTypography =
    Typography(
        headlineLarge =
            TextStyle(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 48.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp,
            ),
        headlineMedium =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
            ),
        titleLarge =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
        titleMedium =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp,
            ),
        bodyLarge =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
        bodyMedium =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
            ),
        bodySmall =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
            ),
        labelMedium =
            TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.sp,
            ),
        labelSmall =
            TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
    )
