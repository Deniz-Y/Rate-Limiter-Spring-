package com.valensas

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
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
        var longTime = (intTime*1000).toLong() + 1000
        logger.info("Sleep {} seconds", intTime + 1)
        sleep(longTime)
        val response = restTemplate.exchange("/test", HttpMethod.GET, null, String::class.java)
        logger.info("Request after sleep - Status: {}",  response.statusCode)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun headerTest(){

    }
}
