package com.codingtest.iprange.service

import com.codingtest.iprange.util.Region
import reactor.core.publisher.Mono

interface IpRangeServiceInterface {

    fun getIpRanges(region: Region, ipVersion: String) : Mono<String>
}