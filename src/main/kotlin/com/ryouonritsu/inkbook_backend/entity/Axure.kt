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
class Axure (
    var axure_name: String,
    var axure_info: String?,
    var project_id: String,
    var title: String?,
    var items: String?,
    var config: String?,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var axure_id = 0

    constructor() : this("", "", "", "", "", "")
}