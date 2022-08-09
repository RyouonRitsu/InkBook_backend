package com.ryouonritsu.inkbook_backend.repository

import com.ryouonritsu.inkbook_backend.entity.DocumentationTemplate
import org.springframework.data.jpa.repository.JpaRepository

interface DocumentationTemplateRepository : JpaRepository<DocumentationTemplate, Long> {
}