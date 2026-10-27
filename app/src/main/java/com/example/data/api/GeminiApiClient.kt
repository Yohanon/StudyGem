package com.example.data.api

import com.example.data.model.AiMode
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.MiscSubCategory
import com.example.data.model.StudySection
import com.example.data.model.StudySessionConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }

    suspend fun generateContent(
        apiKey: String,
        config: StudySessionConfig,
        conversationHistory: List<ChatMessage>,
        userPrompt: String,
        imageBase64: String? = null,
        imageMimeType: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanKey = sanitizeApiKey(apiKey)
        if (cleanKey.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Google AI Studio API Key is required. Please set it in Settings.")
            )
        }

        try {
            val modelId = config.model.id
            val url = "$BASE_URL/$modelId:generateContent?key=$cleanKey"

            val systemInstruction = buildSystemPrompt(config)

            val rootJson = JSONObject()

            // System Instruction
            val sysPartArray = JSONArray().apply {
                put(JSONObject().put("text", systemInstruction))
            }
            rootJson.put("systemInstruction", JSONObject().put("parts", sysPartArray))

            // Contents array
            val contentsArray = JSONArray()

            // Add up to last 6 messages from conversation history for context
            val historySlice = conversationHistory.takeLast(6)
            for (msg in historySlice) {
                if (msg.isError) continue
                val msgObj = JSONObject()
                msgObj.put("role", if (msg.sender == MessageSender.USER) "user" else "model")
                val parts = JSONArray()
                if (msg.imageBase64 != null) {
                    val inlineData = JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", msg.imageBase64)
                    }
                    parts.put(JSONObject().put("inlineData", inlineData))
                }
                parts.put(JSONObject().put("text", msg.text))
                msgObj.put("parts", parts)
                contentsArray.put(msgObj)
            }

            // Current user turn
            val currentUserObj = JSONObject()
            currentUserObj.put("role", "user")
            val currentParts = JSONArray()

            if (!imageBase64.isNullOrBlank()) {
                val inlineData = JSONObject().apply {
                    put("mimeType", imageMimeType ?: "image/jpeg")
                    put("data", imageBase64)
                }
                currentParts.put(JSONObject().put("inlineData", inlineData))
            }

            currentParts.put(JSONObject().put("text", userPrompt))
            currentUserObj.put("parts", currentParts)
            contentsArray.put(currentUserObj)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject().apply {
                put("temperature", if (config.mode == AiMode.LEARN) 0.6 else 0.3)
                put("maxOutputTokens", 4096)
            }
            rootJson.put("generationConfig", genConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", cleanKey)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                val parsedError = parseErrorMessage(response.code, responseBody)
                return@withContext Result.failure(IOException(parsedError))
            }

            if (responseBody.isNullOrBlank()) {
                return@withContext Result.failure(IOException("Empty response received from Gemini API."))
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val promptFeedback = responseJson.optJSONObject("promptFeedback")
                val blockReason = promptFeedback?.optString("blockReason")
                if (!blockReason.isNullOrBlank()) {
                    return@withContext Result.failure(
                        IOException("Response was blocked by content safety filters ($blockReason).")
                    )
                }
                return@withContext Result.failure(IOException("No response candidates returned."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val contentObj = firstCandidate.optJSONObject("content")
            val partsArr = contentObj?.optJSONArray("parts")

            val stringBuilder = StringBuilder()
            if (partsArr != null) {
                for (i in 0 until partsArr.length()) {
                    val p = partsArr.getJSONObject(i)
                    if (p.has("text")) {
                        stringBuilder.append(p.getString("text"))
                    }
                }
            }

            val replyText = stringBuilder.toString().trim()
            if (replyText.isEmpty()) {
                Result.failure(IOException("No textual content in Gemini response."))
            } else {
                Result.success(replyText)
            }
        } catch (e: Exception) {
            val userFriendly = when {
                e is java.net.UnknownHostException -> "No internet connection. Please verify your network."
                e is java.net.SocketTimeoutException -> "Request timed out. Please try again or switch to Gemini Flash."
                else -> e.localizedMessage ?: "Unexpected error communicating with Gemini."
            }
            Result.failure(IOException(userFriendly, e))
        }
    }

    suspend fun verifyApiKey(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanKey = sanitizeApiKey(apiKey)
        if (cleanKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your Google AI Studio API key."))
        }
        try {
            // Ping Gemini 3.8 Flash endpoint with maxOutputTokens=1
            val verificationModel = "gemini-3.8-flash"
            val url = "$BASE_URL/$verificationModel:generateContent?key=$cleanKey"
            val rootJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "ping"))
                        })
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().put("maxOutputTokens", 1))
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", cleanKey)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                Result.success("Success! Key is valid and active.")
            } else {
                // If gemini-3.8-flash returns 404, fallback to gemini-3.5-flash for the ping
                if (response.code == 404) {
                    val fallbackUrl = "$BASE_URL/gemini-3.5-flash:generateContent?key=$cleanKey"
                    val fallbackRequest = Request.Builder()
                        .url(fallbackUrl)
                        .addHeader("x-goog-api-key", cleanKey)
                        .post(requestBody)
                        .build()
                    val fallbackResponse = client.newCall(fallbackRequest).execute()
                    val fallbackBody = fallbackResponse.body?.string()
                    if (fallbackResponse.isSuccessful) {
                        return@withContext Result.success("Success! Key is valid and active.")
                    } else {
                        return@withContext Result.failure(IOException(parseErrorMessage(fallbackResponse.code, fallbackBody)))
                    }
                }
                val msg = parseErrorMessage(response.code, responseBody)
                Result.failure(IOException(msg))
            }
        } catch (e: Exception) {
            val userFriendly = when (e) {
                is java.net.UnknownHostException, is java.net.ConnectException ->
                    "Network error: Unable to reach Google servers. Please check your internet connection."
                is java.net.SocketTimeoutException ->
                    "Network timeout: Connection to Google AI Studio timed out."
                else -> e.localizedMessage ?: "Failed to verify API key with Google servers."
            }
            Result.failure(IOException(userFriendly, e))
        }
    }

    suspend fun testApiKeyConnection(apiKey: String): Result<String> = verifyApiKey(apiKey)

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

    private fun parseErrorMessage(code: Int, body: String?): String {
        if (body.isNullOrBlank()) {
            return "API call failed with HTTP $code."
        }
        return try {
            val json = JSONObject(body)
            val errorObj = json.optJSONObject("error")
            val message = errorObj?.optString("message")
            when {
                code == 400 && message?.contains("API key not valid", ignoreCase = true) == true ->
                    "Invalid API key! Please check your Google AI Studio API key in Settings (aistudio.google.com)."
                code == 400 ->
                    message ?: "Bad request (HTTP 400). Please review your prompt and input."
                code == 403 ->
                    "Permission denied (HTTP 403). Ensure Gemini API is enabled for your API key project."
                code == 429 ->
                    "Quota limit reached (HTTP 429). Please wait a few seconds or switch models."
                code in 500..599 ->
                    "Google AI Studio service error (HTTP $code). Please try again shortly."
                else -> message ?: "Request failed with HTTP $code: $body"
            }
        } catch (_: Exception) {
            "API call failed with HTTP $code."
        }
    }

    private fun buildSystemPrompt(config: StudySessionConfig): String {
        val sb = StringBuilder()

        sb.appendLine("You are StudyGem, a world-class academic tutor and mentor designed solely to help students excel in their education.")
        sb.appendLine("You format all explanations beautifully with Markdown, code blocks with proper syntax highlighting, and well-structured headings/bullet points.")

        if (!config.overrideGuardrails) {
            sb.appendLine("\n### MANDATORY ACADEMIC GUARDRAIL (STRICTLY ENFORCED)")
            sb.appendLine("You are STRICTLY CONFINED to educational, academic, learning, homework, and study subjects.")
            sb.appendLine("If the user's prompt is NOT related to studying, academic curriculum, homework help, conceptual understanding, learning, or educational development (e.g. casual small talk, video games, movie plots, sports trivia, gossip, entertainment, relationship advice, casual humor):")
            sb.appendLine("You MUST POLITELY REFUSE by replying verbatim or in this exact spirit:")
            sb.appendLine("\"I am dedicated exclusively to your studies! Please ask a question related to your homework, academics, or learning topics. (You can enable the Guardrails Override in Settings if needed).\"")
            sb.appendLine("Do NOT provide any answers to off-topic or non-study inquiries under any circumstances when this guardrail is active.")
        } else {
            sb.appendLine("\n### NOTE: GUARDRAIL OVERRIDE IS ENABLED")
            sb.appendLine("The student has temporarily enabled the Guardrails Override. You may assist with questions outside the strict academic boundary, but maintain a respectful, constructive, and supportive tone.")
        }

        sb.appendLine("\n### CURRENT SUBJECT FOCUS: ${config.effectiveSubjectTitle.uppercase()}")
        when (config.section) {
            StudySection.CODING -> {
                sb.appendLine("You must focus exclusively on Computer Science, Software Engineering, Algorithms, Data Structures, Programming Languages (Python, Java, Kotlin, C++, JS, Rust, SQL, etc.), system design, computational thinking, and software debugging.")
                if (!config.overrideGuardrails) {
                    sb.appendLine("If the student asks a question about an unrelated topic (e.g., biology or history), kindly remind them: 'This section is focused strictly on Coding! Please switch to the corresponding subject section or ask a programming/computer science question.'")
                }
            }
            StudySection.MATH -> {
                sb.appendLine("You must focus exclusively on Mathematics: Algebra, Calculus, Linear Algebra, Geometry, Trigonometry, Discrete Math, Probability & Statistics, Logic, Mathematical Proofs, and numerical problem solving.")
                if (!config.overrideGuardrails) {
                    sb.appendLine("If the student asks an unrelated topic question, remind them: 'This section is focused strictly on Mathematics! Please switch to the corresponding subject section or ask a math question.'")
                }
            }
            StudySection.SCIENCE -> {
                sb.appendLine("You must focus exclusively on Natural and Physical Sciences: Physics, Chemistry, Biology, Environmental Science, Astronomy, Earth Science, Laboratory experiments, and scientific theories.")
                if (!config.overrideGuardrails) {
                    sb.appendLine("If the student asks an unrelated topic question, remind them: 'This section is focused strictly on Science! Please switch to the corresponding subject section or ask a science question.'")
                }
            }
            StudySection.MISC -> {
                when (config.miscCategory) {
                    MiscSubCategory.RESEARCH -> {
                        sb.appendLine("Focus strictly on Academic Research: literature reviews, formulating hypotheses, methodology, APA/MLA/Chicago citations, thesis structure, critical analysis, and academic writing standards.")
                    }
                    MiscSubCategory.HISTORY -> {
                        sb.appendLine("Focus strictly on History: world history, ancient & modern civilizations, timelines, historical treaties, socio-political movements, revolutions, and causal analysis.")
                    }
                    MiscSubCategory.GEOGRAPHY -> {
                        sb.appendLine("Focus strictly on Geography: physical geography, climatology, cartography, plate tectonics, human geography, demography, and geopolitical territories.")
                    }
                    MiscSubCategory.BUSINESS_STUDY -> {
                        sb.appendLine("Focus strictly on Business Studies: micro & macro economics, financial statements, accounting, marketing, organizational behavior, business management, and supply chain.")
                    }
                    MiscSubCategory.NEWS -> {
                        sb.appendLine("Focus strictly on Academic News & Current Affairs: analytical, objective breakdown of geopolitical developments, global economic reports, scientific discoveries, and international treaties.")
                    }
                    MiscSubCategory.ENGLISH -> {
                        sb.appendLine("Focus strictly on English: grammar, literature analysis, rhetorical devices, essay drafting, vocabulary building, reading comprehension, and creative/academic writing.")
                    }
                    MiscSubCategory.OPEN_CONVERSATION -> {
                        sb.appendLine("Focus on Academic Coaching & Student Well-Being: You can freely discuss effective study techniques (Spaced Repetition, Feynman technique, Pomodoro), exam anxiety, time management, academic career planning, study motivation, and student mental wellness. Still avoid frivolous spam or unrelated chit-chat.")
                    }
                }
            }
        }

        sb.appendLine("\n### ACTIVE MODE: ${config.mode.title.uppercase()}")
        if (config.mode == AiMode.LEARN) {
            sb.appendLine("GOAL: Conceptual Breakdown & Deep Understanding.")
            sb.appendLine("- Explain the underlying intuition, 'why' things work the way they do, and core principles.")
            sb.appendLine("- Use relatable analogies, simple visualizations in text, and structured breakdowns.")
            sb.appendLine("- Ask Socratic questions or check student understanding.")
            sb.appendLine("- Do NOT just paste raw answers or code without thorough explanations of each piece.")
        } else {
            sb.appendLine("GOAL: Practical Step-by-Step Implementation Guidance.")
            sb.appendLine("- Provide concrete, actionable, worked step-by-step solutions.")
            sb.appendLine("- Include clear code examples with informative inline comments, edge cases, and execution steps.")
            sb.appendLine("- For math/science, write out every algebraic step and formula substitution clearly.")
            sb.appendLine("- Make solutions ready to apply and practice immediately.")
        }

        sb.appendLine("\n### IMAGE INTERPRETATION (MULTIMODAL)")
        sb.appendLine("When an image is attached, inspect it carefully. It may contain a handwritten equation, a diagram, an exam problem, a textbook excerpt, or a code snippet. Transcribe key elements and solve/explain according to the active mode.")

        return sb.toString()
    }
}
