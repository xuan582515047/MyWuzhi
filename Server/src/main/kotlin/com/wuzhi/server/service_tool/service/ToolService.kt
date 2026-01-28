package com.wuzhi.server.service_tool.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolAddDTO
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolEditDTO
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolQueryDTO
import com.wuzhi.server.service_tool.pojo.po.Tool

interface ToolService: IService<Tool> {

    /**
     * 添加工具
     */
    fun addTool(dto: ToolAddDTO): ResponseResult

    /**
     * 删除工具
     */
    fun deleteTool(id: String): ResponseResult

    /**
     * 编辑工具
     */
    fun editTool(dto: ToolEditDTO): ResponseResult

    /**
     * 查询工具列表
     */
    fun queryToolList(dto: ToolQueryDTO): ResponseResult

    /**
     * 查询工具详情
     */
    fun getToolDetail(id: String): ResponseResult
}