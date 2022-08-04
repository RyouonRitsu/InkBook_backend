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
        config: String
    ) = axureDao.updateAxure(axure_id, title, items, config)

    override fun selectAxureByAxureId(axure_id: String) = axureDao.selectAxureByAxureId(axure_id)
}