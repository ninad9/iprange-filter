package com.codingtest.iprange.service

import com.codingtest.iprange.util.Region
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class IpRangeService : IpRangeServiceInterface {
    override fun getIpRanges(region: Region, ipVersion: String): Mono<String> {

        return WebClient.create("https://www.gstatic.com")
            .get()
            .uri("/ipranges/cloud.json")
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { json ->
                processPrefixes(json as Map<String, Any>, region, ipVersion)
            }
    }

    private fun processPrefixes(json: Map<String, Any>, region: Region, ipVersion: String): String {
        val prefixes = mutableListOf<String>()
        val items = json["prefixes"] as List<Map<String, String>>

        for (entry in items) {
            val serviceScope = entry["scope"]?.lowercase() ?: continue
            val ipPrefix = entry["ipv4Prefix"] ?: entry["ipv6Prefix"] ?: continue

            if ((region == Region.ALL || region.scopes.any { serviceScope.contains(it) }) &&
                (ipVersion == "all" ||
                        (ipVersion == "ipv4" && entry.containsKey("ipv4Prefix")) ||
                        (ipVersion == "ipv6" && entry.containsKey("ipv6Prefix")))
            ) {
                prefixes.add(ipPrefix)
            }
        }
        return prefixes.joinToString(separator = "\n")
    }

}