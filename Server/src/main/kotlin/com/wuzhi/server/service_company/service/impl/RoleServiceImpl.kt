package com.wuzhi.server.service_company.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.common.constant.ErrorMessages
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.mapper.EmployeeMapper
import com.wuzhi.server.service_company.mapper.EmployeeWithRoleMapper
import com.wuzhi.server.service_company.mapper.RoleMapper
import com.wuzhi.server.service_company.mapper.RoleWithPermissionMapper
import com.wuzhi.server.service_company.pojo.dto.role.EmployeeRoleDTO
import com.wuzhi.server.service_company.pojo.dto.role.RoleAddDTO
import com.wuzhi.server.service_company.pojo.dto.role.RoleEmployeeDTO
import com.wuzhi.server.service_company.pojo.dto.role.RolePermissionDTO
import com.wuzhi.server.service_company.pojo.dto.role.RoleUpdateDTO
import com.wuzhi.server.service_company.pojo.po.auth.EmployeeWithRole
import com.wuzhi.server.service_company.pojo.po.auth.Role
import com.wuzhi.server.service_company.pojo.po.auth.RoleWithPermission
import com.wuzhi.server.service_company.pojo.po.manage.Employee
import com.wuzhi.server.service_company.pojo.vo.RoleVO
import com.wuzhi.server.service_company.service.EmployeeService
import com.wuzhi.server.service_company.service.EmployeeWithRoleService
import com.wuzhi.server.service_company.service.RoleService
import com.wuzhi.server.service_company.service.RoleWithPermissionService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class RoleServiceImpl(
    private val employeeWithRoleMapper: EmployeeWithRoleMapper,
    private val roleWithPermissionMapper: RoleWithPermissionMapper,
    private val roleMapper: RoleMapper,
    @Lazy private val employeeService: EmployeeService,
    private val employeeWithRoleService: EmployeeWithRoleService,
    private val roleWithPermissionService: RoleWithPermissionService
): RoleService, ServiceImpl<RoleMapper, Role>() {

    @Transactional
    override fun setRoleFromEmployee(dto: RoleEmployeeDTO): ResponseResult {

        // 删除旧的角色用户关联
        val deleteWrapper = KtQueryWrapper(EmployeeWithRole::class.java)
            .eq(EmployeeWithRole::companyId, dto.companyId)
            .eq(EmployeeWithRole::roleId, dto.roleId)
        employeeWithRoleMapper.delete(deleteWrapper)

        // 添加新的角色用户关联
        val linkList = dto.employeeIds.map {
            EmployeeWithRole(
                companyId = dto.companyId,
                employeeId = it,
                roleId = dto.roleId
            )
        }
        // ✅ 使用employeeWithRoleService.saveBatch批量插入，操作EmployeeWithRole实体
        employeeWithRoleService.saveBatch(linkList)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    @Transactional
    override fun setRoleToEmployee(dto: EmployeeRoleDTO): ResponseResult {
        // 删除旧的用户角色关联
        val deleteWrapper = KtQueryWrapper(EmployeeWithRole::class.java)
            .eq(EmployeeWithRole::companyId, dto.companyId)
            .eq(EmployeeWithRole::employeeId, dto.employeeId)
        employeeWithRoleMapper.delete(deleteWrapper)

        // 添加新的用户角色关联
        val linkList = dto.roleIds.map {
            EmployeeWithRole(
                companyId = dto.companyId,
                employeeId = dto.employeeId,
                roleId = it
            )
        }
        // ✅ 使用employeeWithRoleService.saveBatch批量插入，操作EmployeeWithRole实体
        employeeWithRoleService.saveBatch(linkList)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    @Transactional
    override fun setPermission(dto: RolePermissionDTO): ResponseResult {
        // 删除旧的角色权限关联
        val deleteWrapper = KtQueryWrapper(RoleWithPermission::class.java)
            .eq(RoleWithPermission::companyId, dto.companyId)
            .eq(RoleWithPermission::roleId, dto.roleId)
        roleWithPermissionMapper.delete(deleteWrapper)

        // 添加新的角色权限关联
        val linkList = dto.authIds.map {
            RoleWithPermission(
                companyId = dto.companyId,
                roleId = dto.roleId,
                permissionId = it
            )
        }
        // ✅ 使用roleWithPermissionService.saveBatch批量插入，操作RoleWithPermission实体
        roleWithPermissionService.saveBatch(linkList)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun addRole(dto: RoleAddDTO): ResponseResult {
        // 查询当前员工的id
        val empWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, dto.companyId)
            .eq(Employee::userId, UserContext.getUserId())
        val emp = employeeService.getOne(empWrapper)
            ?: return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)
        val operatorEmpId = emp.id

        // 创建Role对象并保存
        val role = Role(
            companyId = dto.companyId,
            name = dto.name,
            description = dto.description,
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            operatorEmpId = operatorEmpId!!
        )
        save(role)
        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    @Transactional
    override fun deleteRole(roleId: String): ResponseResult {
        // 删除角色
        roleMapper.deleteById(roleId)

        // 删除角色用户关联
        val employeeWrapper = KtQueryWrapper(EmployeeWithRole::class.java)
            .eq(EmployeeWithRole::roleId, roleId)
        employeeWithRoleMapper.delete(employeeWrapper)

        // 删除角色权限关联
        val authWrapper = KtQueryWrapper(RoleWithPermission::class.java)
            .eq(RoleWithPermission::roleId, roleId)
        roleWithPermissionMapper.delete(authWrapper)
        return ResponseResult.success(OkMessages.DELETE_SUCCESS)
    }

    override fun updateRole(dto: RoleUpdateDTO): ResponseResult {
        // 如果角色名称和描述都为空，则不更新
        if (dto.name == null && dto.description == null) {
            return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
        }
        // 更新角色
        val wrapper = KtUpdateWrapper(Role::class.java)
            .eq(Role::id, dto.roleId)
            .set(dto.name != null, Role::name, dto.name)
            .set(dto.description != null, Role::description, dto.description)
            .set(Role::updateTime, LocalDateTime.now())
        roleMapper.update(null, wrapper)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun getRoleList(companyId: String): ResponseResult {
        val wrapper = KtQueryWrapper(Role::class.java)
            .eq(Role::companyId, companyId)
        val roleList = list(wrapper)
            .map(RoleVO::fromRole)
        return ResponseResult.success(OkMessages.QUERY_SUCCESS,roleList)
    }

}