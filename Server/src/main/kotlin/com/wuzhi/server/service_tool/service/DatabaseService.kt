package com.wuzhi.server.service_tool.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseAddDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseEditDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseNodeAddDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseNodeEditDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseNodeChangeDTO
import com.wuzhi.server.service_tool.pojo.dto.NodeStatusDTO
import com.wuzhi.server.service_tool.pojo.po.Database

/**
 * 数据点服务接口
 * 
 * 定义数据点管理的所有业务操作接口。
 */
interface DatabaseService : IService<Database> {

    /**
     * 添加数据库
     */
    fun addDatabase(dto: DatabaseAddDTO): ResponseResult

    /**
     * 添加数据库节点
     */
    fun addDatabaseNode(dto: DatabaseNodeAddDTO): ResponseResult

    /**
     * 删除数据库
     */
    fun deleteDatabase(id: String): ResponseResult

    /**
     * 删除数据库节点
     */
    fun deleteDatabaseNode(id: String): ResponseResult

    /**
     * 编辑数据库
     */
    fun editDatabase(dto: DatabaseEditDTO): ResponseResult

    /**
     * 编辑数据库节点
     */
    fun editDatabaseNode(dto: DatabaseNodeEditDTO): ResponseResult

    /**
     * 获取数据库详情
     */
    fun getDatabaseDetail(id: String): ResponseResult

    /**
     * 获取数据库树
     */
    fun getDatabaseTree(companyId: String?): ResponseResult

    /**
     * 更新数据库节点状态
     */
    fun updateDatabaseNodeStatus(dto: List<NodeStatusDTO>): ResponseResult

    /**
     * 修改数据库节点的父级（中间节点和叶子节点）
     */
    fun changeDatabaseParent(dto: DatabaseNodeChangeDTO): ResponseResult

}