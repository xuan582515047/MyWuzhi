package com.wuzhi.server.service_company.controller

import com.wuzhi.server.common.constant.BaseUrlConstant
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.role.EmployeeRoleDTO
import com.wuzhi.server.service_company.pojo.dto.role.RoleAddDTO
import com.wuzhi.server.service_company.pojo.dto.role.RoleDeleteDTO
import com.wuzhi.server.service_company.pojo.dto.role.RoleEmployeeDTO
import com.wuzhi.server.service_company.pojo.dto.role.RolePermissionDTO
import com.wuzhi.server.service_company.pojo.dto.role.RoleUpdateDTO
import com.wuzhi.server.service_company.pojo.po.manage.CompanyRecord
import com.wuzhi.server.service_company.service.CompanyRecordService
import com.wuzhi.server.service_company.service.RoleService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/role")
class RoleController(
    private val roleService: RoleService,
    private val companyRecordService: CompanyRecordService
) {

    /**
     * 给角色分配用户
     */
    @PostMapping("/employee/from")
    fun setRoleFromEmployee(@RequestBody @Valid dto: RoleEmployeeDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_ROLE,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "给角色分配用户：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return roleService.setRoleFromEmployee(dto)
    }

    /**
     * 给用户分配角色
     */
    @PostMapping("/employee/to")
    fun setRoleToEmployee(@RequestBody @Valid dto: EmployeeRoleDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_ROLE,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "给用户分配角色：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return roleService.setRoleToEmployee(dto)
    }

    /**
     * 给角色设置权限
     */
    @PostMapping("/permission")
    fun setPermission(@RequestBody @Valid dto: RolePermissionDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_ROLE,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "给角色设置权限：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return roleService.setPermission(dto)
    }

    /**
     * 添加角色
     */
    @PostMapping("/add")
    fun addRole(@RequestBody @Valid dto: RoleAddDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_ROLE,
            operateType = CompanyRecord.OPERATE_TYPE_ADD,
            targetId = dto.companyId,
            message = "添加角色：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return roleService.addRole(dto)
    }

    /**
     * 删除角色
     */
    @PostMapping("/delete")
    fun deleteRole(@RequestBody @Valid dto: RoleDeleteDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_ROLE,
            operateType = CompanyRecord.OPERATE_TYPE_DELETE,
            targetId = dto.companyId,
            message = "删除角色：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return roleService.deleteRole(dto.roleId)
    }

    /**
     * 更新角色
     */
    @PutMapping("/update")
    fun updateRole(@RequestBody @Valid dto: RoleUpdateDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_ROLE,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "更新角色：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return roleService.updateRole(dto)
    }

    /**
     * 获取角色列表
     */
    @GetMapping("/list/{companyId}")
    fun getRoleList(@PathVariable("companyId") companyId: String): ResponseResult {
        return roleService.getRoleList(companyId)
    }

}
