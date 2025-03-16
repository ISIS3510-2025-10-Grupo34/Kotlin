package com.tutorapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tutorapp.ui.theme.*

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(400),
        color = LightGrey,
        textAlign = TextAlign.Center,
        letterSpacing = 0.5.sp
    ),

    titleLarge = TextStyle(
        fontSize = 30.sp,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(700),
        color = Primary,
        textAlign = TextAlign.Center
    ),



)