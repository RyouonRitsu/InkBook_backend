package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.User
import com.ryouonritsu.inkbook_backend.service.UserService
import com.ryouonritsu.inkbook_backend.utils.TokenUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import okhttp3.OkHttpClient
import okhttp3.Request
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
@Tag(name = "用户接口")
class UserController {
    @Autowired
    lateinit var userService: UserService

    fun getHtml(url: String): Pair<Int, String?> {
        val client = OkHttpClient()
        val request = Request.Builder().get().url(url).build()
        return try {
            val response = client.newCall(request).execute()
            when (response.code) {
                200 -> Pair(200, response.body?.string())
                else -> Pair(response.code, null)
            }
        } catch (e: Exception) {
            Pair(500, e.message)
        }
    }

    fun sendEmail(email: String, subject: String, html: String): Boolean {
        val account = "inkbook_ritsu@163.com"
        val password = "OQOEIDABXODMNBVB"
        val nick = "InkBook Official"
        val props = mapOf(
            "mail.smtp.auth" to "true",
            "mail.smtp.host" to "smtp.163.com",
            "mail.smtp.port" to "25"
        )
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
        return runCatching { Transport.send(htmlMessage) }.isSuccess
    }

    @PostMapping("/sendRegistrationVerificationCode")
    @Tag(name = "用户接口")
    @Operation(summary = "发送注册验证码到指定邮箱")
    fun sendRegistrationVerificationCode(
        @RequestParam("email") @Parameter(description = "邮箱") email: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (email.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "邮箱不能为空"
        )
        val t = userService.selectUserByEmail(email)
        if (t != null) return mapOf(
            "success" to false,
            "message" to "该邮箱已被注册"
        )
        val subject = "InkBook邮箱注册验证码"
        val verification_code = (1..6).joinToString("") { "${(0..9).random()}" }
        // 此处需替换成服务器地址!!!
        val (code, html) = getHtml("http://127.0.0.1:8090/registration_verification?verification_code=$verification_code")
        val success = if (code == 200 && html != null) sendEmail(email, subject, html) else false
        return if (success) {
            request.session.setAttribute("verification_code", verification_code)
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
    @Tag(name = "用户接口")
    @Operation(summary = "用户注册, 除了真实姓名其余必填")
    fun register(
        @RequestParam("email") @Parameter(description = "邮箱") email: String?,
        @RequestParam("verification_code") @Parameter(description = "验证码") verificationCode: String?,
        @RequestParam("username") @Parameter(description = "用户名") username: String?,
        @RequestParam("password1") @Parameter(description = "密码") password1: String?,
        @RequestParam("password2") @Parameter(description = "确认密码") password2: String?,
        @RequestParam(
            value = "real_name",
            required = false,
            defaultValue = ""
        ) @Parameter(description = "真实姓名") real_name: String,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (email.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "邮箱不能为空"
        )
        val t = userService.selectUserByEmail(email)
        if (t != null) return mapOf(
            "success" to false,
            "message" to "该邮箱已被注册"
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
            val vc = request.session.getAttribute("verification_code") as? String
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
            userService.registerNewUser(User(email, username, password1, real_name))
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
    @Tag(name = "用户接口")
    @Operation(summary = "用户登录, 参数均为必填项")
    fun login(
        @RequestParam("username") @Parameter(description = "用户名") username: String?,
        @RequestParam("password") @Parameter(description = "密码") password: String?,
    ): Map<String, Any> {
        if (username.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "用户名不能为空"
        )
        if (password.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "密码不能为空"
        )
        return runCatching {
            val user = userService.selectUserByUsername(username) ?: return mapOf(
                "success" to false,
                "message" to "用户不存在"
            )
            if (user.password != password) return mapOf(
                "success" to false,
                "message" to "密码错误"
            )
            val token = TokenUtils.sign(user)
            mapOf(
                "success" to true,
                "message" to "登录成功",
                "token" to token
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "登录失败, 发生未知错误"
            )
        )
    }

    @PostMapping("/logout")
    @Tag(name = "用户接口")
    @Operation(summary = "用户登出")
    fun logout(@RequestHeader("Authorization") @Parameter(description = "用户登陆后获取的token令牌") token: String): Map<String, Any> {
        return mapOf(
            "success" to true,
            "message" to "登出成功"
        )
    }

    @PostMapping("/showInfo")
    @Tag(name = "用户接口")
    @Operation(summary = "返回已登陆用户的信息, 需要用户登陆才能查询成功")
    fun showInfo(@RequestHeader("Authorization") @Parameter(description = "用户登陆后获取的token令牌") token: String): Map<String, Any> {
        return runCatching {
            val user = userService.selectUserByUserId(TokenUtils.verify(token).second) ?: let {
                return mapOf(
                    "success" to false,
                    "message" to "数据库中没有此用户, 此会话已失效"
                )
            }
            mapOf(
                "success" to true,
                "message" to "获取成功",
                "data" to user.toDict()
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "获取失败, 发生未知错误"
            )
        )
    }
}