package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Axure
import com.ryouonritsu.inkbook_backend.entity.Project
import com.ryouonritsu.inkbook_backend.service.AxureService
import com.ryouonritsu.inkbook_backend.service.ProjectService
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
@RequestMapping("/axure")
@Tag(name = "原型接口")
class AxureController {
    @Autowired
    lateinit var axureService: AxureService

    @PostMapping("/create")
    @Tag(name = "原型接口")
    @Operation(summary = "创建新原型", description = "原型简介为可选项，\n{\n" +
            "    \"success\": true,\n" +
            "    \"message\": \"创建原型成功！\"\n" +
            "}")
    fun createNewAxure (
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("axure_name") @Parameter(description = "原型名字") axure_name: String?,
        @RequestParam("axure_info", required = false) @Parameter(description = "原型简介") axure_info: String?,
        @RequestParam("project_id") @Parameter(description = "所在项目id") project_id: String?,
    ): Map<String, Any> {
        if (project_id.isNullOrBlank()) return mapOf(
            "success" to false,
            "message" to "项目id为空！"
        )
        return runCatching {
            var axure = Axure(axure_name ?: "", axure_info ?: "" , project_id, "", "", "")
            axureService.createNewAxure(axure)
            mapOf(
                "success" to true,
                "message" to "创建原型成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "创建原型失败！"
            )
        )
    }

    @PostMapping("/update")
    @Tag(name = "原型接口")
    @Operation(summary = "更新原型页面信息", description = "在原型页面中点击保存时，将config中的信息上传于此\n" +
            "并根据提供的axure_id更新对应的原型\n" +
            "{\n" +
            "    \"success\": true,\n" +
            "    \"message\": \"更新原型页面信息成功！\"\n" +
            "}")
    fun updateAxure (
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("axure_id") @Parameter(description = "原型id") axure_id: String,
        @RequestParam("title") @Parameter(description = "页面信息中的title") title: String?,
        @RequestParam("config") @Parameter(description = "页面信息中的config") config: String?,
        @RequestParam("items") @Parameter(description = "页面信息中的items") items: String?,
    ): Map<String, Any> {
        return runCatching {
            axureService.updateAxure(axure_id, title ?: "", config ?: "", items ?: "")
            mapOf(
                "success" to true,
                "message" to "更新原型页面信息成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "更新原型页面信息失败！"
            )
        )
    }

    @PostMapping("/getAxureInfo")
    @Tag(name = "原型接口")
    @Operation(summary = "获得原型页面信息", description = "通过原型ID获取对应原型\n{\n" +
            "    \"success\": true,\n" +
            "    \"message\": \"查询原型页面信息成功！\",\n" +
            "    \"data\": {\n" +
            "        \"axure_id\": 1,\n" +
            "        \"axure_name\": \"111\",\n" +
            "        \"axure_info\": \"\",\n" +
            "        \"project_id\": \"3\",\n" +
            "        \"title\": \"title\",\n" +
            "        \"items\": \"config\",\n" +
            "        \"config\": \"items\"\n" +
            "    }\n" +
            "}")
    fun getAxureInfo (
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("axure_id") @Parameter(description = "原型id") axure_id: String,
    ): Map<String, Any> {
        return runCatching {
            val axure = axureService.selectAxureByAxureId(axure_id)
            if (axure == null) {
                return mapOf(
                    "success" to false,
                    "message" to "该原型ID对应原型不存在！"
                )
            }
            mapOf(
                "success" to true,
                "message" to "查询原型页面信息成功！",
                "data" to axure.toDict()
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "查询原型页面信息失败！"
            )
        )
    }

    @PostMapping("/getAxureList")
    @Tag(name = "原型接口")
    @Operation(summary = "展示项目所有原型", description = "根据提供的项目ID查询该项目下所有原型并返回信息\n{\n" +
            "    \"success\": true,\n" +
            "    \"message\": \"查询项目原型列表成功！\",\n" +
            "    \"data\": [\n" +
            "        {\n" +
            "            \"axure_info\": \"\",\n" +
            "            \"axure_id\": 1,\n" +
            "            \"project_id\": \"3\",\n" +
            "            \"axure_name\": \"111\",\n" +
            "            \"title\": \"title\",\n" +
            "            \"config\": \"items\",\n" +
            "            \"items\": \"config\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"axure_info\": \"\",\n" +
            "            \"axure_id\": 2,\n" +
            "            \"project_id\": \"3\",\n" +
            "            \"axure_name\": \"222\",\n" +
            "            \"title\": \"title2\",\n" +
            "            \"config\": \"items2\",\n" +
            "            \"items\": \"config2\"\n" +
            "        }\n" +
            "    ]\n" +
            "}")
    fun getAxureList (
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("project_id") @Parameter(description = "项目id") project_id: String,
    ): Map<String, Any> {
        return runCatching {
            val axureList = axureService.searchAxureByProjectId(project_id)
            if (axureList.isNullOrEmpty()) {
                return mapOf(
                    "success" to false,
                    "message" to "项目原型为空！"
                )
            }
            return mapOf(
                "success" to true,
                "message" to "查询项目原型列表成功！",
                "data" to axureList
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "查询项目原型列表失败！"
            )
        )
    }
}