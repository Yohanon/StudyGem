package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AiMode
import com.example.data.model.ChatMessage
import com.example.data.model.GeminiModel
import com.example.data.model.MiscSubCategory
import com.example.data.model.StudySection
import com.example.data.model.StudySessionConfig
import com.example.data.preferences.SecurePreferencesManager
import com.example.data.repository.StudyRepository
import com.example.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudyUiState(
    val apiKey: String = "",
    val hasValidKey: Boolean = false,
    val showApiKeyDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val themeMode: String = "SYSTEM",
    val selectedModel: GeminiModel = GeminiModel.FLASH_3_8,
    val aiMode: AiMode = AiMode.LEARN,
    val overrideGuardrails: Boolean = false,
    val studySection: StudySection = StudySection.CODING,
    val miscCategory: MiscSubCategory = MiscSubCategory.RESEARCH,
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val inputPrompt: String = "",
    val selectedImageUri: Uri? = null,
    val selectedImageBase64: String? = null,
    val selectedImageMimeType: String? = null,
    val suggestedPrompts: List<String> = emptyList(),
    val snackbarMessage: String? = null,
    val isTestingApiKey: Boolean = false,
    val apiKeyTestResult: String? = null,
    val isApiKeyValid: Boolean? = null
)

class StudyViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = SecurePreferencesManager(application)
    private val repository = StudyRepository()

    private val _uiState = MutableStateFlow(
        StudyUiState(
            apiKey = prefs.getApiKey(),
            hasValidKey = prefs.hasValidApiKey(),
            showApiKeyDialog = false,
            themeMode = prefs.getThemeMode(),
            selectedModel = prefs.getSelectedModel(),
            aiMode = prefs.getAiMode(),
            overrideGuardrails = prefs.isGuardrailsOverridden(),
            studySection = prefs.getStudySection(),
            miscCategory = prefs.getMiscCategory()
        )
    )
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentMessages.collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }

        viewModelScope.launch {
            repository.isGenerating.collect { generating ->
                _uiState.update { it.copy(isGenerating = generating) }
            }
        }

        val initialSection = prefs.getStudySection()
        val initialMisc = prefs.getMiscCategory()
        repository.switchSubject(initialSection, initialMisc)
        updateSuggestedPrompts(initialSection, initialMisc)
    }

    private fun updateSuggestedPrompts(section: StudySection, misc: MiscSubCategory) {
        val prompts = repository.getSuggestedPrompts(section, misc)
        _uiState.update { it.copy(suggestedPrompts = prompts) }
    }

    fun verifyAndSaveApiKey(rawKey: String, onComplete: (Boolean, String) -> Unit) {
        val clean = prefs.sanitizeKey(rawKey)
        if (clean.isBlank()) {
            onComplete(false, "Please enter your Google AI Studio API key.")
            return
        }

        viewModelScope.launch {
            val result = repository.testApiKey(clean)
            result.fold(
                onSuccess = {
                    prefs.setApiKey(clean)
                    _uiState.update {
                        it.copy(
                            apiKey = clean,
                            hasValidKey = true,
                            showApiKeyDialog = false,
                            isApiKeyValid = true,
                            apiKeyTestResult = "✅ Connected successfully! Launching StudyGem...",
                            snackbarMessage = "API key validated and securely stored!"
                        )
                    }
                    onComplete(true, "✅ Connected successfully! Launching StudyGem...")
                },
                onFailure = { error ->
                    val errMsg = error.localizedMessage ?: "Failed to verify API key with Google servers."
                    onComplete(false, errMsg)
                }
            )
        }
    }

    fun saveApiKey(newKey: String) {
        val clean = prefs.sanitizeKey(newKey)
        prefs.setApiKey(clean)
        _uiState.update {
            it.copy(
                apiKey = clean,
                hasValidKey = clean.isNotBlank(),
                showApiKeyDialog = false,
                isApiKeyValid = null,
                apiKeyTestResult = null,
                snackbarMessage = if (clean.isNotBlank()) "API key updated successfully!" else null
            )
        }
    }

    fun removeApiKey() {
        prefs.clearApiKey()
        _uiState.update {
            it.copy(
                apiKey = "",
                hasValidKey = false,
                showSettingsDialog = false,
                snackbarMessage = "API key removed."
            )
        }
    }

    fun testApiKey(key: String) {
        val clean = prefs.sanitizeKey(key)
        if (clean.isBlank()) {
            _uiState.update {
                it.copy(
                    isTestingApiKey = false,
                    isApiKeyValid = false,
                    apiKeyTestResult = "Please enter an API key to test."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isTestingApiKey = true,
                apiKeyTestResult = "Connecting to Gemini 3.8 Flash...",
                isApiKeyValid = null
            )
        }

        viewModelScope.launch {
            val result = repository.testApiKey(clean)
            result.fold(
                onSuccess = { successMsg ->
                    _uiState.update {
                        it.copy(
                            isTestingApiKey = false,
                            isApiKeyValid = true,
                            apiKeyTestResult = "✅ $successMsg"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isTestingApiKey = false,
                            isApiKeyValid = false,
                            apiKeyTestResult = "❌ Error: ${error.localizedMessage ?: "Invalid API key"}"
                        )
                    }
                }
            )
        }
    }

    fun resetApiKeyTest() {
        _uiState.update {
            it.copy(
                isTestingApiKey = false,
                apiKeyTestResult = null,
                isApiKeyValid = null
            )
        }
    }

    fun openApiKeyDialog() {
        resetApiKeyTest()
        _uiState.update { it.copy(showApiKeyDialog = true) }
    }

    fun dismissApiKeyDialog() {
        resetApiKeyTest()
        _uiState.update { it.copy(showApiKeyDialog = false) }
    }

    fun openSettings() {
        _uiState.update { it.copy(showSettingsDialog = true) }
    }

    fun dismissSettings() {
        _uiState.update { it.copy(showSettingsDialog = false) }
    }

    fun setModel(model: GeminiModel) {
        prefs.setSelectedModel(model)
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun setAiMode(mode: AiMode) {
        prefs.setAiMode(mode)
        _uiState.update { it.copy(aiMode = mode) }
    }

    fun setOverrideGuardrails(enabled: Boolean) {
        prefs.setOverrideGuardrails(enabled)
        _uiState.update { it.copy(overrideGuardrails = enabled) }
    }

    fun setThemeMode(mode: String) {
        prefs.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setStudySection(section: StudySection) {
        prefs.setStudySection(section)
        _uiState.update { it.copy(studySection = section) }
        repository.switchSubject(section, _uiState.value.miscCategory)
        updateSuggestedPrompts(section, _uiState.value.miscCategory)
    }

    fun setMiscCategory(category: MiscSubCategory) {
        prefs.setMiscCategory(category)
        _uiState.update { it.copy(miscCategory = category) }
        repository.switchSubject(StudySection.MISC, category)
        updateSuggestedPrompts(StudySection.MISC, category)
    }

    fun onImagePicked(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val result = ImageUtils.uriToBase64(getApplication(), uri)
            if (result != null) {
                _uiState.update {
                    it.copy(
                        selectedImageUri = uri,
                        selectedImageBase64 = result.first,
                        selectedImageMimeType = result.second
                    )
                }
            } else {
                _uiState.update {
                    it.copy(snackbarMessage = "Failed to process image. Please try again.")
                }
            }
        }
    }

    fun removeImage() {
        _uiState.update {
            it.copy(
                selectedImageUri = null,
                selectedImageBase64 = null,
                selectedImageMimeType = null
            )
        }
    }

    fun setPrompt(text: String) {
        _uiState.update { it.copy(inputPrompt = text) }
    }

    fun onPromptSelected(prompt: String) {
        _uiState.update { it.copy(inputPrompt = prompt) }
    }

    fun clearHistory() {
        repository.clearHistory()
        repository.switchSubject(_uiState.value.studySection, _uiState.value.miscCategory)
        _uiState.update { it.copy(snackbarMessage = "Study session cleared.") }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun sendMessage() {
        val state = _uiState.value
        val prompt = state.inputPrompt.trim()
        val imageUri = state.selectedImageUri
        val imageBase64 = state.selectedImageBase64
        val imageMimeType = state.selectedImageMimeType

        if (prompt.isEmpty() && imageBase64 == null) return

        if (state.apiKey.isBlank()) {
            _uiState.update {
                it.copy(
                    hasValidKey = false,
                    snackbarMessage = "Please enter and verify your Google AI Studio API Key first."
                )
            }
            return
        }

        val config = StudySessionConfig(
            section = state.studySection,
            miscCategory = state.miscCategory,
            mode = state.aiMode,
            model = state.selectedModel,
            overrideGuardrails = state.overrideGuardrails
        )

        val actualPrompt = if (prompt.isEmpty() && imageBase64 != null) {
            "Please analyze and explain this study material/image in detail."
        } else {
            prompt
        }

        _uiState.update {
            it.copy(
                inputPrompt = "",
                selectedImageUri = null,
                selectedImageBase64 = null,
                selectedImageMimeType = null
            )
        }

        viewModelScope.launch {
            repository.sendMessage(
                apiKey = state.apiKey,
                config = config,
                userPrompt = actualPrompt,
                imageUri = imageUri,
                imageBase64 = imageBase64,
                imageMimeType = imageMimeType
            )
        }
    }
}
