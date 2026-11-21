package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.local.AppColorTheme
import com.example.data.local.ThemeModeOption

fun getThemeColorScheme(theme: AppColorTheme, isDark: Boolean): ColorScheme {
    return when (theme) {
        AppColorTheme.OCEAN -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFF8DB4FF),
                onPrimary = OceanPrimary,
                primaryContainer = Color(0xFF1D4FB5),
                onPrimaryContainer = Color(0xFFD8E2FF),
                secondary = Color(0xFFADC6FF),
                background = OceanBackgroundDark,
                surface = OceanSurfaceDark,
                onBackground = Color(0xFFEEF1F8),
                onSurface = Color(0xFFEEF1F8),
                tertiary = BrandAccentDark
            )
        } else {
            lightColorScheme(
                primary = OceanPrimary,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFD8E2FF),
                onPrimaryContainer = OceanPrimary,
                secondary = OceanSecondary,
                background = OceanBackgroundLight,
                surface = OceanSurfaceLight,
                onBackground = Color(0xFF15203B),
                onSurface = Color(0xFF15203B),
                tertiary = BrandAccent
            )
        }

        AppColorTheme.ESMERALDA -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFF6EDBA5),
                onPrimary = EmeraldPrimary,
                primaryContainer = Color(0xFF1F8F5F),
                onPrimaryContainer = Color(0xFFB8F3D4),
                secondary = Color(0xFF88DEB1),
                background = EmeraldBackgroundDark,
                surface = EmeraldSurfaceDark,
                onBackground = Color(0xFFF1ECE0),
                onSurface = Color(0xFFF1ECE0),
                tertiary = BrandAccentDark
            )
        } else {
            lightColorScheme(
                primary = EmeraldPrimary,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFC7EED8),
                onPrimaryContainer = EmeraldPrimary,
                secondary = EmeraldSecondary,
                background = EmeraldBackgroundLight,
                surface = EmeraldSurfaceLight,
                onBackground = Color(0xFF1F2B23),
                onSurface = Color(0xFF1F2B23),
                tertiary = BrandAccent
            )
        }

        AppColorTheme.ATARDECER -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFFF9DB5),
                onPrimary = AtardecerPrimary,
                secondary = AtardecerSecondary,
                background = AtardecerBackgroundDark,
                surface = AtardecerSurfaceDark,
                onBackground = Color(0xFFFDF0ED),
                onSurface = Color(0xFFFDF0ED),
                tertiary = Color(0xFF4E89E8)
            )
        } else {
            lightColorScheme(
                primary = AtardecerPrimary,
                onPrimary = Color.White,
                secondary = AtardecerSecondary,
                background = AtardecerBackgroundLight,
                surface = AtardecerSurfaceLight,
                onBackground = Color(0xFF2E171E),
                onSurface = Color(0xFF2E171E),
                tertiary = Color(0xFF2F6FD6)
            )
        }

        AppColorTheme.VIOLETA -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFBBA2FF),
                onPrimary = VioletaPrimary,
                secondary = VioletaSecondary,
                background = VioletaBackgroundDark,
                surface = VioletaSurfaceDark,
                onBackground = Color(0xFFF5F1FD),
                onSurface = Color(0xFFF5F1FD),
                tertiary = Color(0xFFE0508A)
            )
        } else {
            lightColorScheme(
                primary = VioletaPrimary,
                onPrimary = Color.White,
                secondary = VioletaSecondary,
                background = VioletaBackgroundLight,
                surface = VioletaSurfaceLight,
                onBackground = Color(0xFF21153D),
                onSurface = Color(0xFF21153D),
                tertiary = Color(0xFFE0508A)
            )
        }

        AppColorTheme.ROSA -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFFFA3CB),
                onPrimary = RosaPrimary,
                secondary = RosaSecondary,
                background = RosaBackgroundDark,
                surface = RosaSurfaceDark,
                onBackground = Color(0xFFFDF0F6),
                onSurface = Color(0xFFFDF0F6),
                tertiary = Color(0xFF2F6FD6)
            )
        } else {
            lightColorScheme(
                primary = RosaPrimary,
                onPrimary = Color.White,
                secondary = RosaSecondary,
                background = RosaBackgroundLight,
                surface = RosaSurfaceLight,
                onBackground = Color(0xFF331523),
                onSurface = Color(0xFF331523),
                tertiary = Color(0xFF2F6FD6)
            )
        }

        AppColorTheme.GRAFITO -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFC3CAD8),
                onPrimary = GrafitoPrimary,
                secondary = GrafitoSecondary,
                background = GrafitoBackgroundDark,
                surface = GrafitoSurfaceDark,
                onBackground = Color(0xFFF1F3F6),
                onSurface = Color(0xFFF1F3F6),
                tertiary = BrandAccentDark
            )
        } else {
            lightColorScheme(
                primary = GrafitoPrimary,
                onPrimary = Color.White,
                secondary = GrafitoSecondary,
                background = GrafitoBackgroundLight,
                surface = GrafitoSurfaceLight,
                onBackground = Color(0xFF15181D),
                onSurface = Color(0xFF15181D),
                tertiary = BrandAccent
            )
        }
    }
}

@Composable
fun DespensaProTheme(
    colorTheme: AppColorTheme = AppColorTheme.ESMERALDA,
    themeMode: ThemeModeOption = ThemeModeOption.AUTO,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeModeOption.AUTO -> systemInDark
        ThemeModeOption.LIGHT -> false
        ThemeModeOption.DARK -> true
    }

    val colorScheme = getThemeColorScheme(colorTheme, isDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
