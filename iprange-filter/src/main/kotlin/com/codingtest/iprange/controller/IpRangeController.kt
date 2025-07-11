package com.codingtest.iprange.controller

import com.codingtest.iprange.service.IpRangeServiceInterface
import com.codingtest.iprange.util.Region
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

/**
 * REST controller that exposes an endpoint to retrieve filtered
 * IP ranges from GCP based on region and IP version.
 *
 */
@RestController
class IpRangeController(private val ipRangeService: IpRangeServiceInterface) {

    /**
     * Retrieves GCP IP ranges based on the given region and IP version type.
     *
     * @param region Query parameter indicating region (e.g. "EU","AS", "US", etc.).
     *               If omitted, defaults to "all".
     * @param ipType Query parameter indicating IP version: "ipv4", "ipv6", or "all".
     *               Defaults to "all".
     * @return A [Mono] containing either a newline-separated list of IP prefixes or a validation error message.
     *
     */
    @GetMapping("/ip-ranges", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getIpRanges(
        @RequestParam region: String?,
        @RequestParam(required = false, defaultValue = "all") ipType: String
    ): Mono<String> {
        val resolvedRegion = Region.fromString(region ?: "all")
            ?: return Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid region: $region"))

        val ipVersionType = ipType.lowercase()
        if (ipType != "all" && ipVersionType !in setOf("ipv4", "ipv6")) {
            return Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP type: $ipType"))
        }

        return ipRangeService.getIpRanges(resolvedRegion, ipVersionType)
    }
}