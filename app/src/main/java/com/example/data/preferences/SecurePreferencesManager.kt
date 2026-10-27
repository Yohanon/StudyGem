package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.model.AiMode
import com.example.data.model.GeminiModel
import com.example.data.model.MiscSubCategory
import com.example.data.model.StudySection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.security.GeneralSecurityException

class SecurePreferencesManager(private val context: Context) {

    companion object {
        private const val TAG = "SecurePrefs"
        private const val SECURE_PREF_NAME = "study_gem_secure_prefs"

        private const val KEY_API_KEY = "key_gemini_api_key"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_SELECTED_MODEL = "key_selected_model"
        private const val KEY_AI_MODE = "key_ai_mode"
        private const val KEY_OVERRIDE_GUARDRAILS = "key_override_guardrails"
        private const val KEY_STUDY_SECTION = "key_study_section"
        private const val KEY_MISC_CATEGORY = "key_misc_category"

        /**
         * Sanitizes user input API key: removes leading/trailing spaces, surrounding quotes,
         * inner newlines, carriage returns, tabs, and spaces.
         */
        fun sanitizeApiKey(key: String): String {
            return key.trim()
                .removeSurrounding("\"")
                .removeSurrounding("'")
                .removeSurrounding("`")
                .replace(" ", "")
                .replace("\n", "")
                .replace("\r", "")
                .replace("\t", "")
                .trim()
        }

        /**
         * Checks whether a string looks like a valid Gemini API key (starts with AIzaSy or has 30+ chars).
         */
        fun isValidKeyFormat(key: String): Boolean {
            val clean = sanitizeApiKey(key)
            return clean.isNotBlank() && (clean.startsWith("AIzaSy") || clean.length >= 25)
        }
    }

    private var sharedPreferences: SharedPreferences = initializePreferences()

    private fun initializePreferences(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                SECURE_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            when (e) {
                is GeneralSecurityException, is IOException -> {
                    Log.e(TAG, "KeyStore/EncryptedSharedPreferences corrupted. Resetting preference file.", e)
                    try {
                        context.deleteSharedPreferences(SECURE_PREF_NAME)
                        val fallbackMasterKey = MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build()
                        EncryptedSharedPreferences.create(
                            context,
                            SECURE_PREF_NAME,
                            fallbackMasterKey,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        )
                    } catch (fallbackEx: Exception) {
                        Log.e(TAG, "Fallback to standard preferences after KeyStore failure", fallbackEx)
                        context.getSharedPreferences(SECURE_PREF_NAME, Context.MODE_PRIVATE)
                    }
                }
                else -> {
                    Log.e(TAG, "Unexpected error initializing secure preferences", e)
                    context.getSharedPreferences(SECURE_PREF_NAME, Context.MODE_PRIVATE)
                }
            }
        }
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
        val raw = sharedPreferences.getString(KEY_API_KEY, "") ?: ""
        return sanitizeKey(raw)
    }

    fun hasValidApiKey(): Boolean {
        return getApiKey().isNotBlank()
    }

    fun setApiKey(key: String) {
        val clean = sanitizeKey(key)
        sharedPreferences.edit().putString(KEY_API_KEY, clean).apply()
        _apiKeyFlow.value = clean
    }

    fun clearApiKey() {
        sharedPreferences.edit().remove(KEY_API_KEY).apply()
        _apiKeyFlow.value = ""
    }

    fun sanitizeKey(key: String): String = sanitizeApiKey(key)

    fun getThemeMode(): String {
        return sharedPreferences.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
    }

    fun setThemeMode(mode: String) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeModeFlow.value = mode
    }

    fun getSelectedModel(): GeminiModel {
        val id = sharedPreferences.getString(KEY_SELECTED_MODEL, GeminiModel.FLASH_3_8.name)
        return try {
            GeminiModel.valueOf(id ?: GeminiModel.FLASH_3_8.name)
        } catch (_: Exception) {
            GeminiModel.FLASH_3_8
        }
    }

    fun setSelectedModel(model: GeminiModel) {
        sharedPreferences.edit().putString(KEY_SELECTED_MODEL, model.name).apply()
        _selectedModelFlow.value = model
    }

    fun getAiMode(): AiMode {
        val mode = sharedPreferences.getString(KEY_AI_MODE, AiMode.LEARN.name)
        return try {
            AiMode.valueOf(mode ?: AiMode.LEARN.name)
        } catch (_: Exception) {
            AiMode.LEARN
        }
    }

    fun setAiMode(mode: AiMode) {
        sharedPreferences.edit().putString(KEY_AI_MODE, mode.name).apply()
        _aiModeFlow.value = mode
    }

    fun isGuardrailsOverridden(): Boolean {
        return sharedPreferences.getBoolean(KEY_OVERRIDE_GUARDRAILS, false)
    }

    fun setOverrideGuardrails(override: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_OVERRIDE_GUARDRAILS, override).apply()
        _overrideGuardrailsFlow.value = override
    }

    fun getStudySection(): StudySection {
        val section = sharedPreferences.getString(KEY_STUDY_SECTION, StudySection.CODING.name)
        return try {
            StudySection.valueOf(section ?: StudySection.CODING.name)
        } catch (_: Exception) {
            StudySection.CODING
        }
    }

    fun setStudySection(section: StudySection) {
        sharedPreferences.edit().putString(KEY_STUDY_SECTION, section.name).apply()
        _studySectionFlow.value = section
    }

    fun getMiscCategory(): MiscSubCategory {
        val cat = sharedPreferences.getString(KEY_MISC_CATEGORY, MiscSubCategory.RESEARCH.name)
        return try {
            MiscSubCategory.valueOf(cat ?: MiscSubCategory.RESEARCH.name)
        } catch (_: Exception) {
            MiscSubCategory.RESEARCH
        }
    }

    fun setMiscCategory(cat: MiscSubCategory) {
        sharedPreferences.edit().putString(KEY_MISC_CATEGORY, cat.name).apply()
        _miscCategoryFlow.value = cat
    }
}
