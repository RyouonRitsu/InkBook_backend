package com.ryouonritsu.inkbook_backend.controller

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class TestController {
    @PostMapping("/test")
    fun test(@RequestBody body: Map<String, String>): Map<String, Any> {
        val msg = body["msg"].let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "msg is required"
            )
            else it
        }
        return mapOf(
            "success" to true,
            "message" to msg
        )
    }
}