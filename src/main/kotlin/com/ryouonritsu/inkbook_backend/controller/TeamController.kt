package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Team
import com.ryouonritsu.inkbook_backend.service.TeamService
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "团队接口")
class TeamController {
    @Autowired
    lateinit var teamService: TeamService

    @PostMapping("/create")
    @Tag(name = "团队接口")
    fun createNewTeam(
        @RequestParam("teamName") teamName: String?,
        @RequestParam("teamInfo", required = false) teamInfo: String?,
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
    @Tag(name = "团队接口")
    fun deleteTeam(
        @RequestParam("teamId") teamId: String?,
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

    @PostMapping("/update")
    @Tag(name = "团队接口")
    fun updateTeam(
        @RequestParam("teamId") teamId: String?,
        @RequestParam("teamName") teamName: String?,
        @RequestParam("teamInfo", required = false) teamInfo: String?,
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
            "message" to "团队名不可为空！"
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
        return runCatching {
            teamService.updateTeam(teamId, teamName, teamInfo.let {
                if (it.isNullOrBlank()) {
                    ""
                } else it
            })
            mapOf(
                "success" to true,
                "message" to "更新团队信息成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "更新团队信息失败！"
            )
        )
    }

    @PostMapping("/getTeamList")
    @Tag(name = "团队接口")
    fun getTeamList(
        @RequestParam("userId", required = false) user_id: String?,
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
    @Tag(name = "团队接口")
    fun getMemberList(
        @RequestParam("teamId") teamId: String?,
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
    @Tag(name = "团队接口")
    fun inviteMember(
        @RequestParam("acceptId") acceptId: String?,
        @RequestParam("teamId") teamId: String?,
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
    @Tag(name = "团队接口")
    fun setPerm(
        @RequestParam("teamId") teamId: String?,
        @RequestParam("memberId") memberId: String?,
        @RequestParam("userPerm") userPerm: String?,
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
    @Tag(name = "团队接口")
    fun deleteMember(
        @RequestParam("memberId") memberId: String?,
        @RequestParam("teamId") teamId: String?,
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