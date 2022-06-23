package com.valensas.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class TestController {
    @GetMapping("test")
    fun testFunction(): String {
        return "Hello"
    }

    @PostMapping("test")
    fun submit(
        @RequestBody body: TestBody
    ): String {
        return "Hello"
    }
}

data class TestBody(
    val id: Int,
    val name: String,
    val age: LocalDate?
)
