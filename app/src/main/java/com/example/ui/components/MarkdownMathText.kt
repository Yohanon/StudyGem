package com.example.ui.components

import android.content.Context
import android.graphics.Typeface
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

/**
 * Normalizes mixed LaTeX math patterns into Markwon/CommonMark compatible syntax.
 * - LaTeX block delimiters `\[ ... \]` -> `$$\n ... \n$$`
 * - LaTeX inline delimiters `\( ... \)` -> `$$ ... $$`
 * - Single-dollar inline math `$ ... $` (outside code blocks) -> `$$ ... $$`
 * - Ensures standalone block math `$$ ... $$` has surrounding newlines so the block parser detects it.
 */
fun normalizeMarkdownAndMath(raw: String): String {
    if (raw.isBlank()) return raw

    // 1. Normalize LaTeX delimiters: \[ ... \] -> $$\n...\n$$
    var text = raw.replace(Regex("""\\\[([\s\S]*?)\\\]""")) { match ->
        "\n\n$$\n${match.groupValues[1].trim()}\n$$\n\n"
    }

    // 2. Normalize LaTeX inline delimiters: \( ... \) -> $$...$$
    text = text.replace(Regex("""\\\(([\s\S]*?)\\\)""")) { match ->
        "$$${match.groupValues[1].trim()}$$"
    }

    // 3. Ensure display math ($$...$$) on its own line has proper blank line spacing for block parsing
    text = text.replace(Regex("""(?m)^(?!\s*$)\s*\$\$\s*([\s\S]*?)\s*\$\$\s*$""")) { match ->
        "\n\n$$\n${match.groupValues[1].trim()}\n$$\n\n"
    }

    // 4. Convert single dollar inline math `$formula$` to `$$formula$$`
    // Regex matches single $ not preceded or followed by $, avoiding multiline and currency like $50
    text = text.replace(Regex("""(?<!\$)\$(?!\$)(\S[^\$\n]*?\S|\S)(?<!\$)\$(?!\$)""")) { match ->
        val inner = match.groupValues[1]
        // If it looks like a currency amount (e.g. $50 or $12.99), leave it as is
        if (inner.matches(Regex("""^\d+(\.\d{1,2})?$"""))) {
            match.value
        } else {
            "$$$inner$$"
        }
    }

    return text
}

/**
 * Creates and configures a cached Markwon instance tailored to the current Compose color scheme.
 */
private fun createMarkwonInstance(
    context: Context,
    textColor: Color,
    linkColor: Color,
    textSizePx: Float
): Markwon {
    val textColorArgb = textColor.toArgb()
    val linkColorArgb = linkColor.toArgb()
    val inlineCodeBg = textColor.copy(alpha = 0.12f).toArgb()
    val dividerColor = textColor.copy(alpha = 0.20f).toArgb()
    val codeBlockBg = 0xFF1E293B.toInt()
    val codeBlockText = 0xFFF1F5F9.toInt()

    return Markwon.builder(context)
        .usePlugin(MarkwonInlineParserPlugin.create())
        .usePlugin(JLatexMathPlugin.create(textSizePx) { builder ->
            builder.inlinesEnabled(true)
            builder.blocksEnabled(true)
            builder.theme().textColor(textColorArgb)
            builder.theme().inlineTextColor(textColorArgb)
            builder.theme().blockTextColor(textColorArgb)
            builder.errorHandler { _, _ -> null }
        })
        .usePlugin(TablePlugin.create(context))
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                builder
                    .linkColor(linkColorArgb)
                    .codeBackgroundColor(inlineCodeBg)
                    .codeTextColor(textColorArgb)
                    .codeBlockBackgroundColor(codeBlockBg)
                    .codeBlockTextColor(codeBlockText)
                    .codeTypeface(Typeface.MONOSPACE)
                    .codeBlockTypeface(Typeface.MONOSPACE)
                    .headingBreakColor(dividerColor)
                    .thematicBreakColor(dividerColor)
                    .listItemColor(linkColorArgb)
            }
        })
        .build()
}

/**
 * High-performance, rich markdown and LaTeX math renderer for Jetpack Compose.
 * Handles headings, bold, italics, bullet & numbered lists, inline/block code,
 * and inline ($...$) and block ($$...$$) LaTeX equations.
 */
@Composable
fun MarkdownMathText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    textSize: TextUnit = 15.sp,
    lineSpacingMultiplier: Float = 1.25f
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val textSizePx = with(density) { textSize.toPx() }

    // Normalize math syntax once per text change
    val normalizedMarkdown = remember(text) {
        normalizeMarkdownAndMath(text)
    }

    // Cache the configured Markwon instance
    val markwon = remember(context, textColor, linkColor, textSizePx) {
        createMarkwonInstance(
            context = context,
            textColor = textColor,
            linkColor = linkColor,
            textSizePx = textSizePx
        )
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            TextView(ctx).apply {
                this.movementMethod = LinkMovementMethod.getInstance()
                this.includeFontPadding = false
                this.setLineSpacing(0f, lineSpacingMultiplier)
                this.setTextColor(textColor.toArgb())
                this.textSize = textSize.value
            }
        },
        update = { textView ->
            textView.setTextColor(textColor.toArgb())
            textView.textSize = textSize.value
            markwon.setMarkdown(textView, normalizedMarkdown)
        }
    )
}

/**
 * Message content composable that breaks messages into mixed segments:
 * - Standalone fenced code blocks are rendered in a sleek Compose Card with language tag and Copy button.
 * - All other content (mixed paragraphs, headings, bullet lists, bold, italics, inline code,
 *   inline math, and block LaTeX math) is rendered through the MarkdownMathText engine.
 */
@Composable
fun FormattedMessageContent(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = MaterialTheme.colorScheme.primary
) {
    val clipboardManager = LocalClipboardManager.current

    // Parse message into segments: text chunks and fenced code blocks
    val segments = remember(text) {
        parseMessageSegments(text)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        segments.forEach { segment ->
            when (segment) {
                is MessageSegment.Text -> {
                    if (segment.content.isNotBlank()) {
                        MarkdownMathText(
                            text = segment.content.trim(),
                            textColor = textColor,
                            linkColor = linkColor
                        )
                    }
                }
                is MessageSegment.CodeBlock -> {
                    var copied by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = segment.language.ifBlank { "CODE" }.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(segment.code))
                                        copied = true
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy code",
                                        tint = if (copied) Color(0xFF82C0AF) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                Text(
                                    text = segment.code,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFF1F5F9)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class MessageSegment {
    data class Text(val content: String) : MessageSegment()
    data class CodeBlock(val language: String, val code: String) : MessageSegment()
}

/**
 * Splits text into markdown text segments and fenced code block segments.
 */
fun parseMessageSegments(text: String): List<MessageSegment> {
    if (!text.contains("```")) {
        return listOf(MessageSegment.Text(text))
    }

    val result = mutableListOf<MessageSegment>()
    val parts = text.split("```")

    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) {
            // Inside code block
            val lines = part.lines()
            val firstLine = lines.firstOrNull()?.trim().orEmpty()
            val hasLang = firstLine.isNotEmpty() && !firstLine.contains(" ")

            val language = if (hasLang) firstLine else ""
            val codeBody = if (hasLang) lines.drop(1).joinToString("\n").trimEnd() else part.trimEnd()

            result.add(MessageSegment.CodeBlock(language = language, code = codeBody))
        } else {
            // Text chunk
            if (part.isNotEmpty()) {
                result.add(MessageSegment.Text(part))
            }
        }
    }

    return result
}
