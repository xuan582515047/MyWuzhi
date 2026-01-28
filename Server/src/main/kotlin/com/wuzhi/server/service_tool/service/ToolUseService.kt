package com.wuzhi.server.service_tool.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_tool.pojo.dto.NodeStatusDTO
import com.wuzhi.server.service_tool.pojo.dto.toolRecord.ToolRecordAddDTO
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolUseAddDTO
import com.wuzhi.server.service_tool.pojo.dto.toolNode.ToolNodeAddDTO
import com.wuzhi.server.service_tool.pojo.dto.toolNode.ToolNodeChangeDTO
import com.wuzhi.server.service_tool.pojo.dto.toolNode.ToolNodeEditDTO
import com.wuzhi.server.service_tool.pojo.dto.toolRecord.ToolRecordEditDTO
import com.wuzhi.server.service_tool.pojo.dto.toolRecord.ToolRecordQueryDTO
import com.wuzhi.server.service_tool.pojo.po.ToolUseRecord

interface ToolUseService: IService<ToolUseRecord> {

    /**
     * 获取工具使用列表
     */
    fun getToolUseTree(companyId: String?): ResponseResult

    /**
     * 使用工具
     */
    fun useTool(dto: ToolUseAddDTO): ResponseResult

    /**
     * 添加工具使用记录
     */
    fun addToolUseRecord(dto: ToolRecordAddDTO): ResponseResult

    /**
     * 删除工具使用记录
     */
    fun deleteToolUseRecord(id: String): ResponseResult

    /**
     * 编辑工具使用记录
     */
    fun editToolUseRecord(dto: ToolRecordEditDTO): ResponseResult

    /**
     * 查询工具使用记录列表
     */
    fun queryToolUseRecordList(dto: ToolRecordQueryDTO): ResponseResult

    /**
     * 修改工具节点状态
     */
    fun editToolNodeStatus(dto: List<NodeStatusDTO>): ResponseResult

    /**
     * 添加工具节点（中间节点）
     */
    fun addToolNode(dto: ToolNodeAddDTO): ResponseResult

    /**
     * 删除工具节点（中间节点）
     */
    fun deleteToolNode(id: String): ResponseResult

    /**
     * 修改工具节点（中间节点）
     */
    fun editToolNode(dto: ToolNodeEditDTO): ResponseResult

    /**
     * 修改工具节点的父级（中间节点）
     */
    fun changeParent(dto: ToolNodeChangeDTO): ResponseResult
}