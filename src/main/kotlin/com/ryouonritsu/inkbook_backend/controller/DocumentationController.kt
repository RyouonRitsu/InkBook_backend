package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.service.DocumentationService
import com.ryouonritsu.inkbook_backend.utils.RedisUtils
import com.ryouonritsu.inkbook_backend.utils.TokenUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/doc")
@Tag(name = "文档接口")
class DocumentationController {
    val logger: Logger = LoggerFactory.getLogger(DocumentationController::class.java)

    @Autowired
    lateinit var docService: DocumentationService

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
                "message" to "项目id不能为空"
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
        @RequestParam("doc_description", defaultValue = "") @Parameter(description = "文档描述") doc_description: String,
        @RequestParam("doc_content", defaultValue = "") @Parameter(description = "文档内容") doc_content: String,
        @RequestParam("project_id") @Parameter(description = "项目id") project_id: Int?
    ): Map<String, Any> {
        val (result, message) = check(doc_name, project_id)
        if (!result && message != null) return message
        return runCatching {
            val creator_id = TokenUtils.verify(token).second
            val doc = Documentation(doc_name!!, doc_description, doc_content, creator_id, project_id!!)
            docService + doc
            mapOf(
                "success" to true,
                "message" to "文档创建成功"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "文档创建失败, 发生意外错误"
            )
        )
    }
}