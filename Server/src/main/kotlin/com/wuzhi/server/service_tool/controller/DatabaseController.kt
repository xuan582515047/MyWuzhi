package com.wuzhi.server.service_tool.controller

import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseAddDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseEditDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseNodeAddDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseNodeEditDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseNodeChangeDTO
import com.wuzhi.server.service_tool.pojo.dto.NodeStatusDTO
import com.wuzhi.server.service_tool.service.DatabaseService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/database")
class DatabaseController(
    private val databaseService: DatabaseService
) {
    /**
     * 更新数据库节点状态
     */
    @PostMapping("/status")
    fun updateDatabaseNodeStatus(@RequestBody @Valid dto: List<NodeStatusDTO>): ResponseResult {
        return databaseService.updateDatabaseNodeStatus(dto)
    }

    /**
     * 添加数据库
     */
    @PostMapping("/add", consumes = ["multipart/form-data"])
    fun addDatabase(@Valid dto: DatabaseAddDTO): ResponseResult {
        return databaseService.addDatabase(dto)
    }

    /**
     * 添加数据库节点
     */
    @PostMapping("/add/node")
    fun addDatabaseNode(@RequestBody @Valid dto: DatabaseNodeAddDTO): ResponseResult {
        return databaseService.addDatabaseNode(dto)
    }

    /**
     * 删除数据库
     */
    @DeleteMapping("/delete/{id}")
    fun deleteDatabase(@PathVariable("id") id: String): ResponseResult {
        return databaseService.deleteDatabase(id)
    }

    /**
     * 删除数据库节点
     */
    @DeleteMapping("/delete/node/{id}")
    fun deleteDatabaseNode(@PathVariable("id") id: String): ResponseResult {
        return databaseService.deleteDatabaseNode(id)
    }

    /**
     * 编辑数据库
     */
    @PostMapping("/edit")
    fun editDatabase(@RequestBody @Valid dto: DatabaseEditDTO): ResponseResult {
        return databaseService.editDatabase(dto)
    }

    /**
     * 编辑数据库节点
     */
    @PostMapping("/edit/node")
    fun editDatabaseNode(@RequestBody @Valid dto: DatabaseNodeEditDTO): ResponseResult {
        return databaseService.editDatabaseNode(dto)
    }

    /**
     * 查询数据库树
     */
    @GetMapping("/tree")
    fun getDatabaseTree(): ResponseResult {
        return databaseService.getDatabaseTree(null)
    }

    /**
     * 查询数据库树，根据公司id
     */
    @GetMapping("/tree/{companyId}")
    fun getDatabaseTree(@PathVariable("companyId") companyId: String): ResponseResult {
        return databaseService.getDatabaseTree(companyId)
    }

    /**
     * 查询数据库详情
     */
    @GetMapping("/detail/{id}")
    fun getDatabaseDetail(@PathVariable("id") id: String): ResponseResult {
        return databaseService.getDatabaseDetail(id)
    }

    /**
     * 修改数据库节点的父级
     */
    @PostMapping("/change/parent")
    fun changeParent(@RequestBody @Valid dto: DatabaseNodeChangeDTO): ResponseResult {
        return databaseService.changeDatabaseParent(dto)
    }

}