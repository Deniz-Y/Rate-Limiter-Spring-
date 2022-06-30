package com.valensas

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.lang.Thread.sleep

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RateLimiterApplicationTests {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private val logger = LoggerFactory.getLogger(javaClass)

    @Test
    fun contextLoads() {
        val limit = 5
        var intTime = 0
        (0..10).forEach {
            val response = restTemplate.exchange("/test", HttpMethod.GET, null, String::class.java)
            logger.info("Request: {} - Status: {}", it, response.statusCode)
            val expectedStatusCode = if (it < limit) HttpStatus.OK else HttpStatus.TOO_MANY_REQUESTS
            assertEquals(expectedStatusCode, response.statusCode)
            if (expectedStatusCode == HttpStatus.OK) {
                val remaining = response.headers["X-Rate-Limit-Remaining"]?.firstOrNull()
                logger.info("Remaining limit: {}", remaining)
                assertNotNull(remaining)
                assertEquals(limit - it - 1, remaining!!.toInt())
            } else {
                val remainingTime = response.headers["X-Rate-Limit-Retry-After-Seconds"]?.firstOrNull()
                logger.info("Remaining time: {}", remainingTime)
                if (remainingTime != null) {
                    intTime = remainingTime.toInt()
                }
            }
        }
        var longTime = (intTime * 1000).toLong() + 1000
        logger.info("Sleep {} seconds", intTime + 1)
        sleep(longTime)
        val response = restTemplate.exchange("/test", HttpMethod.GET, null, String::class.java)
        logger.info("Request after sleep - Status: {}", response.statusCode)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `apply ip based rate limit`() {
        val limit = 5
        val ip1 = "1.1.1.1"
        val ip2 = "1.1.1.2"

        // Send request from ip1 and get OK
        repeat(limit) { call(ip1, HttpStatus.OK) }
        // Get 429 if the rate limit is exceeded
        call(ip1, HttpStatus.TOO_MANY_REQUESTS)

        // Send request from ip2 and get OK
        repeat(limit) { call(ip2, HttpStatus.OK) }
        // Get 429 if the rate limit is exceeded
        call(ip2, HttpStatus.TOO_MANY_REQUESTS)

        // Send request from both ip addresses and get 429
        listOf(ip1, ip2).forEach { ip -> call(ip, HttpStatus.TOO_MANY_REQUESTS) }
    }

    fun call(ip: String, expectedStatus: HttpStatus) {
        logger.info("Sending request from {}. Expected response status: {}", ip, expectedStatus)
        val headers = HttpHeaders()
        headers.add("X-FORWARDED-FOR", ip)
        val entity: HttpEntity<String> = HttpEntity(null, headers)
        val response = restTemplate.exchange("/test", HttpMethod.GET, entity, String::class.java)
        logger.info("Response status of the request from {} is {}", ip, expectedStatus)
        assertEquals(expectedStatus, response.statusCode)
    }

    @Test
    fun headerTest() {
    }
}
