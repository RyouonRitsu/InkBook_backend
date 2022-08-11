package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.annotation.Recycle
import com.ryouonritsu.inkbook_backend.entity.UML
import com.ryouonritsu.inkbook_backend.repository.ProjectRepository
import com.ryouonritsu.inkbook_backend.repository.UMLRepository
import com.ryouonritsu.inkbook_backend.repository.UserRepository
import com.ryouonritsu.inkbook_backend.service.ProjectService
import com.ryouonritsu.inkbook_backend.service.TeamService
import com.ryouonritsu.inkbook_backend.service.UMLService
import com.ryouonritsu.inkbook_backend.service.UserService
import com.ryouonritsu.inkbook_backend.utils.TokenUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 *
 * @author WuKunchao
 */
@RestController
@RequestMapping("/uml")
@Tag(name = "UML接口")
class UMLController {
    @Autowired
    lateinit var umlService: UMLService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var projectService: ProjectService

    @Autowired
    lateinit var umlRepository: UMLRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @Autowired
    lateinit var teamService: TeamService

    @PostMapping("/create")
    @Tag(name = "UML接口")
    @Operation(
        summary = "创建新UML",
        description = "将发起请求的用户作为UML创建者，展示用户名。"
    )
    fun createNewAxure(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("uml_name") @Parameter(description = "UML名字") uml_name: String,
        @RequestParam("project_id") @Parameter(description = "所在项目id") project_id: Int,
    ): Map<String, Any> {
        return runCatching {
            val user_id = TokenUtils.verify(token).second
            val user = userService.get(user_id) ?: return mapOf(
                "success" to false,
                "message" to "用户不存在！"
            )
            val name = user.username
            val uml = UML(uml_name, "", "", name!!, project_id)
            umlService.createNewUML(uml)
            mapOf(
                "success" to true,
                "message" to "创建UML成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "创建UML失败！"
            )
        )
    }

    @PostMapping("/update")
    @Tag(name = "UML接口")
    @Operation(summary = "更新UML内容", description = "保存UML信息，并更新UML和项目最后编辑时间")
    fun updateAxureInfo(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("uml_id") @Parameter(description = "UMLid") uml_id: String,
        @RequestParam("lastModified") @Parameter(description = "最后编辑时间") lastModified: String,
        @RequestParam("xml") @Parameter(description = "UML内容") xml: String,
    ): Map<String, Any> {
        return runCatching {
            val uml = umlService.selectUMLByUMLId(uml_id)
                ?: return mapOf(
                    "success" to false,
                    "message" to "对应UML不存在！"
                )
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val time = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(formatter)
            umlService.updateUML(uml_id, lastModified, xml)
            projectService.updateProjectLastEditTime(uml.project_id.toString(), time)
            mapOf(
                "success" to true,
                "message" to "更新UML信息成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "更新UML信息失败！"
            )
        )
    }

    @PostMapping("/getUMLInfo")
    @Tag(name = "UML接口")
    @Operation(
        summary = "获得UML信息", description = "通过UMLID获取对应UML"
    )
    fun getAxureInfo(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("uml_id") @Parameter(description = "UMLid") uml_id: String,
    ): Map<String, Any> {
        return runCatching {
            val uml = umlService.selectUMLByUMLId(uml_id)
                ?: return mapOf(
                    "success" to true,
                    "message" to "新建UML画布！",
                    "data" to arrayListOf<UML>()
                )
            mapOf(
                "success" to true,
                "message" to "查询UML信息成功！",
                "data" to listOf(uml.toDict())
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "查询UML信息失败！"
            )
        )
    }

    @GetMapping("/getUMLList")
    @Tag(name = "UML接口")
    @Operation(
        summary = "获得UML列表", description = "通过项目ID获取对应UML\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询项目UML列表成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"creator\": \"123\",\n" +
                "            \"project_id\": 122,\n" +
                "            \"uml_name\": \"test\",\n" +
                "            \"last_modified\": \"2022-08-11T04:51:22.472Z\",\n" +
                "            \"uml_id\": 1\n" +
                "        },\n" +
                "        {\n" +
                "            \"creator\": \"123\",\n" +
                "            \"project_id\": 122,\n" +
                "            \"uml_name\": \"test\",\n" +
                "            \"last_modified\": \"\",\n" +
                "            \"uml_id\": 2\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    )
    fun getUMLList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("project_id") @Parameter(description = "project_id") project_id: String,
    ): Map<String, Any> {
        return runCatching {
            val umlList = umlService.searchUMLByProjectId(project_id)
            if (umlList.isNullOrEmpty()) {
                return mapOf(
                    "success" to true,
                    "message" to "项目UML为空！",
                    "data" to arrayListOf<UML>()
                )
            }
            return mapOf(
                "success" to true,
                "message" to "查询项目UML列表成功！",
                "data" to umlList
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "查询项目UML列表失败！"
            )
        )
    }

    @PostMapping("/updateInfo")
    @Tag(name = "UML接口")
    @Operation(
        summary = "更新UML信息", description = "用于更改UML文件名，不会更新最后编辑时间"
    )
    fun updateUML(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("uml_id") @Parameter(description = "UMLid") uml_id: String,
        @RequestParam("uml_name") @Parameter(description = "UML名字") uml_name: String,
    ): Map<String, Any> {
        return runCatching {
            umlService.updateUMLInfo(uml_id, uml_name)
            mapOf(
                "success" to true,
                "message" to "更新UML信息成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "更新UML信息失败！"
            )
        )
    }

    @PostMapping("/delete")
    @Tag(name = "UML接口")
    @Operation(
        summary = "删除UML", description = "删除给定UMLid对应UML\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"删除UML成功！\"\n" +
                "}"
    )
    fun deleteUML(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("uml_id") @Parameter(description = "UMLid") uml_id: String,
    ): Map<String, Any> {
        return runCatching {
            val user_id = TokenUtils.verify(token).second
            userRepository.findById(user_id).get().favoriteUMLs.removeIf { it.uml_id == uml_id.toInt() }
            umlService.deleteUMLByUMLId(uml_id)
            return mapOf(
                "success" to true,
                "message" to "删除UML成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "删除UML失败！"
            )
        )
    }

    @GetMapping("searchDoc")
    @Tag(name = "UML接口")
    @Operation(summary = "搜索UML", description = "根据关键字搜索UML, 可选择搜索指定团队或项目下的UML")
    @Recycle
    fun searchDoc(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("keyword") @Parameter(description = "关键字") keyword: String?,
        @RequestParam("project_id", defaultValue = "-1") @Parameter(description = "要查询的项目Id") project_id: Int,
        @RequestParam("team_id", defaultValue = "-1") @Parameter(description = "要查询的团队Id") team_id: Int
    ): Map<String, Any> {
        if (keyword.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "关键字不能为空"
        )
        val userId = TokenUtils.verify(token).second
        val UMLs = mutableListOf<UML>()
        if (project_id != -1) UMLs.addAll(umlRepository.findByKeywordAndProjectId(keyword, project_id))
        else if (team_id != -1) UMLs.addAll(umlRepository.findByKeywordAndTeamId(keyword, team_id))
        else {
            val teams = teamService.searchTeamByUserId("$userId") ?: listOf()
            val tIds = teams.map { "${it["team_id"]}".toInt() }
            tIds.forEach { UMLs.addAll(umlRepository.findByKeywordAndTeamId(keyword, it)) }
        }
        return try {
            mapOf(
                "success" to true,
                "message" to "搜索成功",
                "data" to UMLs.map {
                    HashMap(it.toDict()).apply {
                        this["is_favorite"] = it in userRepository.findById(userId).get().favoriteUMLs
                        val project = projectRepository.findById(it.project_id).get()
                        putAll(project.toDict())
                        putAll(teamService.searchTeamByTeamId("${project.team_id}")
                            ?: throw Exception("数据库中没有此团队, 请检查团队id是否正确"))
                    }
                }
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "message" to (e.message ?: "搜索失败, 发生意外错误")
            )
        }
    }
}