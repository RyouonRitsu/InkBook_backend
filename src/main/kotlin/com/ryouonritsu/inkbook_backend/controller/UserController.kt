package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.User
import com.ryouonritsu.inkbook_backend.service.UserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/user")
@Api(value = "用户控制器", tags = ["用户接口"])
class UserController {
    @Autowired
    lateinit var userService: UserService

    @PostMapping("/sendVerificationCode")
    @ApiOperation(value = "获取并发送验证码", notes = "发送验证码到指定邮箱")
    suspend fun sendVerificationCode(
        @RequestParam("email") @ApiParam("邮箱") email: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (email.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "邮箱不能为空"
        )
        val account = "inkbook_ritsu@163.com"
        val password = "OQOEIDABXODMNBVB"
        val nick = "InkBook Official"
        val props = mapOf(
            "mail.smtp.auth" to "true",
            "mail.smtp.host" to "smtp.163.com",
            "mail.smtp.port" to "25"
        )
        val subject = "InkBook邮箱注册验证码"
        val verificationCode = (1..6).joinToString("") { "${(0..9).random()}" }
        val html = "<h4>您的验证码是:</h4>\n<h1>$verificationCode</h1>\n<h4>请在5分钟内使用</h4>"
        val properties = Properties().apply { putAll(props) }
        val authenticator = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(account, password)
            }
        }
        val mailSession = Session.getInstance(properties, authenticator)
        val htmlMessage = MimeMessage(mailSession).apply {
            setFrom(InternetAddress(account, nick, "UTF-8"))
            setRecipient(MimeMessage.RecipientType.TO, InternetAddress(email, "", "UTF-8"))
            setSubject(subject, "UTF-8")
            setContent(html, "text/html; charset=UTF-8")
        }
        val success = runCatching { Transport.send(htmlMessage) }.isSuccess
        return if (success) {
            request.session.setAttribute("verificationCode", verificationCode)
            request.session.setAttribute(
                "invalidTime",
                DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalTime.now().plusMinutes(5))
            )
            mapOf(
                "success" to true,
                "message" to "验证码已发送"
            )
        } else mapOf(
            "success" to false,
            "message" to "验证码发送失败"
        )
    }

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "除了真实姓名其余必填")
    fun register(
        @RequestParam("email") @ApiParam("邮箱") email: String?,
        @RequestParam("verificationCode") @ApiParam("验证码") verificationCode: String?,
        @RequestParam("username") @ApiParam("用户名") username: String?,
        @RequestParam("password1") @ApiParam("密码") password1: String?,
        @RequestParam("password2") @ApiParam("确认密码") password2: String?,
        @RequestParam(value = "realName", required = false, defaultValue = "") @ApiParam("真实姓名") realName: String,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (email.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "邮箱不能为空"
        )
        if (verificationCode.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "验证码不能为空"
        )
        if (username.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "用户名不能为空"
        )
        if (password1.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "密码不能为空"
        )
        if (password2.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "确认密码不能为空"
        )
        return runCatching {
            val vc = request.session.getAttribute("verificationCode") as? String
            val invalidTime = request.session.getAttribute("invalidTime") as? String
            if (vc.isNullOrBlank() || invalidTime.isNullOrBlank()) return let {
                request.session.invalidate()
                mapOf(
                    "success" to false,
                    "message" to "验证码无效"
                )
            }
            if (LocalTime.parse(invalidTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                    .isBefore(LocalTime.now())
            ) return let {
                request.session.invalidate()
                mapOf(
                    "success" to false,
                    "message" to "验证码已失效"
                )
            }
            if (verificationCode != vc) return mapOf(
                "success" to false,
                "message" to "验证码错误, 请再试一次"
            )
            request.session.invalidate()
            val temp = userService.selectUserByUsername(username)
            if (temp != null) return mapOf(
                "success" to false,
                "message" to "用户名已存在"
            )
            if (password1 != password2) return mapOf(
                "success" to false,
                "message" to "两次输入的密码不一致"
            )
            userService.registerNewUser(User(email, username, password1, realName))
            mapOf(
                "success" to true,
                "message" to "注册成功"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "注册失败, 发生未知错误"
            )
        )
    }

    @PostMapping("/login")
    fun login(@RequestBody body: Map<String, String>, request: HttpServletRequest): Map<String, Any> {
        val username = body["username"].let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "username is required"
            )
            else it
        }
        val password = body["password"].let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "password is required"
            )
            else it
        }
        return runCatching {
            val user = userService.selectUserByUsername(username) ?: return mapOf(
                "success" to false,
                "message" to "user does not exist"
            )
            if (user.password != password) return mapOf(
                "success" to false,
                "message" to "password is incorrect"
            )
            request.session.setAttribute("username", username)
            mapOf(
                "success" to true,
                "message" to "login success"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "login failed"
            )
        )
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): Map<String, Any> {
        request.session.invalidate()
        return mapOf(
            "success" to true,
            "message" to "logout success"
        )
    }

    @PostMapping("/showInfo")
    fun showInfo(request: HttpServletRequest): Map<String, Any> {
        val username = request.session.getAttribute("username") as? String ?: return mapOf(
            "success" to false,
            "message" to "please login first"
        )
        return runCatching {
            val user = userService.selectUserByUsername(username) ?: return mapOf(
                "success" to false,
                "message" to "user does not exist in database"
            )
            mapOf(
                "success" to true,
                "message" to "success",
                "username" to user.username,
                "password" to user.password
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "failed"
            )
        )
    }
}