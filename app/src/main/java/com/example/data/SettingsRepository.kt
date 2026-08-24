package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    DARK,
    LIGHT,
    SYSTEM,
    CYBER_NEON,
    SOLAR_AMBER,
    NORDIC_MINT
}

enum class AngleUnit {
    DEG,
    RAD
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("calculator_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean("haptic_feedback", true))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _angleUnit = MutableStateFlow(loadAngleUnit())
    val angleUnit: StateFlow<AngleUnit> = _angleUnit.asStateFlow()

    private val _decimalPrecision = MutableStateFlow(prefs.getInt("decimal_precision", -1)) // -1 is Auto
    val decimalPrecision: StateFlow<Int> = _decimalPrecision.asStateFlow()

    private val _adFreeUntil = MutableStateFlow(prefs.getLong("ad_free_until", 0L))
    val adFreeUntil: StateFlow<Long> = _adFreeUntil.asStateFlow()

    private val _unlockedThemes = MutableStateFlow(loadUnlockedThemes())
    val unlockedThemes: StateFlow<Set<String>> = _unlockedThemes.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val name = prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        return try {
            AppThemeMode.valueOf(name)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
    }

    private fun loadAngleUnit(): AngleUnit {
        val name = prefs.getString("angle_unit", AngleUnit.DEG.name) ?: AngleUnit.DEG.name
        return try {
            AngleUnit.valueOf(name)
        } catch (e: Exception) {
            AngleUnit.DEG
        }
    }

    private fun loadUnlockedThemes(): Set<String> {
        val defaultThemes = setOf(
            AppThemeMode.DARK.name,
            AppThemeMode.LIGHT.name,
            AppThemeMode.SYSTEM.name
        )
        return prefs.getStringSet("unlocked_themes", defaultThemes) ?: defaultThemes
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
        _hapticFeedback.value = enabled
    }

    fun setAngleUnit(unit: AngleUnit) {
        prefs.edit().putString("angle_unit", unit.name).apply()
        _angleUnit.value = unit
    }

    fun setDecimalPrecision(precision: Int) {
        prefs.edit().putInt("decimal_precision", precision).apply()
        _decimalPrecision.value = precision
    }

    fun unlockAdFreeHours(hours: Int) {
        val currentNow = System.currentTimeMillis()
        val currentExpiry = _adFreeUntil.value
        val baseTime = if (currentExpiry > currentNow) currentExpiry else currentNow
        val newExpiry = baseTime + (hours * 3600_000L)
        prefs.edit().putLong("ad_free_until", newExpiry).apply()
        _adFreeUntil.value = newExpiry
    }

    fun isAdFreeActive(): Boolean {
        return System.currentTimeMillis() < _adFreeUntil.value
    }

    fun unlockTheme(themeName: String) {
        val set = _unlockedThemes.value.toMutableSet()
        set.add(themeName)
        prefs.edit().putStringSet("unlocked_themes", set).apply()
        _unlockedThemes.value = set
    }
}
