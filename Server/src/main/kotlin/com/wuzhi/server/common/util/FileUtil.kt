package com.wuzhi.server.common.util

import com.wuzhi.server.common.config.LocalFileConfig
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.copyTo

/**
 * 文件工具类
 * 用于处理文件的上传和保存，返回文件的访问URL
 */
object FileUtil {
    /** 本地文件存储目录 */
    private val UPLOAD_DIR = LocalFileConfig.UPLOAD_DIR

    /** 本地文件访问URL前缀 */
    private val FILE_ACCESS_URL_PREFIX = LocalFileConfig.FILE_ACCESS_URL_PREFIX

    /** 允许的最大文件大小（10MB） */
    private const val MAX_FILE_SIZE = 10 * 1024 * 1024L

    /** 允许的文件扩展名 */
    private val ALLOWED_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "gif", "bmp", // 图片
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", // 文档
        "txt", "md", "json", "xml", "csv", // 文本
        "mp3", "wav", "mp4", "avi", "mov" // 音视频
    )

    /**
     * 保存MultipartFile类型的文件
     * @param file 上传的文件
     * @return 文件的访问URL
     * @throws IOException 文件操作异常
     * @throws IllegalArgumentException 文件大小或格式不合法
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    fun saveFile(file: MultipartFile): String {
        // 验证文件
        validateFile(file.originalFilename, file.size)

        // 生成唯一文件名
        val originalFilename = file.originalFilename ?: "unnamed_file"
        val fileExtension = getFileExtension(originalFilename)
        val uniqueFilename = "${UUID.randomUUID()}${if (fileExtension.isNotEmpty()) ".$fileExtension" else ""}"

        // 创建目标目录（如果不存在）
        val uploadDirPath = Paths.get(UPLOAD_DIR)
        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath)
        }

        // 保存文件
        val targetPath = uploadDirPath.resolve(uniqueFilename)
        file.transferTo(targetPath.toFile())

        // 返回文件的访问URL
        return "$FILE_ACCESS_URL_PREFIX/$uniqueFilename"
    }

    /**
     * 保存Resource类型的文件
     * @param resource Spring Resource对象
     * @param originalFilename 原始文件名（可选）
     * @return 文件的访问URL
     * @throws IOException 文件操作异常
     * @throws IllegalArgumentException 文件大小或格式不合法
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    fun saveFile(resource: Resource, originalFilename: String? = null): String {
        // 获取文件大小（如果可能）
        val fileSize = try {
            resource.contentLength()
        } catch (e: Exception) {
            -1L
        }

        // 确定文件名
        val filename = originalFilename ?: resource.filename ?: "unnamed_file"

        // 验证文件
        validateFile(filename, fileSize)

        // 生成唯一文件名
        val fileExtension = getFileExtension(filename)
        val uniqueFilename = "${UUID.randomUUID()}${if (fileExtension.isNotEmpty()) ".$fileExtension" else ""}"

        // 创建目标目录（如果不存在）
        val uploadDirPath = Paths.get(UPLOAD_DIR)
        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath)
        }

        // 保存文件
        val targetPath = uploadDirPath.resolve(uniqueFilename)
        resource.inputStream.use { input ->
            Files.copy(input, targetPath)
        }

        // 返回文件的访问URL
        return "$FILE_ACCESS_URL_PREFIX/$uniqueFilename"
    }

    /**
     * 保存File类型的文件
     * @param file 本地文件对象
     * @return 文件的访问URL
     * @throws IOException 文件操作异常
     * @throws IllegalArgumentException 文件大小或格式不合法
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    fun saveFile(file: File): String {
        // 验证文件
        validateFile(file.name, file.length())

        // 生成唯一文件名
        val fileExtension = getFileExtension(file.name)
        val uniqueFilename = "${UUID.randomUUID()}${if (fileExtension.isNotEmpty()) ".$fileExtension" else ""}"

        // 创建目标目录（如果不存在）
        val uploadDirPath = Paths.get(UPLOAD_DIR)
        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath)
        }

        // 保存文件
        val targetPath = uploadDirPath.resolve(uniqueFilename)
        file.toPath().copyTo(targetPath, overwrite = true)

        // 返回文件的访问URL
        return "$FILE_ACCESS_URL_PREFIX/$uniqueFilename"
    }

    /**
     * 根据URL获取文件的本地路径
     * @param fileUrl 文件的访问URL
     * @return 本地文件路径
     * @throws IllegalArgumentException 如果URL无效
     * @throws SecurityException 如果路径非法（路径遍历攻击检测）
     */
    fun getLocalPathFromUrl(fileUrl: String): Path {
        val fileName = fileUrl.substringAfter(FILE_ACCESS_URL_PREFIX + "/")
        if (fileName.isBlank()) throw IllegalArgumentException("无效的文件URL: $fileUrl")
        
        val targetPath = Paths.get(UPLOAD_DIR, fileName).normalize()
        val uploadDirPath = Paths.get(UPLOAD_DIR).normalize()
        
        if (!targetPath.startsWith(uploadDirPath)) throw SecurityException("非法文件路径: $fileUrl")
        
        return targetPath
    }

    /**
     * 根据URL获取File对象
     * @param fileUrl 文件的访问URL
     * @return File对象
     */
    fun getFileFromUrl(fileUrl: String): File {
        return getLocalPathFromUrl(fileUrl).toFile()
    }

    /**
     * 删除文件
     * @param fileUrl 文件的访问URL
     * @return 是否删除成功
     */
    fun deleteFile(fileUrl: String): Boolean {
        return try {
            val file = getFileFromUrl(fileUrl)
            if (!file.exists()) {
                return false
            }
            Files.deleteIfExists(file.toPath())
        } catch (e: IllegalArgumentException) {
            // 无效的URL格式
            false
        } catch (e: SecurityException) {
            // 路径遍历攻击检测
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 验证文件
     * @param filename 文件名
     * @param fileSize 文件大小
     * @throws IllegalArgumentException 如果文件大小或格式不合法
     */
    private fun validateFile(filename: String?, fileSize: Long) {
        // 检查文件名
        if (filename.isNullOrBlank()) {
            throw IllegalArgumentException("文件名不能为空")
        }
        // 检查文件大小
        if (fileSize > 0 && fileSize > MAX_FILE_SIZE) {
            throw IllegalArgumentException("文件大小不能超过 ${MAX_FILE_SIZE / (1024 * 1024)}MB")
        }

        // 检查文件扩展名
        val fileExtension = getFileExtension(filename).lowercase()
        if (fileExtension.isNotEmpty() && !ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw IllegalArgumentException("不支持的文件类型: $fileExtension。允许的类型: ${ALLOWED_EXTENSIONS.joinToString(", ")}")
        }
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 文件扩展名（不含点）
     */
    private fun getFileExtension(filename: String): String {
        val dotIndex = filename.lastIndexOf('.')
        return if (dotIndex > 0 && dotIndex < filename.length - 1) {
            filename.substring(dotIndex + 1)
        } else {
            ""
        }
    }
}