package com.ryouonritsu.inkbook_backend.controller

import com.ryouonritsu.inkbook_backend.annotation.Recycle
import com.ryouonritsu.inkbook_backend.entity.Axure
import com.ryouonritsu.inkbook_backend.service.AxureService
import com.ryouonritsu.inkbook_backend.service.ProjectService
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
@RequestMapping("/axure")
@Tag(name = "原型接口")
class AxureController {
    @Autowired
    lateinit var axureService: AxureService

    @Autowired
    lateinit var projectService: ProjectService

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
            val time = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(formatter)
            val axure = Axure(axure_name ?: "", axure_info ?: "", project_id.toInt(), "", "", "", 0, time, name)
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
    @Operation(summary = "更新原型页面信息", description = "保存原型中的页面信息，并更新原型和项目最后编辑时间")
    fun updateAxureInfo(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("axure_id") @Parameter(description = "原型id") axure_id: String,
        @RequestParam("title", required = false) @Parameter(description = "页面信息中的title") title: String?,
        @RequestParam("config", required = false) @Parameter(description = "页面信息中的config") config: String?,
        @RequestParam("items", required = false) @Parameter(description = "页面信息中的items") items: String?,
    ): Map<String, Any> {
        return runCatching {
            val axure = axureService.selectAxureByAxureId(axure_id)
                ?: return mapOf(
                    "success" to false,
                    "message" to "对应原型不存在！"
                )
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val time = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(formatter)
            axureService.updateAxure(axure_id, title ?: "", items ?: "", config ?: "", time)
            projectService.updateProjectLastEditTime(axure.project_id.toString(), time)
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
                "    \"data\": [\n" +
                "{\n" +
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
                "]\n" +
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
            val time = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(formatter)
            val isViewed = axureService.checkRecentView(user_id.toString(), axure_id)
            if (!isViewed.isNullOrBlank()) {
                axureService.updateRecentView(user_id.toString(), axure_id, time)
            } else {
                axureService.addRecentView(user_id.toString(), axure_id, time)
            }
            mapOf(
                "success" to true,
                "message" to "查询原型页面信息成功！",
                "data" to listOf(axure.toDict())
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
        summary = "展示项目所有原型",
        description = "根据提供的项目ID查询该项目下所有原型并返回信息，isFavorite为0表示未收藏，为1表示收藏\n{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询项目原型列表成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"axure_info\": \"\",\n" +
                "            \"axure_id\": 22,\n" +
                "            \"project_id\": \"51\",\n" +
                "            \"axure_name\": \"favortest\",\n" +
                "            \"last_edit\": \"2022-08-05 12:25:14\",\n" +
                "            \"create_user\": \"wkc\",\n" +
                "            \"isFavorite\": \"0\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"axure_info\": \"\",\n" +
                "            \"axure_id\": 23,\n" +
                "            \"project_id\": \"51\",\n" +
                "            \"axure_name\": \"favortest2\",\n" +
                "            \"last_edit\": \"2022-08-05 12:26:52\",\n" +
                "            \"create_user\": \"wkc\",\n" +
                "            \"isFavorite\": \"1\"\n" +
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
            val user_id = TokenUtils.verify(token).second
            val newAxureList = mutableListOf<Map<String, String>>()
            for (axure: Map<String, String> in axureList) {
                val a = axure.toMutableMap()
                if (axureService.checkFavoriteAxure(user_id.toString(), axure["axure_id"].toString()) != null) {
                    a["isFavorite"] = "1"
                } else {
                    a["isFavorite"] = "0"
                }
                newAxureList.add(a)
            }
            return mapOf(
                "success" to true,
                "message" to "查询项目原型列表成功！",
                "data" to newAxureList
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "查询项目原型列表失败！"
            )
        )
    }

    @PostMapping("/getAxureTemplateInfo")
    @Tag(name = "原型接口")
    @Operation(summary = "获得原型模板页面信息", description = "{\n" +
            "    \"success\": true,\n" +
            "    \"message\": \"查询原型模板列表成功！\",\n" +
            "    \"data\": [\n" +
            "        {\n" +
            "            \"axure_template_cover\": \"测试样例\",\n" +
            "            \"axure_name\": \"测试样例\",\n" +
            "            \"axure_info\": \"测试样例\",\n" +
            "            \"title\": \"测试样例\",\n" +
            "            \"items\": \"测试样例\",\n" +
            "            \"config\": \"测试样例\",\n" +
            "            \"config_id\": 0,\n" +
            "            \"axure_template_id\": 1\n" +
            "        }\n" +
            "    ]\n" +
            "}")
    fun getAxureTemplateInfo(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("axure_template_id") @Parameter(description = "原型模板id") axure_template_id: String
    ): Map<String, Any> {
        return runCatching {
            val axureTemplate = axureService.getAxureTemplateByAxureId(axure_template_id)
            return mapOf(
                "success" to true,
                "message" to "查询原型模板列表成功！",
                "data" to listOf(axureTemplate)
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "查询原型模板列表失败！"
            )
        )
    }

    @PostMapping("/getAxureTemplateList")
    @Tag(name = "原型接口")
    @Operation(summary = "展示所有原型模板", description = "{\n" +
            "    \"success\": true,\n" +
            "    \"message\": \"查询原型模板列表成功！\",\n" +
            "    \"data\": [\n" +
            "        {\n" +
            "            \"axure_info\": \"测试样例\",\n" +
            "            \"axure_template_id\": 1,\n" +
            "            \"axure_name\": \"测试样例\",\n" +
            "            \"axure_template_cover\": \"测试样例\"\n" +
            "        }\n" +
            "    ]\n" +
            "}")
    fun getAxureTemplateList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String
    ): Map<String, Any> {
        return runCatching {
            val axureList = axureService.getAxureTemplateList() ?: arrayListOf()
            return mapOf(
                "success" to true,
                "message" to "查询原型模板列表成功！",
                "data" to axureList
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "查询原型模板列表失败！"
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
            val user_id = TokenUtils.verify(token).second
            axureService.deleteAxureByAxureId(user_id.toString(), axure_id)
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
        summary = "获得最近访问原型", description = "不返回原型页面信息\n" +
                "{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查看最近访问原型成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"axure_info\": \"copyTest\",\n" +
                "            \"axure_name\": \"copyTest\",\n" +
                "            \"prj_last_edit_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"deprecated\": false 项目是否被弃置,\n" +
                "            \"last_edit\": \"2022-08-08 13:43:00\",\n" +
                "            \"team_id\": 103,\n" +
                "            \"project_name\": \"copyTest\",\n" +
                "            \"team_name\": \"123\",\n" +
                "            \"project_info\": \"copyTest\",\n" +
                "            \"team_info\": \"123\",\n" +
                "            \"axure_id\": 44,\n" +
                "            \"user_id\": \"3\",\n" +
                "            \"project_id\": 89,\n" +
                "            \"prj_create_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"time\": \"2022-08-08 14:35:44\" 最近访问时间,\n" +
                "            \"create_user\": \"wkc\" 原型创建者,\n" +
                "            \"axure_deprecated\": false 原型是否被弃置\n" +
                "        },\n" +
                "        {\n" +
                "            \"axure_info\": \"\",\n" +
                "            \"axure_name\": \"123\",\n" +
                "            \"prj_last_edit_time\": \"2022-08-08 11:08:48\",\n" +
                "            \"deprecated\": false,\n" +
                "            \"last_edit\": \"2022-08-08 09:29:09\",\n" +
                "            \"team_id\": 103,\n" +
                "            \"project_name\": \"123\",\n" +
                "            \"team_name\": \"123\",\n" +
                "            \"project_info\": \"\",\n" +
                "            \"team_info\": \"123\",\n" +
                "            \"axure_id\": 43,\n" +
                "            \"user_id\": \"3\",\n" +
                "            \"project_id\": 87,\n" +
                "            \"prj_create_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"time\": \"2022-08-08 09:28:38\",\n" +
                "            \"create_user\": \"wkc\",\n" +
                "            \"axure_deprecated\": false\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    )
    @Recycle
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

    @PostMapping("/addFavoriteAxure")
    @Tag(name = "原型接口")
    @Operation(
        summary = "收藏原型", description = ""
    )
    fun addFavoriteAxure(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("axure_id") @Parameter(description = "原型id") axure_id: String,
    ): Map<String, Any> {
        return runCatching {
            val user_id = TokenUtils.verify(token).second
            axureService.addFavoriteAxure(user_id.toString(), axure_id)
            return mapOf(
                "success" to true,
                "message" to "收藏原型成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "收藏原型失败！"
            )
        )
    }

    @PostMapping("/deleteFavoriteAxure")
    @Tag(name = "原型接口")
    @Operation(
        summary = "取消收藏原型", description = ""
    )
    fun deleteFavoriteAxure(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String,
        @RequestParam("axure_id") @Parameter(description = "原型id") axure_id: String,
    ): Map<String, Any> {
        return runCatching {
            val user_id = TokenUtils.verify(token).second
            axureService.deleteFavoriteAxure(user_id.toString(), axure_id)
            return mapOf(
                "success" to true,
                "message" to "取消收藏原型成功！"
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "取消收藏原型失败！"
            )
        )
    }

    @GetMapping("/getFavoriteAxureList")
    @Tag(name = "原型接口")
    @Operation(
        summary = "展示当前用户所有收藏原型", description = "" +
                "{\n" +
                "    \"success\": true,\n" +
                "    \"message\": \"查询收藏原型成功！\",\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"axure_info\": \"copyTest\",\n" +
                "            \"axure_name\": \"copyTest\",\n" +
                "            \"prj_last_edit_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"deprecated\": false,\n" +
                "            \"last_edit\": \"2022-08-08 13:43:00\",\n" +
                "            \"team_id\": 103,\n" +
                "            \"project_name\": \"copyTest\",\n" +
                "            \"team_name\": \"123\",\n" +
                "            \"project_info\": \"copyTest\",\n" +
                "            \"team_info\": \"123\",\n" +
                "            \"axure_id\": 44,\n" +
                "            \"user_id\": \"3\",\n" +
                "            \"project_id\": 89,\n" +
                "            \"prj_create_time\": \"2022-08-08 13:43:00\",\n" +
                "            \"create_user\": \"wkc\",\n" +
                "            \"axure_deprecated\": false\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    )
    @Recycle
    fun getFavoriteAxureList(
        @RequestParam("token") @Parameter(description = "用户登陆后获取的token令牌") token: String
    ): Map<String, Any> {
        return runCatching {
            val user_id = TokenUtils.verify(token).second
            val axureList = axureService.searchFavoriteAxure(user_id.toString())
            if (axureList.isNullOrEmpty()) {
                return mapOf(
                    "success" to false,
                    "message" to "收藏原型为空！"
                )
            }
            return mapOf(
                "success" to true,
                "message" to "查询收藏原型成功！",
                "data" to axureList
            )
        }.onFailure { it.printStackTrace() }.getOrDefault(
            mapOf(
                "success" to false,
                "message" to "查询收藏原型失败！"
            )
        )
    }
}