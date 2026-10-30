package com.example.data.repository

import android.net.Uri
import com.example.data.api.GeminiApiClient
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.MiscSubCategory
import com.example.data.model.StudySection
import com.example.data.model.StudySessionConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class StudyRepository(
    private val apiClient: GeminiApiClient = GeminiApiClient()
) {
    // Keep message history per subject key so students don't lose their notes when switching sections
    private val messagesBySubject = mutableMapOf<String, MutableList<ChatMessage>>()

    private val _currentMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessage>> = _currentMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    suspend fun testApiKey(apiKey: String): Result<String> {
        return apiClient.verifyApiKey(apiKey)
    }

    private var currentSubjectKey: String = StudySection.CODING.name

    fun switchSubject(section: StudySection, miscSubCategory: MiscSubCategory) {
        val key = if (section == StudySection.MISC) {
            "MISC_${miscSubCategory.name}"
        } else {
            section.name
        }
        currentSubjectKey = key
        val existing = messagesBySubject[key] ?: mutableListOf()
        if (existing.isEmpty()) {
            // Add a warm initial welcoming tutor message tailored to the subject
            val welcome = createSubjectWelcomeMessage(section, miscSubCategory)
            existing.add(welcome)
            messagesBySubject[key] = existing
        }
        _currentMessages.value = existing.toList()
    }

    private fun createSubjectWelcomeMessage(section: StudySection, misc: MiscSubCategory): ChatMessage {
        val text = when (section) {
            StudySection.CODING ->
                "👋 Welcome to **Coding** study! I can help you understand algorithms, data structures, debug complex stack traces, or review code architecture. You can also upload a screenshot of your code or terminal error!"
            StudySection.MATH ->
                "👋 Welcome to **Math** study! Share any equation, calculus proof, linear algebra problem, or geometry question. Feel free to snap and upload a photo of handwritten math notes!"
            StudySection.SCIENCE ->
                "👋 Welcome to **Science** study! Ask about physics mechanics, chemistry reaction mechanisms, cellular biology, or scientific theories. Upload diagrams or lab problems anytime."
            StudySection.MISC -> when (misc) {
                MiscSubCategory.RESEARCH ->
                    "👋 Welcome to **Academic Research**! Ask about forming a methodology, literature synthesis, thesis statements, or formatting citations (APA, MLA, Chicago, IEEE)."
                MiscSubCategory.HISTORY ->
                    "👋 Welcome to **History** study! Inquire about historical epochs, treaties, revolutions, geopolitical causes, and chronologies."
                MiscSubCategory.GEOGRAPHY ->
                    "👋 Welcome to **Geography** study! Explore physical geology, climate patterns, cartography, or socio-economic human geography."
                MiscSubCategory.BUSINESS_STUDY ->
                    "👋 Welcome to **Business Studies**! Explore micro/macro economics, financial accounting, marketing strategies, or business operations."
                MiscSubCategory.NEWS ->
                    "👋 Welcome to **News & Current Affairs Analysis**! Let's examine global developments, policy reports, or scientific milestones through an analytical academic lens."
                MiscSubCategory.ENGLISH ->
                    "👋 Welcome to **English & Literature**! I can help you critique essays, parse rhetorical devices, analyze novels, or master syntax and grammar."
                MiscSubCategory.OPEN_CONVERSATION ->
                    "👋 Welcome to **Student Open Conversation**! Let's talk about effective study routines, overcoming exam stress, time management, Pomodoro strategies, or academic goals."
            }
        }
        return ChatMessage(
            sender = MessageSender.AI,
            text = text,
            subjectLabel = section.displayName,
            modeLabel = "Tutor"
        )
    }

    suspend fun sendMessage(
        apiKey: String,
        config: StudySessionConfig,
        userPrompt: String,
        imageUri: Uri? = null,
        imageBase64: String? = null,
        imageMimeType: String? = null
    ): Result<String> {
        val modeTag = if (config.isExplainMode) "Explain" else "Short"
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = userPrompt,
            imageUri = imageUri,
            imageBase64 = imageBase64,
            subjectLabel = config.effectiveSubjectTitle,
            modeLabel = modeTag
        )

        val list = messagesBySubject.getOrPut(currentSubjectKey) { mutableListOf() }
        list.add(userMsg)
        _currentMessages.value = list.toList()

        _isGenerating.value = true

        val result = apiClient.generateContent(
            apiKey = apiKey,
            config = config,
            conversationHistory = list.dropLast(1),
            userPrompt = userPrompt,
            imageBase64 = imageBase64,
            imageMimeType = imageMimeType
        )

        _isGenerating.value = false

        result.fold(
            onSuccess = { reply ->
                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI,
                    text = reply,
                    subjectLabel = config.effectiveSubjectTitle,
                    modeLabel = modeTag
                )
                list.add(aiMsg)
                _currentMessages.value = list.toList()
            },
            onFailure = { error ->
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI,
                    text = "⚠️ **Error:** ${error.localizedMessage ?: "Unable to complete request."}",
                    isError = true,
                    subjectLabel = config.effectiveSubjectTitle,
                    modeLabel = modeTag
                )
                list.add(errorMsg)
                _currentMessages.value = list.toList()
            }
        )

        return result
    }

    fun clearHistory() {
        val list = messagesBySubject.getOrPut(currentSubjectKey) { mutableListOf() }
        list.clear()
        _currentMessages.value = emptyList()
    }

    fun getSuggestedPrompts(section: StudySection, misc: MiscSubCategory): List<String> {
        return when (section) {
            StudySection.CODING -> listOf(
                "Explain the time & space complexity of Quicksort vs Mergesort",
                "How does async/await work under the hood in modern runtimes?",
                "Design a LRU Cache data structure step-by-step",
                "Explain the difference between Dependency Injection and Service Locator"
            )
            StudySection.MATH -> listOf(
                "Explain the intuition behind the Chain Rule in Calculus",
                "How do Eigenvectors and Eigenvalues work geometrically?",
                "Break down Bayes' Theorem with a real-life probability example",
                "Solve this quadratic system: 2x² + 5x - 3 = 0"
            )
            StudySection.SCIENCE -> listOf(
                "Explain the mechanics of CRISPR-Cas9 gene editing",
                "How does Special Relativity explain time dilation?",
                "Walk through the steps of Cellular Respiration and ATP yield",
                "Explain Le Chatelier's principle with an equilibrium reaction"
            )
            StudySection.MISC -> when (misc) {
                MiscSubCategory.RESEARCH -> listOf(
                    "How to formulate a clear academic research hypothesis?",
                    "Differences between Qualitative and Quantitative methodology",
                    "How to properly cite a journal article in APA 7th edition?"
                )
                MiscSubCategory.HISTORY -> listOf(
                    "What were the primary socio-economic causes of the Industrial Revolution?",
                    "Explain the domino effect leading up to World War I",
                    "Analyze the significance of the Magna Carta on constitutional law"
                )
                MiscSubCategory.GEOGRAPHY -> listOf(
                    "Explain the geological process of plate subduction and volcanoes",
                    "How do oceanic thermohaline currents regulate global climate?",
                    "What is demographic transition model and its 5 stages?"
                )
                MiscSubCategory.BUSINESS_STUDY -> listOf(
                    "Explain Price Elasticity of Demand and how it impacts revenue",
                    "Break down the three financial statements and how they link",
                    "What is Porter's Five Forces framework?"
                )
                MiscSubCategory.NEWS -> listOf(
                    "Explain the economic implications of global central bank interest rate changes",
                    "Analyze the geopolitical significance of recent semiconductor trade policies",
                    "Summarize recent milestones in renewable energy transitions"
                )
                MiscSubCategory.ENGLISH -> listOf(
                    "Analyze how Shakespeare uses dramatic irony in Macbeth",
                    "What are the key rhetorical appeals (Ethos, Pathos, Logos)?",
                    "How to craft a strong, persuasive thesis statement for an essay?"
                )
                MiscSubCategory.OPEN_CONVERSATION -> listOf(
                    "How can I use Spaced Repetition and Active Recall to study effectively?",
                    "Tips for managing study fatigue and staying motivated during exams",
                    "How to structure a daily study schedule using the Pomodoro technique"
                )
            }
        }
    }
}
