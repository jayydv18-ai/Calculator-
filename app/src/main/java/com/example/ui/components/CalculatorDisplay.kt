package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCalculatorColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorDisplay(
    expression: String,
    previewResult: String?,
    finalResult: String,
    isCalculated: Boolean,
    errorMessage: String?,
    onCopyResult: () -> Unit,
    onCopyExpression: () -> Unit,
    onShareCalculation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val scrollState = rememberScrollState()

    // Auto-scroll expression to end when expression changes
    LaunchedEffect(expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Quick Action Bar (Copy & Share buttons when there is content)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = expression.isNotEmpty() || finalResult != "0",
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onCopyResult,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("copy_result_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result",
                            tint = colors.secondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onShareCalculation,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_calculation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share calculation",
                            tint = colors.secondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Expression display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = if (expression.isEmpty() && !isCalculated) "" else expression,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = if (isCalculated) 22.sp else 28.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp,
                    color = colors.secondaryText
                ),
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.testTag("expression_display")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Result / Preview Display
        val displayText = when {
            errorMessage != null -> errorMessage
            isCalculated -> finalResult
            previewResult != null -> previewResult
            expression.isNotEmpty() -> ""
            else -> "0"
        }

        val displayColor = when {
            errorMessage != null -> colors.clearText
            isCalculated -> colors.digitText
            previewResult != null -> colors.secondaryText.copy(alpha = 0.7f)
            else -> colors.digitText
        }

        // Adjust font size dynamically based on length
        val fontSize = when {
            errorMessage != null -> 26.sp
            displayText.length > 14 -> 34.sp
            displayText.length > 9 -> 44.sp
            displayText.length > 6 -> 54.sp
            else -> 64.sp
        }

        Text(
            text = displayText,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1.5).sp,
                color = displayColor
            ),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onCopyResult() },
                    onLongClick = { onCopyResult() }
                )
                .testTag("result_display")
        )
    }
}
