package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AiMode
import com.example.data.model.GeminiModel
import com.example.data.model.MiscSubCategory
import com.example.data.model.StudySection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("study_gem_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "key_gemini_api_key"
        private const val KEY_THEME_MODE = "key_theme_mode" // SYSTEM, LIGHT, DARK
        private const val KEY_SELECTED_MODEL = "key_selected_model"
        private const val KEY_AI_MODE = "key_ai_mode"
        private const val KEY_OVERRIDE_GUARDRAILS = "key_override_guardrails"
        private const val KEY_STUDY_SECTION = "key_study_section"
        private const val KEY_MISC_CATEGORY = "key_misc_category"
    }

    private val _apiKeyFlow = MutableStateFlow(getApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    val themeModeFlow: StateFlow<String> = _themeModeFlow.asStateFlow()

    private val _selectedModelFlow = MutableStateFlow(getSelectedModel())
    val selectedModelFlow: StateFlow<GeminiModel> = _selectedModelFlow.asStateFlow()

    private val _aiModeFlow = MutableStateFlow(getAiMode())
    val aiModeFlow: StateFlow<AiMode> = _aiModeFlow.asStateFlow()

    private val _overrideGuardrailsFlow = MutableStateFlow(isGuardrailsOverridden())
    val overrideGuardrailsFlow: StateFlow<Boolean> = _overrideGuardrailsFlow.asStateFlow()

    private val _studySectionFlow = MutableStateFlow(getStudySection())
    val studySectionFlow: StateFlow<StudySection> = _studySectionFlow.asStateFlow()

    private val _miscCategoryFlow = MutableStateFlow(getMiscCategory())
    val miscCategoryFlow: StateFlow<MiscSubCategory> = _miscCategoryFlow.asStateFlow()

    fun getApiKey(): String {
        val raw = prefs.getString(KEY_API_KEY, "") ?: ""
        return sanitizeKey(raw)
    }

    fun setApiKey(key: String) {
        val clean = sanitizeKey(key)
        prefs.edit().putString(KEY_API_KEY, clean).apply()
        _apiKeyFlow.value = clean
    }

    fun sanitizeKey(key: String): String {
        return key.trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .removeSurrounding("`")
            .replace(" ", "")
            .replace("\n", "")
            .replace("\r", "")
            .trim()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
        _apiKeyFlow.value = ""
    }

    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeModeFlow.value = mode
    }

    fun getSelectedModel(): GeminiModel {
        val id = prefs.getString(KEY_SELECTED_MODEL, GeminiModel.FLASH_3_8.name)
        return try {
            GeminiModel.valueOf(id ?: GeminiModel.FLASH_3_8.name)
        } catch (_: Exception) {
            GeminiModel.FLASH_3_8
        }
    }

    fun setSelectedModel(model: GeminiModel) {
        prefs.edit().putString(KEY_SELECTED_MODEL, model.name).apply()
        _selectedModelFlow.value = model
    }

    fun getAiMode(): AiMode {
        val mode = prefs.getString(KEY_AI_MODE, AiMode.LEARN.name)
        return try {
            AiMode.valueOf(mode ?: AiMode.LEARN.name)
        } catch (_: Exception) {
            AiMode.LEARN
        }
    }

    fun setAiMode(mode: AiMode) {
        prefs.edit().putString(KEY_AI_MODE, mode.name).apply()
        _aiModeFlow.value = mode
    }

    fun isGuardrailsOverridden(): Boolean {
        return prefs.getBoolean(KEY_OVERRIDE_GUARDRAILS, false)
    }

    fun setOverrideGuardrails(override: Boolean) {
        prefs.edit().putBoolean(KEY_OVERRIDE_GUARDRAILS, override).apply()
        _overrideGuardrailsFlow.value = override
    }

    fun getStudySection(): StudySection {
        val section = prefs.getString(KEY_STUDY_SECTION, StudySection.CODING.name)
        return try {
            StudySection.valueOf(section ?: StudySection.CODING.name)
        } catch (_: Exception) {
            StudySection.CODING
        }
    }

    fun setStudySection(section: StudySection) {
        prefs.edit().putString(KEY_STUDY_SECTION, section.name).apply()
        _studySectionFlow.value = section
    }

    fun getMiscCategory(): MiscSubCategory {
        val cat = prefs.getString(KEY_MISC_CATEGORY, MiscSubCategory.RESEARCH.name)
        return try {
            MiscSubCategory.valueOf(cat ?: MiscSubCategory.RESEARCH.name)
        } catch (_: Exception) {
            MiscSubCategory.RESEARCH
        }
    }

    fun setMiscCategory(cat: MiscSubCategory) {
        prefs.edit().putString(KEY_MISC_CATEGORY, cat.name).apply()
        _miscCategoryFlow.value = cat
    }
}
