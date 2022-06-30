package com.valensas.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class Test2Controller {
    @GetMapping("test2")
    fun testFunction(): String {
        return "Hello from test 2"
    }

    @PostMapping("test2")
    fun submit(
        @RequestBody body: TestBody
    ): String {
        return "Hello from test 2"
    }
}

data class Test2Body(
    val id: Int,
    val name: String,
    val age: LocalDate?
)
