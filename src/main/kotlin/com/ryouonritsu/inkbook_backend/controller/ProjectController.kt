package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Project
import com.ryouonritsu.inkbook_backend.entity.Team
import com.ryouonritsu.inkbook_backend.service.ProjectService
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
@RequestMapping("/project")
@Tag(name = "项目接口")
class ProjectController {
    @Autowired
    lateinit var projectService: ProjectService

    @Autowired
    lateinit var teamService: TeamService

    @PostMapping("/create")
    @Tag(name = "项目接口")
    @Operation(summary = "创建新项目", description = "项目信息为可选项")
    fun createNewTeam(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("project_name") @Parameter(description = "项目名") project_name: String?,
        @RequestParam("project_info", required = false) @Parameter(description = "项目信息") project_info: String?,
        @RequestParam("team_id") @Parameter(description = "创建该项目的团队id") team_id: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (project_name.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "项目名为空！"
        )
        if (team_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        return runCatching {
            var project = Project(project_name, project_info.let {
                if (it.isNullOrBlank()) {
                    ""
                } else it
            }, team_id)
            projectService.createNewProject(project)
            mapOf(
                "success" to true,
                "message" to "创建项目成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "创建项目失败！"
            )
        )
    }

    @PostMapping("/delete")
    @Tag(name = "项目接口")
    @Operation(summary = "删除项目", description = "先检查是否为该项目团队成员根据传入的项目ID删除对应项目")
    fun deleteTeam(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("project_id") @Parameter(description = "项目ID") project_id: String?,
        request: HttpServletRequest
    ): Map<String, Any> {
        if (project_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "项目id为空！"
        )
        var team_id = projectService.searchTeamIdByProjectId(project_id)
        if (team_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "找不到该项目所在团队！"
        )
        var perm = teamService.checkPerm(user_id, team_id)
        if (perm.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "非该项目所在团队成员！"
        )
        projectService.deleteProject(project_id)
        return mapOf(
            "success" to true,
            "message" to "删除项目成功！"
        )
    }

}