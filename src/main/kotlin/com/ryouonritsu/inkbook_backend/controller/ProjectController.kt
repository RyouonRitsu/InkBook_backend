package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Project
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
    @Operation(
        summary = "创建新项目", description = "项目信息为可选项\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"创建项目成功！\"\n" +
                "}"
    )
    fun createNewTeam(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("project_name") @Parameter(description = "项目名") project_name: String?,
        @RequestParam("project_info", required = false) @Parameter(description = "项目信息") project_info: String?,
        @RequestParam("team_id") @Parameter(description = "创建该项目的团队id") team_id: String?
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
    @Operation(
        summary = "删除项目", description = "先检查是否为该项目团队成员根据传入的项目ID删除对应项目\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"删除项目成功！\"\n" +
                "}"
    )
    fun deleteTeam(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("project_id") @Parameter(description = "项目ID") project_id: String?
    ): Map<String, Any> {
        if (project_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "项目id为空！"
        )
        return runCatching {
            var teamId = projectService.searchTeamIdByProjectId(project_id)
            if (teamId.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "找不到该项目所在团队！"
            )
            var perm = teamService.checkPerm(user_id, teamId)
            if (perm.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "非该项目所在团队成员！"
            )
            projectService.deleteProject(project_id)
            mapOf(
                "success" to true,
                "message" to "删除项目成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "删除项目失败！"
            )
        )
    }

    @PostMapping("/update")
    @Tag(name = "项目接口")
    @Operation(
        summary = "更新项目信息", description = "更新对应项目ID的项目名或信息\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"更新项目成功！\"\n" +
                "}"
    )
    fun updateTeam(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("project_id") @Parameter(description = "项目ID") project_id: String?,
        @RequestParam("project_name") @Parameter(description = "项目名") project_name: String?,
        @RequestParam("project_info", required = false) @Parameter(description = "项目信息") project_info: String?
    ): Map<String, Any> {
        if (project_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "项目id为空！"
        )
        if (project_name.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "项目名不可为空！"
        )
        return runCatching {
            var teamId = projectService.searchTeamIdByProjectId(project_id)
            if (teamId.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "找不到该项目所在团队！"
            )
            var perm = teamService.checkPerm(user_id, teamId)
            if (perm.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "非该项目所在团队成员！"
            )
            projectService.updateProject(project_id, project_name, project_info.let {
                if (it.isNullOrBlank()) {
                    ""
                } else it
            })
            mapOf(
                "success" to true,
                "message" to "更新项目信息成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "更新项目信息失败！"
            )
        )
    }

    @PostMapping("/getProjectList")
    @Tag(name = "项目接口")
    @Operation(
        summary = "获得团队项目列表", description = "返回团队ID对应团队的所有项目" +
                "\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询团队项目成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"project_id\": 2,\n" +
                "            \"team_id\": \"1\",\n" +
                "            \"project_name\": \"55555\",\n" +
                "            \"project_info\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"project_id\": 3,\n" +
                "            \"team_id\": \"1\",\n" +
                "            \"project_name\": \"123\",\n" +
                "            \"project_info\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"project_id\": 4,\n" +
                "            \"team_id\": \"1\",\n" +
                "            \"project_name\": \"1234\",\n" +
                "            \"project_info\": \"\"\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    )
    fun getTeamList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("team_id") @Parameter(description = "团队id") team_id: String?
    ): Map<String, Any> {
        if (team_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        var projectList = projectService.searchProjectByTeamId(team_id)
        if (projectList.isNullOrEmpty()) {
            return mapOf(
                "success" to false,
                "message" to "团队项目为空！"
            )
        }
        return mapOf(
            "success" to true,
            "message" to "查询团队项目成功！",
            "data" to projectList
        )
    }
}