package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Axure
import com.ryouonritsu.inkbook_backend.entity.UML
import com.ryouonritsu.inkbook_backend.service.ProjectService
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
                    "success" to false,
                    "message" to "该原型ID对应UML不存在！"
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
}