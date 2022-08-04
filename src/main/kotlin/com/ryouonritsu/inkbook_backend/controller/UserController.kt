package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.User
import com.ryouonritsu.inkbook_backend.entity.UserFile
import com.ryouonritsu.inkbook_backend.repository.DocumentationRepository
import com.ryouonritsu.inkbook_backend.repository.UserRepository
import com.ryouonritsu.inkbook_backend.service.UserFileService
import com.ryouonritsu.inkbook_backend.utils.RedisUtils
import com.ryouonritsu.inkbook_backend.utils.TokenUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.io.path.Path

@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
class UserController {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var docRepository: DocumentationRepository

    @Autowired
    lateinit var redisUtils: RedisUtils

    @Autowired
    lateinit var userFileService: UserFileService

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
            false, mapOf(
                "success" to false,
                "message" to "邮箱格式不正确"
            )
        )
        if (username.length > 50) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "用户名长度不能超过50"
            )
        )
        if (password.length < 8 || password.length > 30) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "密码长度必须在8-30之间"
            )
        )
        if (real_name.length > 50) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "真实姓名长度不能超过50"
            )
        )
        return Pair(true, null)
    }

    fun emailCheck(email: String?): Pair<Boolean, Map<String, Any>?> {
        if (email.isNullOrBlank()) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "邮箱不能为空"
            )
        )
        if (!email.matches(Regex("[\\w\\\\.]+@[\\w\\\\.]+\\.\\w+"))) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "邮箱格式不正确"
            )
        )
        return Pair(true, null)
    }

    fun sendVerifyCodeEmailUseTemplate(
        template: String,
        verification_code: String,
        email: String,
        subject: String
    ): Map<String, Any> {
        // 此处需替换成服务器地址!!!
//        val (code, html) = getHtml("http://101.42.171.88:8090/registration_verification?verification_code=$verification_code")
        val (code, html) = getHtml("http://localhost:8090/$template?verification_code=$verification_code")
        val success = if (code == 200 && html != null) sendEmail(email, subject, html) else false
        return if (success) {
            redisUtils.set("verification_code", verification_code, 5, TimeUnit.MINUTES)
            redisUtils.set("email", email, 5, TimeUnit.MINUTES)
            mapOf(
                "success" to true,
                "message" to "验证码已发送"
            )
        } else mapOf(
            "success" to false,
            "message" to "验证码发送失败"
        )
    }

    @PostMapping("/sendRegistrationVerificationCode")
    @Tag(name = "用户接口")
    @Operation(
        summary = "发送注册验证码",
        description = "发送注册验证码到指定邮箱, 若modify为true, 则发送修改邮箱验证码, 默认为false"
    )
    fun sendRegistrationVerificationCode(
        @RequestParam("email") @Parameter(description = "邮箱") email: String?,
        @RequestParam("modify", defaultValue = "false") @Parameter(description = "是否修改邮箱") modify: Boolean
    ): Map<String, Any> {
        val (result, message) = emailCheck(email)
        if (!result && message != null) return message
        val t = userRepository.findByEmail(email!!)
        if (t != null) return mapOf(
            "success" to false,
            "message" to "该邮箱已被注册"
        )
        val subject = if (modify) "InkBook修改邮箱验证码" else "InkBook邮箱注册验证码"
        val verification_code = (1..6).joinToString("") { "${(0..9).random()}" }
        return sendVerifyCodeEmailUseTemplate(
            "registration_verification",
            verification_code,
            email,
            subject
        )
    }

    fun verifyCodeCheck(verifyCode: String?): Pair<Boolean, Map<String, Any>?> {
        val vc = redisUtils["verification_code"]
        if (vc.isNullOrBlank()) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "验证码无效"
            )
        )
        if (verifyCode != vc) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "验证码错误, 请再试一次"
            )
        )
        redisUtils - "verification_code"
        return Pair(true, null)
    }

    @PostMapping("/register")
    @Tag(name = "用户接口")
    @Operation(summary = "用户注册", description = "除了真实姓名其余必填")
    fun register(
        @RequestParam("email") @Parameter(description = "邮箱") email: String?,
        @RequestParam("verification_code") @Parameter(description = "验证码") verificationCode: String?,
        @RequestParam("username") @Parameter(description = "用户名") username: String?,
        @RequestParam("password1") @Parameter(description = "密码") password1: String?,
        @RequestParam("password2") @Parameter(description = "确认密码") password2: String?,
        @RequestParam("avatar") @Parameter(description = "个人头像") avatar: String?,
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
        val t = userRepository.findByEmail(email)
        if (t != null) return mapOf(
            "success" to false,
            "message" to "该邮箱已被注册"
        )
        return runCatching {
            val (re, msg) = verifyCodeCheck(verificationCode)
            if (!re && msg != null) return@runCatching msg
            if (redisUtils["email"] != email) return mapOf(
                "success" to false,
                "message" to "该邮箱与验证邮箱不匹配"
            )
            val temp = userRepository.findByUsername(username)
            if (temp != null) return mapOf(
                "success" to false,
                "message" to "用户名已存在"
            )
            if (password1 != password2) return mapOf(
                "success" to false,
                "message" to "两次输入的密码不一致"
            )
            userRepository.save(User(email, username, password1, real_name, avatar ?: ""))
            mapOf(
                "success" to true,
                "message" to "注册成功"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "注册失败, 发生意外错误"
            )
        )
    }

    @PostMapping("/login")
    @Tag(name = "用户接口")
    @Operation(
        summary = "用户登录",
        description = "keep_login为true时, 保持登录状态, 否则token会在3天后失效, 默认为false"
    )
    fun login(
        @RequestParam("username") @Parameter(description = "用户名") username: String?,
        @RequestParam("password") @Parameter(description = "密码") password: String?,
        @RequestParam("keep_login", defaultValue = "false") @Parameter(description = "是否记住登录") keepLogin: Boolean
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
            val user = userRepository.findByUsername(username) ?: return mapOf(
                "success" to false,
                "message" to "用户不存在"
            )
            if (user.password != password) return mapOf(
                "success" to false,
                "message" to "密码错误"
            )
            val token = TokenUtils.sign(user)
            if (keepLogin) redisUtils["${user.uid}"] = token
            else redisUtils.set("${user.uid}", token, 3, TimeUnit.DAYS)
            mapOf(
                "success" to true,
                "message" to "登录成功",
                "data" to mapOf(
                    "token" to token,
                    "user_id" to user.uid
                )
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "登录失败, 发生意外错误"
            )
        )
    }

    @GetMapping("/logout")
    @Tag(name = "用户接口")
    @Operation(summary = "用户登出")
    fun logout(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String
    ): Map<String, Any> {
        redisUtils - "${TokenUtils.verify(token).second}"
        return mapOf(
            "success" to true,
            "message" to "登出成功"
        )
    }

    @GetMapping("/showInfo")
    @Tag(name = "用户接口")
    @Operation(summary = "返回已登陆用户的信息", description = "需要用户登陆才能查询成功")
    fun showInfo(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String
    ): Map<String, Any> {
        return runCatching {
            val user = userRepository.findById(TokenUtils.verify(token).second).get()
            mapOf(
                "success" to true,
                "message" to "获取成功",
                "data" to user.toDict()
            )
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${TokenUtils.verify(token).second}"
                return mapOf(
                    "success" to false,
                    "message" to "数据库中没有此用户, 此会话已失效"
                )
            }
            it.printStackTrace()
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "获取失败, 发生意外错误"
            )
        )
    }

    @GetMapping("/selectUserByUserId")
    @Tag(name = "用户接口")
    @Operation(summary = "根据用户id查询用户信息")
    fun selectUserByUserId(@RequestParam("user_id") @Parameter(description = "用户id") user_id: Long): Map<String, Any> {
        return runCatching {
            val user = userRepository.findById(user_id).get()
            mapOf(
                "success" to true,
                "message" to "获取成功",
                "data" to user.toDict()
            )
        }.onFailure {
            if (it is NoSuchElementException) return mapOf(
                "success" to false,
                "message" to "数据库中没有此用户"
            )
            it.printStackTrace()
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "获取失败, 发生意外错误"
            )
        )
    }

    @PostMapping("/sendForgotPasswordEmail")
    @Tag(name = "用户接口")
    @Operation(summary = "发送找回密码验证码", description = "发送找回密码验证码到指定邮箱")
    fun sendForgotPasswordEmail(
        @RequestParam("email") @Parameter(description = "邮箱") email: String?
    ): Map<String, Any> {
        val (result, message) = emailCheck(email)
        if (!result && message != null) return message
        userRepository.findByEmail(email!!) ?: return mapOf(
            "success" to false,
            "message" to "该邮箱未被注册"
        )
        val subject = "InkBook邮箱找回密码验证码"
        val verification_code = (1..6).joinToString("") { "${(0..9).random()}" }
        return sendVerifyCodeEmailUseTemplate(
            "forgot_password",
            verification_code,
            email,
            subject
        )
    }

    @PostMapping("/changePassword")
    @Tag(name = "用户接口")
    @Operation(
        summary = "修改用户密码",
        description = "可选忘记密码修改或正常修改密码, 参数的必要性根据模式选择, 如\"1: 验证码\"则表示模式1需要填写参数\"验证码\""
    )
    fun changePassword(
        @RequestParam("mode") @Parameter(description = "修改模式, 0为忘记密码修改, 1为正常修改") mode: Int?,
        @RequestParam(
            "token",
            required = false,
            defaultValue = ""
        ) @Parameter(description = "1: 用户登陆后获取的token令牌") token: String,
        @RequestParam("old_password", required = false) @Parameter(description = "1: 旧密码") old_password: String?,
        @RequestParam("password1") @Parameter(description = "新密码") password1: String?,
        @RequestParam("password2") @Parameter(description = "确认新密码") password2: String?,
        @RequestParam("email", required = false) @Parameter(description = "0: 邮箱") email: String?,
        @RequestParam("verify_code", required = false) @Parameter(description = "0: 验证码") verify_code: String?
    ): Map<String, Any> {
        when (mode) {
            0 -> {
                val (result, message) = verifyCodeCheck(verify_code)
                if (!result && message != null) return message
                if (password1.isNullOrBlank() || password2.isNullOrBlank()) return mapOf(
                    "success" to false,
                    "message" to "密码不能为空"
                )
                if (password1 != password2) return mapOf(
                    "success" to false,
                    "message" to "两次密码不一致"
                )
                val (re, msg) = emailCheck(email)
                if (!re && msg != null) return msg
                return runCatching {
                    val user = userRepository.findByEmail(email!!) ?: return mapOf(
                        "success" to false,
                        "message" to "该邮箱未被注册, 发生意外错误, 请检查数据库"
                    )
                    user.password = password1
                    userRepository.save(user)
                    redisUtils - "${user.uid}"
                    mapOf(
                        "success" to true,
                        "message" to "修改成功"
                    )
                }.onFailure { it.printStackTrace() }.getOrDefault(
                    mapOf(
                        "success" to false,
                        "message" to "修改失败, 发生意外错误"
                    )
                )
            }

            1 -> {
                if (token.isBlank()) return mapOf(
                    "success" to false,
                    "message" to "请先登陆"
                )
                return runCatching {
                    val user = userRepository.findById(TokenUtils.verify(token).second).get()
                    if (password1.isNullOrBlank() || password2.isNullOrBlank() || old_password.isNullOrBlank()) return mapOf(
                        "success" to false,
                        "message" to "密码不能为空"
                    )
                    if (user.password != old_password) return mapOf(
                        "success" to false,
                        "message" to "原密码错误"
                    )
                    if (password1.length < 8 || password1.length > 30) return mapOf(
                        "success" to false,
                        "message" to "密码长度必须在8-30位之间"
                    )
                    if (password1 != password2) return mapOf(
                        "success" to false,
                        "message" to "两次密码不一致"
                    )
                    user.password = password1
                    userRepository.save(user)
                    redisUtils - "${user.uid}"
                    mapOf(
                        "success" to true,
                        "message" to "修改成功"
                    )
                }.onFailure {
                    if (it is NoSuchElementException) {
                        redisUtils - "${TokenUtils.verify(token).second}"
                        return mapOf(
                            "success" to false,
                            "message" to "数据库中没有此用户或可能是token验证失败, 此会话已失效"
                        )

                    }
                    it.printStackTrace()
                }.getOrDefault(
                    mapOf(
                        "success" to false,
                        "message" to "修改失败, 发生意外错误"
                    )
                )
            }

            else -> return mapOf(
                "success" to false,
                "message" to "修改模式不在合法范围内, 应为0或1"
            )
        }
    }

    @PostMapping("/uploadFile")
    @Tag(name = "用户接口")
    @Operation(
        summary = "上传文件",
        description = "将用户上传的文件保存在静态文件目录static/user/\${user_id}/\${file_name}下"
    )
    fun uploadFile(
        @RequestParam("file") @Parameter(description = "文件") file: MultipartFile,
        @RequestParam("token") @Parameter(description = "用户认证令牌") token: String,
    ): Map<String, Any> {
        return runCatching {
            if (file.size >= 10 * 1024 * 1024) return mapOf(
                "success" to false,
                "message" to "上传失败, 文件大小超过最大限制10MB！"
            )
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS_")
            val time = LocalDateTime.now().format(formatter)
            val user_id = TokenUtils.verify(token).second
            val fileDir = "static/file/${user_id}"
            val fileName = time + file.originalFilename
            val filePath = "$fileDir/$fileName"
            if (!File(fileDir).exists()) File(fileDir).mkdirs()
            file.transferTo(Path(filePath))
            val fileUrl = "http://101.42.171.88:8090/file/${user_id}/${fileName}"
            userFileService.saveFile(UserFile(fileUrl))
            mapOf(
                "success" to true,
                "message" to "上传成功",
                "data" to mapOf(
                    "url" to fileUrl
                )
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "上传失败, 发生意外错误"
            )
        )
    }

    @PostMapping("/modifyUserInfo")
    @Tag(name = "用户接口")
    @Operation(
        summary = "修改用户信息",
        description = "未填写的信息则保持原样不变"
    )
    fun modifyUserInfo(
        @RequestParam("token") @Parameter(description = "用户认证令牌") token: String,
        @RequestParam("username", defaultValue = "") @Parameter(description = "用户名") username: String?,
        @RequestParam("real_name", defaultValue = "") @Parameter(description = "真实姓名") real_name: String?,
        @RequestParam("avatar", defaultValue = "") @Parameter(description = "个人头像") avatar: String?,
    ): Map<String, Any> {
        return runCatching {
            val user = userRepository.findById(TokenUtils.verify(token).second).get()
            if (!username.isNullOrBlank()) {
                val t = userRepository.findByUsername(username)
                if (t != null) return mapOf(
                    "success" to false,
                    "message" to "用户名已存在"
                )
                if (username.length > 50) return mapOf(
                    "success" to false,
                    "message" to "用户名长度不能超过50"
                )
                user.username = username
            }
            if (!real_name.isNullOrBlank()) {
                if (real_name.length > 50) return mapOf(
                    "success" to false,
                    "message" to "真实姓名长度不能超过50"
                )
                user.realname = real_name
            }
            if (!avatar.isNullOrBlank()) {
                user.avatar = avatar
            }
            userRepository.save(user)
            mapOf(
                "success" to true,
                "message" to "修改成功"
            )
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${TokenUtils.verify(token).second}"
                return mapOf(
                    "success" to false,
                    "message" to "数据库中没有此用户或可能是token验证失败, 此会话已失效"
                )
            }
            it.printStackTrace()
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "修改失败, 发生意外错误"
            )
        )
    }

    @PostMapping("/modifyEmail")
    @Tag(name = "用户接口")
    @Operation(
        summary = "修改邮箱",
        description = "需要进行新邮箱验证和密码验证, 新邮箱验证发送验证码使用注册验证码接口即可"
    )
    fun modifyEmail(
        @RequestParam("token") @Parameter(description = "用户认证令牌") token: String,
        @RequestParam("email") @Parameter(description = "新邮箱") email: String?,
        @RequestParam("verify_code") @Parameter(description = "邮箱验证码") verify_code: String?,
        @RequestParam("password") @Parameter(description = "密码") password: String?
    ): Map<String, Any> {
        return runCatching {
            val user = userRepository.findById(TokenUtils.verify(token).second).get()
            val (result, message) = emailCheck(email)
            if (!result && message != null) return message
            val t = userRepository.findByEmail(email!!)
            if (t != null) return mapOf(
                "success" to false,
                "message" to "该邮箱已被注册"
            )
            val (re, msg) = verifyCodeCheck(verify_code)
            if (!re && msg != null) return@runCatching msg
            if (redisUtils["email"] != email) return mapOf(
                "success" to false,
                "message" to "该邮箱与验证邮箱不匹配"
            )
            if (password != user.password) return mapOf(
                "success" to false,
                "message" to "密码错误"
            )
            val (code, html) = getHtml("http://localhost:8090/change_email?email=${email}")
            val success =
                if (code == 200 && html != null) sendEmail(user.email!!, "InkBook邮箱修改通知", html) else false
            if (!success) throw Exception("邮件发送失败")
            user.email = email
            userRepository.save(user)
            mapOf(
                "success" to true,
                "message" to "修改成功"
            )
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${TokenUtils.verify(token).second}"
                return mapOf(
                    "success" to false,
                    "message" to "数据库中没有此用户或可能是token验证失败, 此会话已失效"
                )
            }
            if (it.message != null) return mapOf(
                "success" to false,
                "message" to "${it.message}"
            ) else it.printStackTrace()
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "修改失败, 发生意外错误"
            )
        )
    }

    @PostMapping("/favorite")
    @Tag(name = "用户接口")
    @Operation(
        summary = "收藏",
        description = "将指定的内容加入收藏夹, undo为true时取消收藏, 默认为false"
    )
    fun favorite(
        @RequestParam("token") @Parameter(description = "用户认证令牌") token: String,
        @RequestParam("undo", defaultValue = "false") @Parameter(description = "是否取消收藏") undo: Boolean,
        @RequestParam("doc_id") @Parameter(description = "要收藏的文档id") docId: Long
    ): Map<String, Any> {
        val userId = TokenUtils.verify(token).second
        val user = try {
            userRepository.findById(userId).get()
        } catch (e: NoSuchElementException) {
            redisUtils - "$userId"
            return mapOf(
                "success" to false,
                "message" to "数据库中没有此用户或可能是token验证失败, 此会话已失效"
            )
        }
        val doc = try {
            docRepository.findById(docId).get()
        } catch (e: NoSuchElementException) {
            return mapOf(
                "success" to false,
                "message" to "数据库中没有此文档, 请检查文档Id是否正确"
            )
        }
        if (undo) {
            if (!user.favoritedocuments.remove(doc)) return mapOf(
                "success" to false,
                "message" to "收藏夹中没有此文档, 取消收藏失败"
            )
        } else {
            if (doc !in user.favoritedocuments) user.favoritedocuments.add(doc)
            else return mapOf(
                "success" to false,
                "message" to "收藏夹中已有此文档, 收藏失败"
            )
        }
        return try {
            userRepository.save(user)
            mapOf(
                "success" to true,
                "message" to "${if (undo) "取消" else ""}收藏成功"
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "message" to "${if (undo) "取消" else ""}收藏失败, 发生意外错误"
            )
        }
    }

    @PostMapping("/favoriteList")
    @Tag(name = "用户接口")
    @Operation(
        summary = "收藏列表",
        description = "获取指定用户的收藏列表, 如不指定用户, 则获取当前登录用户的收藏列表"
    )
    fun favoriteList(
        @RequestParam("token") @Parameter(description = "用户认证令牌") token: String,
        @RequestParam("user_id", defaultValue = "-1") @Parameter(description = "用户id") userId: Long
    ): Map<String, Any> {
        return try {
            if (userId != -1L) {
                mapOf(
                    "success" to true,
                    "message" to "获取成功",
                    "data" to userRepository.findById(userId).get().favoritedocuments.map { it.toDict() }
                )
            } else {
                val user = userRepository.findById(TokenUtils.verify(token).second).get()
                mapOf(
                    "success" to true,
                    "message" to "获取成功",
                    "data" to user.favoritedocuments.map { it.toDict() }
                )
            }
        } catch (e: NoSuchElementException) {
            if (userId != -1L) return mapOf(
                "success" to false,
                "message" to "数据库中没有此用户, 获取失败"
            )
            else {
                redisUtils - "${TokenUtils.verify(token).second}"
                return mapOf(
                    "success" to false,
                    "message" to "数据库中没有此用户或可能是token验证失败, 此会话已失效"
                )
            }
        } catch (e: Exception) {
            return mapOf(
                "success" to false,
                "message" to "获取失败, 发生意外错误"
            )
        }
    }
}