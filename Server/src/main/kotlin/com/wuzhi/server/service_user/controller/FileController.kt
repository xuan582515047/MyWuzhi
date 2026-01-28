package com.wuzhi.server.service_user.controller

import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_user.service.FileService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService
) {

    @GetMapping("/{filename:.+}")
    fun downloadFile(@PathVariable filename: String) = fileService.downloadFile(filename)
    
    @DeleteMapping("/{filename:.+}")
    fun deleteFile(@PathVariable filename: String): ResponseResult {
        return try {
            val success = fileService.deleteFile(filename)
            if (success) {
                ResponseResult.success("文件删除成功")
            } else {
                ResponseResult.fail("文件删除失败或文件不存在")
            }
        } catch (e: Exception) {
            ResponseResult.fail("删除文件时发生错误: ${e.message}")
        }
    }
}