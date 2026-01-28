package com.wuzhi.server.service_chat.tool

import com.fasterxml.jackson.databind.ObjectMapper
import com.wuzhi.server.service_chat.pojo.dto.MissionToolDTO
import com.wuzhi.server.service_company.pojo.po.manage.Mission
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class MissionAddTool(
    private val objectMapper: ObjectMapper
) {

    @Tool(description = "添加任务", returnDirect = true)
    fun addMission(
        @ToolParam(description = "任务名称")
        name: String,
        @ToolParam(description = "任务描述")
        description: String,
        @ToolParam(description = "开始时间，请使用ISO-8601 标准格式字符串")
        startTime: String,
        @ToolParam(description = "结束时间，请使用ISO-8601 标准格式字符串")
        endTime: String
    ): String {
        val dto = MissionToolDTO(
            name = name,
            description = description,
            startTime = startTime,
            endTime = endTime,
            status = Mission.STATUS_NOT_START
        )
        return objectMapper.writeValueAsString(dto)
    }
}