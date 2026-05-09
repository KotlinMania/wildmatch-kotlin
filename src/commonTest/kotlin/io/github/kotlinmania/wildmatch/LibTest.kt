// port-lint: source src/lib.rs
package io.github.kotlinmania.wildmatch

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LibTest {

    @Test
    fun isMatchRandom() {
        val patternLen = 100

        repeat(1_000) {
            val rng = Random.Default
            val baseChars = StringBuilder()
            for (i in 0 until patternLen) {
                baseChars.append(randomAlphanumeric(rng))
            }
            var pattern = baseChars.toString()
            for (i in 0 until rng.nextInt(0, 15)) {
                val idx = rng.nextInt(0, patternLen)
                pattern = pattern.replaceRange(idx, idx + 1, "?")
            }
            for (i in 0 until rng.nextInt(0, 15)) {
                val idx = rng.nextInt(0, patternLen)
                pattern = pattern.replaceRange(idx, idx + 1, "*")
            }
            val m = WildMatch.new(pattern)
            for (patternIdx in 0 until rng.nextInt(0, 1_000)) {
                var input = pattern
                val patternLenLocal = pattern.length
                for (i in 0 until patternLenLocal) {
                    val c = pattern[patternLenLocal - 1 - i]
                    val idx = patternLenLocal - i - 1
                    if (c == '?') {
                        val randChar = randomAlphanumeric(rng).toString()
                        input = input.replaceRange(idx, idx + 1, randChar)
                    }
                    if (c == '*') {
                        val take = rng.nextInt(0, 15)
                        val sb = StringBuilder()
                        for (j in 0 until take) {
                            sb.append(randomAlphanumeric(rng))
                        }
                        input = input.replaceRange(idx, idx + 1, sb.toString())
                    }
                }
                assertTrue(
                    m.matches(input),
                    "Pattern ($patternIdx): $pattern doesn't match input: $input",
                )
            }
        }
    }

    @Test
    fun isMatch() {
        for (pattern in listOf(
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
        )) {
            val m = WildMatch.new(pattern)
            assertTrue(m.matches("cat"), "pattern $pattern should match 'cat'")
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
            val m = WildMatch.newCaseInsensitive(pattern)
            assertTrue(m.matches(input), "pattern $pattern should match $input case-insensitively")
        }
    }

    @Test
    fun noMatch() {
        for (pattern in listOf(
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
        )) {
            val m = WildMatch.new(pattern)
            assertFalse(m.matches("cat"), "pattern $pattern should not match 'cat'")
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
            val m = WildMatch.new(pattern)
            assertFalse(m.matches(expected), "pattern '$pattern' should not match '$expected'")
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
            val m = WildMatch.new(pattern)
            assertTrue(m.matches(expected), "Expected pattern $pattern to match $expected")
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
        val m = WildMatch.new(complexPattern)
        assertTrue(m.matches(text))
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
        val m = WildMatchPattern.new('%', '_', complexPattern)
        assertTrue(m.matches(text))
    }

    @Test
    fun compareViaEqual() {
        val m = WildMatch.new("c?*")
        assertTrue(m.matches("cat"))
        assertTrue(m.matches("car"))
        assertFalse(m.matches("dog"))
    }

    @Test
    fun compareEmpty() {
        val m = WildMatch.new("")
        assertFalse(m.matches("bar"))
        assertTrue(m.matches(""))
    }

    @Test
    fun compareDefault() {
        val m = WildMatch.default()
        assertTrue(m.matches(""))
        assertFalse(m.matches("bar"))
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
        val m = WildMatch.new("Foo/Bar")
        assertEquals("Foo/Bar", m.toString())
    }

    @Test
    fun toStringF() {
        val m = WildMatch.new("F")
        assertEquals("F", m.toString())
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
        val m = WildMatch.new("")
        assertEquals("", m.toString())
    }

    private fun randomAlphanumeric(rng: Random): Char {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return alphabet[rng.nextInt(0, alphabet.length)]
    }
}
