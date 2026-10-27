package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StudyViewModel
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.StudyChatScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme(themePreference = uiState.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = uiState.hasValidKey,
                        label = "ScreenTransition"
                    ) { hasKey ->
                        if (hasKey) {
                            StudyChatScreen(viewModel = viewModel)
                        } else {
                            OnboardingScreen(
                                onVerifyAndSaveKey = { key, onResult ->
                                    viewModel.verifyAndSaveApiKey(key, onResult)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
