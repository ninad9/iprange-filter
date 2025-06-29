package com.codingtest.iprange.controller

import com.codingtest.iprange.service.IpRangeServiceInterface
import com.codingtest.iprange.util.Region
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class IpRangeController(private val ipRangeService: IpRangeServiceInterface) {

    @GetMapping("/ip-ranges", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getIpRanges(
        @RequestParam region: String?,
        @RequestParam(required = false, defaultValue = "all") ipType: String
    ): Mono<String> {
        val resolvedRegion = Region.fromString(region ?: "all")
            ?: return Mono.just("Invalid region: $region")
        return ipRangeService.getIpRanges(resolvedRegion, ipType.lowercase())
    }
}