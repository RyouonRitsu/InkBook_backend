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
    var config_id: Long?,
    var last_edit: String?,
    var create_user: String?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var axure_id = 0

    constructor() : this("", "", "", "", "", "", 0, "", "")

    fun toDict() = mapOf(
        "axure_id" to axure_id,
        "axure_name" to axure_name,
        "axure_info" to axure_info,
        "project_id" to project_id,
        "title" to title,
        "items" to items,
        "config" to config,
        "config_id" to config_id,
        "last_edit" to last_edit,
        "create_user" to create_user
    )
}