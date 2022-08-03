package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Team
import com.ryouonritsu.inkbook_backend.service.TeamService
import com.ryouonritsu.inkbook_backend.service.UserService
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

    @Autowired
    lateinit var userService: UserService

    @PostMapping("/create")
    @Tag(name = "团队接口")
    @Operation(summary = "创建新团队", description = "0为超管，1为管理，2为成员。团队信息为可选项")
    fun createNewTeam(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("teamName") @Parameter(description = "团队名") teamName: String?,
        @RequestParam("teamInfo", required = false) @Parameter(description = "团队信息") teamInfo: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
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
            teamService.addMemberIntoTeam(user_id, team.teamId.toString(), "0")
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
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        var perm = teamService.checkPerm(user_id, teamId)
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
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
        @RequestParam("teamName") @Parameter(description = "团队名") teamName: String?,
        @RequestParam("teamInfo", required = false) @Parameter(description = "团队信息") teamInfo: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (teamName.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队名不可为空！"
        )
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        var perm = teamService.checkPerm(user_id, teamId)
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
    @Operation(
        summary = "获得团队列表",
        description = "0为超管，1为管理，2为成员。返回用户所加入的所有团队，其他用户ID为可选项，若不传入其他用户ID，则默认为当前登录的用户\n{\n" +
                "    \"success\": false,\n" +
                "    \"message\": \"团队为空！\"\n" +
                "}\n" +
                "或\n" +
                "\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询团队成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"team_info\": \"1234\",\n" +
                "            \"user_id\": 2,\n" +
                "            \"team_id\": 10,\n" +
                "            \"team_name\": \"123444\",\n" +
                "            \"user_perm\": 0\n" +
                "        },\n" +
                "        {\n" +
                "            \"team_info\": \"1234\",\n" +
                "            \"user_id\": 2,\n" +
                "            \"team_id\": 11,\n" +
                "            \"team_name\": \"123444\",\n" +
                "            \"user_perm\": 0\n" +
                "        },\n" +
                "        {\n" +
                "            \"team_info\": \"123\",\n" +
                "            \"user_id\": 2,\n" +
                "            \"team_id\": 5,\n" +
                "            \"team_name\": \"123\",\n" +
                "            \"user_perm\": 2\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    )
    fun getTeamList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("other_id", required = false) @Parameter(description = "需要查询的用户ID") other_id: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        var userId = other_id.let {
            if (it.isNullOrBlank())
                user_id
            else
                it
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
    @Operation(
        summary = "获得团队成员列表", description = "0为超管，1为管理，2为成员。根据团队ID来获得对应的成员列表\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询团队成员成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"user_id\": 1,\n" +
                "            \"real_name\": \"2\",\n" +
                "            \"email\": \"1\",\n" +
                "            \"user_perm\": 0,\n" +
                "            \"username\": \"123\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"user_id\": 2,\n" +
                "            \"real_name\": \"2\",\n" +
                "            \"email\": \"1\",\n" +
                "            \"user_perm\": 2,\n" +
                "            \"username\": \"1234\"\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    )
    fun getMemberList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
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
    @Operation(
        summary = "邀请成员",
        description = "0为超管，1为管理，2为成员。\n" +
                "管理员和超管可邀请成员，邀请后若邮箱对应用户存在，则获取用户ID对应用户直接加入团队ID对应团队中，并设置权限为2。\n" +
                "{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"添加团队成员成功！\"\n" +
                "}\n" +
                "或\n" +
                "{\n" +
                "    \"success\": false,\n" +
                "    \"message\": \"该用户不存在！\"\n" +
                "}"
    )
    fun inviteMember(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("email") @Parameter(description = "被邀请成员邮箱") email: String?,
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (email.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "被邀请用户邮箱为空！"
        )
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        val perm = teamService.checkPerm(user_id, teamId)
        if (perm.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "非当前团队成员！"
        )
        if (perm > "1") return mapOf(
            "success" to false,
            "message" to "非团队管理员！"
        )
        val t = userService.selectUserByEmail(email) ?: return mapOf(
            "success" to false,
            "message" to "该用户不存在！"
        )
        val inTeam = teamService.checkPerm(t.user_id.toString(), teamId)
        if (inTeam != null) {
            return mapOf(
                "success" to false,
                "message" to "被邀请用户已加入该团队！"
            )
        }
        teamService.addMemberIntoTeam(t.user_id.toString(), teamId, "2")
        return mapOf(
            "success" to true,
            "message" to "添加团队成员成功！"
        )
    }

    @PostMapping("/setPerm")
    @Tag(name = "团队接口")
    @Operation(summary = "设置权限", description = "0为超管，1为管理，2为成员。仅可设置权限比自己低的，例如0可设置1和2")
    fun setPerm(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
        @RequestParam("memberId") @Parameter(description = "成员ID") memberId: String?,
        @RequestParam("userPerm") @Parameter(description = "用户权限") userPerm: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
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
        var perm = teamService.checkPerm(user_id, teamId)
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
    @Operation(
        summary = "删除成员",
        description = "当前登录用户可从团队中删除成员ID对应成员，前提是权限更高，例如0可删除1和2，1可删除2"
    )
    fun deleteMember(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("memberId") @Parameter(description = "成员ID") memberId: String?,
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (memberId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "待删除用户id为空！"
        )
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        var perm = teamService.checkPerm(user_id, teamId)
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

    @PostMapping("/quitTeam")
    @Tag(name = "团队接口")
    @Operation(
        summary = "用户退出团队", description = "当前登录用户退出团队ID对应团队，如果是超管0，则会直接解散团队\n" +
                "{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"解散团队成功！\"\n" +
                "}\n" +
                "或\n" +
                "{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"退出团队成功！\"\n" +
                "}\n" +
                "或\n" +
                "{\n" +
                "    \"success\": false,\n" +
                "    \"message\": \"非当前团队成员！\"\n" +
                "}"
    )
    fun deleteMember(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        var perm = teamService.checkPerm(user_id, teamId)
        if (perm.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "非当前团队成员！"
        )
        if (perm == "0") {
            teamService.deleteTeam(teamId)
            return mapOf(
                "success" to true,
                "message" to "解散团队成功！"
            )
        }
        teamService.deleteMemberByUserId(user_id, teamId)
        return mapOf(
            "success" to true,
            "message" to "退出团队成功！"
        )
    }

    @PostMapping("/getTeam")
    @Tag(name = "团队接口")
    @Operation(
        summary = "获得指定团队信息",
        description = "可由指定团队ID获得对应团队信息\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询团队信息成功！\",\n" +
                "    \"data\": {\n" +
                "        \"team_info\": \"123\",\n" +
                "        \"team_id\": 5,\n" +
                "        \"team_name\": \"123\"\n" +
                "    }\n" +
                "}"
    )
    fun getTeam(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("teamId") @Parameter(description = "团队ID") teamId: String?
    ): Map<String, Any> {
        if (teamId.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        val team = teamService.searchTeamByTeamId(teamId) ?: let {
            return mapOf(
                "success" to false,
                "message" to "团队id无效！"
            )
        }
        return mapOf(
            "success" to true,
            "message" to "查询团队信息成功！",
            "data" to team
        )
    }
}