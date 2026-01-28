package com.wuzhi.server.common.util

import org.springframework.ai.content.Media
import org.springframework.util.MimeType
import org.springframework.http.MediaType as HttpMediaType
import org.springframework.util.MimeTypeUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*


object MediaConvertUtil {

    // 扩展名到MimeType字符串的映射
    private val MIME_TYPE_STRING_MAP = mapOf(
        // 图片类型
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "png" to "image/png",
        "gif" to "image/gif",
        "bmp" to "image/bmp",
        "webp" to "image/webp",
        "svg" to "image/svg+xml",
        "ico" to "image/x-icon",
        "tiff" to "image/tiff",
        "tif" to "image/tiff",

        // 文本类型
        "txt" to "text/plain",
        "text" to "text/plain",
        "html" to "text/html",
        "htm" to "text/html",
        "css" to "text/css",
        "csv" to "text/csv",
        "xml" to "application/xml",
        "json" to "application/json",
        "js" to "application/javascript",

        // PDF
        "pdf" to "application/pdf",

        // 文档
        "doc" to "application/msword",
        "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "xls" to "application/vnd.ms-excel",
        "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "ppt" to "application/vnd.ms-powerpoint",
        "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",

        // 音频
        "mp3" to "audio/mpeg",
        "wav" to "audio/wav",
        "ogg" to "audio/ogg",
        "m4a" to "audio/mp4",
        "flac" to "audio/flac",
        "aac" to "audio/aac",

        // 视频
        "mp4" to "video/mp4",
        "avi" to "video/x-msvideo",
        "mov" to "video/quicktime",
        "wmv" to "video/x-ms-wmv",
        "flv" to "video/x-flv",
        "mkv" to "video/x-matroska",
        "webm" to "video/webm",

        // 压缩文件
        "zip" to "application/zip",
        "rar" to "application/x-rar-compressed",
        "7z" to "application/x-7z-compressed",
        "tar" to "application/x-tar",
        "gz" to "application/gzip"
    )

    /**
     * 将字符串转换为MimeType对象
     */
    private fun toMimeType(mimeTypeString: String): MimeType {
        return try {
            MimeTypeUtils.parseMimeType(mimeTypeString)
        } catch (e: Exception) {
            // 如果解析失败，返回默认的octet-stream类型
            HttpMediaType.APPLICATION_OCTET_STREAM
        }
    }

    /**
     * 根据文件名或文件类型字符串获取MimeType对象
     */
    private fun detectMimeType(filename: String? = null, contentType: String? = null): MimeType {
        // 优先使用传入的contentType
        if (!contentType.isNullOrBlank()) {
            return toMimeType(contentType)
        }

        // 如果没有文件名，返回默认类型
        if (filename.isNullOrBlank()) {
            return HttpMediaType.APPLICATION_OCTET_STREAM
        }

        // 提取文件扩展名
        val extension = filename.substringAfterLast('.').lowercase(Locale.getDefault())

        // 从映射表中查找MimeType字符串
        val mimeTypeString = MIME_TYPE_STRING_MAP[extension] ?: "application/octet-stream"

        return toMimeType(mimeTypeString)
    }

    /**
     * 使用Files.probeContentType检测MimeType
     */
    private fun probeContentType(file: File): String? {
        return try {
            Files.probeContentType(file.toPath())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 使用Files.probeContentType检测MimeType
     */
    private fun probeContentType(path: Path): String? {
        return try {
            Files.probeContentType(path)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 将 MultipartFile 转换为 Media
     */
    @Throws(IOException::class)
    fun fromMultipartFile(file: MultipartFile): Media {
        val data = file.bytes
        val mimeType = detectMimeType(
            filename = file.originalFilename,
            contentType = file.contentType
        )

        return Media.builder()
            .data(data)
            .mimeType(mimeType)
            .build()
    }

    /**
     * 将 MultipartFile 转换为 Media
     */
    @Throws(IOException::class)
    fun fromMultipartFiles(files: List<MultipartFile>): List<Media> {
        val mediaList = ArrayList<Media>()
        files.forEach { file ->
            val data = file.bytes
            val mimeType = detectMimeType(
                filename = file.originalFilename,
                contentType = file.contentType
            )

            mediaList.add(Media.builder()
                .data(data)
                .mimeType(mimeType)
                .build())
        }
        return mediaList
    }

    /**
     * 将 File 转换为 Media
     */
    @Throws(IOException::class)
    fun fromFile(file: File): Media {
        val data = file.readBytes()
        // 优先使用系统探测的MimeType
        val probedMimeTypeString = probeContentType(file)
        val mimeType = if (probedMimeTypeString != null) {
            toMimeType(probedMimeTypeString)
        } else {
            detectMimeType(filename = file.name)
        }

        return Media.builder()
            .data(data)
            .mimeType(mimeType)
            .build()
    }

    /**
     * 将 File 转换为 Media
     */
    @Throws(IOException::class)
    fun fromFiles(files: List<File>): List<Media> {
        val mediaList = ArrayList<Media>()
        files.forEach { file ->
            val data = file.readBytes()
            // 优先使用系统探测的MimeType
            val probedMimeTypeString = probeContentType(file)
            val mimeType = if (probedMimeTypeString != null) {
                toMimeType(probedMimeTypeString)
            } else {
                detectMimeType(filename = file.name)
            }

            mediaList.add(Media.builder()
                .data(data)
                .mimeType(mimeType)
                .build())
        }
        return mediaList
    }

    /**
     * 将字节数组转换为 Media（需要指定文件名或MimeType）
     */
    fun fromBytes(data: ByteArray, filename: String? = null, mimeType: MimeType? = null): Media {
        val detectedMimeType = mimeType ?: detectMimeType(filename = filename)

        return Media.builder()
            .data(data)
            .mimeType(detectedMimeType)
            .build()
    }

    /**
     * 将字节数组转换为 Media（使用MimeType字符串）
     */
    fun fromBytes(data: ByteArray, filename: String? = null, mimeTypeString: String? = null): Media {
        val mimeType = if (!mimeTypeString.isNullOrBlank()) {
            toMimeType(mimeTypeString)
        } else {
            detectMimeType(filename = filename)
        }

        return Media.builder()
            .data(data)
            .mimeType(mimeType)
            .build()
    }

    /**
     * 根据文件扩展名获取MimeType对象
     */
    fun getMimeTypeByExtension(extension: String): MimeType {
        val mimeTypeString = MIME_TYPE_STRING_MAP[extension.lowercase(Locale.getDefault())]
            ?: "application/octet-stream"
        return toMimeType(mimeTypeString)
    }

    /**
     * 根据文件扩展名获取MimeType字符串
     */
    fun getMimeTypeStringByExtension(extension: String): String {
        return MIME_TYPE_STRING_MAP[extension.lowercase(Locale.getDefault())]
            ?: "application/octet-stream"
    }

    /**
     * 获取所有支持的MimeType映射
     */
    fun getSupportedMimeTypes(): Map<String, String> {
        return MIME_TYPE_STRING_MAP
    }
}