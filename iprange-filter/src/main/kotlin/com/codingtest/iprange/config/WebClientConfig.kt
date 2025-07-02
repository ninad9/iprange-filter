package com.codingtest.iprange.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration


@Configuration
class WebClientConfig( @Value("\${cloud.base-url}")
                       private val baseUrl: String
) {

    @Bean
    fun gcpWebClient(): WebClient =
        WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(5))
                )
            )
            .build()

}