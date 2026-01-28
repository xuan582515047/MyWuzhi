package com.wuzhi.server.service_chat.pojo.dto

import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

data class MessageDTO(
    @NotBlank(message = "对话ID不能为空")
    @RequestParam("conversationId")
    val conversationId: String,

    @NotBlank(message = "消息不能为空")
    @RequestParam("message")
    val message: String,

    @NotBlank(message = "模型不能为空")
    @RequestParam("model")
    val model: String,

    @RequestParam("databaseIds", required = false)
    val databaseIds: List<String>? = null,

    @RequestParam("files", required = false)
    val files: List<MultipartFile>? = null
) {}