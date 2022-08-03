package com.ryouonritsu.inkbook_backend.config

import com.ryouonritsu.inkbook_backend.utils.RedisUtils
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TokenInterceptor : HandlerInterceptor {
    @Autowired
    lateinit var redisUtils: RedisUtils
    val log: Logger = LoggerFactory.getLogger(TokenInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.method == "OPTIONS") {
            response.status = HttpServletResponse.SC_OK
            return true
        }
        response.characterEncoding = "UTF-8"
        val token = request.getParameter("token")
        //val user_id = request.getParameter("user_id")
        if (!token.isNullOrBlank()) {
//            if (TokenUtils.verify(token).first) {
//                log.info("通过拦截器")
//                return true
//            }
            val (re, user_id) = TokenUtils.verify(token)
            log.info("现有的token: $token")
            if (redisUtils["$user_id"] == token && re) {
                log.info("通过拦截器")
                return true
            } else {
                log.info("已经存在一个token, 未通过拦截器")
            }
        }
        response.contentType = "application/json; charset=utf-8"
        try {
            val json = JSONObject()
            json.put("success", false)
            json.put("message", "token验证失败")
            response.writer.append(json.toString())
            log.info("认证失败，未通过拦截器")
        } catch (e: Exception) {
            return false
        }
        return false
    }
}
