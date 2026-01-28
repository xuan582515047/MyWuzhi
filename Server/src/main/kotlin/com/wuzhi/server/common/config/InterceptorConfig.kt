package com.wuzhi.server.common.config

import com.wuzhi.server.common.interceptor.TokenInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport

@Configuration
class InterceptorConfig(
    private val tokenInterceptor: TokenInterceptor
) : WebMvcConfigurationSupport() {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(tokenInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/auth/register/**")
            .excludePathPatterns("/auth/login/**")
            .excludePathPatterns("/auth/refresh/**")
            .excludePathPatterns("/files/**")
    }

}
