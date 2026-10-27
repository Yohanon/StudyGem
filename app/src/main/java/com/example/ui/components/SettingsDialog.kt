package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeminiModel

@Composable
fun SettingsDialog(
    apiKey: String,
    themeMode: String,
    selectedModel: GeminiModel,
    overrideGuardrails: Boolean,
    onEditApiKey: () -> Unit,
    onTestApiKey: (String) -> Unit = {},
    isTestingKey: Boolean = false,
    testResult: String? = null,
    isKeyValid: Boolean? = null,
    onRemoveApiKey: () -> Unit = {},
    onThemeChange: (String) -> Unit,
    onModelChange: (GeminiModel) -> Unit,
    onOverrideChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var isKeyRevealed by remember { mutableStateOf(false) }

    val displayKey = remember(apiKey, isKeyRevealed) {
        if (apiKey.isBlank()) {
            "No key configured"
        } else if (isKeyRevealed) {
            apiKey
        } else {
            if (apiKey.length > 8) {
                "${apiKey.take(6)}••••••••••••••••••••${apiKey.takeLast(4)}"
            } else {
                "••••••••••••"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Study Settings", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: BYOK Secure Key Inspector & Actions
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Personal Gemini API Key",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (apiKey.isNotBlank()) {
                                IconButton(
                                    onClick = { isKeyRevealed = !isKeyRevealed },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isKeyRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isKeyRevealed) "Hide key" else "Reveal key",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = displayKey,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (apiKey.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )

                        Text(
                            text = "Stored with AES256_GCM hardware-backed encryption.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )

                        // Test Status result banner if tested
                        if (testResult != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (isKeyValid) {
                                    true -> MaterialTheme.colorScheme.primaryContainer
                                    false -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = testResult,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp),
                                    color = when (isKeyValid) {
                                        true -> MaterialTheme.colorScheme.onPrimaryContainer
                                        false -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        // Actions Row: Test Key & Update Key
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onTestApiKey(apiKey) },
                                enabled = apiKey.isNotBlank() && !isTestingKey,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_key_settings_button")
                            ) {
                                if (isTestingKey) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Testing...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Key", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = onEditApiKey,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("update_key_settings_button")
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Update", fontSize = 12.sp)
                            }
                        }

                        // Remove / Disconnect Key
                        if (apiKey.isNotBlank()) {
                            TextButton(
                                onClick = onRemoveApiKey,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove Key & Log Out", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Section 2: Study Guardrail & Override Option (OFF by default)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (overrideGuardrails)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (overrideGuardrails) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (overrideGuardrails) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Override Study Guardrails",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (overrideGuardrails) "Override Active (Open Chat)" else "Guardrails Locked (Study Only)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (overrideGuardrails) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Switch(
                                checked = overrideGuardrails,
                                onCheckedChange = onOverrideChange,
                                modifier = Modifier.testTag("guardrail_override_switch")
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (overrideGuardrails)
                                "⚠️ Guardrails override is ON. The AI is permitted to answer questions outside academic boundaries."
                            else
                                "🛡️ Strict Academic Guardrails are ACTIVE (default). All non-study or off-topic queries will be automatically declined.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Section 3: Model Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gemini Model",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    GeminiModel.entries.forEach { model ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onModelChange(model) }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            RadioButton(
                                selected = selectedModel == model,
                                onClick = { onModelChange(model) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = model.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selectedModel == model) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = model.tag,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Section 4: Theme Mode
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("SYSTEM", "System", Icons.Default.SettingsBrightness),
                            Triple("LIGHT", "Light", Icons.Default.LightMode),
                            Triple("DARK", "Dark", Icons.Default.DarkMode)
                        ).forEach { (mode, label, icon) ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { onThemeChange(mode) },
                                label = { Text(label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Section 5: Clear History Action
                OutlinedButton(
                    onClick = onClearHistory,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Current Study Session")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
