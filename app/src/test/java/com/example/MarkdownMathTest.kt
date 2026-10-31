package com.example

import com.example.ui.components.MessageSegment
import com.example.ui.components.normalizeMarkdownAndMath
import com.example.ui.components.parseMessageSegments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownMathTest {

    @Test
    fun `test normalize LaTeX bracket notations`() {
        val input = "Here is an equation: \\[ E = mc^2 \\] and inline \\( x = 5 \\)."
        val normalized = normalizeMarkdownAndMath(input)
        assertTrue(normalized.contains("$$"))
        assertTrue(normalized.contains("E = mc^2"))
        assertTrue(normalized.contains("\$\$\nx = 5\n\$\$") || normalized.contains("\$\$x = 5\$\$"))
    }

    @Test
    fun `test normalize single dollar inline math`() {
        val input = "Calculate the value of \$a^2 + b^2 = c^2\$ for right triangles."
        val normalized = normalizeMarkdownAndMath(input)
        assertTrue(normalized.contains("\$\$a^2 + b^2 = c^2\$\$"))
    }

    @Test
    fun `test preserve currency format`() {
        val input = "The textbook costs \$50 or \$12.99 on sale."
        val normalized = normalizeMarkdownAndMath(input)
        assertTrue(normalized.contains("\$50"))
        assertTrue(normalized.contains("\$12.99"))
    }

    @Test
    fun `test parse message segments with code blocks`() {
        val message = """
            Here is a formula:
            ${"$$"}x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}${"$$"}
            
            ```python
            def solve():
                return 42
            ```
            
            That is all.
        """.trimIndent()

        val segments = parseMessageSegments(message)
        assertEquals(3, segments.size)
        assertTrue(segments[0] is MessageSegment.Text)
        assertTrue(segments[1] is MessageSegment.CodeBlock)
        assertTrue(segments[2] is MessageSegment.Text)

        val codeSegment = segments[1] as MessageSegment.CodeBlock
        assertEquals("python", codeSegment.language)
        assertTrue(codeSegment.code.contains("def solve():"))
    }
}
