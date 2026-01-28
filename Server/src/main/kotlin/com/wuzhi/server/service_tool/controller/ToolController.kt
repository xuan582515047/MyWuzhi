package com.wuzhi.server.service_tool.controller

import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolAddDTO
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolEditDTO
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolQueryDTO
import com.wuzhi.server.service_tool.service.ToolService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tool")
class ToolController(
    private val toolService: ToolService
) {

    /**
     * 添加工具
     */
    @PostMapping("/add")
    fun addTool(@RequestBody @Valid dto: ToolAddDTO): ResponseResult{
        return toolService.addTool(dto)
    }

    /**
     * 删除工具
     */
    @DeleteMapping("/delete/{id}")
    fun deleteTool(id: String): ResponseResult{
        return toolService.deleteTool(id)
    }

    /**
     * 编辑工具
     */
    @PostMapping("/edit")
    fun editTool(@RequestBody @Valid dto: ToolEditDTO): ResponseResult{
        return toolService.editTool(dto)
    }

    /**
     * 查询工具列表
     */
    @PostMapping("/list")
    fun queryToolList(@RequestBody @Valid dto: ToolQueryDTO): ResponseResult{
        return toolService.queryToolList(dto)
    }

    /**
     * 查询工具详情
     */
    @GetMapping("/detail/{id}")
    fun getToolDetail(@PathVariable id: String): ResponseResult{
        return toolService.getToolDetail(id)
    }

}