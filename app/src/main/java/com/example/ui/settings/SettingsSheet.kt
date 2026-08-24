package com.example.ui.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.AdsManager
import com.example.data.AppThemeMode
import com.example.ui.theme.LocalCalculatorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    currentTheme: AppThemeMode,
    hapticsEnabled: Boolean,
    decimalPrecision: Int,
    isAdFree: Boolean,
    adFreeUntil: Long,
    unlockedThemes: Set<String>,
    onThemeChange: (AppThemeMode) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onPrecisionChange: (Int) -> Unit,
    onWatchRewardedAdForAdFree: () -> Unit,
    onWatchRewardedAdForTheme: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val colors = LocalCalculatorColors.current
    val scrollState = rememberScrollState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrecisionDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.operatorKey.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = colors.equalsKey,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.title_settings),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.digitText
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.secondaryText
                    )
                }
            }

            HorizontalDivider(
                color = colors.functionKey.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Appearance & Themes
                Text(
                    text = "THEMES & APPEARANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.secondaryText.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                // Theme Chips / Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val themes = listOf(
                        AppThemeMode.DARK to "Sophisticated Dark",
                        AppThemeMode.LIGHT to "Modern Light",
                        AppThemeMode.SYSTEM to "System Default",
                        AppThemeMode.CYBER_NEON to "Cyber Neon",
                        AppThemeMode.SOLAR_AMBER to "Solar Amber",
                        AppThemeMode.NORDIC_MINT to "Nordic Mint"
                    )

                    themes.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { (mode, label) ->
                                val isSelected = currentTheme == mode
                                val isUnlocked = unlockedThemes.contains(mode.name)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) colors.operatorKey else colors.keypadSurface)
                                        .clickable {
                                            if (isUnlocked) {
                                                onThemeChange(mode)
                                            } else {
                                                onWatchRewardedAdForTheme(mode)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 12.dp)
                                        .testTag("theme_${mode.name.lowercase()}"),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = if (isSelected) colors.equalsKey else colors.digitText,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            if (!isUnlocked) {
                                                Text(
                                                    text = "Watch ad to unlock",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = colors.equalsKey,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = colors.equalsKey,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else if (!isUnlocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = colors.secondaryText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Monetization / Rewards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.keypadSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.operatorKey),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = colors.equalsKey,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isAdFree) "Ad-Free Active" else "Get 24h Ad-Free",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = colors.digitText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = if (isAdFree) {
                                        "Enjoy clean uninterrupted calculations"
                                    } else {
                                        "Watch 1 short rewarded ad for 24h ad-free"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = colors.secondaryText
                                    )
                                )
                            }
                        }

                        if (!isAdFree) {
                            TextButton(
                                onClick = onWatchRewardedAdForAdFree,
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = colors.operatorKey,
                                    contentColor = colors.equalsKey
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("rewarded_ad_free_button")
                            ) {
                                Text("Watch", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section: Calculation Settings
                Text(
                    text = "PREFERENCES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.secondaryText.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                // Haptic feedback toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.keypadSurface)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = colors.secondaryText
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.settings_haptics),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.digitText,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "Subtle vibration on button taps",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = colors.secondaryText
                                )
                            )
                        }
                    }

                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = onHapticsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.equalsText,
                            checkedTrackColor = colors.equalsKey,
                            uncheckedThumbColor = colors.secondaryText,
                            uncheckedTrackColor = colors.functionKey
                        ),
                        modifier = Modifier.testTag("haptics_switch")
                    )
                }

                // Decimal precision setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.keypadSurface)
                        .clickable { showPrecisionDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("precision_setting_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = null,
                            tint = colors.secondaryText
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.settings_precision),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.digitText,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = if (decimalPrecision < 0) "Auto (dynamic)" else "$decimalPrecision decimal places",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = colors.secondaryText
                                )
                            )
                        }
                    }

                    Text(
                        text = if (decimalPrecision < 0) "Auto" else "$decimalPrecision",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.equalsKey,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Section: Info & Legal
                Text(
                    text = "ABOUT & LEGAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.secondaryText.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.keypadSurface)
                ) {
                    SettingRowItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.settings_about),
                        subtitle = "Version 1.0.0 • Pure offline calculator",
                        onClick = { showAboutDialog = true }
                    )
                    HorizontalDivider(color = colors.functionKey.copy(alpha = 0.3f))
                    SettingRowItem(
                        icon = Icons.Default.Security,
                        title = stringResource(R.string.settings_privacy),
                        subtitle = "Zero tracking • Local storage only",
                        onClick = { showPrivacyDialog = true }
                    )
                    HorizontalDivider(color = colors.functionKey.copy(alpha = 0.3f))
                    SettingRowItem(
                        icon = Icons.Default.Policy,
                        title = stringResource(R.string.settings_terms),
                        subtitle = "License & terms of service",
                        onClick = { showTermsDialog = true }
                    )
                }
            }
        }
    }

    // Precision Selection Dialog
    if (showPrecisionDialog) {
        val precisions = listOf(-1 to "Auto (recommended)", 2 to "2 decimal places", 4 to "4 decimal places", 6 to "6 decimal places", 8 to "8 decimal places", 10 to "10 decimal places")
        AlertDialog(
            onDismissRequest = { showPrecisionDialog = false },
            title = {
                Text(
                    text = "Select Decimal Precision",
                    color = colors.digitText,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    precisions.forEach { (prec, label) ->
                        val isSelected = decimalPrecision == prec
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.operatorKey.copy(alpha = 0.4f) else Color.Transparent)
                                .clickable {
                                    onPrecisionChange(prec)
                                    showPrecisionDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) colors.equalsKey else colors.digitText,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.equalsKey,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            containerColor = colors.cardSurface,
            confirmButton = {
                TextButton(
                    onClick = { showPrecisionDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.equalsKey)
                ) {
                    Text("Done")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "Calculator",
                    color = colors.digitText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "A modern, minimalist, and ultra-fast scientific calculator designed for Android.\n\n" +
                            "• Instant startup & smooth 60fps animations\n" +
                            "• Operator precedence & scientific functions\n" +
                            "• Offline calculation history with Room database\n" +
                            "• Non-intrusive Google AdMob integration\n" +
                            "• Version 1.0.0",
                    color = colors.secondaryText,
                    fontSize = 14.sp
                )
            },
            containerColor = colors.cardSurface,
            confirmButton = {
                TextButton(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.equalsKey)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "Privacy Policy",
                    color = colors.digitText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "We value your privacy:\n\n" +
                            "1. All calculations, history, and settings are stored locally on your device.\n" +
                            "2. No user accounts, personal data, or calculation inputs are collected or transmitted.\n" +
                            "3. Google AdMob serves banner and non-intrusive rewarded/interstitial ads complying with Google Play policies and User Messaging Platform (UMP) consent guidelines.\n" +
                            "4. The calculator engine works 100% offline.",
                    color = colors.secondaryText,
                    fontSize = 14.sp
                )
            },
            containerColor = colors.cardSurface,
            confirmButton = {
                TextButton(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.equalsKey)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Terms of Service Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "Terms of Service",
                    color = colors.digitText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Calculator is provided for general mathematical and scientific calculation purposes.\n\n" +
                            "• Free to use with optional rewarded ad unlocks for ad-free experience.\n" +
                            "• Built with high precision numerical algorithms and safety protection against division by zero and domain overflow.",
                    color = colors.secondaryText,
                    fontSize = 14.sp
                )
            },
            containerColor = colors.cardSurface,
            confirmButton = {
                TextButton(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.equalsKey)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SettingRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.digitText,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.secondaryText
                )
            )
        }
    }
}
