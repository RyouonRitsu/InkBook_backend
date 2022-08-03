package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Team
import com.ryouonritsu.inkbook_backend.service.TeamService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
    @Operation(summary = "创建新团队", description = "团队信息为可选项")
    fun createNewTeam(
        @RequestParam("teamName") @Parameter(description = "团队名") teamName: String?,
        @RequestParam("teamInfo", required = false) @Parameter(description = "团队信息") teamInfo: String?,
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
    @Operation(summary = "删除团队", description = "根据传入的团队ID删除对应团队")
    fun deleteTeam(
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
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
    @Operation(summary = "更新团队信息", description = "更新对应团队ID的团队名或信息")
    fun updateTeam(
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
        @RequestParam("teamName") @Parameter(description = "团队名") teamName: String?,
        @RequestParam("teamInfo", required = false) @Parameter(description = "团队信息") teamInfo: String?,
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
    @Operation(summary = "获得团队列表", description = "返回用户所加入的所有团队，用户ID为可选项，若不传入用户ID，则默认为当前登录的用户")
    fun getTeamList(
        @RequestParam("userId", required = false) @Parameter(description = "用户ID") user_id: String?,
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
    @Operation(summary = "获得团队成员列表", description = "根据团队ID来获得对应的成员列表")
    fun getMemberList(
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
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
    @Operation(summary = "邀请成员", description = "管理员和超管可邀请成员，邀请后成员ID对应成员直接加入团队ID对应团队中")
    fun inviteMember(
        @RequestParam("acceptId") @Parameter(description = "被邀请成员ID") acceptId: String?,
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
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
    @Operation(summary = "设置权限", description = "0为超管，1为管理，2为成员。仅可设置权限比自己低的，例如0可设置1和2")
    fun setPerm(
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
        @RequestParam("memberId") @Parameter(description = "成员ID") memberId: String?,
        @RequestParam("userPerm") @Parameter(description = "用户权限") userPerm: String?,
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
    @Operation(summary = "删除成员", description = "当前登录用户可从团队中删除成员ID对应成员，前提是权限更高，例如0可删除1和2，1可删除2")
    fun deleteMember(
        @RequestParam("memberId") @Parameter(description = "成员ID") memberId: String?,
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
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