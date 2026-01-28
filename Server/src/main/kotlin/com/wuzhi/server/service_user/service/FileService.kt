package com.wuzhi.server.service_user.service

import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity

interface FileService {
    fun downloadFile(filename: String): ResponseEntity<Resource>
    fun deleteFile(filename: String): Boolean
}