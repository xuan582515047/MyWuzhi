package com.wuzhi.server.service_tool.controller

import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_tool.mapper.TagMapper
import com.wuzhi.server.service_tool.pojo.bo.SortType
import com.wuzhi.server.service_tool.pojo.po.Tool
import com.wuzhi.server.service_tool.pojo.bo.ToolType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/tool/constant")
class ConstantListController(
    private val tagMapper: TagMapper
) {

    /**
     * 获取标签列表
     */
    @GetMapping("/tags")
    fun getConstantList(): ResponseResult {
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, tagMapper.selectList(null))
    }

    /**
     * 获取工具类型列表
     */
    @GetMapping("/types")
    fun getToolTypeList(): ResponseResult {
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, listOf(
            ToolType("内部链接", ToolType.INTERNAL_LINK),
            ToolType("外部链接", ToolType.EXTERNAL_LINK),
            ToolType("MCP服务器", ToolType.MCP_SERVER)
        ))
    }

    /**
     * 获取排序类型列表
     */
    @GetMapping("/sortTypes")
    fun getSortTypeList(): ResponseResult {
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, listOf(
            SortType("按默认排序", SortType.DEFAULT),
            SortType("按创建时间排序", SortType.CREATE_TIME),
            SortType("按更新时间排序", SortType.UPDATE_TIME),
            SortType("按价格排序", SortType.PRICE),
            SortType("按使用量排序", SortType.USE_COUNT),
            SortType("按查看次数排序", SortType.VIEW_COUNT),
            SortType("按评分排序", SortType.RATING),
        ))
    }
}
