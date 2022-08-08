package com.ryouonritsu.inkbook_backend.entity

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.persistence.*

@Entity
class DocumentationDict(
    var name: String,
    var description: String = ""
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1L
    val createTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
    var updateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai")) /*此属性暂未设置更新, 仅保留用于方便后续添加功能*/
    var deprecated = false
    var hasChildren = false

    @ManyToOne(
        targetEntity = DocumentationDict::class,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH]
    )
    @JoinColumn(name = "parent_id")
    var parent: DocumentationDict? = null

    @OneToMany(
        targetEntity = DocumentationDict::class,
        mappedBy = "parent",
        cascade = [CascadeType.ALL],
        fetch = FetchType.EAGER
    )
    var children = mutableListOf<DocumentationDict>()

    @OneToMany(targetEntity = Documentation::class, mappedBy = "dict", cascade = [CascadeType.ALL])
    var documents = mutableListOf<Documentation>()

    constructor() : this(name = "")

    fun toDict() = mapOf(
        "dir_id" to id,
        "dir_name" to name,
        "dir_description" to description,
        "dir_createTime" to createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        "dir_updateTime" to updateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        "dir_deprecated" to deprecated,
        "dir_hasChildren" to hasChildren,
        "dir_parent_id" to parent?.id,
        "dir_documents" to documents.filterNot { it.deprecated }.map { it.toDict() }
    )
}