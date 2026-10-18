package io.github.kotlinmania.wildmatch

import io.github.kotlinmania.glob.Pattern
import kotlin.test.Test
import kotlin.test.assertTrue

class PatternsComparisonTest {
    private val text =
        "Lorem ipsum dolor sit amet, " +
            "consetetur sadipscing elitr, sed diam nonumy eirmod tempor " +
            "invidunt ut labore et dolore magna aliquyam erat, sed diam " +
            "voluptua. At vero eos et accusam et justo duo dolores et ea " +
            "rebum. Stet clita kasd gubergren, no sea takimata sanctus est " +
            "Lorem ipsum dolor sit amet."

    private val fullTextPattern = text

    private val fullTextRegex =
        "^Lorem ipsum dolor sit amet, " +
            "consetetur sadipscing elitr, sed diam nonumy eirmod tempor " +
            "invidunt ut labore et dolore magna aliquyam erat, sed diam " +
            "voluptua\\. At vero eos et accusam et justo duo dolores et ea " +
            "rebum\\. Stet clita kasd gubergren, no sea takimata sanctus est " +
            "Lorem ipsum dolor sit amet\\.$"

    private val complexPattern = "Lorem?ipsum*dolore*ea* ?????ata*."

    private val complexRegex = "^Lorem.ipsum.*dolore.*ea.* .....ata.*\\.$"

    private val mostComplexPattern = "?a*b*?**c?d****?e*f*g*?*h?i*?*?**j*******k"

    private val mostComplexRegex =
        "^.a.*b.*..*.*c.d.*.*.*.*.e.*f.*g.*..*h.i.*..*..*.*j.*.*.*.*.*.*.*k$"

    @Test
    fun compilePatterns() {
        val wm1 = WildMatch.new(fullTextPattern)
        val wm2 = WildMatch.new(mostComplexPattern)
        val rx1 = Regex(fullTextRegex)
        val rx2 = Regex(mostComplexRegex)
        val gl1 = Pattern.new(fullTextPattern)
        val gl2Result = runCatching { Pattern.new(mostComplexPattern) }

        assertTrue(wm1.matches(fullTextPattern))
        assertTrue(wm2.pattern().isNotEmpty())
        assertTrue(rx1.matches(text))
        assertTrue(rx2.pattern.isNotEmpty())
        assertTrue(gl1.matches(fullTextPattern))
        assertTrue(gl2Result.isFailure)
    }

    @Test
    fun matchPatterns() {
        val pattern1 = WildMatch.new(fullTextPattern)
        val pattern2 = WildMatch.new(complexPattern)
        val regex1 = Regex(fullTextRegex)
        val regex2 = Regex(complexRegex)
        val glob1 = Pattern.new(fullTextPattern)
        val glob2 = Pattern.new(complexPattern)

        assertTrue(pattern1.matches(text))
        assertTrue(pattern2.matches(text))
        assertTrue(regex1.matches(text))
        assertTrue(regex2.matches(text))
        assertTrue(glob1.matches(text))
        assertTrue(glob2.matches(text))
    }
}
