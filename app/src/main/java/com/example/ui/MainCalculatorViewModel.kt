package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.CalculatorApplication
import com.example.ads.AdsManager
import com.example.data.AngleUnit
import com.example.data.AppThemeMode
import com.example.data.SettingsRepository
import com.example.data.db.AppDatabase
import com.example.data.db.CalculationEntity
import com.example.engine.CalculationResult
import com.example.engine.CalculatorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val finalResult: String = "0",
    val previewResult: String? = null,
    val isCalculated: Boolean = false,
    val errorMessage: String? = null,
    val isScientificMode: Boolean = false,
    val showHistorySheet: Boolean = false,
    val showSettingsSheet: Boolean = false
)

class MainCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CalculatorApplication
    private val database: AppDatabase = app.database
    private val settingsRepo: SettingsRepository = app.settingsRepository

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<CalculationEntity>> = database.calculationDao()
        .getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentTheme: StateFlow<AppThemeMode> = settingsRepo.themeMode
    val hapticFeedbackEnabled: StateFlow<Boolean> = settingsRepo.hapticFeedback
    val angleUnit: StateFlow<AngleUnit> = settingsRepo.angleUnit
    val decimalPrecision: StateFlow<Int> = settingsRepo.decimalPrecision
    val adFreeUntil: StateFlow<Long> = settingsRepo.adFreeUntil
    val unlockedThemes: StateFlow<Set<String>> = settingsRepo.unlockedThemes

    fun toggleScientificMode() {
        _uiState.value = _uiState.value.copy(
            isScientificMode = !_uiState.value.isScientificMode
        )
    }

    fun setHistorySheetVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showHistorySheet = visible)
    }

    fun setSettingsSheetVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showSettingsSheet = visible)
    }

    fun toggleAngleUnit() {
        val nextUnit = if (angleUnit.value == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG
        settingsRepo.setAngleUnit(nextUnit)
        updatePreview(_uiState.value.expression)
    }

    fun setTheme(theme: AppThemeMode) {
        settingsRepo.setThemeMode(theme)
    }

    fun setHaptics(enabled: Boolean) {
        settingsRepo.setHapticFeedback(enabled)
    }

    fun setPrecision(precision: Int) {
        settingsRepo.setDecimalPrecision(precision)
        updatePreview(_uiState.value.expression)
    }

    fun onDigit(digit: String) {
        val currentExpr = _uiState.value.expression
        val isCalc = _uiState.value.isCalculated

        val newExpr = if (isCalc) {
            digit
        } else {
            currentExpr + digit
        }

        _uiState.value = _uiState.value.copy(
            expression = newExpr,
            isCalculated = false,
            errorMessage = null
        )
        updatePreview(newExpr)
    }

    fun onDecimal() {
        val currentExpr = _uiState.value.expression
        val isCalc = _uiState.value.isCalculated

        if (isCalc) {
            val newExpr = "0."
            _uiState.value = _uiState.value.copy(
                expression = newExpr,
                isCalculated = false,
                errorMessage = null
            )
            updatePreview(newExpr)
            return
        }

        // Prevent multiple decimals in the last number token
        val lastNumber = currentExpr.takeLastWhile { it.isDigit() || it == '.' }
        if (lastNumber.contains('.')) {
            return
        }

        val newExpr = if (currentExpr.isEmpty() || !currentExpr.last().isDigit()) {
            currentExpr + "0."
        } else {
            currentExpr + "."
        }

        _uiState.value = _uiState.value.copy(
            expression = newExpr,
            isCalculated = false,
            errorMessage = null
        )
        updatePreview(newExpr)
    }

    fun onOperator(operator: String) {
        val currentExpr = _uiState.value.expression
        val isCalc = _uiState.value.isCalculated
        val finalRes = _uiState.value.finalResult

        val baseExpr = if (isCalc && finalRes != "Error") {
            finalRes
        } else {
            currentExpr
        }

        if (baseExpr.isEmpty()) {
            if (operator == "−" || operator == "-") {
                _uiState.value = _uiState.value.copy(
                    expression = "−",
                    isCalculated = false,
                    errorMessage = null
                )
            }
            return
        }

        val lastChar = baseExpr.last()
        val operatorsList = listOf('+', '−', '-', '×', '*', '÷', '/', '^')

        val newExpr = if (lastChar in operatorsList) {
            // Replace the dangling operator with the new one
            baseExpr.dropLast(1) + operator
        } else {
            baseExpr + operator
        }

        _uiState.value = _uiState.value.copy(
            expression = newExpr,
            isCalculated = false,
            errorMessage = null
        )
        updatePreview(newExpr)
    }

    fun onPercentage() {
        val currentExpr = _uiState.value.expression
        if (currentExpr.isNotEmpty() && (currentExpr.last().isDigit() || currentExpr.last() == ')')) {
            val newExpr = "$currentExpr%"
            _uiState.value = _uiState.value.copy(
                expression = newExpr,
                isCalculated = false,
                errorMessage = null
            )
            updatePreview(newExpr)
        }
    }

    fun onFunction(functionCall: String) {
        val currentExpr = _uiState.value.expression
        val isCalc = _uiState.value.isCalculated

        val newExpr = if (isCalc) {
            functionCall
        } else {
            currentExpr + functionCall
        }

        _uiState.value = _uiState.value.copy(
            expression = newExpr,
            isCalculated = false,
            errorMessage = null
        )
        updatePreview(newExpr)
    }

    fun onParenthesis(paren: String) {
        val currentExpr = _uiState.value.expression
        val isCalc = _uiState.value.isCalculated

        val newExpr = if (isCalc) {
            paren
        } else {
            currentExpr + paren
        }

        _uiState.value = _uiState.value.copy(
            expression = newExpr,
            isCalculated = false,
            errorMessage = null
        )
        updatePreview(newExpr)
    }

    fun onClear() {
        _uiState.value = _uiState.value.copy(
            expression = "",
            finalResult = "0",
            previewResult = null,
            isCalculated = false,
            errorMessage = null
        )
    }

    fun onDelete() {
        val currentExpr = _uiState.value.expression
        if (currentExpr.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                finalResult = "0",
                previewResult = null,
                isCalculated = false,
                errorMessage = null
            )
            return
        }

        // Multi-character function token removal check (e.g. "sin(", "asin(", "log(", "ln(")
        val functionTokens = listOf("sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "asin(", "acos(", "atan(", "sin(", "cos(", "tan(", "log(", "ln(", "√(")
        var matchFound = false
        var newExpr = currentExpr

        for (token in functionTokens) {
            if (currentExpr.endsWith(token)) {
                newExpr = currentExpr.dropLast(token.length)
                matchFound = true
                break
            }
        }

        if (!matchFound) {
            newExpr = currentExpr.dropLast(1)
        }

        _uiState.value = _uiState.value.copy(
            expression = newExpr,
            isCalculated = false,
            errorMessage = null
        )
        updatePreview(newExpr)
    }

    fun onEquals(activity: Activity? = null) {
        val currentExpr = _uiState.value.expression
        if (currentExpr.isBlank()) return

        val result = CalculatorEngine.calculate(
            expression = currentExpr,
            angleUnit = angleUnit.value,
            precision = decimalPrecision.value
        )

        when (result) {
            is CalculationResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    finalResult = result.formatted,
                    previewResult = null,
                    isCalculated = true,
                    errorMessage = null
                )

                // Save to Room DB
                viewModelScope.launch {
                    try {
                        database.calculationDao().insertCalculation(
                            CalculationEntity(
                                expression = currentExpr,
                                result = result.formatted,
                                timestamp = System.currentTimeMillis(),
                                isScientific = _uiState.value.isScientificMode
                            )
                        )
                    } catch (e: Exception) {
                        // Silent catch
                    }
                }

                // Check and trigger interstitial ad frequency threshold if appropriate
                if (activity != null) {
                    AdsManager.onUserCalculationCompleted(activity, settingsRepo)
                }
            }
            is CalculationResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.message,
                    previewResult = null,
                    isCalculated = true
                )
            }
        }
    }

    private fun updatePreview(expr: String) {
        if (expr.isBlank()) {
            _uiState.value = _uiState.value.copy(previewResult = null)
            return
        }
        val preview = CalculatorEngine.previewCalculate(
            expression = expr,
            angleUnit = angleUnit.value,
            precision = decimalPrecision.value
        )
        _uiState.value = _uiState.value.copy(previewResult = preview)
    }

    fun selectHistoryItem(item: CalculationEntity) {
        _uiState.value = _uiState.value.copy(
            expression = item.expression,
            finalResult = item.result,
            previewResult = null,
            isCalculated = true,
            errorMessage = null,
            showHistorySheet = false
        )
    }

    fun deleteHistoryItem(item: CalculationEntity) {
        viewModelScope.launch {
            database.calculationDao().deleteCalculation(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            database.calculationDao().clearAll()
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Calculator Result", text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(app, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareCalculation(expression: String, result: String) {
        val shareText = "$expression = $result"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(intent, "Share Calculation").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        app.startActivity(chooser)
    }

    fun watchRewardedAdForAdFree(activity: Activity) {
        AdsManager.showRewardedAd(
            activity = activity,
            onUserEarnedReward = {
                settingsRepo.unlockAdFreeHours(24)
                Toast.makeText(app, "24 Hours Ad-Free Unlocked!", Toast.LENGTH_LONG).show()
            },
            onAdClosed = {}
        )
    }

    fun watchRewardedAdForTheme(activity: Activity, theme: AppThemeMode) {
        AdsManager.showRewardedAd(
            activity = activity,
            onUserEarnedReward = {
                settingsRepo.unlockTheme(theme.name)
                settingsRepo.setThemeMode(theme)
                Toast.makeText(app, "${theme.name.replace('_', ' ')} Theme Unlocked!", Toast.LENGTH_LONG).show()
            },
            onAdClosed = {}
        )
    }
}
