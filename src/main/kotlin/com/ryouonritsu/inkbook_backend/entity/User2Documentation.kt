package com.ryouonritsu.inkbook_backend.entity

import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.*

@Entity
class User2Documentation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(
        targetEntity = User::class,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH]
    )
    @JoinColumn(name = "uid")
    var user: User? = null

    @ManyToOne(
        targetEntity = Documentation::class,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH]
    )
    @JoinColumn(name = "did")
    var doc: Documentation? = null

    var lastviewedtime = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))

    constructor(user: User, doc: Documentation) {
        this.user = user
        this.doc = doc
    }

    constructor()
}