package com.ryouonritsu.inkbook_backend.controller

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @PostMapping("/test")
    @ApiOperation(value = "测试", notes = "测试")
    fun test(
        @RequestParam("msg") @ApiParam("msg") msg: String?): Map<String, Any> {
        return mapOf(
            "success" to true,
            "message" to msg.toString()
        )
    }
}