package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.*

@Entity
class DocumentationTemplate(
    var name: String,
    @Column(columnDefinition = "LONGTEXT") var description: String = "",
    @Column(columnDefinition = "LONGTEXT") var content: String = "",
    @Column(columnDefinition = "TEXT") var cover: String? = null,
    @Column(columnDefinition = "TEXT") var preview: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id = -1L

    constructor() : this("")

    fun toDict() = mapOf(
        "doc_template_id" to id,
        "doc_template_name" to name,
        "doc_template_description" to description,
        "doc_template_content" to content,
        "doc_template_cover" to cover,
        "doc_template_preview" to preview
    )
}