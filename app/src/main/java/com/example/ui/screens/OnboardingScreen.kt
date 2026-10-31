package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandNavy

@Composable
fun OnboardingScreen(
    onVerifyAndSaveKey: (String, (Boolean, String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    }

    var keyInput by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<String?>(null) }
    var isVerificationSuccess by remember { mutableStateOf<Boolean?>(null) }

    fun cleanKey(raw: String): String {
        return raw.trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .removeSurrounding("`")
            .replace(" ", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace("\t", "")
            .trim()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Hero Graphic
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BrandNavy,
                shadowElevation = 6.dp,
                modifier = Modifier.size(96.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_study_gem),
                    contentDescription = "StudyGem Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            // Title & Subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome to StudyGem",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your private, AI-powered study mentor for Coding, Math, Science & Academics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // BYOK Educational Explanation Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Why Do You Need a Gemini Key?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "StudyGem connects directly to Google's cutting-edge Gemini models to explain complex concepts, solve problems step-by-step, and analyze homework diagrams.\n\nBy bringing your own free Google AI Studio key, you get fast, unlimited study sessions with zero third-party ads or subscriptions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Encrypted on-device using Jetpack MasterKey (AES256_GCM). Never leaves your phone.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Direct External Link Button: "Get Free Gemini Key"
            OutlinedButton(
                onClick = {
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://aistudio.google.com/app/apikey")
                    )
                    context.startActivity(browserIntent)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("get_free_key_button")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Get Free Gemini Key (aistudio.google.com)",
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Key Input & Validation Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter Your API Key",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = {
                            keyInput = it
                            // Reset test status on modification
                            isVerificationSuccess = null
                            verificationResult = null
                        },
                        label = { Text("Google AI Studio Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_api_key_input"),
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isKeyVisible) "Hide key" else "Show key"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipData = clipboardManager?.primaryClip
                                        if (clipData != null && clipData.itemCount > 0) {
                                            val pasteText = clipData.getItemAt(0).text?.toString() ?: ""
                                            if (pasteText.isNotBlank()) {
                                                keyInput = cleanKey(pasteText)
                                                isVerificationSuccess = null
                                                verificationResult = null
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste from clipboard"
                                    )
                                }
                            }
                        }
                    )

                    // Format guidance tip
                    val sanitized = cleanKey(keyInput)
                    if (sanitized.isNotBlank() && !sanitized.startsWith("AIzaSy")) {
                        Text(
                            text = "💡 Google AI Studio keys typically begin with 'AIzaSy'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }

                    // Live Verification Feedback Banner
                    AnimatedVisibility(
                        visible = verificationResult != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (isVerificationSuccess) {
                                true -> MaterialTheme.colorScheme.primaryContainer
                                false -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isVerificationSuccess == true) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (isVerificationSuccess == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = verificationResult ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isVerificationSuccess == true) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // "Verify & Start Studying" Primary Action Button
                    Button(
                        onClick = {
                            val keyToVerify = cleanKey(keyInput)
                            if (keyToVerify.isBlank()) return@Button

                            isVerifying = true
                            verificationResult = "Validating key with Gemini 3.8 Flash..."
                            isVerificationSuccess = null

                            onVerifyAndSaveKey(keyToVerify) { success, message ->
                                isVerifying = false
                                isVerificationSuccess = success
                                verificationResult = message
                            }
                        },
                        enabled = sanitized.isNotBlank() && !isVerifying,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_and_start_button")
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Verifying with Gemini 3.8 Flash...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verify & Start Studying",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
