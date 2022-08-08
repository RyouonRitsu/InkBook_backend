package com.ryouonritsu.inkbook_backend.entity

import javax.persistence.*

/**
 *
 * @author WuKunchao
 */
@Entity
class UserFile(
    @Column(columnDefinition = "TEXT") var url: String,
    @Column(columnDefinition = "TEXT") var filePath: String = "",
    @Column(columnDefinition = "TEXT") var fileName: String = "",
    var creatorId: Long = -1L
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var fileId = 0L

    constructor() : this("")
}