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
    val createTime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
    var lastedittime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))

    @OneToOne(targetEntity = Project::class)
    var project: Project? = null

    @OneToOne(targetEntity = Team::class)
    var team: Team? = null

    @OneToOne(targetEntity = User::class)
    @JoinColumn(name = "creator")
    var creator: User? = null

    @OneToMany(targetEntity = User2Documentation::class, mappedBy = "doc", cascade = [CascadeType.ALL])
    var user2documentations = mutableListOf<User2Documentation>()
    var deprecated = false

    @ManyToOne(
        targetEntity = DocumentationDict::class,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH]
    )
    var dict: DocumentationDict? = null
    var shared = false
    @Column(columnDefinition = "TEXT") var sharedUrl = ""

    constructor(
        doc_name: String,
        doc_description: String?,
        doc_content: String?,
        project: Project?,
        team: Team?,
        creator: User
    ) {
        this.dname = doc_name
        this.ddescription = doc_description
        this.dcontent = doc_content
        this.project = project
        this.team = team
        this.creator = creator
    }

    constructor(doc: Documentation, project: Project? = null, creator: User? = null) {
        this.dname = "${doc.dname}_拷贝"
        this.ddescription = doc.ddescription
        this.dcontent = doc.dcontent
        this.project = project ?: doc.project
        this.team = doc.team
        this.creator = creator ?: doc.creator
    }

    constructor(
        docTemplate: DocumentationTemplate,
        name: String? = null,
        description: String? = null,
        project: Project?,
        team: Team,
        creator: User
    ) {
        this.dname = name ?: docTemplate.name
        this.ddescription = description ?: docTemplate.description
        this.dcontent = docTemplate.content
        this.project = project
        this.team = team
        this.creator = creator
    }

    constructor()

    fun toDict(): Map<String, Any?> = mapOf(
        "type" to "documentation",
        "dir_id" to "$did",
        "doc_id" to "$did",
        "dir_name" to dname,
        "doc_name" to dname,
        "doc_description" to ddescription,
        "doc_content" to dcontent,
        "dir_createTime" to createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        "create_time" to createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        "last_edit_time" to lastedittime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        "creator_id" to "${creator?.uid}",
        "creator_name" to creator?.username,
        "project_id" to "${project?.project_id}",
        "project_name" to project?.project_name,
        "team_id" to "${team?.teamId}",
        "team_name" to team?.teamName,
        "deprecated" to deprecated,
        "dict_id" to "${dict?.id}",
        "shared" to shared,
        "shared_url" to sharedUrl
    )
}