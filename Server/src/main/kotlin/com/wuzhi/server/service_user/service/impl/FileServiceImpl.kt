package com.wuzhi.server.service_user.service.impl

import com.wuzhi.server.common.config.LocalFileConfig
import com.wuzhi.server.common.util.FileUtil
import com.wuzhi.server.service_user.service.FileService
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.io.File
import java.net.URLEncoder

@Service
class FileServiceImpl : FileService {

    override fun downloadFile(filename: String): ResponseEntity<Resource> {
        return try {
            val fileUrl = "${LocalFileConfig.FILE_ACCESS_URL_PREFIX}/$filename"
            val localPath = FileUtil.getLocalPathFromUrl(fileUrl)
            val file = localPath.toFile()
            
            if (!file.exists()) {
                return ResponseEntity.notFound().build()
            }
            
            val resource = FileSystemResource(file)
            val contentType = "application/octet-stream"
            
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${URLEncoder.encode(file.name, "UTF-8")}\"")
                .body(resource)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
    
    override fun deleteFile(filename: String): Boolean {
        return try {
            val fileUrl = "${LocalFileConfig.FILE_ACCESS_URL_PREFIX}/$filename"
            FileUtil.deleteFile(fileUrl)
        } catch (e: Exception) {
            false
        }
    }
}