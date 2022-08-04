package com.ryouonritsu.inkbook_backend.config

import org.springframework.boot.web.servlet.MultipartConfigFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.unit.DataSize
import javax.servlet.MultipartConfigElement


/**
 *
 * @author WuKunchao
 */
@Configuration
class FileUploadConfig {
    @Bean
    fun multipartConfigElement(): MultipartConfigElement {
        val factory = MultipartConfigFactory()
        // 单个数据大小 KB,MB 大写
        factory.setMaxFileSize(DataSize.parse("10MB"))
        // 总上传数据大小
        factory.setMaxRequestSize(DataSize.parse("100MB"))
        return factory.createMultipartConfig()
    }
}