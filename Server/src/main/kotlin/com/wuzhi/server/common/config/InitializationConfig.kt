package com.wuzhi.server.common.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.nio.file.Files

@Component
class InitializationConfig : ApplicationListener<ApplicationReadyEvent> {
    
    private val logger = LoggerFactory.getLogger(InitializationConfig::class.java)
    
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        logger.info("项目运行目录: {}", LocalFileConfig.PROJECT_DIR)
        
        try {
            // 1. 创建 temp 临时文件目录
            createDirectory(LocalFileConfig.TEMP_DIR, "临时文件目录")
            
            // 2. 创建 files 上传文件存储目录
            createDirectory(LocalFileConfig.UPLOAD_DIR, "上传文件存储目录")
            
            logger.info("文件系统初始化完成")
        } catch (e: Exception) {
            logger.error("文件系统初始化失败: {}", e.message, e)
        }
    }
    
    private fun createDirectory(dirPath: String, dirDescription: String) {
        val path = java.nio.file.Paths.get(dirPath)
        
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path)
                logger.info("创建{}成功: {}", dirDescription, path.toAbsolutePath())
            } else {
                logger.info("{}已存在: {}", dirDescription, path.toAbsolutePath())
            }
            
            val dir = path.toFile()
            if (!dir.canWrite()) {
                logger.error("{}没有写入权限: {}", dirDescription, path.toAbsolutePath())
            } else if (!dir.canRead()) {
                logger.error("{}没有读取权限: {}", dirDescription, path.toAbsolutePath())
            } else {
                logger.info("{}权限正常: {}", dirDescription, path.toAbsolutePath())
            }
        } catch (e: Exception) {
            logger.error("创建{}失败: {} - {}", dirDescription, dirPath, e.message, e)
            throw e
        }
    }
}