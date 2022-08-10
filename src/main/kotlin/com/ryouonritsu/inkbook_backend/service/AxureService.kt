package com.ryouonritsu.inkbook_backend.service

import com.ryouonritsu.inkbook_backend.entity.Axure
import com.ryouonritsu.inkbook_backend.entity.AxureTemplate

/**
 *
 * @author WuKunchao
 */
interface AxureService {
    fun createNewAxure(axure: Axure)
    fun updateAxure(axure_id: String, title: String, items: String, config: String, last_edit: String)
    fun updateAxureInfo(axure_id: String, axure_name: String, axure_info: String)
    fun selectAxureByAxureId(axure_id: String): Axure?
    fun searchAxureAllByProjectId(axure_id: String): List<Axure>?
    fun searchAxureByProjectId(project_id: String): List<Map<String, String>>?
    fun deleteAxureByAxureId(user_id: String, axure_id: String)
    fun addRecentView(user_id: String, axure_id: String, time: String)
    fun getRecentViewList(user_id: String): List<Map<String, String>>?
    fun checkRecentView(user_id: String, axure_id: String): String?
    fun updateRecentView(user_id: String, axure_id: String, time: String)
    fun addFavoriteAxure(user_id: String, axure_id: String)
    fun checkFavoriteAxure(user_id: String, axure_id: String): Map<String, String>?
    fun deleteFavoriteAxure(user_id: String, axure_id: String)
    fun searchFavoriteAxure(user_id: String): List<Map<String, String>>?
    fun getAxureTemplateList(): List<Map<String, Any>>?
    fun getAxureTemplateByAxureId(axure_template_id: String): AxureTemplate?
    fun findByKeywordAndProjectId(keyword: String, project_id: String): List<Map<String, Any>>
    fun findByKeywordAndTeamId(keyword: String, team_id: String): List<Map<String, Any>>
}