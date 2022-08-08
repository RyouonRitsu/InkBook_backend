package com.ryouonritsu.inkbook_backend.entity

import java.time.LocalDateTime
import java.time.ZoneId
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
    var updateTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
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
}