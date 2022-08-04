package com.ryouonritsu.inkbook_backend

import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.entity.User
import com.ryouonritsu.inkbook_backend.repository.DocumentationRepository
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

    @Test
    fun contextLoads() {
        val user = User("1", "2", "3", "4", "5")
        userRepository.save(user)
        val doc = Documentation("9", "8", "7", 5, user)
        docRepository.save(doc)
        docRepository.findByCreator(user).forEach { println(it) }
        docRepository.findByPid(5).forEach { println(it) }
        docRepository.findById(doc.did!!).ifPresent {
            println(it.creator?.username)
        }
    }

}
