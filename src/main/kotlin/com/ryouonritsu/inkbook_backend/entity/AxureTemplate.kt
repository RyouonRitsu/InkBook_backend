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
class AxureTemplate (
    var axure_template_cover: String,
    var axure_template_preview: String,
    var axure_name: String,
    var axure_info: String?,
    var title: String?,
    var items: String?,
    var config: String?,
    var config_id: Long?,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var axure_template_id = 0

    constructor() : this("", "", "", "", "", "", "", 0)

    fun toDict() = mapOf(
        "axure_template_id" to axure_template_id,
        "axure_template_cover" to axure_template_cover,
        "axure_template_preview" to axure_template_preview,
        "axure_name" to axure_name,
        "axure_info" to axure_info,
        "title" to title,
        "items" to items,
        "config" to config,
        "config_id" to config_id
    )
}