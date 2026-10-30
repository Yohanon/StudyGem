package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StudySection
import com.example.ui.StudyViewModel
import com.example.ui.components.*

@Composable
fun StudyChatScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Show snackbar when message changes
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StudyTopBar(
                currentModel = uiState.selectedModel,
                onModelChange = { viewModel.setModel(it) },
                themeMode = uiState.themeMode,
                onThemeToggle = {
                    val next = when (uiState.themeMode) {
                        "SYSTEM" -> "DARK"
                        "DARK" -> "LIGHT"
                        else -> "SYSTEM"
                    }
                    viewModel.setThemeMode(next)
                },
                onOpenApiKeyDialog = { viewModel.openApiKeyDialog() },
                onOpenSettings = { viewModel.openSettings() }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.navigationBarsPadding()) {
                ChatInputArea(
                    prompt = uiState.inputPrompt,
                    onPromptChange = { viewModel.setPrompt(it) },
                    onSend = { viewModel.sendMessage() },
                    isGenerating = uiState.isGenerating,
                    attachedImageUri = uiState.selectedImageUri,
                    onImageSelected = { viewModel.onImagePicked(it) },
                    onRemoveImage = { viewModel.removeImage() },
                    suggestedPrompts = uiState.suggestedPrompts,
                    onSelectSuggestedPrompt = { viewModel.onPromptSelected(it) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Setup Banner if API key is missing
            if (uiState.apiKey.isBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { viewModel.openApiKeyDialog() }
                        .testTag("missing_api_key_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Setup Required: Google AI Studio API Key",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Enter your Google AI Studio API Key to get started (Get one free at aistudio.google.com). Tap here to setup.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Subject Selector (Coding, Math, Science, Misc + Subtopics)
            SubjectSelector(
                selectedSection = uiState.studySection,
                selectedMisc = uiState.miscCategory,
                onSectionSelected = { viewModel.setStudySection(it) },
                onMiscSelected = { viewModel.setMiscCategory(it) }
            )

            // Explain Toggle Button (Off: extremely short max 1-2 paragraphs, On: focused explanation)
            ExplainToggle(
                isExplainMode = uiState.isExplainMode,
                onToggleExplain = { viewModel.setExplainMode(it) }
            )

            // Guardrail Status Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.overrideGuardrails) Icons.Default.LockOpen else Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (uiState.overrideGuardrails) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (uiState.overrideGuardrails)
                            "Guardrail Override: ON (Open Conversation)"
                        else
                            "Study Guardrails: ACTIVE (Strictly Study Only)",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = if (uiState.overrideGuardrails) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = uiState.selectedModel.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("chat_messages_list")
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Modal Dialogs
    if (uiState.showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = uiState.apiKey,
            onSave = { viewModel.saveApiKey(it) },
            onDismiss = { viewModel.dismissApiKeyDialog() },
            canDismiss = uiState.apiKey.isNotBlank(),
            onTestKey = { viewModel.testApiKey(it) },
            isTestingKey = uiState.isTestingApiKey,
            testResult = uiState.apiKeyTestResult,
            isKeyValid = uiState.isApiKeyValid
        )
    }

    if (uiState.showSettingsDialog) {
        SettingsDialog(
            apiKey = uiState.apiKey,
            themeMode = uiState.themeMode,
            selectedModel = uiState.selectedModel,
            overrideGuardrails = uiState.overrideGuardrails,
            onEditApiKey = {
                viewModel.dismissSettings()
                viewModel.openApiKeyDialog()
            },
            onTestApiKey = { viewModel.testApiKey(it) },
            isTestingKey = uiState.isTestingApiKey,
            testResult = uiState.apiKeyTestResult,
            isKeyValid = uiState.isApiKeyValid,
            onRemoveApiKey = {
                viewModel.removeApiKey()
                viewModel.dismissSettings()
            },
            onThemeChange = { viewModel.setThemeMode(it) },
            onModelChange = { viewModel.setModel(it) },
            onOverrideChange = { viewModel.setOverrideGuardrails(it) },
            onClearHistory = {
                viewModel.clearHistory()
                viewModel.dismissSettings()
            },
            onDismiss = { viewModel.dismissSettings() }
        )
    }
}
