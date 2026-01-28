package com.wuzhi.server.service_tool.controller

import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_tool.pojo.dto.toolNode.ToolNodeAddDTO
import com.wuzhi.server.service_tool.pojo.dto.toolNode.ToolNodeChangeDTO
import com.wuzhi.server.service_tool.pojo.dto.toolNode.ToolNodeEditDTO
import com.wuzhi.server.service_tool.service.ToolUseService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tool/node")
class ToolNodeController(
    private val toolUseService: ToolUseService
) {

    /**
     * 添加工具节点（中间节点）
     */
    @PostMapping("/add")
    fun addToolNode(@RequestBody @Valid dto: ToolNodeAddDTO): ResponseResult {
        return toolUseService.addToolNode(dto)
    }

    /**
     * 删除工具节点（中间节点或叶子节点）
     */
    @DeleteMapping("/delete/{id}")
    fun deleteToolNode(@PathVariable("id") id: String): ResponseResult {
        return toolUseService.deleteToolNode(id)
    }

    /**
     * 修改工具节点（中间节点或叶子节点）
     */
    @PostMapping("/edit")
    fun editToolNode(@RequestBody @Valid dto: ToolNodeEditDTO): ResponseResult {
        return toolUseService.editToolNode(dto)
    }

    /**
     * 修改工具节点的父级（中间节点或叶子节点）
     */
    @PostMapping("/change")
    fun changeParent(@RequestBody @Valid dto: ToolNodeChangeDTO): ResponseResult {
        return toolUseService.changeParent(dto)
    }
}