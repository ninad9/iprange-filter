package com.codingtest.iprange.service

import com.codingtest.iprange.util.Region
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class IpRangeServiceTest {

    private fun mockServiceWith(json: Map<String, Any>): IpRangeService {
        val webClient = mock<WebClient>()
        val uriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val headersSpec = mock<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mock<WebClient.ResponseSpec>()

        whenever(webClient.get()).thenReturn(uriSpec)
        whenever(uriSpec.uri("/ipranges/cloud.json")).thenReturn(headersSpec)
        whenever(headersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(Map::class.java)).thenReturn(Mono.just(json))

        return IpRangeService(webClient)
    }

    /**
     * Test Spec: Should return correct IPv4 prefix for matching US region and ipVersion = "ipv4"
     * */
    @Test
    fun getIpRanges_shouldReturnIpv4ForUSRegion() {
        val mockJson = mapOf(
            "prefixes" to listOf(
                mapOf("scope" to "us-central1", "ipv4Prefix" to "1.1.1.0/24")
            )
        )

        val service = mockServiceWith(mockJson)
        val result = service.getIpRanges(Region.US, "ipv4").block()

        assertEquals("1.1.1.0/24", result?.trim())
    }

    /**
     * Test Spec: Should return correct IPv6 prefix for matching EU region and ipVersion = "ipv6"
     */
    @Test
    fun getIpRanges_shouldReturnIpv6ForEURegion() {
        val mockJson = mapOf(
            "prefixes" to listOf(
                mapOf("scope" to "europe-west1", "ipv6Prefix" to "2001:db8::/32")
            )
        )

        val service = mockServiceWith(mockJson)
        val result = service.getIpRanges(Region.EU, "ipv6").block()

        assertEquals("2001:db8::/32", result?.trim())
    }

    /**
     * Test Spec: Should return both IPv4 and IPv6 prefixes when ipVersion = "all"
     */
    @Test
    fun getIpRanges_shouldReturnBothIpv4AndIpv6ForIpVersionAll() {
        val mockJson = mapOf(
            "prefixes" to listOf(
                mapOf("scope" to "asia-south1", "ipv4Prefix" to "3.3.3.0/24"),
                mapOf("scope" to "asia-south1", "ipv6Prefix" to "2404:6800::/32")
            )
        )

        val service = mockServiceWith(mockJson)
        val result = service.getIpRanges(Region.AS, "all").block()

        assertEquals("3.3.3.0/24\n2404:6800::/32", result?.trim())
    }

    /**
     * Test Spec: Should return all prefixes (regardless of region/scope) when Region = ALL
     */
    @Test
    fun getIpRanges_shouldReturnAllPrefixesForRegionALL() {
        val mockJson = mapOf(
            "prefixes" to listOf(
                mapOf("scope" to "us-west1", "ipv4Prefix" to "4.4.4.0/24"),
                mapOf("scope" to "europe-west1", "ipv6Prefix" to "2607:f8b0::/32")
            )
        )

        val service = mockServiceWith(mockJson)
        val result = service.getIpRanges(Region.ALL, "all").block()

        assertEquals("4.4.4.0/24\n2607:f8b0::/32", result?.trim())
    }

    /**
     * Test Spec: Should return empty result when region does not match any scope in JSON
     */
    @Test
    fun getIpRanges_shouldReturnEmptyWhenRegionDoesNotMatchScope() {
        val mockJson = mapOf(
            "prefixes" to listOf(
                mapOf("scope" to "europe-west1", "ipv4Prefix" to "2.2.2.0/24")
            )
        )

        val service = mockServiceWith(mockJson)
        val result = service.getIpRanges(Region.US, "ipv4").block()

        assertEquals("", result)
    }

    /**
     * Test Spec: Should return empty result when ipVersion does not match any prefix type
     */
    @Test
    fun getIpRanges_shouldReturnEmptyWhenIpVersionDoesNotMatchPrefix() {
        val mockJson = mapOf(
            "prefixes" to listOf(
                mapOf("scope" to "us-central1", "ipv4Prefix" to "1.2.3.0/24")
            )
        )

        val service = mockServiceWith(mockJson)
        val result = service.getIpRanges(Region.US, "ipv6").block()

        assertEquals("", result)
    }

}