package com.wuzhi.server.service_tool.pojo.dto.tool

import jakarta.validation.constraints.NotBlank

class ToolAddDTO(

    @NotBlank(message = "工具名称不能为空")
    val name: String,

    val description: String,

    @NotBlank(message = "工具价格不能为空")
    val price: String,

    @NotBlank(message = "工具地址不能为空")
    val url: String,

    @NotBlank(message = "工具版本不能为空")
    val version: String,

    @NotBlank(message = "工具类型不能为空")
    val type: String,

    val tagIds: List<String>,

    val keywords: List<String>,
) {
}