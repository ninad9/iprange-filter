package com.codingtest.iprange.util

enum class Region (val scopes: List<String>) {
    EU(listOf("europe")),
    US(listOf("us-central", "us-east", "us-west", "us")),
    ME(listOf("me-")),
    NA(listOf("northamerica", "north-america")),
    SA(listOf("southamerica", "south-america")),
    AS(listOf("asia")),
    AF(listOf("africa")),
    AUS(listOf("australia")),
    ALL(listOf("all"));

    companion object {
        fun fromString(name: String): Region? =
            values().find { it.name.equals(name, ignoreCase = true) }
    }
}