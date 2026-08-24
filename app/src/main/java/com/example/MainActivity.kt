package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ads.AdsManager
import com.example.ui.CalculatorScreen
import com.example.ui.MainCalculatorViewModel
import com.example.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainCalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsState()

            CalculatorTheme(themeMode = currentTheme) {
                CalculatorScreen(
                    viewModel = viewModel,
                    activity = this
                )
            }
        }

        // Centralized Consent & AdMob initialization after setting content
        AdsManager.requestConsentAndInit(this)
    }
}
