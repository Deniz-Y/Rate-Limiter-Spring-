package com.valensas

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class RateLimiterApplication

fun main(args: Array<String>) {
    runApplication<RateLimiterApplication>(*args)
}
