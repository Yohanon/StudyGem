package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.GeminiModel
import com.example.ui.theme.BrandGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTopBar(
    currentModel: GeminiModel,
    onModelChange: (GeminiModel) -> Unit,
    themeMode: String,
    onThemeToggle: () -> Unit,
    onOpenApiKeyDialog: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_study_gem),
                    contentDescription = "StudyGem Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Column {
                    Text(
                        text = "StudyGem",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = "AI Study Assistant",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        },
        actions = {
            // Model Selector Pill (Flash vs Pro toggle)
            Box {
                Surface(
                    onClick = { modelDropdownExpanded = true },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("model_selector_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentModel.shortName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Gemini Model",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = modelDropdownExpanded,
                    onDismissRequest = { modelDropdownExpanded = false }
                ) {
                    GeminiModel.entries.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = model.displayName,
                                        fontWeight = if (currentModel == model) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = model.tag,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onModelChange(model)
                                modelDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Theme Toggle (Light <-> Dark <-> System)
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = when (themeMode) {
                        "DARK" -> Icons.Default.DarkMode
                        "LIGHT" -> Icons.Default.LightMode
                        else -> Icons.Default.SettingsBrightness
                    },
                    contentDescription = "Toggle Light/Dark Theme"
                )
            }

            // Key Icon to view or edit API key
            IconButton(
                onClick = onOpenApiKeyDialog,
                modifier = Modifier.testTag("api_key_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Manage API Key",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Settings Icon
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("settings_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}
