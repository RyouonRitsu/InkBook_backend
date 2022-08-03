package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Project
import com.ryouonritsu.inkbook_backend.entity.Team
import com.ryouonritsu.inkbook_backend.service.ProjectService
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

    @PostMapping("/create")
    @Tag(name = "团队接口")
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
}