package com.wuzhi.server.service_company.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.role.EmployeeRoleDTO
import com.wuzhi.server.service_company.pojo.dto.role.RoleAddDTO
import com.wuzhi.server.service_company.pojo.dto.role.RoleEmployeeDTO
import com.wuzhi.server.service_company.pojo.dto.role.RolePermissionDTO
import com.wuzhi.server.service_company.pojo.dto.role.RoleUpdateDTO
import com.wuzhi.server.service_company.pojo.po.auth.Role

interface RoleService : IService<Role> {

    /**
     * 给角色分配用户
     */
    fun setRoleFromEmployee(dto: RoleEmployeeDTO): ResponseResult

    /**
     * 给员工添加角色
     */
    fun setRoleToEmployee(dto: EmployeeRoleDTO): ResponseResult

    /**
     * 给角色设置权限
     */
    fun setPermission(dto: RolePermissionDTO): ResponseResult

    /**
     * 添加角色
     */
    fun addRole(dto: RoleAddDTO): ResponseResult

    /**
     * 删除角色
     */
    fun deleteRole(roleId: String): ResponseResult

    /**
     * 更新角色
     */
    fun updateRole(dto: RoleUpdateDTO): ResponseResult

    /**
     * 获取角色列表
     */
    fun getRoleList(companyId: String): ResponseResult
}