package io.github.kotlinmania.wildmatch

/**
 * A wildcard matcher using `*` as the multi-character wildcard and `?` as the
 * single-character wildcard.
 */
object WildMatch {
    /** Constructor with pattern which can be used for matching. */
    fun new(pattern: String): WildMatchPattern =
        WildMatchPattern.new(pattern, '*', '?')

    /** Constructor with pattern which can be used for matching with case-insensitive comparison. */
    fun newCaseInsensitive(pattern: String): WildMatchPattern =
        WildMatchPattern.newCaseInsensitive(pattern, '*', '?')

    /** Produces an empty pattern with the default `*` / `?` wildcards. */
    fun default(): WildMatchPattern =
        WildMatchPattern.default('*', '?')
}
