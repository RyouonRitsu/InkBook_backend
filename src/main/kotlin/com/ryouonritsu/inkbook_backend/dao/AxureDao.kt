package com.ryouonritsu.inkbook_backend.dao

import com.ryouonritsu.inkbook_backend.entity.Axure
import org.apache.ibatis.annotations.Mapper

/**
 *
 * @author WuKunchao
 */
@Mapper
interface AxureDao {
    fun createNewAxure(axure: Axure)
    fun updateAxure(axure_id: String, title: String, items: String, config: String)
    fun selectAxureByAxureId(axure_id: String): Axure?
    fun searchAxureByProjectId(project_id: String): List<Map<String, String>>?
    fun deleteAxureByAxureId(axure_id: String)
}