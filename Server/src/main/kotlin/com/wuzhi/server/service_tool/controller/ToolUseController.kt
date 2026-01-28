package com.wuzhi.server.service_tool.controller

import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_tool.pojo.dto.NodeStatusDTO
import com.wuzhi.server.service_tool.pojo.dto.toolRecord.ToolRecordAddDTO
import com.wuzhi.server.service_tool.pojo.dto.tool.ToolUseAddDTO
import com.wuzhi.server.service_tool.pojo.dto.toolRecord.ToolRecordEditDTO
import com.wuzhi.server.service_tool.pojo.dto.toolRecord.ToolRecordQueryDTO
import com.wuzhi.server.service_tool.service.ToolUseService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/tool/use")
class ToolUseController(
    private val toolUseService: ToolUseService
) {

    /**
     * 修改工具节点状态
     */
    @PostMapping("/status")
    fun editToolNodeStatus(@RequestBody @Valid dto: List<NodeStatusDTO>): ResponseResult {
        return toolUseService.editToolNodeStatus(dto)
    }

    /**
     * 获取工具使用的树
     */
    @GetMapping("/tree/{companyId}")
    fun getToolUseTree(@PathVariable("companyId") companyId: String): ResponseResult {
        return toolUseService.getToolUseTree(companyId)
    }

    /**
     * 用户使用工具
     */
    @PostMapping("/add")
    fun useTool(@RequestBody @Valid dto: ToolUseAddDTO): ResponseResult{
        return toolUseService.useTool(dto)
    }

    /**
     * 添加工具使用记录
     */
    @PostMapping("/record")
    fun addToolUseRecord(dto: ToolRecordAddDTO): ResponseResult {
        return toolUseService.addToolUseRecord(dto)
    }

    /**
     * 删除工具使用记录
     */
    @DeleteMapping("/delete/{id}")
    fun deleteToolUseRecord(id: String): ResponseResult {
        return toolUseService.deleteToolUseRecord(id)
    }

    /**
     * 编辑工具使用记录
     */
    @PostMapping("/edit")
    fun editToolUseRecord(@RequestBody @Valid dto: ToolRecordEditDTO): ResponseResult {
        return toolUseService.editToolUseRecord(dto)
    }

    /**
     * 查询工具使用记录列表
     */
    @PostMapping("/list")
    fun queryToolUseRecordList(@RequestBody @Valid dto: ToolRecordQueryDTO): ResponseResult {
        return toolUseService.queryToolUseRecordList(dto)
    }
}
