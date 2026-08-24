package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AngleUnit
import com.example.ui.theme.LocalCalculatorColors

@Composable
fun ScientificKeypad(
    angleUnit: AngleUnit,
    onToggleAngleUnit: () -> Unit,
    onFunctionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    var isInverseMode by remember { mutableStateOf(false) }

    val btnHeight = 44.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: DEG/RAD, 2nd (Inv), sin, cos, tan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = angleUnit.name,
                onClick = onToggleAngleUnit,
                backgroundColor = colors.operatorKey.copy(alpha = 0.7f),
                textColor = colors.operatorText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_deg_rad"
            )
            CalculatorButton(
                text = if (isInverseMode) "1st" else "2nd",
                onClick = { isInverseMode = !isInverseMode },
                backgroundColor = if (isInverseMode) colors.equalsKey else colors.functionKey,
                textColor = if (isInverseMode) colors.equalsText else colors.functionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_inv"
            )
            CalculatorButton(
                text = if (isInverseMode) "sin⁻¹" else "sin",
                onClick = { onFunctionClick(if (isInverseMode) "asin(" else "sin(") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_sin"
            )
            CalculatorButton(
                text = if (isInverseMode) "cos⁻¹" else "cos",
                onClick = { onFunctionClick(if (isInverseMode) "acos(" else "cos(") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_cos"
            )
            CalculatorButton(
                text = if (isInverseMode) "tan⁻¹" else "tan",
                onClick = { onFunctionClick(if (isInverseMode) "atan(" else "tan(") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_tan"
            )
        }

        // Row 2: ln, log, √, x², xʸ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "ln",
                onClick = { onFunctionClick("ln(") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 14.sp,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_ln"
            )
            CalculatorButton(
                text = "log",
                onClick = { onFunctionClick("log(") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 14.sp,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_log"
            )
            CalculatorButton(
                text = "√",
                onClick = { onFunctionClick("√(") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 16.sp,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_sqrt"
            )
            CalculatorButton(
                text = "x²",
                onClick = { onFunctionClick("^2") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 14.sp,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_square"
            )
            CalculatorButton(
                text = "xʸ",
                onClick = { onFunctionClick("^") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 14.sp,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_power"
            )
        }

        // Row 3: π, e, x!, (, )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "π",
                onClick = { onFunctionClick("π") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 16.sp,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_pi"
            )
            CalculatorButton(
                text = "e",
                onClick = { onFunctionClick("e") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 16.sp,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_e"
            )
            CalculatorButton(
                text = "x!",
                onClick = { onFunctionClick("!") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 14.sp,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_factorial"
            )
            CalculatorButton(
                text = "(",
                onClick = { onFunctionClick("(") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 16.sp,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_open_paren"
            )
            CalculatorButton(
                text = ")",
                onClick = { onFunctionClick(")") },
                backgroundColor = colors.functionKey,
                textColor = colors.functionText,
                fontSize = 16.sp,
                cornerRadius = 16.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight),
                testTag = "btn_close_paren"
            )
        }
    }
}
