package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Project
import com.ryouonritsu.inkbook_backend.repository.DocumentationRepository
import com.ryouonritsu.inkbook_backend.repository.User2DocumentationRepository
import com.ryouonritsu.inkbook_backend.repository.UserRepository
import com.ryouonritsu.inkbook_backend.service.DocumentationService
import com.ryouonritsu.inkbook_backend.service.ProjectService
import com.ryouonritsu.inkbook_backend.service.TeamService
import com.ryouonritsu.inkbook_backend.utils.TokenUtils
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

    @Autowired
    lateinit var documentationService: DocumentationService

    @Autowired
    lateinit var docRepository: DocumentationRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var user2DocRepository: User2DocumentationRepository

    @PostMapping("/create")
    @Tag(name = "项目接口")
    @Operation(
        summary = "创建新项目", description = "项目信息为可选项\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"创建项目成功！\"\n" +
                "}"
    )
    fun createNewProject(
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
            val project = Project(project_name, project_info.let {
                if (it.isNullOrBlank()) {
                    ""
                } else it
            }, team_id.toLong())
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

    @PostMapping("/deprecate")
    @Tag(name = "项目接口")
    @Operation(
        summary = "弃置或恢复项目", description = "先检查是否为该项目团队成员根据传入的项目ID将对应项目放入或拿出回收站，\n" +
                "相应的文档和原型也会被回收或拿出，并隐藏或显示相关的最近访问记录和收藏\n" +
                "deprecated默认为true，即放入回收站，若要启动，则传入deprecated为false即可\n" +
                "即文档和对应的\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"删除项目成功！\"\n" +
                "}"
    )
    fun deprecateProject(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("project_id") @Parameter(description = "项目ID") project_id: String?,
        @RequestParam("deprecated", required = false) @Parameter(description = "是否弃置") deprecated: Boolean?
    ): Map<String, Any> {
        val isDeprecated = deprecated ?: true
        if (project_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "项目id为空！"
        )
        return runCatching {
            val teamId = projectService.searchTeamIdByProjectId(project_id)
            if (teamId.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "找不到该项目所在团队！"
            )
            val perm = teamService.checkPerm(user_id, teamId)
            if (perm.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "非该项目所在团队成员！"
            )
            projectService.deprecateProjectByProjectId(project_id, isDeprecated)
            if (isDeprecated) {
                return mapOf(
                    "success" to true,
                    "message" to "项目弃置成功！"
                )
            }
            mapOf(
                "success" to true,
                "message" to "项目恢复成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "项目操作失败！"
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
    fun deleteProject(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("project_id") @Parameter(description = "项目ID") project_id: String?
    ): Map<String, Any> {
        if (project_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "项目id为空！"
        )
        return runCatching {
            val teamId = projectService.searchTeamIdByProjectId(project_id)
            if (teamId.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "找不到该项目所在团队！"
            )
            val perm = teamService.checkPerm(user_id, teamId)
            if (perm.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "非该项目所在团队成员！"
            )
            val docList = documentationService.findByProjectId(project_id.toInt())
            docList.forEach {
                val doc_id = it.did ?: -1
                val user = userRepository.findById(TokenUtils.verify(token).second).get()
                user.favoritedocuments.removeAll { it.did == doc_id }
                val records = user2DocRepository.findByDocId(doc_id).map { it.id }
                user2DocRepository.deleteAllById(records)
                docRepository.deleteById(doc_id)
            }
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
    fun updateProject(
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
            val teamId = projectService.searchTeamIdByProjectId(project_id)
            if (teamId.isNullOrBlank()) return mapOf(
                "success" to false,
                "message" to "找不到该项目所在团队！"
            )
            val perm = teamService.checkPerm(user_id, teamId)
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
        summary = "获得团队项目列表", description = "返回团队ID对应团队的所有项目，deprecated为1（true）表示已被弃置，放入回收站" +
                "\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询团队项目成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"project_id\": 2,\n" +
                "            \"team_id\": \"1\",\n" +
                "            \"project_name\": \"55555\",\n" +
                "            \"project_info\": \"\",\n" +
                "            \"deprecated\": \"0\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"project_id\": 3,\n" +
                "            \"team_id\": \"1\",\n" +
                "            \"project_name\": \"123\",\n" +
                "            \"project_info\": \"\",\n" +
                "            \"deprecated\": \"1\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"project_id\": 4,\n" +
                "            \"team_id\": \"1\",\n" +
                "            \"project_name\": \"1234\",\n" +
                "            \"project_info\": \"\",\n" +
                "            \"deprecated\": \"1\"\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    )
    fun getProjectList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("team_id") @Parameter(description = "团队id") team_id: String?
    ): Map<String, Any> {
        if (team_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        val projectList = projectService.searchProjectByTeamId(team_id)
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

    @PostMapping("/getProject")
    @Tag(name = "项目接口")
    @Operation(
        summary = "获得指定项目信息",
        description = "可由指定项目ID获得对应项目信息\n" +
                "{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询团队信息成功！\",\n" +
                "    \"data\": [\n{\n" +
                "        \"project_id\": 3,\n" +
                "        \"team_id\": \"1\",\n" +
                "        \"project_name\": \"新名字\",\n" +
                "        \"project_info\": \"\"\n" +
                "    }\n" +
                "   ]\n" +
                "}"
    )
    fun getProject(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("project_id") @Parameter(description = "项目ID") project_id: String?
    ): Map<String, Any> {
        if (project_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "项目id为空！"
        )
        val project = projectService.searchProjectByProjectId(project_id) ?: let {
            return mapOf(
                "success" to false,
                "message" to "团队id无效！"
            )
        }
        return mapOf(
            "success" to true,
            "message" to "查询团队信息成功！",
            "data" to listOf(project)
        )
    }
}