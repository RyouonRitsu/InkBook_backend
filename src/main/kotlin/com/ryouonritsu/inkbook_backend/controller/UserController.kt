package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.User
import com.ryouonritsu.inkbook_backend.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class UserController {
    @Autowired
    lateinit var userService: UserService

    @PostMapping("/user/register")
    fun register(@RequestBody body: Map<String, String>): Map<String, Any> {
        val username = body["username"].let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "username is required"
            )
            else it
        }
        val password1 = body["password1"].let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "password1 is required"
            )
            else it
        }
        val password2 = body["password2"].let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "password2 is required"
            )
            else it
        }
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

    @PostMapping("/user/login")
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

    @PostMapping("/user/logout")
    fun logout(request: HttpServletRequest): Map<String, Any> {
        request.session.invalidate()
        return mapOf(
            "success" to true,
            "message" to "logout success"
        )
    }

    @PostMapping("/user/showInfo")
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