package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.User
import com.ryouonritsu.inkbook_backend.service.UserService
import com.ryouonritsu.inkbook_backend.utils.RedisUtils
import com.ryouonritsu.inkbook_backend.utils.TokenUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
class UserController {
    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var redisUtils: RedisUtils

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

    fun check(email: String, username: String, password: String, real_name: String): Pair<Boolean, Map<String, Any>?> {
        if (!email.matches(Regex("[\\w\\\\.]+@[\\w\\\\.]+\\.\\w+"))) return Pair(
            false,
            mapOf(
                "success" to false,
                "message" to "邮箱格式不正确"
            )
        )
        if (username.length > 50) return Pair(
            false,
            mapOf(
                "success" to false,
                "message" to "用户名长度不能超过50"
            )
        )
        if (password.length < 8 || password.length > 30) return Pair(
            false,
            mapOf(
                "success" to false,
                "message" to "密码长度必须在8-30之间"
            )
        )
        if (real_name.length > 50) return Pair(
            false,
            mapOf(
                "success" to false,
                "message" to "真实姓名长度不能超过50"
            )
        )
        return Pair(true, null)
    }

    @PostMapping("/sendRegistrationVerificationCode")
    @Tag(name = "用户接口")
    @Operation(summary = "发送注册验证码到指定邮箱")
    fun sendRegistrationVerificationCode(
        @RequestParam("email") @Parameter(description = "邮箱") email: String?,
    ): Map<String, Any> {
        if (email.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "邮箱不能为空"
        )
        if (!email.matches(Regex("[\\w\\\\.]+@[\\w\\\\.]+\\.\\w+"))) return mapOf(
            "success" to false,
            "message" to "邮箱格式不正确"
        )
        val t = userService.selectUserByEmail(email)
        if (t != null) return mapOf(
            "success" to false,
            "message" to "该邮箱已被注册"
        )
        val subject = "InkBook邮箱注册验证码"
        val verification_code = (1..6).joinToString("") { "${(0..9).random()}" }
        // 此处需替换成服务器地址!!!
//        val (code, html) = getHtml("http://101.42.171.88:8090/registration_verification?verification_code=$verification_code")
        val (code, html) = getHtml("http://localhost:8090/registration_verification?verification_code=$verification_code")
        val success = if (code == 200 && html != null) sendEmail(email, subject, html) else false
        return if (success) {
            redisUtils.set("verification_code", verification_code, 5, TimeUnit.MINUTES)
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
        val (result, message) = check(email, username, password1, real_name)
        if (!result && message != null) return message
        val t = userService.selectUserByEmail(email)
        if (t != null) return mapOf(
            "success" to false,
            "message" to "该邮箱已被注册"
        )
        return runCatching {
            val vc = redisUtils["verification_code"]
            if (vc.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "验证码无效"
            )
            if (verificationCode != vc) return mapOf(
                "success" to false,
                "message" to "验证码错误, 请再试一次"
            )
            redisUtils - "verification_code"
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
            redisUtils.set("token", token, 30, TimeUnit.MINUTES)
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

    @GetMapping("/logout")
    @Tag(name = "用户接口")
    @Operation(summary = "用户登出")
    fun logout(@RequestHeader("Authorization") @Parameter(description = "用户登陆后获取的token令牌") token: String): Map<String, Any> {
        redisUtils - "token"
        return mapOf(
            "success" to true,
            "message" to "登出成功"
        )
    }

    @GetMapping("/showInfo")
    @Tag(name = "用户接口")
    @Operation(summary = "返回已登陆用户的信息, 需要用户登陆才能查询成功")
    fun showInfo(@RequestHeader("Authorization") @Parameter(description = "用户登陆后获取的token令牌") token: String): Map<String, Any> {
        return runCatching {
            val user = userService.selectUserByUserId(TokenUtils.verify(token).second) ?: let {
                redisUtils - "token"
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

    @GetMapping("/selectUserByUserId")
    @Tag(name = "用户接口")
    @Operation(summary = "根据用户id查询用户信息")
    fun selectUserByUserId(@RequestParam("user_id") @Parameter(description = "用户id") user_id: Long): Map<String, Any> {
        return runCatching {
            val user = userService.selectUserByUserId(user_id) ?: return mapOf(
                "success" to false,
                "message" to "数据库中没有此用户"
            )
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