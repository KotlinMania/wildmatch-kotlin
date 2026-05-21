// port-lint: source src/lib.rs
package io.github.kotlinmania.wildmatch

/**
 * A wildcard matcher using `*` as the multi-character wildcard and `?` as the
 * single-character wildcard.
 *
 * This entry point is the Kotlin analog of the upstream default wildcard matcher.
 * Kotlin has no const-generic char parameters, so the default wildcards are bound
 * through factory methods on this object instead of through the type.
 */
object WildMatch {
    /** Constructor with pattern which can be used for matching. */
    fun new(pattern: String): WildMatchPattern =
        WildMatchPattern.new(pattern, '*', '?')

    /** Constructor with pattern which can be used for matching with case-insensitive comparison. */
    fun newCaseInsensitive(pattern: String): WildMatchPattern =
        WildMatchPattern.newCaseInsensitive(pattern, '*', '?')

    /**
     * Produces the default [WildMatchPattern] paired with the `*` / `?` wildcards: an empty
     * pattern with case-sensitive matching.
     */
    fun default(): WildMatchPattern =
        WildMatchPattern.default('*', '?')
}
