// port-lint: source src/lib.rs
package io.github.kotlinmania.wildmatch

/**
 * Match strings against a simple wildcard pattern.
 *
 * Tests a wildcard pattern `p` against an input string `s`. Returns true only when `p` matches
 * the entirety of `s`.
 *
 * See also the example described on [wikipedia](https://en.wikipedia.org/wiki/Matching_wildcards)
 * for matching wildcards.
 *
 * No escape characters are defined.
 *
 * - `?` matches exactly one occurrence of any character.
 * - `*` matches arbitrary many (including zero) occurrences of any character.
 *
 * Examples matching wildcards:
 * ```
 * check(WildMatch.new("cat").matches("cat"))
 * check(WildMatch.new("*cat*").matches("dog_cat_dog"))
 * check(WildMatch.new("c?t").matches("cat"))
 * check(WildMatch.new("c?t").matches("cot"))
 * ```
 * Examples not matching wildcards:
 * ```
 * check(!WildMatch.new("dog").matches("cat"))
 * check(!WildMatch.new("*d").matches("cat"))
 * check(!WildMatch.new("????").matches("cat"))
 * check(!WildMatch.new("?").matches("cat"))
 * ```
 *
 * You can specify custom [Char] values for the single and multi-character wildcards. For example,
 * to use `%` as the multi-character wildcard and `_` as the single-character wildcard:
 * ```
 * check(WildMatchPattern.new("%cat%", '%', '_').matches("dog_cat_dog"))
 * ```
 */

/**
 * Wildcard matcher used to match strings.
 *
 * [multiWildcard] is the character used to represent a multiple-character wildcard (e.g., `*`),
 * and [singleWildcard] is the character used to represent a single-character wildcard
 * (e.g., `?`).
 *
 * Throws [IllegalArgumentException] at construction time if both wildcard characters are identical.
 *
 * Examples:
 * ```
 * // Fails to construct: '*' cannot be both wildcards.
 * WildMatchPattern.new("", '*', '*')
 * ```
 *
 * ```
 * // Fails to construct: '*' cannot be both wildcards.
 * WildMatchPattern.newCaseInsensitive("", '*', '*')
 * ```
 *
 * ```
 * // Constructs fine.
 * WildMatchPattern.new("", '*', '?')
 * ```
 *
 * ```
 * // Constructs fine.
 * WildMatchPattern.newCaseInsensitive("", '*', '?')
 * ```
 */
class WildMatchPattern private constructor(
    val multiWildcard: Char,
    val singleWildcard: Char,
    private val pattern: List<Char>,
    val isCaseInsensitive: Boolean,
) {
    init {
        require(multiWildcard != singleWildcard) {
            "single and multi wildcards cannot be the same"
        }
    }

    /** Returns true if pattern applies to the given input string */
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
                                isCaseInsensitive &&
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

        // If we have reached the end of both the pattern and the text, the pattern matches the text.
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

    /** Returns the pattern formatted as a string of characters. */
    override fun toString(): String = pattern.joinToString("")

    /**
     * Equality combines structural equality between [WildMatchPattern] instances with
     * pattern matching against a [String].
     *
     * Two patterns are structurally equal when their wildcard characters, simplified pattern
     * contents, and case-insensitive flag all agree. When compared against a [String], the
     * pattern is equal to the string if and only if it matches it.
     */
    override fun equals(other: Any?): Boolean = when (other) {
        is WildMatchPattern ->
            multiWildcard == other.multiWildcard &&
                singleWildcard == other.singleWildcard &&
                isCaseInsensitive == other.isCaseInsensitive &&
                pattern == other.pattern
        is String -> matches(other)
        else -> false
    }

    override fun hashCode(): Int {
        var result = multiWildcard.hashCode()
        result = 31 * result + singleWildcard.hashCode()
        result = 31 * result + pattern.hashCode()
        result = 31 * result + isCaseInsensitive.hashCode()
        return result
    }

    companion object {
        /** Constructor with pattern which can be used for matching. */
        fun new(
            pattern: String,
            multiWildcard: Char,
            singleWildcard: Char,
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
                isCaseInsensitive = false,
            )
        }

        /**
         * Constructor with pattern which can be used for matching with case-insensitive
         * comparison.
         */
        fun newCaseInsensitive(
            pattern: String,
            multiWildcard: Char,
            singleWildcard: Char,
        ): WildMatchPattern {
            val base = new(pattern, multiWildcard, singleWildcard)
            return WildMatchPattern(
                multiWildcard = base.multiWildcard,
                singleWildcard = base.singleWildcard,
                pattern = base.pattern,
                isCaseInsensitive = true,
            )
        }

        /**
         * Constructs a default [WildMatchPattern] with the supplied wildcard characters and an
         * empty pattern. Produced by the same shape as the upstream `Default` derive.
         */
        fun default(multiWildcard: Char, singleWildcard: Char): WildMatchPattern =
            WildMatchPattern(
                multiWildcard = multiWildcard,
                singleWildcard = singleWildcard,
                pattern = emptyList(),
                isCaseInsensitive = false,
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
