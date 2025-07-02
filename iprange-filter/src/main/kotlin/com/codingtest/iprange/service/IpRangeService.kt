package com.codingtest.iprange.service

import com.codingtest.iprange.util.Region
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * Service responsible for retrieving and filtering IP ranges
 * from a cloud.json endpoint exposed by GCP.
 *
 * This service uses a WebClient bean named "gcpWebClient".
 */
@Service
class IpRangeService(
    @Qualifier("gcpWebClient")
    private val webClient: WebClient
) : IpRangeServiceInterface {

    /**
     * Retrieves IP range data from GCP and filters it based on region and IP version.
     *
     * @param region The target region for filtering IP scopes.
     * @param ipVersion The IP version to filter by: "ipv4", "ipv6", or "all".
     * @return A Mono emitting filtered IP prefixes as a newline-separated string.
     */
    override fun getIpRanges(region: Region, ipVersion: String): Mono<String> {

        return webClient
            .get()
            .uri("/ipranges/cloud.json")
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { json ->
                processPrefixes(json as Map<String, Any>, region, ipVersion)
            }
            .onErrorResume { ex ->
                Mono.just("Could not fetch IP ranges: ${ex.message ?: "Unknown error"}")
            }
    }

    /**
     * Filters the list of prefixes from GCP JSON based on region and IP version.
     *
     * @param json The parsed JSON map received from GCP.
     * @param region The region for which IP prefixes need to be filtered.
     * @param ipVersion The desired IP version.
     * @return A string containing matching IP prefixes, one per line.
     */
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