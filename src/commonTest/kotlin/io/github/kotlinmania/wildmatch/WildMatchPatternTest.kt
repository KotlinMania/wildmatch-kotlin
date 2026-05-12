// port-lint: source src/lib.rs
package io.github.kotlinmania.wildmatch

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class WildMatchPatternTest {

    @Test
    fun isMatchRandom() {
        val patternLen = 100
        val rng = Random(0xC0FFEE)

        repeat(1_000) {
            val patternBuilder = StringBuilder(patternLen)
            repeat(patternLen) {
                patternBuilder.append(randomAlphanumericChar(rng))
            }
            val pattern = StringBuilder(patternBuilder)
            repeat(rng.nextInt(0, 15)) {
                val idx = rng.nextInt(0, patternLen)
                pattern[idx] = '?'
            }
            repeat(rng.nextInt(0, 15)) {
                val idx = rng.nextInt(0, patternLen)
                pattern[idx] = '*'
            }
            val patternString = pattern.toString()
            val matcher = WildMatch.new(patternString)
            repeat(rng.nextInt(0, 1_000)) { patternIdx ->
                val input = StringBuilder(patternString)
                for ((i, c) in patternString.reversed().withIndex()) {
                    val idx = patternString.length - i - 1
                    if (c == '?') {
                        input[idx] = randomAlphanumericChar(rng)
                    }
                    if (c == '*') {
                        val replacement = buildString {
                            repeat(rng.nextInt(0, 15)) {
                                append(randomAlphanumericChar(rng))
                            }
                        }
                        input.deleteAt(idx)
                        input.insert(idx, replacement)
                    }
                }
                val inputString = input.toString()
                assertTrue(
                    matcher.matches(inputString),
                    "Pattern ($patternIdx): $patternString doesn't match input: $inputString",
                )
            }
        }
    }

    @Test
    fun isMatch() {
        val patterns = listOf(
            "**",
            "*",
            "*?*",
            "c*",
            "c?*",
            "???",
            "c?t",
            "cat",
            "*cat",
            "cat*",
        )
        for (pattern in patterns) {
            val matcher = WildMatch.new(pattern)
            assertTrue(matcher.matches("cat"), "pattern $pattern should match cat")
        }
    }

    @Test
    fun isMatchCaseInsensitive() {
        val cases = listOf(
            "CAT" to "cat",
            "CAT" to "CAT",
            "CA?" to "Cat",
            "C*" to "cAt",
            "C?*" to "cAT",
            "C**" to "caT",
            "КОТ" to "кот",
            "КОТ" to "КОТ",
            "КО?" to "Кот",
            "К*" to "кОт",
            "К?*" to "кОТ",
            "К**" to "коТ",
        )
        for ((pattern, input) in cases) {
            val matcher = WildMatch.newCaseInsensitive(pattern)
            assertTrue(matcher.matches(input), "case-insensitive pattern $pattern should match $input")
        }
    }

    @Test
    fun noMatch() {
        val patterns = listOf(
            "*d*",
            "*d",
            "d*",
            "*c",
            "?",
            "??",
            "????",
            "?????",
            "*????",
            "cats",
            "cat?",
            "cacat",
            "cat*dog",
            "CAT",
        )
        for (pattern in patterns) {
            val matcher = WildMatch.new(pattern)
            assertFalse(matcher.matches("cat"), "pattern $pattern should NOT match cat")
        }
    }

    @Test
    fun noMatchLong() {
        val cases = listOf(
            "1" to "",
            "?" to "",
            "?" to "11",
            "*1?" to "123",
            "*12" to "122",
            "cat?" to "wildcats",
            "cat*" to "wildcats",
            "*x*" to "wildcats",
            "*a" to "wildcats",
            "" to "wildcats",
            " " to "wildcats",
            " " to "\n",
            " " to "\t",
            "???" to "wildcats",
        )
        for ((pattern, expected) in cases) {
            val matcher = WildMatch.new(pattern)
            assertFalse(
                matcher.matches(expected),
                "pattern $pattern should NOT match $expected",
            )
        }
    }

    @Test
    fun matchLong() {
        val cases = listOf(
            "*" to "",
            "*" to "1",
            "?" to "1",
            "*121" to "12121",
            "?*3" to "111333",
            "*113" to "1113",
            "*113" to "113",
            "*113" to "11113",
            "*113" to "111113",
            "*???a" to "bbbba",
            "*???a" to "bbbbba",
            "*???a" to "bbbbbba",
            "*o?a*" to "foobar",
            "*ooo?ar" to "foooobar",
            "*o?a*r" to "foobar",
            "*cat*" to "d&(*og_cat_dog",
            "*?*" to "d&(*og_cat_dog",
            "*a*" to "d&(*og_cat_dog",
            "a*b" to "a*xb",
            "*" to "*",
            "*" to "?",
            "?" to "?",
            "wildcats" to "wildcats",
            "wild*cats" to "wild?cats",
            "wi*ca*s" to "wildcats",
            "wi*ca?s" to "wildcats",
            "*o?" to "hog_cat_dog",
            "*o?" to "cat_dog",
            "*at_dog" to "cat_dog",
            " " to " ",
            "* " to "\n ",
            "\n" to "\n",
            "*32" to "432",
            "*32" to "332",
            "*332" to "332",
            "*32" to "32",
            "*32" to "3232",
            "*32" to "3232332",
            "*?2" to "332",
            "*?2" to "3332",
            "33*" to "333",
            "da*da*da*" to "daaadabadmanda",
            "*?" to "xx",
        )
        for ((pattern, expected) in cases) {
            val matcher = WildMatch.new(pattern)
            assertTrue(
                matcher.matches(expected),
                "Expected pattern $pattern to match $expected",
            )
        }
    }

    @Test
    fun complexPattern() {
        val text = "Lorem ipsum dolor sit amet, " +
            "consetetur sadipscing elitr, sed diam nonumy eirmod tempor " +
            "invidunt ut labore et dolore magna aliquyam erat, sed diam " +
            "voluptua. At vero eos et accusam et justo duo dolores et ea " +
            "rebum. Stet clita kasd gubergren, no sea takimata sanctus est " +
            "Lorem ipsum dolor sit amet."
        val complexPattern = "Lorem?ipsum*dolore*ea* ?????ata*."
        val matcher = WildMatch.new(complexPattern)
        assertTrue(matcher.matches(text))
    }

    @Test
    fun complexPatternAlternativeWildcards() {
        val text = "Lorem ipsum dolor sit amet, " +
            "consetetur sadipscing elitr, sed diam nonumy eirmod tempor " +
            "invidunt ut labore et dolore magna aliquyam erat, sed diam " +
            "voluptua. At vero eos et accusam et justo duo dolores et ea " +
            "rebum. Stet clita kasd gubergren, no sea takimata sanctus est " +
            "Lorem ipsum dolor sit amet."
        val complexPattern = "Lorem_ipsum%dolore%ea% _____ata%."
        val matcher = WildMatchPattern.new(complexPattern, '%', '_')
        assertTrue(matcher.matches(text))
    }

    @Test
    fun compareViaEqual() {
        val matcher = WildMatch.new("c?*")
        assertTrue(matcher.equals("cat"))
        assertTrue(matcher.equals("car"))
        assertFalse(matcher.equals("dog"))
    }

    @Test
    fun compareEmpty() {
        val matcher = WildMatch.new("")
        assertFalse(matcher.equals("bar"))
        assertTrue(matcher.equals(""))
    }

    @Test
    fun compareDefault() {
        val matcher = WildMatch.default()
        assertTrue(matcher.equals(""))
        assertFalse(matcher.equals("bar"))
    }

    @Test
    fun compareWildMatch() {
        assertEquals(WildMatch.default(), WildMatch.new(""))
        assertEquals(WildMatch.new("abc"), WildMatch.new("abc"))
        assertEquals(WildMatch.new("a*bc"), WildMatch.new("a*bc"))
        assertNotEquals(WildMatch.new("abc"), WildMatch.new("a*bc"))
        assertNotEquals(WildMatch.new("a*bc"), WildMatch.new("a?bc"))
        assertEquals(WildMatch.new("a***c"), WildMatch.new("a*c"))
    }

    @Test
    fun printString() {
        val matcher = WildMatch.new("Foo/Bar")
        assertEquals("Foo/Bar", matcher.toString())
    }

    @Test
    fun toStringF() {
        val matcher = WildMatch.new("F")
        assertEquals("F", matcher.toString())
    }

    @Test
    fun toStringWithStar() {
        assertEquals("a*bc", WildMatch.new("a*bc").toString())
        assertEquals("a*bc", WildMatch.new("a**bc").toString())
        assertEquals("a*bc*", WildMatch.new("a*bc*").toString())
    }

    @Test
    fun toStringWithQuestionSign() {
        assertEquals("a?bc", WildMatch.new("a?bc").toString())
        assertEquals("a??bc", WildMatch.new("a??bc").toString())
    }

    @Test
    fun toStringEmpty() {
        val matcher = WildMatch.new("")
        assertEquals("", matcher.toString())
    }
}

private fun randomAlphanumericChar(rng: Random): Char {
    val idx = rng.nextInt(0, 62)
    return when {
        idx < 26 -> ('A' + idx)
        idx < 52 -> ('a' + (idx - 26))
        else -> ('0' + (idx - 52))
    }
}
