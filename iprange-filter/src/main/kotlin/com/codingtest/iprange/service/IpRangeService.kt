package com.codingtest.iprange.service

import com.codingtest.iprange.data.GcpResponse
import com.codingtest.iprange.util.Region
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
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
    private var cachedResponse: GcpResponse? = null
    private var lastFetchedTime: Long = 0
    private val refreshIntervalMillis: Long = 3600000

    /**
     * Retrieves IP range data from GCP and filters it based on region and IP version.
     *
     * @param region The target region for filtering IP scopes.
     * @param ipVersion The IP version to filter by: "ipv4", "ipv6", or "all".
     * @return A Mono emitting filtered IP prefixes as a newline-separated string.
     */
    override fun getIpRanges(region: Region, ipVersion: String): Mono<String> {

        val now = System.currentTimeMillis()
        val useCache = cachedResponse != null && (now - lastFetchedTime) < refreshIntervalMillis

        val responseMono = if (useCache) {
            Mono.just(cachedResponse!!)
        } else {
            webClient
                .get()
                .uri("/ipranges/cloud.json")
                .retrieve()
                .bodyToMono(GcpResponse::class.java)
                .doOnNext { response ->
                    cachedResponse = response
                    lastFetchedTime = now
                }
        }

        return responseMono
            .mapNotNull { response -> processPrefixes(response.prefixes, region, ipVersion) }
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
     * @param items The parsed JSON map received from GCP.
     * @param region The region for which IP prefixes need to be filtered.
     * @param ipVersion The desired IP version.
     * @return A string containing matching IP prefixes, one per line.
     */
    private fun processPrefixes(items: List<Map<String, String>>, region: Region, ipVersion: String): String {
        val prefixes = mutableListOf<String>()

        for (entry in items) {
            val serviceScope = entry["scope"]?.lowercase() ?: continue
            val ipPrefix = entry["ipv4Prefix"] ?: entry["ipv6Prefix"] ?: continue

            if (isVersionAllAndRegionValid(region, serviceScope) &&
                isValidIpVersion(ipVersion, entry)) {
                prefixes.add(ipPrefix)
            }
        }
        return prefixes.joinToString(separator = "\n")
    }

    private fun isVersionAllAndRegionValid(region: Region, serviceScope: String) =
        (isRegionAll(region) || isValidScope(region, serviceScope))

    private fun isValidIpVersion(ipVersion: String, entry: Map<String, String>) =
        (isIpVersionAll(ipVersion) || isEntryForIpv4(ipVersion, entry) ||
                isEntryForIpv6(ipVersion, entry))

    private fun isValidScope(region: Region, serviceScope: String) =
        region.scopes.any { serviceScope.contains(it) }

    private fun isEntryForIpv6(ipVersion: String, entry: Map<String, String>) =
        (isIpv6(ipVersion) && entry.containsKey("ipv6Prefix"))

    private fun isEntryForIpv4(ipVersion: String, entry: Map<String, String>) =
        (isIpv4(ipVersion) && entry.containsKey("ipv4Prefix"))

    private fun isIpv6(ipVersion: String) = ipVersion == "ipv6"

    private fun isIpv4(ipVersion: String) = ipVersion == "ipv4"

    private fun isIpVersionAll(ipVersion: String) = ipVersion == "all"

    private fun isRegionAll(region: Region) = region == Region.ALL

}