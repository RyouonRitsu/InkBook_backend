package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.annotation.Recycle
import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.entity.User2Documentation
import com.ryouonritsu.inkbook_backend.repository.DocumentationRepository
import com.ryouonritsu.inkbook_backend.repository.User2DocumentationRepository
import com.ryouonritsu.inkbook_backend.repository.UserRepository
import com.ryouonritsu.inkbook_backend.service.ProjectService
import com.ryouonritsu.inkbook_backend.service.TeamService
import com.ryouonritsu.inkbook_backend.utils.RedisUtils
import com.ryouonritsu.inkbook_backend.utils.TokenUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/doc")
@Tag(name = "文档接口")
class DocumentationController {
    val logger: Logger = LoggerFactory.getLogger(DocumentationController::class.java)

    @Autowired
    lateinit var docRepository: DocumentationRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var user2DocRepository: User2DocumentationRepository

    @Autowired
    lateinit var projectService: ProjectService

    @Autowired
    lateinit var teamService: TeamService

    @Autowired
    lateinit var redisUtils: RedisUtils

    fun check(doc_name: String?, project_id: Int?): Pair<Boolean, Map<String, Any>?> {
        if (doc_name.isNullOrBlank()) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "文档名称不能为空"
            )
        )
        if (project_id == null) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "项目Id不能为空"
            )
        )
        if (doc_name.length > 200) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "文档名称不能超过200个字符"
            )
        )
        return Pair(true, null)
    }

    @PostMapping("/newDoc")
    @Tag(name = "文档接口")
    @Operation(summary = "新建文档", description = "文档描述和文档内容不是必要的")
    fun newDoc(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("doc_name") @Parameter(description = "文档名称") doc_name: String?,
        @RequestParam(
            "doc_description",
            defaultValue = ""
        ) @Parameter(description = "文档描述") doc_description: String,
        @RequestParam("doc_content", defaultValue = "") @Parameter(description = "文档内容") doc_content: String,
        @RequestParam("project_id") @Parameter(description = "项目Id") project_id: Int?
    ): Map<String, Any> {
        val (result, message) = check(doc_name, project_id)
        if (!result && message != null) return message
        return runCatching {
            val creator_id = TokenUtils.verify(token).second
            val creator = userRepository.findById(creator_id).get()
            val doc = Documentation(doc_name!!, doc_description, doc_content, project_id!!, creator)
            docRepository.save(doc)
            mapOf(
                "success" to true,
                "message" to "文档创建成功"
            )
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${TokenUtils.verify(token).second}"
                mapOf(
                    "success" to false,
                    "message" to "用户不存在, 可能是数据库出错, 请检查后重试, 当前会话已失效"
                )
            }
            it.printStackTrace()
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "文档创建失败, 发生意外错误"
            )
        )
    }

    @PostMapping("/deleteDoc")
    @Tag(name = "文档接口")
    @Operation(
        summary = "删除文档",
        description = "使用文档Id删除文档, recycle参数代表是否放入回收站, 默认为true, 填写false会不可逆的删除文档"
    )
    fun deleteDoc(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("doc_id") @Parameter(description = "文档Id") doc_id: Long?,
        @RequestParam("recycle", defaultValue = "true") @Parameter(description = "是否放入回收站") recycle: Boolean
    ): Map<String, Any> {
        return runCatching {
            if (doc_id == null) return@runCatching mapOf(
                "success" to false,
                "message" to "文档Id不能为空"
            )
            val doc = try {
                docRepository.findById(doc_id).get()
            } catch (e: NoSuchElementException) {
                return@runCatching mapOf(
                    "success" to false,
                    "message" to "文档不存在"
                )
            }
            if (doc.deprecated) return mapOf(
                "success" to false,
                "message" to "文档已被删除"
            )
            if (recycle) {
                doc.deprecated = true
                docRepository.save(doc)
            } else {
                val user = userRepository.findById(TokenUtils.verify(token).second).get()
                user.favoritedocuments.removeAll { it.did == doc_id }
                docRepository.deleteById(doc_id)
            }
            mapOf(
                "success" to true,
                "message" to "文档删除成功"
            )
        }.onFailure {
            if (it is NoSuchElementException) {
                redisUtils - "${TokenUtils.verify(token).second}"
                return mapOf(
                    "success" to false,
                    "message" to "用户不存在, 可能是数据库出错, 请检查后重试, 当前会话已失效"
                )
            }
            it.printStackTrace()
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "文档删除失败, 发生意外错误"
            )
        )
    }

    @PostMapping("/recoverDoc")
    @Tag(name = "文档接口")
    @Operation(summary = "恢复文档", description = "使用文档Id恢复文档")
    fun recoverDoc(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("doc_id") @Parameter(description = "文档Id") doc_id: Long?
    ): Map<String, Any> {
        return runCatching {
            if (doc_id == null) return@runCatching mapOf(
                "success" to false,
                "message" to "文档Id不能为空"
            )
            val doc = try {
                docRepository.findById(doc_id).get()
            } catch (e: NoSuchElementException) {
                return@runCatching mapOf(
                    "success" to false,
                    "message" to "文档不存在"
                )
            }
            if (!doc.deprecated) return mapOf(
                "success" to false,
                "message" to "文档未被删除"
            )
            doc.deprecated = false
            docRepository.save(doc)
            return mapOf(
                "success" to true,
                "message" to "文档恢复成功"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "文档恢复失败, 发生意外错误"
            )
        )
    }

    @PostMapping("/updateDocInfo")
    @Tag(name = "文档接口")
    @Operation(
        summary = "更新文档信息",
        description = "更新文档信息, 留空表示不更新此参数对应的信息, 此操作**不会刷新**文档和项目的最后编辑时间和最后浏览时间"
    )
    fun updateDocInfo(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("doc_id") @Parameter(description = "要操作的文档Id") doc_id: Long?,
        @RequestParam("doc_name", defaultValue = "") @Parameter(description = "文档名称") doc_name: String,
        @RequestParam(
            "doc_description",
            defaultValue = ""
        ) @Parameter(description = "文档描述") doc_description: String
    ): Map<String, Any> {
        return runCatching {
            if (doc_id == null) return@runCatching mapOf(
                "success" to false,
                "message" to "文档Id不能为空"
            )
            val doc = docRepository.findById(doc_id).get()
            if (doc.deprecated) return mapOf(
                "success" to false,
                "message" to "文档已被删除, 请恢复后再进行此操作"
            )
            if (doc_name.isNotBlank()) {
                if (doc_name.length > 200) return@runCatching mapOf(
                    "success" to false,
                    "message" to "文档名称不能超过200个字符"
                )
                doc.dname = doc_name
            }
            if (doc_description.isNotBlank()) doc.ddescription = doc_description
            docRepository.save(doc)
            mapOf(
                "success" to true,
                "message" to "文档信息更新成功"
            )
        }.onFailure {
            if (it is NoSuchElementException) return mapOf(
                "success" to false,
                "message" to "文档不存在, 请检查后再试"
            )
            it.printStackTrace()
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "文档信息更新失败, 发生意外错误"
            )
        )
    }

    @PostMapping("/save")
    @Tag(name = "文档接口")
    @Operation(
        summary = "保存文档",
        description = "保存文档内容, 此操作会**刷新**文档和项目**最后编辑时间**和**最后浏览时间**"
    )
    fun save(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("doc_id") @Parameter(description = "要操作的文档Id") doc_id: Long?,
        @RequestParam("doc_content", defaultValue = "") @Parameter(description = "文档内容") doc_content: String
    ): Map<String, Any> {
        return runCatching {
            if (doc_id == null) return@runCatching mapOf(
                "success" to false,
                "message" to "文档Id不能为空"
            )
            val doc = docRepository.findById(doc_id).get()
            if (doc.deprecated) return mapOf(
                "success" to false,
                "message" to "文档已被删除, 请恢复后再进行此操作"
            )
            doc.dcontent = doc_content
            doc.lastedittime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
            docRepository.save(doc)
            val user = userRepository.findById(TokenUtils.verify(token).second).get()
            val record = user2DocRepository.findByUserAndDoc(user, doc) ?: User2Documentation(user, doc)
            record.lastviewedtime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
            user2DocRepository.save(record)

            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            projectService.updateProjectLastEditTime(doc.pid.toString(), time)

            mapOf(
                "success" to true,
                "message" to "文档保存成功"
            )
        }.onFailure {
            if (it is NoSuchElementException) return mapOf(
                "success" to false,
                "message" to "文档不存在, 请检查后再试"
            )
            it.printStackTrace()
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "文档保存失败, 发生意外错误"
            )
        )
    }

    @GetMapping("/getDocInfo")
    @Tag(name = "文档接口")
    @Operation(summary = "获取文档信息", description = "根据文档Id获取文档的所有信息, 此操作会**刷新最后浏览时间**")
    fun getDocContent(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("doc_id") @Parameter(description = "要操作的文档Id") doc_id: Long?
    ): Map<String, Any> {
        return runCatching {
            if (doc_id == null) return@runCatching mapOf(
                "success" to false,
                "message" to "文档Id不能为空"
            )
            val doc = docRepository.findById(doc_id).get()
            val user = userRepository.findById(TokenUtils.verify(token).second).get()
            val record = user2DocRepository.findByUserAndDoc(user, doc) ?: User2Documentation(user, doc)
            record.lastviewedtime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
            user2DocRepository.save(record)
            mapOf(
                "success" to true,
                "message" to "文档获取成功",
                "data" to listOf(let {
                    val projectId = doc.pid
                    val project = projectService.searchProjectByProjectId("$projectId")
                        ?: throw Exception("数据库中没有此项目, 请检查项目id是否正确")
                    val team = teamService.searchTeamByTeamId(project["team_id"].toString())
                        ?: throw Exception("数据库中没有此团队, 请检查团队id是否正确")
                    HashMap(doc.toDict()).apply {
                        this["is_favorite"] = doc in user.favoritedocuments
                        putAll(project)
                        putAll(team)
                    }
                })
            )
        }.onFailure {
            if (it is NoSuchElementException) return mapOf(
                "success" to false,
                "message" to "文档不存在, 请检查后再试"
            )
            it.printStackTrace()
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "文档获取失败, 发生意外错误"
            )
        )
    }

    @GetMapping("/getDocList")
    @Tag(name = "文档接口")
    @Operation(
        summary = "获取文档列表",
        description = "获取文档列表, 默认返回当前用户创建的所有文档列表, 若提供了项目Id, 则返回该项目下的所有文档列表, 此操作**不会刷新**文档最后浏览时间"
    )
    @Recycle
    fun getDocList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("project_id", defaultValue = "-1") @Parameter(description = "要查询的项目Id") project_id: Int
    ): Map<String, Any> {
        return runCatching {
            val user = userRepository.findById(TokenUtils.verify(token).second).get()
            val docList = if (project_id == -1) {
                docRepository.findByCreator(user)
                    .map { HashMap(it.toDict()).apply { this["is_favorite"] = it in user.favoritedocuments } }
            } else {
                docRepository.findByPid(project_id).map {
                    val projectId = it.pid
                    val project = projectService.searchProjectByProjectId("$projectId")
                        ?: throw Exception("数据库中没有此项目, 请检查项目id是否正确")
                    val team = teamService.searchTeamByTeamId(project["team_id"].toString())
                        ?: throw Exception("数据库中没有此团队, 请检查团队id是否正确")
                    HashMap(it.toDict()).apply {
                        this["is_favorite"] = it in user.favoritedocuments
                        putAll(project)
                        putAll(team)
                    }
                }
            }
            mapOf(
                "success" to true,
                "message" to "文档列表获取成功",
                "data" to docList
            )
        }.onFailure {
            if (it is NoSuchElementException) return mapOf(
                "success" to false,
                "message" to "用户不存在, 请检查数据库数据"
            )
            it.printStackTrace()
            return mapOf(
                "success" to false,
                "message" to (it.message ?: "文档列表获取失败, 发生意外错误")
            )
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "文档列表获取失败, 发生意外错误"
            )
        )
    }

    @GetMapping("searchDoc")
    @Tag(name = "文档接口")
    @Operation(summary = "搜索文档", description = "根据关键字搜索文档")
    @Recycle
    fun searchDoc(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("keyword") @Parameter(description = "关键字") keyword: String?
    ): Map<String, Any> {
        if (keyword.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "关键字不能为空"
        )
        val userId = TokenUtils.verify(token).second
        val teams = teamService.searchTeamByUserId("$userId") ?: listOf()
        val projects = mutableListOf<Map<String, String>>()
        teams.forEach {
            projects.addAll(projectService.searchProjectByTeamId("${it["team_id"]}") ?: listOf())
        }
        val pIds = projects.map { "${it["project_id"]}".toInt() }
        var docs = mutableListOf<Documentation>()
        pIds.forEach { docs.addAll(docRepository.findByPid(it)) }
        docs = docs.filter {
            keyword in it.dname!! || keyword in it.dcontent!! || keyword in it.ddescription!!
        }.toMutableList()
        return try {
            mapOf(
                "success" to true,
                "message" to "搜索成功",
                "data" to docs.map {
                    val projectId = it.pid
                    val project = projectService.searchProjectByProjectId("$projectId")
                        ?: throw Exception("数据库中没有此项目, 请检查项目id是否正确")
                    val team = teamService.searchTeamByTeamId(project["team_id"].toString())
                        ?: throw Exception("数据库中没有此团队, 请检查团队id是否正确")
                    HashMap(it.toDict()).apply {
                        this["is_favorite"] = it in userRepository.findById(userId).get().favoritedocuments
                        putAll(project)
                        putAll(team)
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

    @GetMapping("/getDocRecycleBin")
    @Tag(name = "文档接口")
    @Operation(summary = "获取文档回收站", description = "根据团队Id获取团队的文档回收站")
    fun getDocRecycleBin(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("team_id") @Parameter(description = "团队id") team_id: String
    ): Map<String, Any> {
        val userId = TokenUtils.verify(token).second
        val projects = projectService.searchProjectByTeamId(team_id) ?: listOf()
        val pIds = projects.map { "${it["project_id"]}".toInt() }
        val docs = mutableListOf<Documentation>()
        pIds.forEach { docs.addAll(docRepository.findByPidAndDeprecated(it, deprecated = true)) }
        return try {
            mapOf(
                "success" to true,
                "message" to "获取成功",
                "data" to docs.map {
                    val projectId = it.pid
                    val project = projectService.searchProjectByProjectId("$projectId")
                        ?: throw Exception("数据库中没有此项目, 请检查项目id是否正确")
                    val team = teamService.searchTeamByTeamId(team_id)
                        ?: throw Exception("数据库中没有此团队, 请检查团队id是否正确")
                    HashMap(it.toDict()).apply {
                        this["is_favorite"] = it in userRepository.findById(userId).get().favoritedocuments
                        putAll(project)
                        putAll(team)
                    }
                }
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "message" to (e.message ?: "获取失败, 发生意外错误")
            )
        }
    }
}