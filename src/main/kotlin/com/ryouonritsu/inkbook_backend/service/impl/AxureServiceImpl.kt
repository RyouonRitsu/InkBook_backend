package com.ryouonritsu.inkbook_backend.service.impl

import com.ryouonritsu.inkbook_backend.dao.AxureDao
import com.ryouonritsu.inkbook_backend.entity.Axure
import com.ryouonritsu.inkbook_backend.service.AxureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 *
 * @author WuKunchao
 */
@Service
class AxureServiceImpl : AxureService {
    @Autowired
    lateinit var axureDao: AxureDao

    override fun createNewAxure(axure: Axure) = axureDao.createNewAxure(axure)

    override fun updateAxure(
        axure_id: String,
        title: String,
        items: String,
        config: String,
        last_edit: String
    ) = axureDao.updateAxure(axure_id, title, items, config, last_edit)

    override fun selectAxureByAxureId(axure_id: String) = axureDao.selectAxureByAxureId(axure_id)

    override fun searchAxureAllByProjectId(axure_id: String) = axureDao.searchAxureAllByProjectId(axure_id)

    override fun searchAxureByProjectId(project_id: String) = axureDao.searchAxureByProjectId(project_id)

    override fun deleteAxureByAxureId(user_id: String, axure_id: String) =
        axureDao.deleteAxureByAxureId(user_id, axure_id)

    override fun updateAxureInfo(
        axure_id: String,
        axure_name: String,
        axure_info: String
    ) = axureDao.updateAxureInfo(axure_id, axure_name, axure_info)

    override fun addRecentView(
        user_id: String,
        axure_id: String,
        time: String
    ) = axureDao.addRecentView(user_id, axure_id, time)

    override fun getRecentViewList(user_id: String) = axureDao.getRecentViewList(user_id)

    override fun checkRecentView(user_id: String, axure_id: String) = axureDao.checkRecentView(user_id, axure_id)

    override fun updateRecentView(
        user_id: String,
        axure_id: String,
        time: String
    ) = axureDao.updateRecentView(user_id, axure_id, time)

    override fun addFavoriteAxure(user_id: String, axure_id: String) = axureDao.addFavoriteAxure(user_id, axure_id)

    override fun checkFavoriteAxure(user_id: String, axure_id: String) = axureDao.checkFavoriteAxure(user_id, axure_id)

    override fun deleteFavoriteAxure(user_id: String, axure_id: String) =
        axureDao.deleteFavoriteAxure(user_id, axure_id)

    override fun searchFavoriteAxure(user_id: String) = axureDao.searchFavoriteAxure(user_id)

    override fun getAxureTemplateList() = axureDao.getAxureTemplateList()

    override fun getAxureTemplateByAxureId(axure_template_id: String) =
        axureDao.getAxureTemplateByAxureId(axure_template_id)
}