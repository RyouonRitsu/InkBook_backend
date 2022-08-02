package com.ryouonritsu.inkbook_backend.config

import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TokenInterceptor : HandlerInterceptor {
    val log: Logger = LoggerFactory.getLogger(TokenInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.method == "OPTIONS") {
            response.status = HttpServletResponse.SC_OK
            return true
        }
        response.characterEncoding = "UTF-8"
        val token = request.getHeader("token")
        if (token != null) {
            if (TokenUtils.verify(token)) {
                log.info("通过拦截器")
                return true
            }
        }
        response.contentType = "application/json; charset=utf-8"
        try {
            val json = JSONObject()
            json.put("message", "token验证失败")
            json.put("code", "500")
            response.writer.append(json.toString())
            log.info("认证失败，未通过拦截器")
        } catch (e: Exception) {
            return false
        }
        return false
    }
}