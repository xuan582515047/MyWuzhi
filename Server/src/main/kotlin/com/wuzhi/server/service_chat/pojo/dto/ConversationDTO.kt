package com.wuzhi.server.service_chat.pojo.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class ConversationDTO(
    @NotBlank
    val id: String,
    @NotBlank
    val title: String,
    val createTime: LocalDateTime? = null,
    val updateTime: LocalDateTime? = null
) {
}