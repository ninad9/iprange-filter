package com.codingtest.iprange.service

import com.codingtest.iprange.data.GcpResponse
import com.codingtest.iprange.util.Region
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.core.publisher.Mono
import java.io.IOException
import java.net.URI

class IpRangeServiceTest {

    private fun mockService(prefixes: List<Map<String, String>>): IpRangeService {
        val (webClient, responseSpec) = generalMock()
        whenever(responseSpec.bodyToMono(GcpResponse::class.java)).thenReturn(Mono.just(GcpResponse(prefixes)))
        return IpRangeService(webClient)
    }

    private fun mockServiceForException(message: String): IpRangeService {
        val (webClient, responseSpec) = generalMock()
        whenever(responseSpec.bodyToMono(GcpResponse::class.java)).thenReturn(
            Mono.error(RuntimeException(message))
        )
        return IpRangeService(webClient)
    }

    private fun generalMock(): Pair<WebClient, WebClient.ResponseSpec> {
        val webClient = mock<WebClient>()
        val uriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val headersSpec = mock<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mock<WebClient.ResponseSpec>()

        whenever(webClient.get()).thenReturn(uriSpec)
        whenever(uriSpec.uri("/ipranges/cloud.json")).thenReturn(headersSpec)
        whenever(headersSpec.retrieve()).thenReturn(responseSpec)
        return Pair(webClient, responseSpec)
    }

    /**
     * Test Spec: Should return correct IPv4 prefix for matching US region and ipVersion = "ipv4"
     * */
    @Test
    fun getIpRanges_shouldReturnIpv4ForUSRegion() {
        val mockJson = listOf(
            mapOf("scope" to "us-central1", "ipv4Prefix" to "1.1.1.0/24")
        )

        val service = mockService(mockJson)
        val result = service.getIpRanges(Region.US, "ipv4").block()

        assertEquals("1.1.1.0/24", result?.trim())
    }

    /**
     * Test Spec: Should return correct IPv6 prefix for matching EU region and ipVersion = "ipv6"
     */
    @Test
    fun getIpRanges_shouldReturnIpv6ForEURegion() {
        val mockJson = listOf(
            mapOf("scope" to "europe-west1", "ipv6Prefix" to "2001:db8::/32")
        )

        val service = mockService(mockJson)
        val result = service.getIpRanges(Region.EU, "ipv6").block()

        assertEquals("2001:db8::/32", result?.trim())
    }

    /**
     * Test Spec: Should return both IPv4 and IPv6 prefixes when ipVersion = "all"
     */
    @Test
    fun getIpRanges_shouldReturnBothIpv4AndIpv6ForIpVersionAll() {
        val mockJson = listOf(
            mapOf("scope" to "asia-south1", "ipv4Prefix" to "3.3.3.0/24"),
            mapOf("scope" to "asia-south1", "ipv6Prefix" to "2404:6800::/32")
        )

        val service = mockService(mockJson)
        val result = service.getIpRanges(Region.AS, "all").block()

        assertEquals("3.3.3.0/24\n2404:6800::/32", result?.trim())
    }

    /**
     * Test Spec: Should return all prefixes (regardless of region/scope) when Region = ALL
     */
    @Test
    fun getIpRanges_shouldReturnAllPrefixesForRegionALL() {
        val mockJson = listOf(
            mapOf("scope" to "us-west1", "ipv4Prefix" to "4.4.4.0/24"),
            mapOf("scope" to "europe-west1", "ipv6Prefix" to "2607:f8b0::/32")
        )

        val service = mockService(mockJson)
        val result = service.getIpRanges(Region.ALL, "all").block()

        assertEquals("4.4.4.0/24\n2607:f8b0::/32", result?.trim())
    }

    /**
     * Test Spec: Should return empty result when region does not match any scope in JSON
     */
    @Test
    fun getIpRanges_shouldReturnEmptyWhenRegionDoesNotMatchScope() {
        val mockJson = listOf(
            mapOf("scope" to "europe-west1", "ipv4Prefix" to "2.2.2.0/24")
        )

        val service = mockService(mockJson)
        val result = service.getIpRanges(Region.US, "ipv4").block()

        assertEquals("", result)
    }

    /**
     * Test Spec: Should return empty result when ipVersion does not match any prefix type
     */
    @Test
    fun getIpRanges_shouldReturnEmptyWhenIpVersionDoesNotMatchPrefix() {
        val mockJson = listOf(
            mapOf("scope" to "us-central1", "ipv4Prefix" to "1.2.3.0/24")
        )

        val service = mockService(mockJson)
        val result = service.getIpRanges(Region.US, "ipv6").block()

        assertEquals("", result)
    }

    /**
     * Test Spec: Returns fallback message when WebClient fails (e.g. timeout)
     */
    @Test
    fun getIpRanges_shouldReturnFallbackMessageWhenWebClientFails() {
        val service = mockServiceForException("Simulated timeout")

        val result = service.getIpRanges(Region.ALL, "all").block()

        assertEquals("Could not fetch IP ranges: Simulated timeout", result)
    }

    /**
     * Test Spec: Should return IPs correctly for Region.GL
     */
    @Test
    fun getIpRanges_shouldReturnGlobalScopeIpsForRegionGL() {
        val mockJson = listOf(
            mapOf("scope" to "global", "ipv4Prefix" to "8.8.8.0/24"),
            mapOf("scope" to "global", "ipv6Prefix" to "2001:4860:4860::/48")
        )

        val service = mockService(mockJson)
        val result = service.getIpRanges(Region.GL, "all").block()

        assertEquals("8.8.8.0/24\n2001:4860:4860::/48", result?.trim())
    }

    /**
     * Test Spec: Should return "Connection error" message when WebClientRequestException occurs
     */
    @Test
    fun getIpRanges_shouldReturnConnectionErrorMessage() {
        val (webClient, responseSpec) = generalMock()
        whenever(responseSpec.bodyToMono(GcpResponse::class.java)).thenReturn(
            Mono.error(
                WebClientRequestException(
                    IOException("Connection refused"),
                    HttpMethod.GET,
                    URI("http://test.com"),
                    HttpHeaders.EMPTY
                )
            )
        )
        val service = IpRangeService(webClient)
        val result = service.getIpRanges(Region.ALL, "all").block()

        assertEquals("Connection error: Connection refused", result)
    }

    /**
     * Test Spec: Should reuse cached response within refresh interval
     */
    @Test
    fun getIpRanges_shouldUseCacheWithinRefreshInterval() {
        val mockJson = listOf(
            mapOf("scope" to "us-central1", "ipv4Prefix" to "5.5.5.0/24")
        )
        val (webClient, responseSpec) = generalMock()
        whenever(responseSpec.bodyToMono(GcpResponse::class.java)).thenReturn(Mono.just(GcpResponse(mockJson)))
        val service = IpRangeService(webClient)
        val firstCall = service.getIpRanges(Region.US, "ipv4").block()
        assertEquals("5.5.5.0/24", firstCall?.trim())

        val secondCall = service.getIpRanges(Region.US, "ipv4").block()
        assertEquals("5.5.5.0/24", secondCall?.trim())
    }
}