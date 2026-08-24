package com.example.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobBanner
import com.example.ui.components.CalculatorButton
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.HapticHelper
import com.example.ui.components.ScientificKeypad
import com.example.ui.history.HistorySheet
import com.example.ui.settings.SettingsSheet
import com.example.ui.theme.LocalCalculatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: MainCalculatorViewModel,
    activity: Activity?
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colors = LocalCalculatorColors.current

    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val hapticsEnabled by viewModel.hapticFeedbackEnabled.collectAsState()
    val angleUnit by viewModel.angleUnit.collectAsState()
    val decimalPrecision by viewModel.decimalPrecision.collectAsState()
    val adFreeUntil by viewModel.adFreeUntil.collectAsState()
    val unlockedThemes by viewModel.unlockedThemes.collectAsState()

    val isAdFree = adFreeUntil > System.currentTimeMillis()

    val onButtonTap: () -> Unit = {
        HapticHelper.performHapticFeedback(context, hapticsEnabled, view)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand logo & title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.operatorKey),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = colors.equalsKey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "CALCULATOR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 14.sp,
                            color = colors.digitText.copy(alpha = 0.85f)
                        )
                    )
                }

                // Action buttons (Scientific pill, History, Settings)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Scientific Mode Toggle Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (uiState.isScientificMode) colors.equalsKey else colors.functionKey)
                            .clickable {
                                onButtonTap()
                                viewModel.toggleScientificMode()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("toggle_scientific_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = "Scientific Mode",
                                tint = if (uiState.isScientificMode) colors.equalsText else colors.functionText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (uiState.isScientificMode) angleUnit.name else "SCI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (uiState.isScientificMode) colors.equalsText else colors.functionText
                                )
                            )
                        }
                    }

                    // History Button
                    IconButton(
                        onClick = {
                            onButtonTap()
                            viewModel.setHistorySheetVisible(true)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.functionKey.copy(alpha = 0.5f))
                            .testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = colors.functionText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = {
                            onButtonTap()
                            viewModel.setSettingsSheetVisible(true)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.functionKey.copy(alpha = 0.5f))
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colors.functionText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Calculation Display
            CalculatorDisplay(
                expression = uiState.expression,
                previewResult = uiState.previewResult,
                finalResult = uiState.finalResult,
                isCalculated = uiState.isCalculated,
                errorMessage = uiState.errorMessage,
                onCopyResult = {
                    onButtonTap()
                    val target = if (uiState.isCalculated) uiState.finalResult else (uiState.previewResult ?: uiState.finalResult)
                    viewModel.copyToClipboard(target)
                },
                onCopyExpression = {
                    onButtonTap()
                    if (uiState.expression.isNotEmpty()) {
                        viewModel.copyToClipboard(uiState.expression)
                    }
                },
                onShareCalculation = {
                    onButtonTap()
                    val target = if (uiState.isCalculated) uiState.finalResult else (uiState.previewResult ?: uiState.finalResult)
                    viewModel.shareCalculation(uiState.expression, target)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // Keypad Surface (Sophisticated Rounded Panel)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = colors.keypadSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Expandable Scientific Keypad
                    AnimatedVisibility(
                        visible = uiState.isScientificMode,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        ScientificKeypad(
                            angleUnit = angleUnit,
                            onToggleAngleUnit = {
                                onButtonTap()
                                viewModel.toggleAngleUnit()
                            },
                            onFunctionClick = { func ->
                                onButtonTap()
                                viewModel.onFunction(func)
                            },
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Main 4x5 Keypad Grid
                    MainKeypad(
                        onDigitClick = { digit ->
                            onButtonTap()
                            viewModel.onDigit(digit)
                        },
                        onDecimalClick = {
                            onButtonTap()
                            viewModel.onDecimal()
                        },
                        onOperatorClick = { op ->
                            onButtonTap()
                            viewModel.onOperator(op)
                        },
                        onPercentClick = {
                            onButtonTap()
                            viewModel.onPercentage()
                        },
                        onClearClick = {
                            onButtonTap()
                            viewModel.onClear()
                        },
                        onDeleteClick = {
                            onButtonTap()
                            viewModel.onDelete()
                        },
                        onEqualsClick = {
                            onButtonTap()
                            viewModel.onEquals(activity)
                        }
                    )
                }
            }

            // Bottom Adaptive AdMob Banner
            AdMobBanner(
                modifier = Modifier.fillMaxWidth(),
                isAdFree = isAdFree
            )
        }
    }

    // Bottom Sheets
    if (uiState.showHistorySheet) {
        HistorySheet(
            historyList = historyList,
            onSelectHistory = { item ->
                onButtonTap()
                viewModel.selectHistoryItem(item)
            },
            onDeleteHistory = { item ->
                onButtonTap()
                viewModel.deleteHistoryItem(item)
            },
            onClearAll = {
                onButtonTap()
                viewModel.clearAllHistory()
            },
            onCopy = { res ->
                onButtonTap()
                viewModel.copyToClipboard(res)
            },
            onShare = { expr, res ->
                onButtonTap()
                viewModel.shareCalculation(expr, res)
            },
            onDismiss = {
                viewModel.setHistorySheetVisible(false)
            }
        )
    }

    if (uiState.showSettingsSheet) {
        SettingsSheet(
            currentTheme = currentTheme,
            hapticsEnabled = hapticsEnabled,
            decimalPrecision = decimalPrecision,
            isAdFree = isAdFree,
            adFreeUntil = adFreeUntil,
            unlockedThemes = unlockedThemes,
            onThemeChange = { theme ->
                onButtonTap()
                viewModel.setTheme(theme)
            },
            onHapticsToggle = { enabled ->
                viewModel.setHaptics(enabled)
            },
            onPrecisionChange = { prec ->
                onButtonTap()
                viewModel.setPrecision(prec)
            },
            onWatchRewardedAdForAdFree = {
                if (activity != null) {
                    viewModel.watchRewardedAdForAdFree(activity)
                }
            },
            onWatchRewardedAdForTheme = { theme ->
                if (activity != null) {
                    viewModel.watchRewardedAdForTheme(activity, theme)
                }
            },
            onDismiss = {
                viewModel.setSettingsSheetVisible(false)
            }
        )
    }
}

@Composable
fun MainKeypad(
    onDigitClick: (String) -> Unit,
    onDecimalClick: () -> Unit,
    onOperatorClick: (String) -> Unit,
    onPercentClick: () -> Unit,
    onClearClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEqualsClick: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    val rowSpacing = 10.dp
    val colSpacing = 10.dp
    val buttonHeight = 64.dp
    val cornerRadius = 32.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        // Row 1: AC, ⌫, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "AC",
                onClick = onClearClick,
                backgroundColor = colors.functionKey,
                textColor = colors.clearText,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_clear"
            )
            CalculatorButton(
                onClick = onDeleteClick,
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_backspace"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = colors.functionText,
                    modifier = Modifier.size(24.dp)
                )
            }
            CalculatorButton(
                text = "%",
                onClick = onPercentClick,
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_percent"
            )
            CalculatorButton(
                text = "÷",
                onClick = { onOperatorClick("÷") },
                backgroundColor = colors.operatorKey,
                textColor = colors.operatorText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_divide"
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "7",
                onClick = { onDigitClick("7") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 26.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_7"
            )
            CalculatorButton(
                text = "8",
                onClick = { onDigitClick("8") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 26.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_8"
            )
            CalculatorButton(
                text = "9",
                onClick = { onDigitClick("9") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 26.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_9"
            )
            CalculatorButton(
                text = "×",
                onClick = { onOperatorClick("×") },
                backgroundColor = colors.operatorKey,
                textColor = colors.operatorText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_multiply"
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "4",
                onClick = { onDigitClick("4") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 26.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_4"
            )
            CalculatorButton(
                text = "5",
                onClick = { onDigitClick("5") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 26.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_5"
            )
            CalculatorButton(
                text = "6",
                onClick = { onDigitClick("6") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 26.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_6"
            )
            CalculatorButton(
                text = "−",
                onClick = { onOperatorClick("−") },
                backgroundColor = colors.operatorKey,
                textColor = colors.operatorText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_subtract"
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "1",
                onClick = { onDigitClick("1") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 26.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_1"
            )
            CalculatorButton(
                text = "2",
                onClick = { onDigitClick("2") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 26.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_2"
            )
            CalculatorButton(
                text = "3",
                onClick = { onDigitClick("3") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 26.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_3"
            )
            CalculatorButton(
                text = "+",
                onClick = { onOperatorClick("+") },
                backgroundColor = colors.operatorKey,
                textColor = colors.operatorText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_add"
            )
        }

        // Row 5: 00, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(colSpacing)
        ) {
            CalculatorButton(
                text = "00",
                onClick = { onDigitClick("00") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 22.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_00"
            )
            CalculatorButton(
                text = "0",
                onClick = { onDigitClick("0") },
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 26.sp,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_0"
            )
            CalculatorButton(
                text = ".",
                onClick = onDecimalClick,
                backgroundColor = colors.digitKey,
                textColor = colors.digitText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_dot"
            )
            CalculatorButton(
                text = "=",
                onClick = onEqualsClick,
                backgroundColor = colors.equalsKey,
                textColor = colors.equalsText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                cornerRadius = cornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                testTag = "btn_equals"
            )
        }
    }
}
