package com.ryouonritsu.inkbook_backend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.Executors


@Configuration
class WebConfiguration : WebMvcConfigurer {
    @Autowired
    lateinit var tokenInterceptor: TokenInterceptor

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedHeaders("*")
            .allowedMethods("*")
            .allowedOriginPatterns("*")
            .allowCredentials(true)
    }

    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        configurer.setTaskExecutor(ConcurrentTaskExecutor(Executors.newFixedThreadPool(3)))
        configurer.setDefaultTimeout(30000)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        val excludePath = listOf(
            "/user/**",
            "/user/login",
            "/user/register",
            "/user/sendRegistrationVerificationCode",
            "/user/selectUserByUserId",
            "/user/sendForgotPasswordEmail",
            "/user/changePassword",
            "/doc.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs",
            "/webjars/**",
            "/change_email",
            "/forgot_password",
            "/registration_verification"
        )
        registry.addInterceptor(tokenInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(excludePath)
        super.addInterceptors(registry)
    }
}