package com.ryouonritsu.inkbook_backend.controller

import io.swagger.v3.oas.annotations.Hidden
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@Hidden
class TestController {
    @RequestMapping("/test")
    @Hidden
    fun test(model: Model): String {
        model.addAttribute("msg", "okkkkhttp3")
        return "test"
    }

    @GetMapping("/use")
    @Hidden
    @ResponseBody
    fun use(): Map<String, Any> {
        val client = OkHttpClient()
        val request = Request.Builder().get().url("http://127.0.0.1:8090/test").build()
        return try {
            val response = client.newCall(request).execute()
            when (response.code) {
                200 -> {
                    val string = response.body?.string() ?: return mapOf(
                        "success" to false,
                        "message" to "无法获取body"
                    )
                    mapOf(
                        "success" to true,
                        "message" to string
                    )
                }

                else -> mapOf(
                    "success" to false,
                    "message" to "${response.code}"
                )
            }
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "message" to e
            )
        }
    }
}