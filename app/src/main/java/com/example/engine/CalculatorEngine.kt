package com.example.engine

import com.example.data.AngleUnit
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

sealed class CalculationResult {
    data class Success(val value: Double, val formatted: String) : CalculationResult()
    data class Error(val message: String) : CalculationResult()
}

object CalculatorEngine {

    private const val PI_SYMBOL = "π"
    private const val E_SYMBOL = "e"
    private const val SQRT_SYMBOL = "√"
    private const val MULTIPLY_SYMBOL = "×"
    private const val DIVIDE_SYMBOL = "÷"
    private const val MINUS_SYMBOL = "−"
    private const val PLUS_SYMBOL = "+"

    fun calculate(expression: String, angleUnit: AngleUnit = AngleUnit.DEG, precision: Int = -1): CalculationResult {
        if (expression.isBlank()) {
            return CalculationResult.Success(0.0, "0")
        }

        val sanitized = sanitizeExpression(expression)

        return try {
            val tokens = tokenize(sanitized)
            val postfix = infixToPostfix(tokens)
            val result = evaluatePostfix(postfix, angleUnit)

            if (result.isNaN()) {
                CalculationResult.Error("Invalid calculation")
            } else if (result.isInfinite()) {
                CalculationResult.Error("Cannot divide by zero")
            } else {
                CalculationResult.Success(result, formatResult(result, precision))
            }
        } catch (e: ArithmeticException) {
            CalculationResult.Error(e.message ?: "Calculation error")
        } catch (e: Exception) {
            CalculationResult.Error("Invalid calculation")
        }
    }

    fun previewCalculate(expression: String, angleUnit: AngleUnit = AngleUnit.DEG, precision: Int = -1): String? {
        if (expression.isBlank()) return null
        val trimmed = expression.trim()
        val lastChar = trimmed.last()
        val candidate = if (lastChar in listOf('+', '−', '-', '×', '*', '÷', '/', '^', '(')) {
            return null
        } else {
            trimmed
        }

        // Count unbalanced parentheses and balance them for preview
        val openCount = candidate.count { it == '(' }
        val closeCount = candidate.count { it == ')' }
        val balanced = if (openCount > closeCount) {
            candidate + ")".repeat(openCount - closeCount)
        } else {
            candidate
        }

        val res = calculate(balanced, angleUnit, precision)
        return when (res) {
            is CalculationResult.Success -> res.formatted
            is CalculationResult.Error -> null
        }
    }

    fun formatResult(value: Double, precision: Int = -1): String {
        if (value.isInfinite() || value.isNaN()) return "Error"

        // Clean small epsilon rounding errors (e.g. 0.1 + 0.2 = 0.3)
        var cleanValue = value
        if (abs(cleanValue) < 1e-14) {
            cleanValue = 0.0
        } else if (abs(cleanValue - round(cleanValue)) < 1e-12) {
            cleanValue = round(cleanValue)
        }

        val absVal = abs(cleanValue)
        val isHuge = absVal >= 1e12 || (absVal > 0 && absVal < 1e-6)
        val symbols = DecimalFormatSymbols(Locale.US)

        if (isHuge) {
            val expFormat = DecimalFormat("0.######E0", symbols)
            return expFormat.format(cleanValue).replace("E", "e")
        }

        if (precision >= 0) {
            val pattern = if (precision == 0) "0" else "0." + "#".repeat(precision)
            val customFormat = DecimalFormat(pattern, symbols)
            val formatted = customFormat.format(cleanValue)
            return if (formatted == "-0") "0" else formatted
        }

        // Auto precision: up to 10 decimal digits without trailing zeros
        val rounded = BigDecimal(cleanValue.toString())
            .setScale(10, RoundingMode.HALF_UP)
            .stripTrailingZeros()

        val autoFormat = DecimalFormat("#,##0.##########", symbols)
        autoFormat.isGroupingUsed = false
        val formatted = autoFormat.format(rounded.toDouble())
        return if (formatted == "-0" || formatted == "-0.0") "0" else formatted
    }

    private fun sanitizeExpression(expr: String): String {
        return expr
            .replace(MULTIPLY_SYMBOL, "*")
            .replace(DIVIDE_SYMBOL, "/")
            .replace(MINUS_SYMBOL, "-")
            .replace(PI_SYMBOL, "(${Math.PI})")
            .replace(E_SYMBOL, "(${Math.E})")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("√", "sqrt")
            .replace(" ", "")
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val op: Char, val precedence: Int, val isRightAssociative: Boolean = false) : Token()
        data class Function(val name: String) : Token()
        data class PostfixOp(val op: Char) : Token()
        object LeftParen : Token()
        object RightParen : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectUnary = true

        while (i < expr.length) {
            val c = expr[i]

            when {
                c.isWhitespace() -> {
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        i++
                    }
                    val numStr = expr.substring(start, i)
                    val num = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
                    tokens.add(Token.Number(num))
                    expectUnary = false
                }
                c == '(' -> {
                    // Implicit multiplication: e.g. 5( -> 5*(
                    if (!expectUnary && tokens.isNotEmpty()) {
                        val last = tokens.last()
                        if (last is Token.Number || last is Token.RightParen || last is Token.PostfixOp) {
                            tokens.add(Token.Operator('*', 2))
                        }
                    }
                    tokens.add(Token.LeftParen)
                    expectUnary = true
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.RightParen)
                    expectUnary = false
                    i++
                }
                c == '!' -> {
                    tokens.add(Token.PostfixOp('!'))
                    expectUnary = false
                    i++
                }
                c == '%' -> {
                    tokens.add(Token.PostfixOp('%'))
                    expectUnary = false
                    i++
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '^' -> {
                    if (expectUnary && (c == '+' || c == '-')) {
                        // Unary plus or minus
                        if (c == '-') {
                            tokens.add(Token.Operator('u', 4, isRightAssociative = true))
                        }
                    } else {
                        val precedence = when (c) {
                            '+', '-' -> 1
                            '*', '/' -> 2
                            '^' -> 3
                            else -> 1
                        }
                        val rightAssoc = (c == '^')
                        tokens.add(Token.Operator(c, precedence, rightAssoc))
                        expectUnary = true
                    }
                    i++
                }
                c.isLetter() -> {
                    val start = i
                    while (i < expr.length && expr[i].isLetter()) {
                        i++
                    }
                    val funcName = expr.substring(start, i)
                    // Check implicit multiplication e.g. 2sin -> 2*sin
                    if (!expectUnary && tokens.isNotEmpty()) {
                        val last = tokens.last()
                        if (last is Token.Number || last is Token.RightParen || last is Token.PostfixOp) {
                            tokens.add(Token.Operator('*', 2))
                        }
                    }
                    tokens.add(Token.Function(funcName))
                    expectUnary = true
                }
                else -> {
                    i++
                }
            }
        }

        return tokens
    }

    private fun infixToPostfix(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> output.add(token)
                is Token.Function -> stack.addLast(token)
                is Token.PostfixOp -> output.add(token)
                is Token.Operator -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top is Token.Function) {
                            output.add(stack.removeLast())
                        } else if (top is Token.Operator) {
                            if ((!token.isRightAssociative && token.precedence <= top.precedence) ||
                                (token.isRightAssociative && token.precedence < top.precedence)
                            ) {
                                output.add(stack.removeLast())
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    stack.addLast(token)
                }
                is Token.LeftParen -> stack.addLast(token)
                is Token.RightParen -> {
                    var foundParen = false
                    while (stack.isNotEmpty()) {
                        val top = stack.removeLast()
                        if (top is Token.LeftParen) {
                            foundParen = true
                            break
                        } else {
                            output.add(top)
                        }
                    }
                    if (!foundParen) throw IllegalArgumentException("Mismatched parentheses")
                    if (stack.isNotEmpty() && stack.last() is Token.Function) {
                        output.add(stack.removeLast())
                    }
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top is Token.LeftParen || top is Token.RightParen) {
                throw IllegalArgumentException("Mismatched parentheses")
            }
            output.add(top)
        }

        return output
    }

    private data class StackValue(val num: Double, val isPercentage: Boolean = false)

    private fun evaluatePostfix(tokens: List<Token>, angleUnit: AngleUnit): Double {
        val stack = ArrayDeque<StackValue>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> stack.addLast(StackValue(token.value, false))
                is Token.Operator -> {
                    if (token.op == 'u') {
                        if (stack.isEmpty()) throw IllegalArgumentException("Invalid unary operator")
                        val a = stack.removeLast()
                        stack.addLast(StackValue(-a.num, false))
                    } else {
                        if (stack.size < 2) throw IllegalArgumentException("Invalid binary operator")
                        val b = stack.removeLast()
                        val a = stack.removeLast()
                        val result = when (token.op) {
                            '+' -> if (b.isPercentage) a.num + (a.num * b.num) else a.num + b.num
                            '-' -> if (b.isPercentage) a.num - (a.num * b.num) else a.num - b.num
                            '*' -> a.num * b.num
                            '/' -> {
                                if (b.num == 0.0) throw ArithmeticException("Cannot divide by zero")
                                a.num / b.num
                            }
                            '^' -> a.num.pow(b.num)
                            else -> throw IllegalArgumentException("Unknown operator: ${token.op}")
                        }
                        stack.addLast(StackValue(result, false))
                    }
                }
                is Token.PostfixOp -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid postfix operator")
                    val a = stack.removeLast()
                    when (token.op) {
                        '!' -> stack.addLast(StackValue(factorial(a.num), false))
                        '%' -> stack.addLast(StackValue(a.num / 100.0, true))
                        else -> throw IllegalArgumentException("Unknown postfix op: ${token.op}")
                    }
                }
                is Token.Function -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid function argument")
                    val arg = stack.removeLast()
                    val result = evaluateFunction(token.name, arg.num, angleUnit)
                    stack.addLast(StackValue(result, false))
                }
                else -> throw IllegalArgumentException("Unexpected token in postfix evaluation")
            }
        }

        if (stack.size != 1) throw IllegalArgumentException("Invalid expression evaluation")
        return stack.first().num
    }

    private fun evaluateFunction(name: String, arg: Double, angleUnit: AngleUnit): Double {
        return when (name.lowercase(Locale.ROOT)) {
            "sin" -> {
                if (angleUnit == AngleUnit.DEG) {
                    val normalizedDeg = ((arg % 360.0) + 360.0) % 360.0
                    when {
                        normalizedDeg == 0.0 || normalizedDeg == 180.0 || normalizedDeg == 360.0 -> 0.0
                        normalizedDeg == 90.0 -> 1.0
                        normalizedDeg == 270.0 -> -1.0
                        normalizedDeg == 30.0 || normalizedDeg == 150.0 -> 0.5
                        normalizedDeg == 210.0 || normalizedDeg == 330.0 -> -0.5
                        else -> sin(Math.toRadians(arg))
                    }
                } else {
                    sin(arg)
                }
            }
            "cos" -> {
                if (angleUnit == AngleUnit.DEG) {
                    val normalizedDeg = ((arg % 360.0) + 360.0) % 360.0
                    when {
                        normalizedDeg == 0.0 || normalizedDeg == 360.0 -> 1.0
                        normalizedDeg == 90.0 || normalizedDeg == 270.0 -> 0.0
                        normalizedDeg == 180.0 -> -1.0
                        normalizedDeg == 60.0 || normalizedDeg == 300.0 -> 0.5
                        normalizedDeg == 120.0 || normalizedDeg == 240.0 -> -0.5
                        else -> cos(Math.toRadians(arg))
                    }
                } else {
                    cos(arg)
                }
            }
            "tan" -> {
                if (angleUnit == AngleUnit.DEG) {
                    val normalizedDeg = ((arg % 360.0) + 360.0) % 360.0
                    if (normalizedDeg == 90.0 || normalizedDeg == 270.0) {
                        throw ArithmeticException("Invalid calculation")
                    }
                    when {
                        normalizedDeg == 0.0 || normalizedDeg == 180.0 || normalizedDeg == 360.0 -> 0.0
                        normalizedDeg == 45.0 || normalizedDeg == 225.0 -> 1.0
                        normalizedDeg == 135.0 || normalizedDeg == 315.0 -> -1.0
                        else -> tan(Math.toRadians(arg))
                    }
                } else {
                    tan(arg)
                }
            }
            "asin" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error for sin⁻¹")
                val rad = asin(arg)
                if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
            }
            "acos" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error for cos⁻¹")
                val rad = acos(arg)
                if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
            }
            "atan" -> {
                val rad = atan(arg)
                if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
            }
            "log" -> {
                if (arg <= 0.0) throw ArithmeticException("Domain error for log")
                log10(arg)
            }
            "ln" -> {
                if (arg <= 0.0) throw ArithmeticException("Domain error for ln")
                ln(arg)
            }
            "sqrt" -> {
                if (arg < 0.0) throw ArithmeticException("Cannot take square root of negative number")
                sqrt(arg)
            }
            "abs" -> abs(arg)
            else -> throw IllegalArgumentException("Unknown function: $name")
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n) || n > 170) {
            throw ArithmeticException("Factorial only defined for non-negative integers <= 170")
        }
        val intN = n.toInt()
        var result = 1.0
        for (i in 2..intN) {
            result *= i
        }
        return result
    }
}

