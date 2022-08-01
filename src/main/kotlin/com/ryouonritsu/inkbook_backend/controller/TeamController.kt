package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.service.TeamService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author WuKunchao
 */
@RestController
class TeamController {
    @Autowired
    lateinit var teamService: TeamService

    @PostMapping("/group/create")
    fun createNewTeam(@RequestBody body: Map<String, String>): Map<String, Any> {
        return mapOf(
            "success" to true,
            "message" to "todo"
        )
    }
}