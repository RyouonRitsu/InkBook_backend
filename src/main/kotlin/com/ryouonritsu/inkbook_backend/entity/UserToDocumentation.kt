package com.ryouonritsu.inkbook_backend.entity

import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.*

@Entity
class UserToDocumentation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var u_to_d_id: Long? = null

    @OneToOne(targetEntity = User::class)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @OneToOne(targetEntity = Documentation::class)
    @JoinColumn(name = "doc_id")
    var doc: Documentation? = null
    var last_viewed_time = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
}