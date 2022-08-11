package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.annotation.Recycle
import com.ryouonritsu.inkbook_backend.entity.*
import com.ryouonritsu.inkbook_backend.repository.*
import com.ryouonritsu.inkbook_backend.service.ProjectService
import com.ryouonritsu.inkbook_backend.service.TeamService
import com.ryouonritsu.inkbook_backend.utils.RedisUtils
import com.ryouonritsu.inkbook_backend.utils.TokenUtils
import com.spire.doc.Document
import com.spire.doc.FileFormat
import com.spire.doc.documents.ImageType
import com.spire.doc.documents.XHTMLValidationType
import io.github.furstenheim.CopyDown
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

@RestController
@RequestMapping("/doc")
@Tag(name = "文档接口")
class DocumentationController {
    val logger: Logger = LoggerFactory.getLogger(DocumentationController::class.java)

    @Autowired
    lateinit var docRepository: DocumentationRepository

    @Autowired
    lateinit var docDictRepository: DocumentationDictRepository

    @Autowired
    lateinit var docTemplateRepository: DocumentationTemplateRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var user2DocRepository: User2DocumentationRepository

    @Autowired
    lateinit var userFileRepository: UserFileRepository

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @Autowired
    lateinit var projectService: ProjectService

    @Autowired
    lateinit var teamRepository: TeamRepository

    @Autowired
    lateinit var teamService: TeamService

    @Autowired
    lateinit var redisUtils: RedisUtils

    @Autowired
    lateinit var userController: UserController

    fun check(doc_name: String?, project_id: Int, team_id: Int): Pair<Boolean, Map<String, Any>?> {
        if (doc_name.isNullOrBlank()) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "文档名称不能为空"
            )
        )
        if (project_id == -1 && team_id == -1) return Pair(
            false, mapOf(
                "success" to false,
                "message" to "项目Id和团队Id至少其一不能为空"
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
    @Operation(
        summary = "新建文档",
        description = "文档描述和文档内容不是必要的, 项目Id可以不提供, 若项目Id不提供则表示该文档是团队文档, 故必须提供团队Id;\n" +
                "目标文档目录Id如果不填写, 默认存放在项目的文档目录下\n" +
                "可以使用模版创建, 填写模板对应Id即可, 如果留空则默认使用空白模版"
    )
    fun newDoc(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("doc_name") @Parameter(description = "文档名称") doc_name: String?,
        @RequestParam(
            "doc_description",
            defaultValue = ""
        ) @Parameter(description = "文档描述") doc_description: String,
        @RequestParam("doc_content", defaultValue = "") @Parameter(description = "文档内容") doc_content: String,
        @RequestParam(
            "project_id",
            defaultValue = "-1"
        ) @Parameter(description = "项目Id, 项目文档必填此项") project_id: Int,
        @RequestParam("team_id", defaultValue = "-1") @Parameter(description = "团队Id, 团队文档必填此项") team_id: Int,
        @RequestParam(
            "dest_folder_id",
            defaultValue = "-1"
        ) @Parameter(description = "目标文档目录Id") dest_folder_id: Long,
        @RequestParam(
            "template_id",
            defaultValue = "-1"
        ) @Parameter(description = "使用的模板Id") template_id: Long
    ): Map<String, Any> {
        val (result, message) = check(doc_name, project_id, team_id)
        if (!result && message != null) return message
        return runCatching {
            val creatorId = TokenUtils.verify(token).second
            val creator = userRepository.findById(creatorId).get()
            val project = if (project_id != -1) projectRepository.findById(project_id).get() else null
            val team = if (team_id == -1 && project != null) teamRepository.findById(project.team_id.toInt()).get()
            else if (team_id != -1) teamRepository.findById(team_id).get()
            else throw Exception("缺少必填参数, 无法创建文档, 请检查后重试")
            var doc = if (template_id == -1L) {
                docRepository.save(Documentation(doc_name!!, doc_description, doc_content, project, team, creator))
            } else {
                val template = try {
                    docTemplateRepository.findById(template_id).get()
                } catch (e: NoSuchElementException) {
                    return mapOf(
                        "success" to false,
                        "message" to "模板不存在"
                    )
                }
                docRepository.save(
                    Documentation(
                        docTemplate = template,
                        name = doc_name,
                        description = doc_description.ifBlank { null },
                        project = project,
                        team = team,
                        creator = creator
                    )
                )
            }
            val dest = if (dest_folder_id != -1L) docDictRepository.findById(dest_folder_id).get()
            else docDictRepository.findById(
                project?.prjDictId ?: throw Exception("无法找到project, 故无法放置文档到项目文件夹")
            ).get()
            dest.documents.add(doc)
            doc.dict = dest
            docDictRepository.save(dest)
            doc = docRepository.save(doc)
            mapOf(
                "success" to true,
                "message" to "文档创建成功",
                "data" to listOf(doc.toDict())
            )
        }.onFailure {
            if (it is NoSuchElementException) {
                return mapOf(
                    "success" to false,
                    "message" to "数据不存在, 可能是数据库出错, 请检查后重试"
                )
            }
            it.printStackTrace()
            return mapOf(
                "success" to false,
                "message" to (it.message ?: "文档创建失败, 发生意外错误")
            )
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
            // if (doc.deprecated) return mapOf(
            //     "success" to false,
            //     "message" to "文档已被删除"
            // )
            if (recycle) {
                doc.deprecated = true
                docRepository.save(doc)
            } else {
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

            val time =
                LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            if (doc.project != null && doc.project?.project_id != 0) {
                projectService.updateProjectLastEditTime(doc.project!!.project_id.toString(), time)
            }

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
                    HashMap(doc.toDict()).apply {
                        this["is_favorite"] = doc in user.favoritedocuments
                        putAll(doc.project?.toDict() ?: mapOf("parent_dict_id" to doc.dict?.id))
                        putAll(doc.team?.toDict() ?: throw Exception("数据库中没有此团队, 请检查团队id是否正确"))
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
                docRepository.findByCreatorOrderByLastedittimeDesc(user)
                    .map { HashMap(it.toDict()).apply { this["is_favorite"] = it in user.favoritedocuments } }
            } else {
                docRepository.findByPid(project_id).map {
                    HashMap(it.toDict()).apply {
                        this["is_favorite"] = it in user.favoritedocuments
                        putAll(it.project?.toDict() ?: mapOf("parent_dict_id" to it.dict?.id))
                        putAll(it.team?.toDict() ?: throw Exception("数据库中没有此团队, 请检查团队id是否正确"))
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
    @Operation(summary = "搜索文档", description = "根据关键字搜索文档, 可选择搜索指定团队或项目下的文档")
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
        val docs = mutableListOf<Documentation>()
        if (project_id != -1) docs.addAll(docRepository.findByKeywordAndProjectId(keyword, project_id))
        else if (team_id != -1) docs.addAll(docRepository.findByKeywordAndTeamId(keyword, team_id))
        else {
            val teams = teamService.searchTeamByUserId("$userId") ?: listOf()
            val tIds = teams.map { "${it["team_id"]}".toInt() }
            tIds.forEach { docs.addAll(docRepository.findByKeywordAndTeamId(keyword, it)) }
        }
        return try {
            mapOf(
                "success" to true,
                "message" to "搜索成功",
                "data" to docs.map {
                    HashMap(it.toDict()).apply {
                        this["is_favorite"] = it in userRepository.findById(userId).get().favoritedocuments
                        putAll(it.project?.toDict() ?: mapOf("parent_dict_id" to it.dict?.id))
                        putAll(it.team?.toDict() ?: throw Exception("数据库中没有此团队, 请检查团队id是否正确"))
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
                    HashMap(it.toDict()).apply {
                        this["is_favorite"] = it in userRepository.findById(userId).get().favoritedocuments
                        putAll(it.project?.toDict() ?: mapOf("parent_dict_id" to it.dict?.id))
                        putAll(it.team?.toDict() ?: throw Exception("数据库中没有此团队, 请检查团队id是否正确"))
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

    @PostMapping("/mkdir")
    @Tag(name = "文档接口")
    @Operation(summary = "创建文档目录", description = "创建文档目录在指定dest_folder_id下")
    fun mkdir(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("dest_folder_id") @Parameter(description = "目标文档目录Id") dest_folder_id: Long?,
        @RequestParam("dict_name") @Parameter(description = "文档目录名称") dictName: String?,
        @RequestParam(
            "dict_description",
            defaultValue = ""
        ) @Parameter(description = "文档目录描述") dictDescription: String
    ): Map<String, Any> {
        if (dest_folder_id == null) {
            return mapOf(
                "success" to false,
                "message" to "目标文档目录Id不能为空"
            )
        }
        if (dictName.isNullOrBlank()) {
            return mapOf(
                "success" to false,
                "message" to "文档目录名称不能为空"
            )
        }
        val dest = try {
            docDictRepository.findById(dest_folder_id).get()
        } catch (e: NoSuchElementException) {
            return mapOf(
                "success" to false,
                "message" to "目标文档目录不存在"
            )
        }
        if (docDictRepository.findByNameAndTid(dictName, dest.tid).isEmpty()) return try {
            var dir = docDictRepository.save(
                DocumentationDict(
                    name = dictName,
                    description = dictDescription,
                    tid = dest.tid
                )
            )
            dest.children.add(dir)
            dest.hasChildren = true
            dir.parent = dest
            docDictRepository.save(dest)
            dir = docDictRepository.save(dir)
            mapOf(
                "success" to true,
                "message" to "文档目录创建成功",
                "data" to listOf(mapOf("dict_id" to "${dir.id}"))
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf(
                "success" to false,
                "message" to (e.message ?: "文档目录创建失败, 发生意外错误")
            )
        }
        return mapOf(
            "success" to false,
            "message" to "同名目录已存在"
        )
    }

    fun del(dir: DocumentationDict) {
        dir.documents.forEach { docRepository.delete(it) }
        dir.children.forEach {
            del(it)
            docDictRepository.delete(it)
        }
        docDictRepository.delete(dir)
    }

    @PostMapping("/delDir")
    @Tag(name = "文档接口")
    @Operation(summary = "删除文档目录", description = "删除指定Id的文档目录及其内容")
    fun delDir(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("folder_id") @Parameter(description = "文档目录Id") folder_id: Long?
    ): Map<String, Any> {
        if (folder_id == null) {
            return mapOf(
                "success" to false,
                "message" to "文档目录Id不能为空"
            )
        }
        val dir = try {
            docDictRepository.findById(folder_id).get()
        } catch (e: NoSuchElementException) {
            return mapOf(
                "success" to false,
                "message" to "文档目录不存在"
            )
        }
        return try {
            del(dir)
            mapOf(
                "success" to true,
                "message" to "文档目录删除成功"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf(
                "success" to false,
                "message" to (e.message ?: "文档目录删除失败, 发生意外错误")
            )
        }
    }

    fun walkDir(dir: DocumentationDict): Map<String, Any?> {
        if (dir.deprecated) return mapOf()
        val result = HashMap(dir.toDict()).apply {
            if (dir.hasChildren || dir.children.isNotEmpty() || dir.documents.isNotEmpty()) {
                val children = dir.children.map { walkDir(it) }.filterNot { it.isEmpty() }.toMutableList()
                children.addAll(dir.documentsList())
                if (children.isNotEmpty()) this["children"] = children
                if (!dir.hasChildren) {
                    this["dir_hasChildren"] = true
                    dir.hasChildren = true
                    docDictRepository.save(dir)
                }
            }
        }
        return result
    }

    @GetMapping("/walkDir")
    @Tag(name = "文档接口")
    @Operation(summary = "遍历文档目录", description = "遍历指定的文档目录")
    fun traverseDir(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("folder_id") @Parameter(description = "文档目录Id") folder_id: Long?
    ): Map<String, Any> {
        if (folder_id == null) {
            return mapOf(
                "success" to false,
                "message" to "文档目录Id不能为空"
            )
        }
        return try {
            val dir = docDictRepository.findById(folder_id).get()
            mapOf(
                "success" to true,
                "message" to "文档目录遍历成功",
                "data" to listOf(walkDir(dir))
            )
        } catch (e: NoSuchElementException) {
            mapOf(
                "success" to false,
                "message" to "文档目录不存在"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf(
                "success" to false,
                "message" to (e.message ?: "文档目录遍历失败, 发生意外错误")
            )
        }
    }

    @PostMapping("/editTemplate")
    @Tag(name = "文档接口")
    @Operation(
        summary = "编辑文档模板",
        description = "编辑文档模板, 如不提供模版Id, 则为新建模版操作, 各种封面和预览文件url可以通过用户的上传文件接口获得;\n" +
                "如果提供模板Id, 则对对应模板进行更新操作, 留空参数表示此参数不做修改"
    )
    fun editTemplate(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("template_id", defaultValue = "-1") @Parameter(description = "模版Id") template_id: Long,
        @RequestParam("name") @Parameter(description = "文档模板名称") name: String?,
        @RequestParam("description", defaultValue = "") @Parameter(description = "文档模板描述") description: String,
        @RequestParam("content", defaultValue = "") @Parameter(description = "文档模板内容") content: String,
        @RequestParam("cover", defaultValue = "") @Parameter(description = "模版封面url") cover: String,
        @RequestParam("preview", defaultValue = "") @Parameter(description = "模版预览url") preview: String
    ): Map<String, Any> {
        if (name.isNullOrBlank()) {
            return mapOf(
                "success" to false,
                "message" to "文档模板名称不能为空"
            )
        }
        return try {
            val template = if (template_id == -1L) docTemplateRepository.save(
                DocumentationTemplate(
                    name = name,
                    description = description,
                    content = content,
                    cover = cover.ifBlank { null },
                    preview = preview.ifBlank { null }
                )
            )
            else {
                val t = docTemplateRepository.findById(template_id).get().apply {
                    if (name.isNotBlank()) this.name = name
                    if (description.isNotBlank()) this.description = description
                    if (content.isNotBlank()) this.content = content
                    if (cover.isNotBlank()) this.cover = cover
                    if (preview.isNotBlank()) this.preview = preview
                }
                docTemplateRepository.save(t)
            }
            mapOf(
                "success" to true,
                "message" to "文档模板编辑成功",
                "data" to listOf(template.toDict())
            )
        } catch (e: NoSuchElementException) {
            mapOf(
                "success" to false,
                "message" to "文档模板不存在"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf(
                "success" to false,
                "message" to (e.message ?: "文档模板编辑失败, 发生意外错误")
            )
        }
    }

    @PostMapping("/deleteTemplate")
    @Tag(name = "文档接口")
    @Operation(summary = "删除文档模板", description = "删除指定Id的文档模板")
    fun deleteTemplate(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("template_id") @Parameter(description = "文档模板Id") template_id: Long
    ): Map<String, Any> {
        return try {
            docTemplateRepository.findById(template_id).get()
            docTemplateRepository.deleteById(template_id)
            mapOf(
                "success" to true,
                "message" to "文档模板删除成功"
            )
        } catch (e: NoSuchElementException) {
            mapOf(
                "success" to false,
                "message" to "文档模板不存在"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf(
                "success" to false,
                "message" to (e.message ?: "文档模板删除失败, 发生意外错误")
            )
        }
    }

    @GetMapping("/getTemplateList")
    @Tag(name = "文档接口")
    @Operation(summary = "获取文档模板列表")
    fun getTemplateList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String
    ): Map<String, Any> {
        return try {
            mapOf(
                "success" to true,
                "message" to "文档模板列表获取成功",
                "data" to docTemplateRepository.findAll().map { it.toDict() }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf(
                "success" to false,
                "message" to (e.message ?: "文档模板列表获取失败, 发生意外错误")
            )
        }
    }

    @PostMapping("/htmlToMarkdown")
    @Tag(name = "文档接口")
    @Operation(summary = "html2md", description = "将html转换为markdown")
    fun htmlToMarkdown(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("url") @Parameter(description = "上传到后端后返回的url") url: String?
    ): Map<String, Any> {
        if (url.isNullOrBlank()) {
            return mapOf(
                "success" to false,
                "message" to "url不能为空"
            )
        }
        return try {
            val f = userFileRepository.findByUrl(url) ?: return mapOf(
                "success" to false,
                "message" to "文件不存在"
            )
//            val optionsBuilder = OptionsBuilder.anOptions()
//            val options = optionsBuilder.withBr("").build()
            val converter = CopyDown()
            val markdown = converter.convert(File(f.filePath).readText())
            val filePath = f.filePath.replace(".html", ".md")
            val fileName = f.fileName.replace(".html", ".md")
            val file = File(filePath)
            val userId = TokenUtils.verify(token).second
            file.writeText(markdown)
            val fileUrl = "http://101.42.171.88:8090/file/doc/${fileName}"
            userFileRepository.findByUrl(fileUrl) ?: userFileRepository.save(
                UserFile(
                    fileUrl,
                    filePath,
                    fileName,
                    userId
                )
            )
            mapOf(
                "success" to true,
                "message" to "html转换为markdown成功",
                "data" to listOf(mapOf("url" to fileUrl))
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf(
                "success" to false,
                "message" to (e.message ?: "文件转换失败, 发生意外错误")
            )
        }
    }

    @PostMapping("/htmlToDocx")
    @Tag(name = "文档接口")
    @Operation(summary = "html2docx", description = "将html转换为docx")
    fun htmlToDocx(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("url") @Parameter(description = "上传到后端后返回的url") url: String?
    ): Map<String, Any> {
        if (url.isNullOrBlank()) {
            return mapOf(
                "success" to false,
                "message" to "url不能为空"
            )
        }
        return try {
            val f = userFileRepository.findByUrl(url) ?: return mapOf(
                "success" to false,
                "message" to "文件不存在"
            )
            val doc = Document()
            doc.loadFromFile(f.filePath, FileFormat.Html, XHTMLValidationType.None)
            val filePath = f.filePath.replace(".html", ".docx")
            val fileName = f.fileName.replace(".html", ".docx")
            val userId = TokenUtils.verify(token).second
            doc.saveToFile(filePath, FileFormat.Docx_2013)
            val fileUrl = "http://101.42.171.88:8090/file/doc/${fileName}"
            userFileRepository.findByUrl(fileUrl) ?: userFileRepository.save(
                UserFile(
                    fileUrl,
                    filePath,
                    fileName,
                    userId
                )
            )
            mapOf(
                "success" to true,
                "message" to "html转换为docx成功",
                "data" to listOf(mapOf("url" to fileUrl))
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf(
                "success" to false,
                "message" to (e.message ?: "文件转换失败, 发生意外错误")
            )
        }
    }

    @PostMapping("/htmlToPNG")
    @Tag(name = "文档接口")
    @Operation(summary = "html2png", description = "将html转换为png")
    fun htmlToPNG(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("url") @Parameter(description = "上传到后端后返回的url") url: String?
    ): Map<String, Any> {
        if (url.isNullOrBlank()) {
            return mapOf(
                "success" to false,
                "message" to "url不能为空"
            )
        }
        return try {
            val f = userFileRepository.findByUrl(url) ?: return mapOf(
                "success" to false,
                "message" to "文件不存在"
            )
            val doc = Document()
            doc.loadFromFile(f.filePath, FileFormat.Html, XHTMLValidationType.None)
            val filePath = f.filePath.replace(".html", "")
            val fileName = f.fileName.replace(".html", "")
            val userId = TokenUtils.verify(token).second
            val prefix = "http://101.42.171.88:8090/file/doc/"
            var index = 0
            val fileUrls = mutableListOf<String>()
            doc.saveToImages(ImageType.Bitmap).forEach {
                val path = "$filePath${index}.png"
                val name = "$fileName${index++}.png"
                val u = "$prefix$name"
                ImageIO.write(it, "png", File(path))
                userFileRepository.findByUrl(u) ?: userFileRepository.save(UserFile(u, path, name, userId))
                fileUrls.add(u)
            }
            mapOf(
                "success" to true,
                "message" to "html转换为png成功",
                "data" to listOf(mapOf("url" to fileUrls))
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf(
                "success" to false,
                "message" to (e.message ?: "文件转换失败, 发生意外错误")
            )
        }
    }

    @PostMapping("/uploadDoc")
    @Tag(name = "文档接口")
    @Operation(
        summary = "开启文件预览",
        description = "上传html代码，生成html文件，并将代码嵌入body中，并返回url"
    )
    fun uploadDoc(
        @RequestParam("token") @Parameter(description = "用户认证令牌") token: String,
        @RequestParam("html_code") @Parameter(description = "文档HTML代码") html_code: String,
        @RequestParam("doc_id") @Parameter(description = "文档id") doc_id: Long,
    ): Map<String, Any> {
        return runCatching {
            val userId = TokenUtils.verify(token).second
            val fileDir = "static/file/doc/"
            val fileName = "doc_${doc_id}.html"
            val filePath = "$fileDir/$fileName"
            if (!File(fileDir).exists()) File(fileDir).mkdirs()
            val myFile = File(filePath)
            val head =
                "<!DOCTYPE html><html lang=\"zh-CH\"><head><meta charset=\"UTF-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Document</title></head><body>"
            val tail = "</body></html>"
            myFile.writeText(head + html_code + tail)
            val fileUrl = "http://101.42.171.88:8090/file/doc/${fileName}"
            val doc = try {
                docRepository.findById(doc_id).get()
            } catch (e: NoSuchElementException) {
                return mapOf(
                    "success" to false,
                    "message" to "文档不存在"
                )
            }
            doc.shared = true
            doc.sharedUrl = fileUrl
            docRepository.save(doc)
            userFileRepository.findByUrl(fileUrl) ?: userFileRepository.save(
                UserFile(
                    fileUrl,
                    filePath,
                    fileName,
                    userId
                )
            )
            mapOf(
                "success" to true,
                "message" to "开启预览成功！",
                "data" to listOf(
                    mapOf(
                        "url" to fileUrl
                    )
                )
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "开启预览失败！"
            )
        )
    }

    @PostMapping("/disableSharing")
    @Tag(name = "文档接口")
    @Operation(summary = "文档共享失效", description = "禁用指定文档的共享")
    fun disableSharing(
        @RequestParam("token") @Parameter(description = "用户认证令牌") token: String,
        @RequestParam("doc_id") @Parameter(description = "文档id") doc_id: Long
    ): Map<String, Any> {
        return try {
            val doc = docRepository.findById(doc_id).get()
            if (!doc.shared) return mapOf(
                "success" to false,
                "message" to "文档未共享"
            )
            val result = userController.deleteFile(token, doc.sharedUrl)
            if (result["success"] as Boolean) {
                doc.shared = false
                doc.sharedUrl = ""
                docRepository.save(doc)
                mapOf(
                    "success" to true,
                    "message" to "此文档的共享链接已失效"
                )
            } else {
                mapOf(
                    "success" to false,
                    "message" to "操作失败, ${result["message"]}"
                )
            }
        } catch (e: NoSuchElementException) {
            mapOf(
                "success" to false,
                "message" to "文档不存在"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf(
                "success" to false,
                "message" to "操作失败, 发生意外错误"
            )
        }
    }
}