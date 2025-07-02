package com.codingtest.iprange.service

import com.codingtest.iprange.util.Region
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.core.publisher.Mono
import java.util.concurrent.TimeoutException

/**
 * Service responsible for retrieving and filtering IP ranges
 * from a cloud.json endpoint exposed by GCP.
 *
 * This service uses a WebClient bean named "gcpWebClient".
 */
@Suppress("UNCHECKED_CAST")
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
            .mapNotNull { json ->
                (json as? Map<String, Any>)?.let { processPrefixes(it, region, ipVersion) }
            }
            .switchIfEmpty(Mono.just("Malformed or empty response from GCP"))
            .onErrorResume { ex ->
                when (ex) {
                    is WebClientRequestException -> Mono.just("Connection error: ${ex.message}")
                    else -> Mono.just("Could not fetch IP ranges: ${ex.message}")
                }
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
        val items = (json["prefixes"] as? List<*>)?.filterIsInstance<Map<String, String>>()
            ?: return "Invalid JSON format: missing or malformed 'prefixes'"

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