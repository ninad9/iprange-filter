package com.codingtest.iprange.controller

import com.codingtest.iprange.service.IpRangeServiceInterface
import com.codingtest.iprange.util.Region
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(IpRangeController::class)
class IpRangeControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @TestConfiguration
    class MockServiceConfig {
        @Bean
        fun ipRangeService(): IpRangeServiceInterface = mock()
    }

    @Autowired
    private lateinit var ipRangeService: IpRangeServiceInterface

    /**
     * Test Spec: Should return IPv4 prefix when valid region and ipType = ipv4
     */
    @Test
    fun getIpRanges_shouldReturnIPsWhenValidRegionAndIpv4TypeAreProvided() {
        whenever(ipRangeService.getIpRanges(Region.US, "ipv4"))
            .thenReturn(Mono.just("1.2.3.0/24"))

        webTestClient.get()
            .uri("/ip-ranges?region=us&ipType=ipv4")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE)
            .expectBody(String::class.java)
            .isEqualTo("1.2.3.0/24")
    }

    /**
     * Test Spec: Should return IPv6 prefix for valid region when ipType = ipv6
     */
    @Test
    fun getIpRanges_shouldReturnIPsWhenAsiaRegionAndIpv6TypeAreProvided() {
        whenever(ipRangeService.getIpRanges(Region.AS, "ipv6"))
            .thenReturn(Mono.just("2404:6800:4001::/48"))

        webTestClient.get()
            .uri("/ip-ranges?region=as&ipType=ipv6")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE)
            .expectBody(String::class.java)
            .isEqualTo("2404:6800:4001::/48")
    }

    /**
     * Test Spec: Should use default ipType = all and return both ipv4 and ipv6 addresses
     * when ipType is not provided in query
     */
    @Test
    fun getIpRanges_shouldUseDefaultIpTypeWhenNotProvided() {
        whenever(ipRangeService.getIpRanges(Region.EU, "all"))
            .thenReturn(Mono.just("2.2.2.0/24\n2001:db8::/32"))

        webTestClient.get()
            .uri("/ip-ranges?region=eu")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE)
            .expectBody(String::class.java)
            .isEqualTo("2.2.2.0/24\n2001:db8::/32")
    }

    /**
     * Test Spec: Should return global prefix when region is not provided
     */
    @Test
    fun getIpRanges_shouldUseRegionALLWhenRegionIsNotProvided() {
        whenever(ipRangeService.getIpRanges(Region.ALL, "all"))
            .thenReturn(Mono.just("203.0.113.0/24\\n2001:4860:4860::8888/32\"\n"))

        webTestClient.get()
            .uri("/ip-ranges")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("203.0.113.0/24\\n2001:4860:4860::8888/32\"\n")
    }

    /**
     * Test Spec: Should return error for invalid region param
     */
    @Test
    fun getIpRanges_shouldReturnErrorMessageWhenRegionIsInvalid() {
        webTestClient.get()
            .uri("/ip-ranges?region=unknown&ipType=ipv6")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("Invalid region: unknown")
    }

    /**
     * Test Spec: Should return error for unsupported ipType
     */
    @Test
    fun getIpRanges_shouldReturnErrorWhenIpTypeIsInvalid() {
        webTestClient.get()
            .uri("/ip-ranges?region=us&ipType=abcd")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("Invalid IP type: abcd")
    }
}