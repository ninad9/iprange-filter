package com.codingtest.iprange.data

/**
 * Data class representing the response structure from the GCP IP ranges endpoint.
 */
data class GcpResponse (
    val prefixes: List<Map<String, String>> = emptyList()
)