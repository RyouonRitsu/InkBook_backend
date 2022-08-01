package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.service.TeamService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

/**
 *
 * @author WuKunchao
 */
@RestController
class TeamController {
    @Autowired
    lateinit var teamService: TeamService

    @PostMapping("/team/create")
    fun createNewTeam(@RequestBody body: Map<String, String>, request: HttpServletRequest): Map<String, Any> {
        var userId = request.session.getAttribute("userId").toString().let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "用户未登录！"
            )
            else it
        }
        return mapOf(
            "success" to true,
            "message" to "todo"
        )
    }
}