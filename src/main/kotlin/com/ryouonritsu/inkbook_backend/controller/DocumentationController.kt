package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.repository.DocumentationRepository
import com.ryouonritsu.inkbook_backend.repository.UserRepository
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
    @Operation(summary = "删除文档", description = "使用文档Id删除文档")
    fun deleteDoc(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("doc_id") @Parameter(description = "文档Id") doc_id: Long?
    ): Map<String, Any> {
        return runCatching {
            if (doc_id == null) return@runCatching mapOf(
                "success" to false,
                "message" to "文档Id不能为空"
            )
            docRepository.deleteById(doc_id)
            mapOf(
                "success" to true,
                "message" to "文档删除成功"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "文档删除失败, 发生意外错误"
            )
        )
    }

    @PostMapping("/updateDocInfo")
    @Tag(name = "文档接口")
    @Operation(
        summary = "更新文档信息",
        description = "更新文档信息, 留空表示不更新此参数对应的信息, 此操作不会刷新文档的最后编辑时间"
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
    @Operation(summary = "保存文档", description = "保存文档内容, 此操作会刷新文档最后编辑时间和最后浏览时间")
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
            doc.dcontent = doc_content
            doc.lastedittime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
            docRepository.save(doc)
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
    @Operation(summary = "获取文档信息", description = "根据文档Id获取文档的所有信息, 此操作会刷新最后浏览时间")
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
            val creator = doc.creator
            mapOf(
                "success" to true,
                "message" to "文档获取成功",
                "data" to HashMap(doc.toDict()).apply { this["creator_name"] = creator?.username }
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
        description = "获取文档列表, 默认返回当前用户创建的所有文档列表, 若提供了项目Id, 则返回该项目下的所有文档列表, 此操作不会刷新文档最后浏览时间"
    )
    fun getDocList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("project_id", defaultValue = "-1") @Parameter(description = "要查询的项目Id") project_id: Int
    ): Map<String, Any> {
        return runCatching {
            val creator = userRepository.findById(TokenUtils.verify(token).second).get()
            val docList = if (project_id == -1) {
                docRepository.findByCreator(creator)
                    .map { HashMap(it.toDict()).apply { this["creator_name"] = creator.username } }
            } else {
                docRepository.findByPid(project_id).map {
                    HashMap(it.toDict()).apply {
                        this["creator_name"] =
                            userRepository.findById(this["creator_id"] as Long).get().username ?: "数据库出错, 查无此人"
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
        }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "文档列表获取失败, 发生意外错误"
            )
        )
    }
}