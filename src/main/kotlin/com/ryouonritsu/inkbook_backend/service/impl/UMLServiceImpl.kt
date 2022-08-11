package com.ryouonritsu.inkbook_backend.service.impl

import com.ryouonritsu.inkbook_backend.dao.UMLDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 *
 * @author WuKunchao
 */
@Service
class UMLServiceImpl {
    @Autowired
    lateinit var umlDao: UMLDao
}