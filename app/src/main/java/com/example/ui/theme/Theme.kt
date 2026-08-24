package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.AppThemeMode

@Immutable
data class CalculatorColors(
    val background: Color,
    val keypadSurface: Color,
    val cardSurface: Color,
    val digitKey: Color,
    val digitText: Color,
    val functionKey: Color,
    val functionText: Color,
    val operatorKey: Color,
    val operatorText: Color,
    val equalsKey: Color,
    val equalsText: Color,
    val clearText: Color,
    val secondaryText: Color,
    val isDark: Boolean
)

val LocalCalculatorColors = staticCompositionLocalOf {
    CalculatorColors(
        background = DarkBackground,
        keypadSurface = DarkKeypadSurface,
        cardSurface = DarkCardSurface,
        digitKey = DarkDigitKey,
        digitText = DarkDigitText,
        functionKey = DarkFunctionKey,
        functionText = DarkFunctionText,
        operatorKey = DarkOperatorKey,
        operatorText = DarkOperatorText,
        equalsKey = DarkEqualsKey,
        equalsText = DarkEqualsText,
        clearText = DarkClearText,
        secondaryText = DarkSecondaryText,
        isDark = true
    )
}

val SophisticatedDarkColors = CalculatorColors(
    background = DarkBackground,
    keypadSurface = DarkKeypadSurface,
    cardSurface = DarkCardSurface,
    digitKey = DarkDigitKey,
    digitText = DarkDigitText,
    functionKey = DarkFunctionKey,
    functionText = DarkFunctionText,
    operatorKey = DarkOperatorKey,
    operatorText = DarkOperatorText,
    equalsKey = DarkEqualsKey,
    equalsText = DarkEqualsText,
    clearText = DarkClearText,
    secondaryText = DarkSecondaryText,
    isDark = true
)

val ModernLightColors = CalculatorColors(
    background = LightBackground,
    keypadSurface = LightKeypadSurface,
    cardSurface = LightCardSurface,
    digitKey = LightDigitKey,
    digitText = LightDigitText,
    functionKey = LightFunctionKey,
    functionText = LightFunctionText,
    operatorKey = LightOperatorKey,
    operatorText = LightOperatorText,
    equalsKey = LightEqualsKey,
    equalsText = LightEqualsText,
    clearText = LightClearText,
    secondaryText = LightSecondaryText,
    isDark = false
)

val CyberNeonColors = CalculatorColors(
    background = NeonBackground,
    keypadSurface = NeonKeypadSurface,
    cardSurface = NeonCardSurface,
    digitKey = NeonDigitKey,
    digitText = NeonDigitText,
    functionKey = NeonFunctionKey,
    functionText = NeonFunctionText,
    operatorKey = NeonOperatorKey,
    operatorText = NeonOperatorText,
    equalsKey = NeonEqualsKey,
    equalsText = NeonEqualsText,
    clearText = NeonClearText,
    secondaryText = NeonSecondaryText,
    isDark = true
)

val SolarAmberColors = CalculatorColors(
    background = AmberBackground,
    keypadSurface = AmberKeypadSurface,
    cardSurface = AmberCardSurface,
    digitKey = AmberDigitKey,
    digitText = AmberDigitText,
    functionKey = AmberFunctionKey,
    functionText = AmberFunctionText,
    operatorKey = AmberOperatorKey,
    operatorText = AmberOperatorText,
    equalsKey = AmberEqualsKey,
    equalsText = AmberEqualsText,
    clearText = AmberClearText,
    secondaryText = AmberSecondaryText,
    isDark = true
)

val NordicMintColors = CalculatorColors(
    background = MintBackground,
    keypadSurface = MintKeypadSurface,
    cardSurface = MintCardSurface,
    digitKey = MintDigitKey,
    digitText = MintDigitText,
    functionKey = MintFunctionKey,
    functionText = MintFunctionText,
    operatorKey = MintOperatorKey,
    operatorText = MintOperatorText,
    equalsKey = MintEqualsKey,
    equalsText = MintEqualsText,
    clearText = MintClearText,
    secondaryText = MintSecondaryText,
    isDark = true
)

@Composable
fun CalculatorTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val calcColors = when (themeMode) {
        AppThemeMode.DARK -> SophisticatedDarkColors
        AppThemeMode.LIGHT -> ModernLightColors
        AppThemeMode.SYSTEM -> if (systemDark) SophisticatedDarkColors else ModernLightColors
        AppThemeMode.CYBER_NEON -> CyberNeonColors
        AppThemeMode.SOLAR_AMBER -> SolarAmberColors
        AppThemeMode.NORDIC_MINT -> NordicMintColors
    }

    val materialColors = if (calcColors.isDark) {
        darkColorScheme(
            background = calcColors.background,
            surface = calcColors.keypadSurface,
            surfaceVariant = calcColors.cardSurface,
            primary = calcColors.equalsKey,
            onPrimary = calcColors.equalsText,
            onBackground = calcColors.digitText,
            onSurface = calcColors.digitText,
            onSurfaceVariant = calcColors.secondaryText
        )
    } else {
        lightColorScheme(
            background = calcColors.background,
            surface = calcColors.keypadSurface,
            surfaceVariant = calcColors.cardSurface,
            primary = calcColors.equalsKey,
            onPrimary = calcColors.equalsText,
            onBackground = calcColors.digitText,
            onSurface = calcColors.digitText,
            onSurfaceVariant = calcColors.secondaryText
        )
    }

    CompositionLocalProvider(LocalCalculatorColors provides calcColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography,
            content = content
        )
    }
}
