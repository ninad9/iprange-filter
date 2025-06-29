package com.codingtest.iprange

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class IpRangeFilterApplication

fun main(args: Array<String>) {
    runApplication<IpRangeFilterApplication>(*args)
}
