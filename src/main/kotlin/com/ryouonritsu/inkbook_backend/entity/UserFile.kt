package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 *
 * @author WuKunchao
 */
@Entity
class UserFile(var url: String) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var fileId = 0L

    constructor() : this("")
}