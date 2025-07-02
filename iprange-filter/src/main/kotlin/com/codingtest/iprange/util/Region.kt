package com.codingtest.iprange.util

/**
 * Represents a geographic region used to filter IP prefixes based on service scopes.
 */
enum class Region(val scopes: List<String>) {
    EU(listOf("europe")),
    US(listOf("us-central", "us-east", "us-west", "us")),
    ME(listOf("me-")),
    NA(listOf("northamerica")),
    SA(listOf("southamerica")),
    AS(listOf("asia")),
    AF(listOf("africa")),
    AUS(listOf("australia")),
    GL(listOf("global")),
    ALL(listOf("all"));

    /**
     * Converts a string name into a corresponding Region enum value.
     *
     * @param name The name to parse.  Example: `"eu"`, `"US"`, `"all"`
     * @return Matching Region or `null` if no match found
     */
    companion object {
        fun fromString(name: String): Region? =
            entries.find { it.name.equals(name, ignoreCase = true) }
    }
}