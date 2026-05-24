// port-lint: source src/lib.rs
package io.github.kotlinmania.wildmatch

import kotlinx.serialization.Serializable

/**
 * Match strings against a simple wildcard pattern.
 * Tests a wildcard pattern `p` against an input string `s`. Returns true only when `p` matches the entirety of `s`.
 *
 * See also the example described on [wikipedia](https://en.wikipedia.org/wiki/Matching_wildcards) for matching wildcards.
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
 * You can specify custom [Char] values for the single and multi-character
 * wildcards. For example, to use `%` as the multi-character wildcard and
 * `_` as the single-character wildcard:
 * ```
 * check(WildMatchPattern.new('%', '_', "%cat%").matches("dog_cat_dog"))
 * ```
 */

/**
 * A wildcard matcher using `*` as the multi-character wildcard and `?` as
 * the single-character wildcard.
 */
public object WildMatch {
    public fun new(pattern: String): WildMatchPattern =
        WildMatchPattern.new('*', '?', pattern)

    public fun newCaseInsensitive(pattern: String): WildMatchPattern =
        WildMatchPattern.newCaseInsensitive('*', '?', pattern)

    public fun default(): WildMatchPattern =
        WildMatchPattern.default('*', '?')
}

/**
 * Wildcard matcher used to match strings.
 *
 * [multiWildcard] is the character used to represent a multiple-character
 * wildcard (e.g., `*`), and [singleWildcard] is the character used to
 * represent a single-character wildcard (e.g., `?`).
 *
 * # Throws
 *
 * Throws [IllegalArgumentException] at construction time if both wildcard
 * characters are identical.
 *
 * # Examples
 *
 * ```
 * // Throws: '*' cannot be both wildcards.
 * // WildMatchPattern.new('*', '*', "")
 *
 * // Throws: '*' cannot be both wildcards.
 * // WildMatchPattern.newCaseInsensitive('*', '*', "")
 *
 * // OK.
 * WildMatchPattern.new('*', '?', "")
 *
 * // OK.
 * WildMatchPattern.newCaseInsensitive('*', '?', "")
 * ```
 */
@Serializable
public class WildMatchPattern public constructor(
    public val multiWildcard: Char,
    public val singleWildcard: Char,
    private val pattern: List<Char>,
    private val caseInsensitive: Boolean,
) : Comparable<WildMatchPattern> {

    init {
        require(multiWildcard != singleWildcard) {
            "single and multi wildcards cannot be the same"
        }
    }

    override fun toString(): String {
        val sb = StringBuilder(pattern.size)
        for (c in pattern) {
            sb.append(c)
        }
        return sb.toString()
    }

    /** Returns true if pattern applies to the given input string */
    public fun matches(input: String): Boolean {
        if (pattern.isEmpty()) {
            return input.isEmpty()
        }
        var patternIdx = 0
        if (input.isNotEmpty()) {
            var inputChar = input[0]
            var inputPos = 1
            val none = -1
            var startIdx = none
            var matchedPos = 0

            loop@ while (true) {
                if (patternIdx < pattern.size && pattern[patternIdx] == multiWildcard) {
                    startIdx = patternIdx
                    matchedPos = inputPos
                    patternIdx += 1
                } else if (
                    patternIdx < pattern.size &&
                    (pattern[patternIdx] == singleWildcard ||
                        pattern[patternIdx] == inputChar ||
                        (caseInsensitive &&
                            pattern[patternIdx].lowercase() == inputChar.lowercase()))
                ) {
                    patternIdx += 1
                    if (inputPos < input.length) {
                        inputChar = input[inputPos]
                        inputPos += 1
                    } else {
                        break@loop
                    }
                } else if (startIdx != none) {
                    patternIdx = startIdx + 1
                    if (matchedPos < input.length) {
                        inputChar = input[matchedPos]
                        matchedPos += 1
                    } else {
                        break@loop
                    }
                    inputPos = matchedPos
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
     * N.B. Consecutive multi-wildcards are simplified to a single multi-wildcard.
     */
    public fun pattern(): String {
        val sb = StringBuilder(pattern.size)
        for (c in pattern) {
            sb.append(c)
        }
        return sb.toString()
    }

    /** Returns the pattern string as a list of chars. */
    public fun patternChars(): List<Char> = pattern

    /** Returns if the pattern is case-insensitive. */
    public fun isCaseInsensitive(): Boolean = caseInsensitive

    @Deprecated("use matches instead", ReplaceWith("matches(input)"))
    public fun isMatch(input: String): Boolean = this.matches(input)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WildMatchPattern) return false
        return multiWildcard == other.multiWildcard &&
            singleWildcard == other.singleWildcard &&
            pattern == other.pattern &&
            caseInsensitive == other.caseInsensitive
    }

    override fun hashCode(): Int {
        var result = multiWildcard.hashCode()
        result = 31 * result + singleWildcard.hashCode()
        result = 31 * result + pattern.hashCode()
        result = 31 * result + caseInsensitive.hashCode()
        return result
    }

    override fun compareTo(other: WildMatchPattern): Int {
        val n = minOf(pattern.size, other.pattern.size)
        for (i in 0 until n) {
            val cmp = pattern[i].compareTo(other.pattern[i])
            if (cmp != 0) return cmp
        }
        val sizeCmp = pattern.size.compareTo(other.pattern.size)
        if (sizeCmp != 0) return sizeCmp
        return caseInsensitive.compareTo(other.caseInsensitive)
    }

    public companion object {
        /** Constructor with pattern which can be used for matching. */
        public fun new(
            multiWildcard: Char,
            singleWildcard: Char,
            pattern: String,
        ): WildMatchPattern {
            require(multiWildcard != singleWildcard) {
                "single and multi wildcards cannot be the same"
            }

            val simplified: MutableList<Char> = pattern.toMutableList()
            var newLen = simplified.size
            var wildcardCount = 0

            for (idx in (simplified.size - 1) downTo 0) {
                if (simplified[idx] == multiWildcard) {
                    wildcardCount += 1
                } else {
                    if (wildcardCount > 1) {
                        newLen -= wildcardCount - 1
                        rotateLeftInPlace(simplified, idx + 1, simplified.size, wildcardCount - 1)
                    }
                    wildcardCount = 0
                }
            }
            if (wildcardCount > 1) {
                newLen -= wildcardCount - 1
                rotateLeftInPlace(simplified, 0, simplified.size, wildcardCount - 1)
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

        /** Constructor with pattern which can be used for matching with case-insensitive comparison. */
        public fun newCaseInsensitive(
            multiWildcard: Char,
            singleWildcard: Char,
            pattern: String,
        ): WildMatchPattern {
            val m = new(multiWildcard, singleWildcard, pattern)
            return WildMatchPattern(
                multiWildcard = m.multiWildcard,
                singleWildcard = m.singleWildcard,
                pattern = m.pattern,
                caseInsensitive = true,
            )
        }

        public fun default(multiWildcard: Char, singleWildcard: Char): WildMatchPattern {
            return WildMatchPattern(
                multiWildcard = multiWildcard,
                singleWildcard = singleWildcard,
                pattern = emptyList(),
                caseInsensitive = false,
            )
        }

        private fun rotateLeftInPlace(
            list: MutableList<Char>,
            from: Int,
            end: Int,
            n: Int,
        ) {
            val len = end - from
            if (len <= 1 || n == 0) return
            val shift = ((n % len) + len) % len
            if (shift == 0) return
            val temp = ArrayList<Char>(shift)
            for (i in 0 until shift) {
                temp.add(list[from + i])
            }
            for (i in shift until len) {
                list[from + i - shift] = list[from + i]
            }
            for (i in 0 until shift) {
                list[end - shift + i] = temp[i]
            }
        }
    }
}
