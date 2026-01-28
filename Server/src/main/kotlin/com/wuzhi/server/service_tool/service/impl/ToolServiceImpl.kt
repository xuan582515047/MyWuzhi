package com.wuzhi.server.service_tool.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.common.constant.ErrorMessages
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.PageResult
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_tool.mapper.ToolMapper
import com.wuzhi.server.service_tool.mapper.ToolNodeMapper
import com.wuzhi.server.service_tool.pojo.bo.SortType
import com.wuzhi.server.service_tool.pojo.bo.ToolType
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolAddDTO
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolEditDTO
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolQueryDTO
import com.wuzhi.server.service_tool.pojo.po.Tool
import com.wuzhi.server.service_tool.pojo.po.ToolNode
import com.wuzhi.server.service_tool.pojo.vo.ToolVO
import com.wuzhi.server.service_tool.service.ToolService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ToolServiceImpl(
    private val toolNodeMapper: ToolNodeMapper
): ToolService, ServiceImpl<ToolMapper, Tool>(){
    //TODO 这里需要很严格的判断和流程

    override fun addTool(dto: ToolAddDTO): ResponseResult {
        if (dto.type == ToolType.EXTERNAL_LINK && BigDecimal(dto.price) != BigDecimal.ZERO){
            return ResponseResult.fail(ErrorMessages.EXTERNAL_LINK_PRICE_NOT_ZERO)
        }
        val tool = Tool(
            name = dto.name,
            description = dto.description,
            price = BigDecimal(dto.price),
            url = dto.url,
            viewCount = 0,
            useCount = 0,
            rating = 0,
            version = dto.version,
            authorUserId = UserContext.getUserId(),
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            status = Tool.STATUS_PRIVATE,
            auditStatus = Tool.AUDIT_STATUS_NONE,
            type = dto.type,
            tagIds = dto.tagIds.joinToString(","),
            keywords = dto.keywords.joinToString(","),
        )
        save(tool)
        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    @Transactional
    override fun deleteTool(id: String): ResponseResult {
        // 删除用户已使用的工具
        val wrapper = KtQueryWrapper(ToolNode::class.java)
            .eq(ToolNode::toolId, id)
        toolNodeMapper.delete(wrapper)

        // 删除工具
        removeById(id)

        return ResponseResult.success(OkMessages.DELETE_SUCCESS)
    }

    override fun editTool(dto: ToolEditDTO): ResponseResult {
        // 更新工具
        val updateWrapper = KtUpdateWrapper(Tool::class.java)
            .eq(Tool::id, dto.id)
            .set(dto.name != null, Tool::name, dto.name)
            .set(dto.description != null,Tool::description, dto.description)
            .set(dto.price != null,Tool::price, BigDecimal(dto.price))
            .set(dto.url != null,Tool::url, dto.url)
            .set(dto.version != null,Tool::version, dto.version)
            .set(dto.type != null,Tool::type, dto.type)
            .set(dto.tagIds != null,Tool::tagIds, dto.tagIds?.joinToString(","))
            .set(dto.keywords != null,Tool::keywords, dto.keywords?.joinToString(","))
            .set(Tool::updateTime, LocalDateTime.now())
        update(updateWrapper)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun queryToolList(dto: ToolQueryDTO): ResponseResult {
        //TODO ES分词查询

        // 将空值字符串转为null
        if (dto.query != null && dto.query == "") dto.query = null
        if (dto.type != null && dto.type == "") dto.type = null
        if (dto.minPrice != null && dto.minPrice == "") dto.minPrice = null
        if (dto.maxPrice != null && dto.maxPrice == "") dto.maxPrice = null
        if (dto.tagIds != null && dto.tagIds == "") dto.tagIds = null

        // 构建基础查询条件
        val wrapper = KtQueryWrapper(Tool::class.java)
            .like(dto.query != null, Tool::name, dto.query)
            .like(dto.query != null, Tool::description, dto.query)
            .eq(dto.type != null, Tool::type, dto.type)
            .ge(dto.minPrice != null, Tool::price, BigDecimal(dto.minPrice ?: "0.0"))
            .le(dto.maxPrice != null, Tool::price, BigDecimal(dto.maxPrice ?: "0.0"))
        // tagIds筛选（字符串匹配，TODO: 后续优化为关联表查询）
        if (dto.tagIds != null) {
            // 支持多标签筛选（逗号分隔），使用OR条件匹配任意一个标签
            val str = dto.tagIds!!
            val tagIdList = str.split(",").filter { it.isNotBlank() }
            if (tagIdList.isNotEmpty()) {
                // 使用 or 条件构建多个 like 查询
                tagIdList.forEachIndexed { index, tagId ->
                    if (index == 0) {
                        wrapper.like(Tool::tagIds, tagId.trim())
                    } else {
                        wrapper.or().like(Tool::tagIds, tagId.trim())
                    }
                }
            }
        }
        // 根据排序类型排序
        when (dto.sortBy) {
            SortType.PRICE -> wrapper.orderBy(true, dto.isAsc, Tool::price )
            SortType.RATING -> wrapper.orderBy(true, dto.isAsc, Tool::rating)
            SortType.VIEW_COUNT -> wrapper.orderBy(true, dto.isAsc, Tool::viewCount)
            SortType.USE_COUNT -> wrapper.orderBy(true, dto.isAsc, Tool::useCount)
            SortType.CREATE_TIME -> wrapper.orderBy(true, dto.isAsc, Tool::createTime)
            SortType.UPDATE_TIME -> wrapper.orderBy(true, dto.isAsc, Tool::updateTime)
            SortType.DEFAULT -> wrapper.orderBy(true, dto.isAsc, Tool::useCount)
        }
        
        // 使用MyBatis-Plus分页查询
        val page = Page<Tool>(dto.page, dto.pageSize)
        val pageResult = page(page, wrapper)
        val toolList = pageResult.records.map(ToolVO::fromTool)
        
        return ResponseResult.success(
            OkMessages.QUERY_SUCCESS,
            PageResult(
                data = toolList,
                total = pageResult.total
            )
        )
    }

    override fun getToolDetail(id: String): ResponseResult {
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, getById(id))
    }

}
