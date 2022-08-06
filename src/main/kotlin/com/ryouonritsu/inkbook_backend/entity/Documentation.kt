package com.ryouonritsu.inkbook_backend.entity

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.persistence.*

@Entity
class Documentation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var did: Long? = null
    var dname: String? = null

    @Column(columnDefinition = "LONGTEXT")
    var ddescription: String? = null

    @Column(columnDefinition = "LONGTEXT")
    var dcontent: String? = null
    var lastedittime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
    var pid: Int? = null

    @OneToOne(targetEntity = User::class, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator")
    var creator: User? = null

    @OneToMany(targetEntity = User2Documentation::class, mappedBy = "doc")
    var user2documentations = mutableListOf<User2Documentation>()
    var deprecated = false

    constructor(
        doc_name: String,
        doc_description: String?,
        doc_content: String?,
        project_id: Int,
        creator: User
    ) {
        this.dname = doc_name
        this.ddescription = doc_description
        this.dcontent = doc_content
        this.pid = project_id
        this.creator = creator
    }

    constructor()

    fun toDict(): Map<String, Any?> = mapOf(
        "doc_id" to "$did",
        "doc_name" to dname,
        "doc_description" to ddescription,
        "doc_content" to dcontent,
        "last_edit_time" to lastedittime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        "creator_id" to "${creator?.uid}",
        "creator_name" to creator?.username,
        "project_id" to "$pid",
        "deprecated" to deprecated
    )
}