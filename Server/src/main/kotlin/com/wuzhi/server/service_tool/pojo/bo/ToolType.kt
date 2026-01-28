package com.wuzhi.server.service_tool.pojo.bo

class ToolType(
    val name: String,
    val value: String
) {
    companion object{
        // 外部链接，纯粹的iframe，必须免费
        const val EXTERNAL_LINK = "external_link"
        // 内部链接，iframe，但是会跟平台页面交互
        const val INTERNAL_LINK = "internal_link"
        // MCP服务器
        const val MCP_SERVER = "mcp_server"
    }
}