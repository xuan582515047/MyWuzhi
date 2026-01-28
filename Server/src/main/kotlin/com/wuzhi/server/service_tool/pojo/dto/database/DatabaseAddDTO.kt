package com.wuzhi.server.service_tool.pojo.dto.database

import jakarta.validation.constraints.NotBlank
import org.springframework.web.multipart.MultipartFile

class DatabaseAddDTO(

    @NotBlank(message = "数据库名称不能为空")
    val name: String,

    @NotBlank(message = "父节点不能为空")
    val parentId: String,

    val description: String? = null,

    val companyId: String? = null,

    @NotBlank(message = "数据库类型不能为空")
    val type: String,

    val url: String? = null,

    val text: String? = null,

    val file: MultipartFile? = null
) {
}