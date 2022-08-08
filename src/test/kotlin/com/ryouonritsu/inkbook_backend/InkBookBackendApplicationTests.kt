package com.ryouonritsu.inkbook_backend

import com.ryouonritsu.inkbook_backend.entity.DocumentationDict
import com.ryouonritsu.inkbook_backend.repository.DocumentationDictRepository
import com.ryouonritsu.inkbook_backend.repository.DocumentationRepository
import com.ryouonritsu.inkbook_backend.repository.User2DocumentationRepository
import com.ryouonritsu.inkbook_backend.repository.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class InkBookBackendApplicationTests {
    @Autowired
    lateinit var docRepository: DocumentationRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var user2DocRepository: User2DocumentationRepository

    @Autowired
    lateinit var docDictRepository: DocumentationDictRepository

    @Test
    fun test1() {
        var d1 = docDictRepository.save(DocumentationDict(name = "d1"))
        var d2 = docDictRepository.save(DocumentationDict(name = "d2"))
        println(d1.id)
        val id1 = d1.id
        println(d2.id)
        val id2 = d2.id
        d1.children.add(d2)
        d1.hasChildren = true
        d2.parent = d1
        docDictRepository.save(d1)
        docDictRepository.save(d2)
        d2 = docDictRepository.findById(id2).get()
        d2.description = "test"
        docDictRepository.save(d2)
        d1 = docDictRepository.findById(id1).get()
        println(d1.children.first().description)
    }

    @Test
    fun test2() {
        var d1 = docDictRepository.findById(25L).get()
        println(d1.children.map { it.id })
        val d2 = docDictRepository.findById(26L).get()
//        d1.children.remove(d2)
//        d1 = docDictRepository.save(d1)
        docDictRepository.delete(d2)
        d1 = docDictRepository.findById(25L).get()
        println(d1.children.map { it.id })
    }

}
