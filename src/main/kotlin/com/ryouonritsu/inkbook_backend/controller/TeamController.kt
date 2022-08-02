package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Team
import com.ryouonritsu.inkbook_backend.service.TeamService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

/**
 *
 * @author WuKunchao
 */
@RestController
@RequestMapping("/team")
@Api(value = "User Controller", tags = ["团队接口"])
class TeamController {
    @Autowired
    lateinit var teamService: TeamService

    @PostMapping("/create")
    fun createNewTeam(
        @RequestParam("teamName") @ApiParam("teamName") teamName: String?,
        @RequestParam("teamInfo") @ApiParam("teamInfo") teamInfo: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        var userId = request.session.getAttribute("user_id")?.toString().let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "用户未登录！"
            ) else it
        }
        if (teamName.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队名为空！"
        )
        return runCatching {
            var team = Team(teamName, teamInfo.let {
                if (it.isNullOrBlank()) {
                    ""
                } else it
            })
            teamService.createNewTeam(team)
            teamService.addMemberIntoTeam(userId, team.teamId.toString(), "0")
            mapOf(
                "success" to true,
                "message" to "创建团队成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "创建团队失败！"
            )
        )
    }

    @PostMapping("/delete")
    fun deleteTeam(
        @RequestParam("teamId") @ApiParam("teamId") teamId: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        var userId = request.session.getAttribute("user_id")?.toString().let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "用户未登录！"
            ) else it
        }
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        var perm = teamService.checkPerm(userId, teamId)
        println(perm)
        if (perm.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "非团队成员！"
        )
        if (perm != "0") return mapOf(
            "success" to false,
            "message" to "非团队创建者！"
        )
        teamService.deleteTeam(teamId)
        return mapOf(
            "success" to true,
            "message" to "解散团队成功！"
        )
    }

    @PostMapping("/getTeamList")
    fun getTeamList(
        @RequestParam("userId") @ApiParam("userId") user_id: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        var userId = request.session.getAttribute("user_id")?.toString().let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "用户未登录！"
            ) else it
        }
        if (!user_id.isNullOrBlank()) {
            userId = user_id
        }
        var teamList = teamService.searchTeamByUserId(userId)
        if (teamList.isNullOrEmpty()) {
            return mapOf(
                "success" to false,
                "message" to "团队为空！"
            )
        }
        return mapOf(
            "success" to true,
            "message" to "查询团队成功！",
            "data" to teamList
        )
    }

    @PostMapping("/getMemberList")
    fun getMemberList(
        @RequestParam("teamId") @ApiParam("teamId") teamId: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        var userId = request.session.getAttribute("user_id")?.toString().let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "用户未登录！"
            ) else it
        }
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        var memberList = teamService.searchMemberByTeamId(teamId)
        if (memberList.isNullOrEmpty()) {
            return mapOf(
                "success" to false,
                "message" to "团队成员为空！"
            )
        }
        return mapOf(
            "success" to true,
            "message" to "查询团队成员成功！",
            "data" to memberList
        )
    }

    @PostMapping("/inviteMember")
    fun inviteMember(
        @RequestParam("acceptId") @ApiParam("acceptId") acceptId: String?,
        @RequestParam("teamId") @ApiParam("teamId") teamId: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        var userId = request.session.getAttribute("user_id")?.toString().let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "用户未登录！"
            ) else it
        }
        if (acceptId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "被邀请用户id为空！"
        )
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        var perm = teamService.checkPerm(userId, teamId)
        if (perm.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "非当前团队成员！"
        )
        if (perm > "1") return mapOf(
            "success" to false,
            "message" to "非团队管理员！"
        )
        teamService.addMemberIntoTeam(acceptId, teamId, "2")
        return mapOf(
            "success" to true,
            "message" to "添加团队成员成功！"
        )
    }

    @PostMapping("/setPerm")
    fun setPerm(
        @RequestParam("teamId") @ApiParam("teamId") teamId: String?,
        @RequestParam("memberId") @ApiParam("memberId") memberId: String?,
        @RequestParam("userPerm") @ApiParam("userPerm") userPerm: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        var userId = request.session.getAttribute("user_id")?.toString().let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "用户未登录！"
            ) else it
        }
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        if (memberId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队成员id为空！"
        )
        if (userPerm.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "更改权限为空！"
        )
        var perm = teamService.checkPerm(userId, teamId)
        if (perm.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "非当前团队成员！"
        )
        if (perm >= userPerm) return mapOf(
            "success" to false,
            "message" to "当前用户权限不足！"
        )
        teamService.updatePerm(memberId, teamId, userPerm)
        return mapOf(
            "success" to true,
            "message" to "修改团队成员权限成功！"
        )
    }

    @PostMapping("/deleteMember")
    fun deleteMember(
        @RequestParam("memberId") @ApiParam("memberId") memberId: String?,
        @RequestParam("teamId") @ApiParam("teamId") teamId: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        var userId = request.session.getAttribute("user_id")?.toString().let {
            if (it.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "用户未登录！"
            ) else it
        }
        if (memberId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "待删除用户id为空！"
        )
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        var perm = teamService.checkPerm(userId, teamId)
        if (perm.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "非当前团队成员！"
        )
        if (perm > "1") return mapOf(
            "success" to false,
            "message" to "非团队管理员！"
        )
        teamService.deleteMemberByUserId(memberId, teamId)
        return mapOf(
            "success" to true,
            "message" to "删除团队成员成功！"
        )
    }
}