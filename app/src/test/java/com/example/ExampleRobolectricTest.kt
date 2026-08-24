package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AngleUnit
import com.example.engine.CalculationResult
import com.example.engine.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Calculator", appName)
  }

  @Test
  fun `test calculator precedence and scientific calculations`() {
    // 10 + 5 * 2 = 20
    val res1 = CalculatorEngine.calculate("10+5×2")
    assertTrue(res1 is CalculationResult.Success)
    assertEquals("20", (res1 as CalculationResult.Success).formatted)

    // sin(30) in DEG = 0.5
    val res2 = CalculatorEngine.calculate("sin(30)", AngleUnit.DEG)
    assertTrue(res2 is CalculationResult.Success)
    assertEquals("0.5", (res2 as CalculationResult.Success).formatted)

    // cos(90) in DEG = 0
    val resCos = CalculatorEngine.calculate("cos(90)", AngleUnit.DEG)
    assertTrue(resCos is CalculationResult.Success)
    assertEquals("0", (resCos as CalculationResult.Success).formatted)

    // tan(45) in DEG = 1
    val resTan = CalculatorEngine.calculate("tan(45)", AngleUnit.DEG)
    assertTrue(resTan is CalculationResult.Success)
    assertEquals("1", (resTan as CalculationResult.Success).formatted)

    // Percentage: 100 + 10% = 110
    val resPctAdd = CalculatorEngine.calculate("100+10%")
    assertTrue(resPctAdd is CalculationResult.Success)
    assertEquals("110", (resPctAdd as CalculationResult.Success).formatted)

    // Percentage: 100 - 20% = 80
    val resPctSub = CalculatorEngine.calculate("100−20%")
    assertTrue(resPctSub is CalculationResult.Success)
    assertEquals("80", (resPctSub as CalculationResult.Success).formatted)

    // Percentage: 50 * 10% = 5
    val resPctMul = CalculatorEngine.calculate("50×10%")
    assertTrue(resPctMul is CalculationResult.Success)
    assertEquals("5", (resPctMul as CalculationResult.Success).formatted)

    // Factorial: 5! = 120
    val resFact = CalculatorEngine.calculate("5!")
    assertTrue(resFact is CalculationResult.Success)
    assertEquals("120", (resFact as CalculationResult.Success).formatted)

    // Precision check: 0.1 + 0.2 = 0.3
    val resPrec = CalculatorEngine.calculate("0.1+0.2")
    assertTrue(resPrec is CalculationResult.Success)
    assertEquals("0.3", (resPrec as CalculationResult.Success).formatted)

    // Implicit multiplication: 2(3+4) = 14
    val resImplicit = CalculatorEngine.calculate("2(3+4)")
    assertTrue(resImplicit is CalculationResult.Success)
    assertEquals("14", (resImplicit as CalculationResult.Success).formatted)

    // Division by zero safe handling
    val res3 = CalculatorEngine.calculate("10÷0")
    assertTrue(res3 is CalculationResult.Error)
    assertEquals("Cannot divide by zero", (res3 as CalculationResult.Error).message)
  }
}


