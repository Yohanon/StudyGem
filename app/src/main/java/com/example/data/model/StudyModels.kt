package com.example.data.model

import android.net.Uri

enum class AiMode(
    val title: String,
    val shortDesc: String,
    val fullDesc: String
) {
    LEARN(
        title = "Learn Mode",
        shortDesc = "Conceptual breakdown & intuition",
        fullDesc = "Explains core concepts, principles, analogies, and the 'why' behind topics so you truly understand."
    ),
    DEMONSTRATE(
        title = "Demonstrate Mode",
        shortDesc = "Step-by-step practical implementation",
        fullDesc = "Provides actionable worked solutions, runnable code walkthroughs, formula calculations, and concrete implementations."
    )
}

enum class GeminiModel(
    val id: String,
    val displayName: String,
    val shortName: String,
    val tag: String
) {
    FLASH_3_8("gemini-3.8-flash", "Gemini 3.8 Flash", "3.8 Flash", "Latest & Most Capable"),
    FLASH_3_5("gemini-3.5-flash", "Gemini 3.5 Flash", "3.5 Flash", "Fast & Balanced"),
    FLASH_3_5_LITE("gemini-3.5-flash-lite", "Gemini 3.5 Flash Light", "3.5 Light", "Ultra Lightweight")
}

enum class StudySection(val displayName: String, val iconName: String) {
    CODING("Coding", "code"),
    MATH("Math", "calculate"),
    SCIENCE("Science", "science"),
    MISC("Misc", "category")
}

enum class MiscSubCategory(val displayName: String, val description: String) {
    RESEARCH("Research", "Academic papers, literature reviews, citations & methodology"),
    HISTORY("History", "Historical events, timelines, civilisations & causal analysis"),
    GEOGRAPHY("Geography", "Geopolitics, cartography, climate, physical & human geography"),
    BUSINESS_STUDY("Business Study", "Economics, accounting, marketing, strategy & finance"),
    NEWS("News", "Objective current affairs, geopolitical context & editorial analysis"),
    ENGLISH("English", "Literature analysis, essay writing, grammar & linguistic mastery"),
    OPEN_CONVERSATION("Open Conversation", "Academic coaching, study habits, motivation & well-being")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val imageUri: Uri? = null,
    val imageBase64: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val subjectLabel: String? = null,
    val modeLabel: String? = null
)

enum class MessageSender {
    USER,
    AI
}

data class StudySessionConfig(
    val section: StudySection = StudySection.CODING,
    val miscCategory: MiscSubCategory = MiscSubCategory.RESEARCH,
    val mode: AiMode = AiMode.LEARN,
    val model: GeminiModel = GeminiModel.FLASH_3_8,
    val overrideGuardrails: Boolean = false
) {
    val effectiveSubjectTitle: String
        get() = if (section == StudySection.MISC) {
            "Misc: ${miscCategory.displayName}"
        } else {
            section.displayName
        }
}
