package com.wuzhi.server.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

/**
 * 本地文件配置
 * 
 * 配置静态资源映射，支持本地文件访问。
 */
@Configuration
class LocalFileConfig : WebMvcConfigurer {
    
    companion object {
        /** 项目运行目录 */
        val PROJECT_DIR: String = System.getProperty("user.dir")
        
        /** 临时文件目录 (项目运行目录/temp) */
        val TEMP_DIR: String = PROJECT_DIR + File.separator + "temp"
        
        /** 上传文件存储目录 (项目运行目录/files) */
        val UPLOAD_DIR: String = PROJECT_DIR + File.separator + "files"
        
        /** 静态资源处理器模式（Spring资源匹配模式） */
        const val RESOURCE_HANDLER_PATTERN = "/files/**"
        
        /** 本地文件访问URL前缀 */
        const val FILE_ACCESS_URL_PREFIX = "/files"
    }
    
    /**
     * 配置静态资源处理器
     * 
     * 将上传的文件目录映射为静态资源，支持通过HTTP访问。
     * 
     * @param registry 资源处理器注册表
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // 使用File.toURI()生成跨平台的文件URI路径
        val location = File(UPLOAD_DIR, "").toURI().toString()
        
        registry.addResourceHandler(RESOURCE_HANDLER_PATTERN)
                .addResourceLocations(location)
                .setCachePeriod(60 * 60 * 24 * 30 * 12) // 缓存12个月
    }
}