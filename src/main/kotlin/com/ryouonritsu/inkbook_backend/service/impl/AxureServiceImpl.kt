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

    override fun searchAxureByProjectId(project_id: String) = axureDao.searchAxureByProjectId(project_id)

    override fun deleteAxureByAxureId(axure_id: String) = axureDao.deleteAxureByAxureId(axure_id)

    override fun updateAxureInfo(
        axure_id: String,
        axure_name: String,
        axure_info: String
    ) = axureDao.updateAxureInfo(axure_id, axure_name, axure_info)
}