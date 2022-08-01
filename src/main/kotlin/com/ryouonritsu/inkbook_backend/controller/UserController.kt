package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.User
import com.ryouonritsu.inkbook_backend.service.UserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/user")
@Api(value = "User Controller", tags = ["用户控制器接口"])
class UserController {
    @Autowired
    lateinit var userService: UserService

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "用户注册")
    fun register(
        @RequestParam("username") @ApiParam("username") username: String?,
        @RequestParam("password1") @ApiParam("password1") password1: String?,
        @RequestParam("password2") @ApiParam("password2") password2: String?,
    ): Map<String, Any> {
        if (username.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "username is required"
        )
        if (password1.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "password1 is required"
        )
        if (password2.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "password2 is required"
        )
        return runCatching {
            val temp = userService.selectUserByUsername(username)
            if (temp != null) return mapOf(
                "success" to false,
                "message" to "username already exists"
            )
            if (password1 != password2) return mapOf(
                "success" to false,
                "message" to "password1 and password2 must be same"
            )
            userService.registerNewUser(User(username, password1))
            mapOf(
                "success" to true,
                "message" to "register success"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "register failed"
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