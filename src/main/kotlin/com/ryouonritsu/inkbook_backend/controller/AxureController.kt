package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.entity.Axure
import com.ryouonritsu.inkbook_backend.service.AxureService
import com.ryouonritsu.inkbook_backend.service.UserService
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
import java.time.format.DateTimeFormatter

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

    @Autowired
    lateinit var userService: UserService

    @PostMapping("/create")
    @Tag(name = "原型接口")
    @Operation(
        summary = "创建新原型",
        description = "原型简介为可选项，同时会将发起请求的用户作为原型创建者，若有真名则展示真名，否则展示用户名。\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"创建原型成功！\"\n" +
                "}"
    )
    fun createNewAxure(
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
            val user_id = TokenUtils.verify(token).second
            val user = userService.get(user_id) ?: return mapOf(
                "success" to false,
                "message" to "用户不存在！"
            )
            val name = user.realname.let {
                if (it.isNullOrBlank()) {
                    user.username
                } else it
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val time = LocalDateTime.now().format(formatter)
            val axure = Axure(axure_name ?: "", axure_info ?: "", project_id, "", "", "", 0, time, name)
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
    @Operation(summary = "更新原型页面信息", description = "")
    fun updateAxureInfo(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("axure_id") @Parameter(description = "原型id") axure_id: String,
        @RequestParam("title", required = false) @Parameter(description = "页面信息中的title") title: String?,
        @RequestParam("config", required = false) @Parameter(description = "页面信息中的config") config: String?,
        @RequestParam("items", required = false) @Parameter(description = "页面信息中的items") items: String?,
    ): Map<String, Any> {
        return runCatching {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val time = LocalDateTime.now().format(formatter)
            axureService.updateAxure(axure_id, title ?: "", items ?: "", config ?: "", time)
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

    @PostMapping("/updateInfo")
    @Tag(name = "原型接口")
    @Operation(
        summary = "更新原型信息", description = "为空白符或者不传则会清空，若要保持不变则传回相同数据\n" +
                "不会更新最后编辑时间以及configID"
    )
    fun updateAxure(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("axure_id") @Parameter(description = "原型id") axure_id: String,
        @RequestParam("axure_name") @Parameter(description = "原型名字") axure_name: String?,
        @RequestParam("axure_info", required = false) @Parameter(description = "原型简介") axure_info: String?,
    ): Map<String, Any> {
        return runCatching {
            axureService.updateAxureInfo(axure_id, axure_name ?: "", axure_info ?: "")
            mapOf(
                "success" to true,
                "message" to "更新原型信息成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "更新原型信息失败！"
            )
        )
    }

    @PostMapping("/getAxureInfo")
    @Tag(name = "原型接口")
    @Operation(
        summary = "获得原型页面信息", description = "通过原型ID获取对应原型，并更新最后访问时间\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询原型页面信息成功！\",\n" +
                "    \"data\": {\n" +
                "        \"axure_id\": 7,\n" +
                "        \"axure_name\": \"新版本\",\n" +
                "        \"axure_info\": \"\",\n" +
                "        \"project_id\": \"3\",\n" +
                "        \"title\": \"123\",\n" +
                "        \"items\": \"123\",\n" +
                "        \"config\": \"123\",\n" +
                "        \"config_id\": 2,\n" +
                "        \"last_edit\": \"2022-08-05 02:10:41\",\n" +
                "        \"create_user\": \"2\"\n" +
                "    }\n" +
                "}"
    )
    fun getAxureInfo(
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
            val user_id = TokenUtils.verify(token).second
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val time = LocalDateTime.now().format(formatter)
            val isViewed = axureService.checkRecentView(user_id.toString(), axure_id)
            if (!isViewed.isNullOrBlank()) {
                axureService.updateRecentView(user_id.toString(), axure_id, time)
            } else {
                axureService.addRecentView(user_id.toString(), axure_id, time)
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
    @Operation(
        summary = "展示项目所有原型", description = "根据提供的项目ID查询该项目下所有原型并返回信息\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询项目原型列表成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"axure_info\": \"\",\n" +
                "            \"axure_id\": 2,\n" +
                "            \"project_id\": \"3\",\n" +
                "            \"config_id\": 0,\n" +
                "            \"axure_name\": \"222\",\n" +
                "            \"last_edit\": \" \",\n" +
                "            \"create_user\": \" \",\n" +
                "            \"title\": \"\",\n" +
                "            \"config\": \"\",\n" +
                "            \"items\": \"{\\\"referenceLine\\\":{\\\"row\\\":[],\\\"col\\\":[]},\\\"canvasSize\\\":{\\\"width\\\":338,\\\"height\\\":600}}\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"axure_info\": \"\",\n" +
                "            \"axure_id\": 7,\n" +
                "            \"project_id\": \"3\",\n" +
                "            \"config_id\": 2,\n" +
                "            \"axure_name\": \"新版本\",\n" +
                "            \"last_edit\": \"2022-08-05 02:10:41\",\n" +
                "            \"create_user\": \"2\",\n" +
                "            \"title\": \"123\",\n" +
                "            \"config\": \"123\",\n" +
                "            \"items\": \"123\"\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    )
    fun getAxureList(
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

    @PostMapping("/delete")
    @Tag(name = "原型接口")
    @Operation(
        summary = "删除原型", description = "删除给定原型id对应原型\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"删除原型成功！\"\n" +
                "}"
    )
    fun deleteAxure(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("axure_id") @Parameter(description = "原型id") axure_id: String,
    ): Map<String, Any> {
        return runCatching {
            axureService.deleteAxureByAxureId(axure_id)
            return mapOf(
                "success" to true,
                "message" to "删除原型成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "删除原型失败！"
            )
        )
    }

    @PostMapping("/getRecentViewList")
    @Tag(name = "原型接口")
    @Operation(
        summary = "获得最近访问原型", description = "{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查看最近访问原型成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"axure_info\": \"\",\n" +
                "            \"axure_name\": \"新版本\",\n" +
                "            \"last_edit\": \"2022-08-05 02:10:41\",\n" +
                "            \"team_id\": \"1\",\n" +
                "            \"title\": \"123\",\n" +
                "            \"project_name\": \"新名字\",\n" +
                "            \"team_name\": \"4\",\n" +
                "            \"project_info\": \"\",\n" +
                "            \"team_info\": \"4\",\n" +
                "            \"axure_id\": \"7\",\n" +
                "            \"user_id\": \"3\",\n" +
                "            \"project_id\": \"3\",\n" +
                "            \"config_id\": 2,\n" +
                "            \"time\": \"2022-08-05 10:28:04\",\n" +
                "            \"create_user\": \"2\",\n" +
                "            \"config\": \"123\",\n" +
                "            \"items\": \"123\"\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    )
    fun getRecentViewList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String
    ): Map<String, Any> {
        return runCatching {
            val user_id = TokenUtils.verify(token).second
            val info = axureService.getRecentViewList(user_id.toString())
            if (info.isNullOrEmpty()) return mapOf(
                "success" to false,
                "message" to "最近访问原型为空！"
            )
            return mapOf(
                "success" to true,
                "message" to "查看最近访问原型成功！",
                "data" to info
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "查看最近访问原型失败！"
            )
        )
    }
}