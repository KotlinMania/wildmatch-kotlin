package io.github.kotlinmania.wildmatch

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.BooleanDeserialize
import io.github.kotlinmania.serdecore.de.CharDeserialize
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.IgnoredAny
import io.github.kotlinmania.serdecore.de.MapAccess
import io.github.kotlinmania.serdecore.de.StringDeserialize
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.de.mutableListDeserialize
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer

/**
 * Wildcard matcher used to match strings.
 *
 * [multiWildcard] is the character used to represent a multiple-character
 * wildcard (e.g., `*`), and [singleWildcard] is the character used to
 * represent a single-character wildcard (e.g., `?`).
 *
 * Throws [IllegalArgumentException] at construction time if both wildcard
 * characters are identical.
 */
class WildMatchPattern(
    val multiWildcard: Char = '*',
    val singleWildcard: Char = '?',
    private val pattern: List<Char> = emptyList(),
    private val caseInsensitive: Boolean = false,
) : Comparable<WildMatchPattern>,
    Serialize {
    init {
        require(multiWildcard != singleWildcard) {
            "single and multi wildcards cannot be the same"
        }
    }

    @Deprecated(
        message = "use matches instead",
        replaceWith = ReplaceWith("matches(input)"),
        level = DeprecationLevel.WARNING,
    )
    fun isMatch(input: String): Boolean = matches(input)

    /** Returns true if pattern applies to the given input string. */
    fun matches(input: String): Boolean {
        if (pattern.isEmpty()) {
            return input.isEmpty()
        }
        val inputChars = input.toCharArray()
        var inputCursor = 0
        var patternIdx = 0
        if (inputCursor < inputChars.size) {
            var inputChar = inputChars[inputCursor]
            inputCursor += 1
            val none = Int.MAX_VALUE
            var startIdx = none
            var matchedCursor = 0

            while (true) {
                if (patternIdx < pattern.size && pattern[patternIdx] == multiWildcard) {
                    startIdx = patternIdx
                    matchedCursor = inputCursor
                    patternIdx += 1
                } else if (patternIdx < pattern.size &&
                    (
                        pattern[patternIdx] == singleWildcard ||
                            pattern[patternIdx] == inputChar ||
                            (
                                caseInsensitive &&
                                    pattern[patternIdx].lowercase() == inputChar.lowercase()
                            )
                    )
                ) {
                    patternIdx += 1
                    if (inputCursor < inputChars.size) {
                        inputChar = inputChars[inputCursor]
                        inputCursor += 1
                    } else {
                        break
                    }
                } else if (startIdx != none) {
                    patternIdx = startIdx + 1
                    if (matchedCursor < inputChars.size) {
                        inputChar = inputChars[matchedCursor]
                        matchedCursor += 1
                        inputCursor = matchedCursor
                    } else {
                        break
                    }
                } else {
                    return false
                }
            }
        }

        while (patternIdx < pattern.size && pattern[patternIdx] == multiWildcard) {
            patternIdx += 1
        }

        // A match consumed both the pattern and the text.
        return patternIdx == pattern.size
    }

    /**
     * Returns the pattern string.
     *
     * N.B. Consecutive multi-wildcards are simplified to a single multi-wildcard.
     */
    fun pattern(): String = pattern.joinToString("")

    /** Returns the pattern string as a list of chars. */
    fun patternChars(): List<Char> = pattern

    /** Formats the pattern into a string representation. */
    fun fmt(): String = pattern.joinToString("")

    /** Returns whether the pattern is case-insensitive. */
    fun isCaseInsensitive(): Boolean = caseInsensitive

    /** Returns true if the pattern matches the given string. */
    fun eq(other: String): Boolean = matches(other)

    /** Returns the pattern formatted as a string of characters. */
    override fun toString(): String = pattern.joinToString("")

    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
        val state = serializer.serializeStruct("WildMatchPattern", 2).getOrThrow()
        state.serializeField("pattern", PatternSerialize(pattern)).getOrThrow()
        state.serializeField("case_insensitive", BoolSerialize(caseInsensitive)).getOrThrow()
        return state.end()
    }

    /**
     * Equality combines structural equality between [WildMatchPattern] instances
     * with pattern matching against a [String].
     */
    override fun equals(other: Any?): Boolean =
        when (other) {
            is WildMatchPattern ->
                multiWildcard == other.multiWildcard &&
                    singleWildcard == other.singleWildcard &&
                    caseInsensitive == other.caseInsensitive &&
                    pattern == other.pattern
            is String -> matches(other)
            else -> false
        }

    override fun hashCode(): Int {
        var result = multiWildcard.hashCode()
        result = 31 * result + singleWildcard.hashCode()
        result = 31 * result + pattern.hashCode()
        result = 31 * result + caseInsensitive.hashCode()
        return result
    }

    /** Lexicographic comparison over wildcard chars, pattern chars, then mode. */
    override fun compareTo(other: WildMatchPattern): Int {
        val byMulti = multiWildcard.compareTo(other.multiWildcard)
        if (byMulti != 0) return byMulti
        val bySingle = singleWildcard.compareTo(other.singleWildcard)
        if (bySingle != 0) return bySingle
        val shorter = if (pattern.size < other.pattern.size) pattern.size else other.pattern.size
        for (i in 0 until shorter) {
            val byChar = pattern[i].compareTo(other.pattern[i])
            if (byChar != 0) return byChar
        }
        val byLen = pattern.size.compareTo(other.pattern.size)
        if (byLen != 0) return byLen
        return caseInsensitive.compareTo(other.caseInsensitive)
    }

    companion object : Deserialize<WildMatchPattern> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<WildMatchPattern> =
            deserializer.deserializeStruct(
                "WildMatchPattern",
                listOf("pattern", "case_insensitive"),
                WildMatchPatternVisitor,
            )

        /** Constructor with pattern which can be used for matching. */
        fun new(
            pattern: String,
            multiWildcard: Char = '*',
            singleWildcard: Char = '?',
        ): WildMatchPattern {
            require(multiWildcard != singleWildcard) {
                "single and multi wildcards cannot be the same"
            }

            val simplified: MutableList<Char> = pattern.toMutableList()
            var newLen = simplified.size
            var wildcardCount = 0

            for (idx in simplified.indices.reversed()) {
                if (simplified[idx] == multiWildcard) {
                    wildcardCount += 1
                } else {
                    if (wildcardCount > 1) {
                        newLen -= wildcardCount - 1
                        rotateLeft(simplified, idx + 1, simplified.size, wildcardCount - 1)
                    }
                    wildcardCount = 0
                }
            }
            if (wildcardCount > 1) {
                newLen -= wildcardCount - 1
                rotateLeft(simplified, 0, simplified.size, wildcardCount - 1)
            }

            while (simplified.size > newLen) {
                simplified.removeAt(simplified.size - 1)
            }

            return WildMatchPattern(
                multiWildcard = multiWildcard,
                singleWildcard = singleWildcard,
                pattern = simplified.toList(),
                caseInsensitive = false,
            )
        }

        /**
         * Constructor with pattern which can be used for matching with
         * case-insensitive comparison.
         */
        fun newCaseInsensitive(
            pattern: String,
            multiWildcard: Char = '*',
            singleWildcard: Char = '?',
        ): WildMatchPattern {
            val base = new(pattern, multiWildcard, singleWildcard)
            return WildMatchPattern(
                multiWildcard = base.multiWildcard,
                singleWildcard = base.singleWildcard,
                pattern = base.pattern,
                caseInsensitive = true,
            )
        }

        /** Constructs an empty [WildMatchPattern] with the supplied wildcard characters. */
        fun default(multiWildcard: Char = '*', singleWildcard: Char = '?'): WildMatchPattern =
            WildMatchPattern(
                multiWildcard = multiWildcard,
                singleWildcard = singleWildcard,
                pattern = emptyList(),
                caseInsensitive = false,
            )

        private fun rotateLeft(list: MutableList<Char>, start: Int, end: Int, by: Int) {
            val len = end - start
            if (len <= 1 || by == 0) return
            val k = ((by % len) + len) % len
            if (k == 0) return
            val head = ArrayList<Char>(k)
            for (i in 0 until k) {
                head.add(list[start + i])
            }
            for (i in 0 until len - k) {
                list[start + i] = list[start + k + i]
            }
            for (i in 0 until k) {
                list[start + len - k + i] = head[i]
            }
        }
    }
}

private class PatternSerialize(
    private val pattern: List<Char>,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
        val seq = serializer.serializeSeq(pattern.size).getOrThrow()
        for (c in pattern) {
            seq.serializeElement(CharSerialize(c)).getOrThrow()
        }
        return seq.end()
    }
}

private class CharSerialize(
    private val value: Char,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeChar(value)
}

private class BoolSerialize(
    private val value: Boolean,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeBool(value)
}

private object WildMatchPatternVisitor : Visitor<WildMatchPattern> {
    override fun expecting(): String = "struct WildMatchPattern"

    override fun <A : MapAccess> visitMap(access: A): SerdeResult<WildMatchPattern> {
        var pattern: List<Char>? = null
        var caseInsensitive: Boolean? = null
        while (true) {
            val key = access.nextKey(StringDeserialize).getOrThrow() ?: break
            when (key) {
                "pattern" -> pattern = access.nextValue(mutableListDeserialize(CharDeserialize)).getOrThrow()
                "case_insensitive" -> caseInsensitive = access.nextValue(BooleanDeserialize).getOrThrow()
                else -> access.nextValue(IgnoredAny).getOrThrow()
            }
        }
        if (pattern == null || caseInsensitive == null) {
            return SerdeResult.failure(SerdeError.custom("missing field in WildMatchPattern"))
        }
        return SerdeResult.success(
            WildMatchPattern(
                multiWildcard = '*',
                singleWildcard = '?',
                pattern = pattern,
                caseInsensitive = caseInsensitive,
            ),
        )
    }
}
