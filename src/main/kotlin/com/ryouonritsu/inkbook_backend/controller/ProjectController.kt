package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Axure
import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.entity.DocumentationDict
import com.ryouonritsu.inkbook_backend.entity.Project
import com.ryouonritsu.inkbook_backend.repository.*
import com.ryouonritsu.inkbook_backend.service.AxureService
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    lateinit var projectRepository: ProjectRepository

    @Autowired
    lateinit var teamService: TeamService

    @Autowired
    lateinit var teamRepository: TeamRepository

    @Autowired
    lateinit var axureService: AxureService

    @Autowired
    lateinit var documentationService: DocumentationService

    @Autowired
    lateinit var docRepository: DocumentationRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var user2DocRepository: User2DocumentationRepository

    @Autowired
    lateinit var docDictRepository: DocumentationDictRepository

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
            val time = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSS_"))
            var project = Project(project_name, project_info.let {
                if (it.isNullOrBlank()) {
                    ""
                } else it
            }, time, time, team_id.toLong())
            project = projectRepository.save(project)
            // add project to dict
            // to 吴: 新增加的文档中心创建项目根目录逻辑(删除应仿照此处改写)
            val team = teamRepository.findById(team_id.toInt()).get() // 此处可能有bug
            val prjDict = docDictRepository.save(DocumentationDict(name = project_name, pid = project.project_id))
            val prjRoot = docDictRepository.findById(team.prjRootId).get()
            prjRoot.children.add(prjDict)
            prjRoot.hasChildren = true
            prjDict.parent = prjRoot
            docDictRepository.save(prjDict)
            docDictRepository.save(prjRoot)
            // to 吴: 此处应该将${prjDict.id}放入到project实体中保存, 并在每次返回project信息的时候携带上
            project.prjDictId = prjDict.id
            // end add to dict
//            projectService.createNewProject(project) to 吴: 此处为了测试暂时注释掉了, 你后续再改你的Mapper
            projectRepository.save(project)
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

    @PostMapping("/copy")
    @Tag(name = "项目接口")
    @Operation(
        summary = "创建项目副本", description = "复制项目，及其下所属的文档和原型。在三者名称后添加 副本 字样，\n" +
                "并更新项目创建时间和更新时间，更新文档和原型所属的项目ID，其余保持不变。\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"复制项目成功！\"\n" +
                "}"
    )
    fun copyNewProject(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("project_id") @Parameter(description = "母本项目的项目id") project_id: String,
        @RequestParam("team_id") @Parameter(description = "创建该项目的团队id") team_id: String?
    ): Map<String, Any> {
        val project = projectService.searchProjectByProjectId(project_id) ?: return mapOf(
            "success" to false,
            "message" to "项目不存在！"
        )
        if (team_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        val project_name = project.get("project_name")!! + " 副本"
        val project_info = project.get("project_info")!!
        var msg = "复制项目失败！"
        return runCatching {
            val time = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val project = Project(project_name, project_info.let {
                if (it.isNullOrBlank()) {
                    ""
                } else it
            }, time, time, team_id.toLong())
            projectService.createNewProject(project)
            val newProjectId = project.project_id
            msg = "复制原型失败！"
            axureService.searchAxureAllByProjectId(project_id)?.forEach {
                val axure = Axure(
                    it.axure_name + " 副本",
                    it.axure_info,
                    newProjectId,
                    it.title,
                    it.items,
                    it.config,
                    it.config_id,
                    it.last_edit,
                    it.create_user
                )
                axureService.createNewAxure(axure)
            }

            msg = "复制文档失败！"
            docRepository.findByPid(project_id.toInt()).forEach {
                val prj = projectRepository.findById(newProjectId).get()
                val team = teamRepository.findById(prj.team_id.toInt()).get()
                val doc = Documentation(it.dname!! + " 副本", it.ddescription, it.dcontent, prj, team, it.creator!!)
                docRepository.save(doc)
            }

            mapOf(
                "success" to true,
                "message" to "复制项目成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to msg
            )
        )
    }

    @PostMapping("/deprecate")
    @Tag(name = "项目接口")
    @Operation(
        summary = "弃置或恢复项目",
        description = "先检查是否为该项目团队成员根据传入的项目ID将对应项目放入或拿出回收站，\n" +
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
            val docList = docRepository.findByPid(project_id.toInt())
            docList.forEach {
                val doc_id = it.did ?: -1
                val user = userRepository.findById(TokenUtils.verify(token).second).get()
                user.favoritedocuments.removeAll { it.did == doc_id }
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

    @PostMapping("/search")
    @Tag(name = "项目接口")
    @Operation(
        summary = "搜索项目", description = "根据项目名字或简介在所在团队由关键字进行模糊搜索\n" +
                "没有对应项目时将会返回一个空data即[]\n" +
                "{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"搜索项目成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"project_name\": \"Project\",\n" +
                "            \"project_info\": \"\",\n" +
                "            \"prj_create_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"prj_last_edit_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"team_id\": 104,\n" +
                "            \"project_id\": 83,\n" +
                "            \"deprecated\": false\n" +
                "        },\n" +
                "        {\n" +
                "            \"project_name\": \"Project2\",\n" +
                "            \"project_info\": \"\",\n" +
                "            \"prj_create_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"prj_last_edit_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"team_id\": 104,\n" +
                "            \"project_id\": 85,\n" +
                "            \"deprecated\": false\n" +
                "        },\n" +
                "        {\n" +
                "            \"project_name\": \"Project3\",\n" +
                "            \"project_info\": \"\",\n" +
                "            \"prj_create_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"prj_last_edit_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"team_id\": 104,\n" +
                "            \"project_id\": 86,\n" +
                "            \"deprecated\": false\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    )
    fun searchProject(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("user_id") @Parameter(description = "用于认证的用户id") user_id: String,
        @RequestParam("team_id") @Parameter(description = "团队ID") team_id: String?,
        @RequestParam("keyword") @Parameter(description = "关键字") keyword: String?,
    ): Map<String, Any> {
        if (team_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "团队id为空！"
        )
        if (keyword == null) return mapOf(
            "success" to false,
            "message" to "关键字为空！"
        )
        return runCatching {
            val projectList = projectService.searchProjectByKeyWord(team_id, keyword) ?: arrayListOf<Project>()
            mapOf(
                "success" to true,
                "message" to "搜索项目成功！",
                "data" to projectList
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "搜索项目失败！"
            )
        )
    }

    @PostMapping("/getProjectList")
    @Tag(name = "项目接口")
    @Operation(
        summary = "获得团队项目列表",
        description = "返回团队ID对应团队的所有项目，deprecated为true表示已被弃置，放入回收站" +
                "\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询团队项目成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"prj_create_time\": \"2022-08-08 09:29:09\",\n" +
                "            \"project_id\": 62,\n" +
                "            \"prj_last_edit_time\": \"2022-08-08 09:29:09\",\n" +
                "            \"deprecated\": false,\n" +
                "            \"team_id\": 5,\n" +
                "            \"project_name\": \"hgh\",\n" +
                "            \"project_info\": \"v\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"prj_create_time\": \"2022-08-08 09:29:09\",\n" +
                "            \"project_id\": 80,\n" +
                "            \"prj_last_edit_time\": \"2022-08-08 09:29:09\",\n" +
                "            \"deprecated\": false,\n" +
                "            \"team_id\": 5,\n" +
                "            \"project_name\": \"asas\",\n" +
                "            \"project_info\": \"sa\"\n" +
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
                "    \"data\": [\n" +
                "        {\n" +
                "            \"prj_create_time\": \"2022-08-08 09:29:09\",\n" +
                "            \"project_id\": 13,\n" +
                "            \"prj_last_edit_time\": \"2022-08-08 09:29:09\",\n" +
                "            \"deprecated\": false,\n" +
                "            \"team_id\": 20,\n" +
                "            \"project_name\": \"我是谁啊\",\n" +
                "            \"project_info\": \"皮卡丘\"\n" +
                "        }\n" +
                "    ]\n" +
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