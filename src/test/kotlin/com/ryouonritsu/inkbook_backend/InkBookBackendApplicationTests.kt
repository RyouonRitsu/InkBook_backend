package com.ryouonritsu.inkbook_backend

import com.ryouonritsu.inkbook_backend.entity.Documentation
import com.ryouonritsu.inkbook_backend.entity.User
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

    @Test
    fun contextLoads() {
        user2DocRepository.deleteAllById(listOf(6L, 7L))
    }

}
