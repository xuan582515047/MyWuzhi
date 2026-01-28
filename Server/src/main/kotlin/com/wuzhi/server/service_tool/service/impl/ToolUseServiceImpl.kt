package com.wuzhi.server.service_tool.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_tool.mapper.ToolNodeMapper
import com.wuzhi.server.service_tool.mapper.ToolUseRecordMapper
import com.wuzhi.server.service_tool.pojo.dto.NodeStatusDTO
import com.wuzhi.server.service_tool.pojo.dto.toolRecord.ToolRecordAddDTO
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolUseAddDTO
import com.wuzhi.server.service_tool.pojo.dto.toolNode.ToolNodeAddDTO
import com.wuzhi.server.service_tool.pojo.dto.toolNode.ToolNodeChangeDTO
import com.wuzhi.server.service_tool.pojo.dto.toolNode.ToolNodeEditDTO
import com.wuzhi.server.service_tool.pojo.dto.toolRecord.ToolRecordEditDTO
import com.wuzhi.server.service_tool.pojo.dto.toolRecord.ToolRecordQueryDTO
import com.wuzhi.server.service_tool.pojo.po.DatabaseNode
import com.wuzhi.server.service_tool.pojo.po.ToolNode
import com.wuzhi.server.service_tool.pojo.po.ToolUseRecord
import com.wuzhi.server.service_tool.pojo.vo.ToolTreeVO
import com.wuzhi.server.service_tool.service.ToolService
import com.wuzhi.server.service_tool.service.ToolUseService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ToolUseServiceImpl(
    private val toolNodeMapper: ToolNodeMapper,
    private val toolService: ToolService
): ToolUseService, ServiceImpl<ToolUseRecordMapper, ToolUseRecord>(){

    override fun editToolNodeStatus(dto: List<NodeStatusDTO>): ResponseResult {
        dto.forEach {
            val toolNode = toolNodeMapper.selectById(it.id)
            toolNode.status = it.status
            toolNodeMapper.updateById(toolNode)
        }
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun addToolNode(dto: ToolNodeAddDTO): ResponseResult {
        val node = ToolNode(
            name = dto.name,
            type = ToolNode.TYPE_MIDDLE,
            userId = UserContext.getUserId(),
            companyId = dto.companyId,
            operatorUserId = UserContext.getUserId(),
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            status = ToolNode.STATUS_ENABLE,
            url = ""
        )
        toolNodeMapper.insert(node)
        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    override fun deleteToolNode(id: String): ResponseResult {
        toolNodeMapper.deleteById(id)
        return ResponseResult.success(OkMessages.DELETE_SUCCESS)
    }

    override fun editToolNode(dto: ToolNodeEditDTO): ResponseResult {
        val wrapper = KtUpdateWrapper(ToolNode::class.java)
            .eq(ToolNode::id, dto.id)
            .eq(ToolNode::userId, UserContext.getUserId())
            .set(ToolNode::name, dto.name)
        toolNodeMapper.update(null, wrapper)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun changeParent(dto: ToolNodeChangeDTO): ResponseResult {
        val wrapper = KtUpdateWrapper(ToolNode::class.java)
            .eq(ToolNode::id, dto.id)
            .eq(ToolNode::userId, UserContext.getUserId())
            .set(ToolNode::parentId, dto.parentId)
        toolNodeMapper.update(null, wrapper)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun getToolUseTree(companyId: String?): ResponseResult {
        // 查询用户使用的工具节点
        val wrapper = KtQueryWrapper(ToolNode::class.java)
            .eq(ToolNode::userId, UserContext.getUserId())
        val nodeList = toolNodeMapper.selectList(wrapper)

        // 生成树，返回
        val tree = getNodeChildren(null, nodeList)
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, tree)
    }

    override fun useTool(dto: ToolUseAddDTO): ResponseResult {
        val tool = toolService.getById(dto.toolId)
        val toolNode = ToolNode(
            userId = UserContext.getUserId(),
            name = dto.name ?: tool.name,
            type = ToolNode.TYPE_LEAF,
            companyId = dto.companyId,
            parentId = dto.parentId,
            toolId = dto.toolId,
            operatorUserId = UserContext.getUserId(),
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            status = ToolNode.STATUS_ENABLE,
            url = tool.url
        )
        toolNodeMapper.insert(toolNode)
        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    override fun addToolUseRecord(dto: ToolRecordAddDTO): ResponseResult {
        // 如果没有详情，则从工具表中获取详情
        var detail = dto.detail
        if (detail == null || detail.isBlank()){
            val tool = toolService.getById(dto.toolId)
            detail = "工具名称：${tool.name}，工具类型：${tool.type}，工具版本：${tool.version}，工具描述：${tool.description}"
        }

        // 保存使用记录
        val record = ToolUseRecord(
            userId = UserContext.getUserId(),
            toolId = dto.toolId,
            companyId = dto.companyId,
            detail = detail,
            createTime = LocalDateTime.now()
        )
        save(record)
        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    override fun deleteToolUseRecord(id: String): ResponseResult {
        removeById(id)
        return ResponseResult.success(OkMessages.DELETE_SUCCESS)
    }

    override fun editToolUseRecord(dto: ToolRecordEditDTO): ResponseResult {
        // 查询
        val wrapper = KtUpdateWrapper(ToolUseRecord::class.java)
            .eq(ToolUseRecord::toolId, dto.toolId)
            .eq(ToolUseRecord::userId, UserContext.getUserId())
            .eq(dto.companyId != null, ToolUseRecord::companyId, dto.companyId)
            .set(dto.detail != null, ToolUseRecord::detail, dto.detail)

        // 更新
        update(wrapper)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun queryToolUseRecordList(dto: ToolRecordQueryDTO): ResponseResult {
        val wrapper = KtQueryWrapper(ToolUseRecord::class.java)
            .eq(ToolUseRecord::toolId, dto.toolId)
            .eq(ToolUseRecord::userId, UserContext.getUserId())
            .eq(dto.companyId != null, ToolUseRecord::companyId, dto.companyId)
        val pageResult = page(Page(dto.pageNum, dto.pageSize), wrapper)
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, pageResult)
    }

    private fun getNodeChildren(parentId: String?, nodeList: List<ToolNode>): List<ToolTreeVO>{
        return nodeList.filter {
            it.parentId == parentId
        }.map {
            val id = it.id!!
            if (it.type == DatabaseNode.TYPE_MIDDLE){
                // 中间节点，递归查询子节点
                ToolTreeVO(
                    id = id,
                    name = it.name,
                    status = it.status,
                    type = DatabaseNode.TYPE_MIDDLE,
                    children = getNodeChildren(id, nodeList)
                )
            }else{
                // 叶子节点，查询工具信息，返回工具链接
                val tool = toolService.getById(it.toolId)
                ToolTreeVO(
                    id = id,
                    name = it.name,
                    url = tool.url,
                    status = it.status,
                    type = DatabaseNode.TYPE_LEAF,
                    children = null
                )
            }
        }
    }

}
