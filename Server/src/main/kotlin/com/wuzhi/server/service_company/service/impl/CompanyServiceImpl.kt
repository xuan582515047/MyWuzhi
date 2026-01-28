package com.wuzhi.server.service_company.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.common.constant.ErrorMessages
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.common.util.UuidUtil
import com.wuzhi.server.service_company.mapper.PermissionMapper
import com.wuzhi.server.service_company.mapper.CompanyMapper
import com.wuzhi.server.service_company.mapper.EmployeeMapper
import com.wuzhi.server.service_company.mapper.EmployeeWithRoleMapper
import com.wuzhi.server.service_company.mapper.RoleWithPermissionMapper
import com.wuzhi.server.service_company.pojo.dto.company.CompanyAddDTO
import com.wuzhi.server.service_company.pojo.dto.company.CompanyAuthDTO
import com.wuzhi.server.service_company.pojo.dto.company.CompanyUpdateDTO
import com.wuzhi.server.service_company.pojo.po.auth.EmployeeWithRole
import com.wuzhi.server.service_company.pojo.po.auth.Role
import com.wuzhi.server.service_company.pojo.po.auth.RoleWithPermission
import com.wuzhi.server.service_company.pojo.po.manage.Company
import com.wuzhi.server.service_company.pojo.po.manage.CompanyNode
import com.wuzhi.server.service_company.pojo.po.manage.Department
import com.wuzhi.server.service_company.pojo.po.manage.Employee
import com.wuzhi.server.service_company.pojo.po.manage.Mission
import com.wuzhi.server.service_company.pojo.vo.PermissionTreeVO
import com.wuzhi.server.service_company.pojo.vo.company.CompanyDetailVO
import com.wuzhi.server.service_company.pojo.vo.company.CompanyVO
import com.wuzhi.server.service_company.pojo.vo.company.CompanyStatisticsVO
import com.wuzhi.server.service_company.pojo.vo.department.DepartmentTreeVO
import com.wuzhi.server.service_company.service.CompanyNodeService
import com.wuzhi.server.service_company.service.CompanyService
import com.wuzhi.server.service_company.service.DepartmentService
import com.wuzhi.server.service_company.service.EmployeeService
import com.wuzhi.server.service_company.service.MissionService
import com.wuzhi.server.service_company.service.RoleService
import com.wuzhi.server.service_user.service.UserService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.Modifier
import java.time.LocalDateTime
import java.util.UUID

@Service
class CompanyServiceImpl(
    private val employeeMapper: EmployeeMapper,
    private val employeeWithRoleMapper: EmployeeWithRoleMapper,
    private val roleService: RoleService,
    private val roleWithPermissionMapper: RoleWithPermissionMapper,
    private val permissionMapper: PermissionMapper,
    private val departmentService: DepartmentService,
    @Lazy private val missionService: MissionService,
    private val employeeService: EmployeeService,
    private val userService: UserService,
    private val companyNodeService: CompanyNodeService
    ): CompanyService, ServiceImpl<CompanyMapper, Company>() {
    //TODO 这里需要很多验证，现在简化处理了

    override fun getCompanyPermission(companyId: String): ResponseResult {
        val permissionList = permissionMapper.selectList(null)

        // 按parentId分组
        val groupedItems = permissionList.groupBy { it.parentId }

        // 递归构建子树
        fun buildTree(parentId: String?): List<PermissionTreeVO> {
            val children = groupedItems[parentId]
                ?: return emptyList()

            return children.map { permission ->
                PermissionTreeVO(
                    id = permission.id!!,
                    code = permission.code,
                    name = permission.name,
                    description = permission.description,
                    children = buildTree(permission.id) // 递归构建子树
                )
            }
        }

        val result = buildTree(null)
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, result)
    }

    override fun getCompanyDetail(companyId: String): ResponseResult {
        val company = getById(companyId)
        if (company == null){
            return ResponseResult.error(ErrorMessages.COMPANY_NOT_EXIST)
        }
        val vo = CompanyDetailVO.fromCompany(company)
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, vo)
    }

    override fun authCompany(dto: CompanyAuthDTO): ResponseResult {
        // 查询出Company对象
        val company = getById(dto.companyId) ?:
            return ResponseResult.error(ErrorMessages.COMPANY_NOT_EXIST)

        // 补全公司信息
        company.name = dto.name
        company.creditCode = dto.creditCode
        company.industry = dto.industry
        company.legalRepresentative = dto.legalRepresentative
        company.approveStatus = Company.APPROVE_STATUS_APPROVED
        updateById(company)
        return ResponseResult.success(OkMessages.AUTH_SUCCESS)
    }

    @Transactional
    override fun addCompany(dto: CompanyAddDTO): ResponseResult {
        // 添加Company
        val company = Company(
            name = dto.name,
            shortName = dto.shortName,
            description = dto.description,
            phone = dto.phone,
            email = dto.email,
            status = Company.STATUS_ACTIVE,
            approveStatus = Company.APPROVE_STATUS_NOT_APPROVED,
            industry = dto.industry,
            createTime = LocalDateTime.now()
        )
        save(company)

        // 查询用户，UUID生成员工ID
        val empId = UuidUtil.generateUuid()
        val user = userService.getById(UserContext.getUserId())
            ?: return ResponseResult.error(ErrorMessages.USER_NOT_EXIST)

        // 添加初始部门
        val department = Department(
            name = company.name,
            description = company.description,
            companyId = company.id!!,
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            operatorEmpId = empId
        )
        departmentService.save(department)

        // 添加初始员工
        val employee = Employee(
            id = empId,
            userId = UserContext.getUserId(),
            name = user.name,
            companyId = company.id!!,
            departmentId = department.id!!,
            status = Employee.STATUS_ACTIVE,
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            operatorEmpId = empId
        )
        employeeService.save(employee)

        // 生成部门node的ID
        val departmentNodeId = UuidUtil.generateUuid()

        // 添加初始员工和部门节点
        val departmentNode = CompanyNode(
            id = departmentNodeId,
            parentId = null,
            companyId = company.id!!,
            level = 0,
            targetId = department.id!!,
            name = department.name,
            type = CompanyNode.TYPE_DEPARTMENT
        )
        val employeeNode = CompanyNode(
            parentId = departmentNode.id,
            companyId = company.id!!,
            level = departmentNode.level + 1,
            targetId = employee.id!!,
            name = employee.name,
            type = CompanyNode.TYPE_EMPLOYEE
        )
        companyNodeService.saveBatch(listOf(departmentNode, employeeNode))

        // 添加初始角色
        val role = Role(
            companyId = company.id!!,
            name = "超级管理员",
            description = "超级管理员，拥有所有初始权限",
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            operatorEmpId = empId
        )
        roleService.save(role)

        // 查询所有的权限
        val permissionList = permissionMapper.selectList(null)

        // 添加初始角色权限关联
        permissionList.forEach { permission ->
            val roleWithPermission = RoleWithPermission(
                companyId = company.id!!,
                roleId = role.id!!,
                permissionId = permission.id!!,
            )
            roleWithPermissionMapper.insert(roleWithPermission)
        }

        // 添加初始员工角色关联
        val roleLink = EmployeeWithRole(
            companyId = company.id!!,
            employeeId = employee.id,
            roleId = role.id!!,
        )
        employeeWithRoleMapper.insert(roleLink)

        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    @Transactional
    override fun deleteCompany(companyId: String): ResponseResult {
        // 删除EmployeeWithRole
        val employeeWithRoleWrapper = KtQueryWrapper(EmployeeWithRole::class.java)
            .eq(EmployeeWithRole::companyId, companyId)
        employeeWithRoleMapper.delete(employeeWithRoleWrapper)

        // 删除RoleWithPermission
        val roleWithPermissionWrapper = KtQueryWrapper(RoleWithPermission::class.java)
            .eq(RoleWithPermission::companyId, companyId)
        roleWithPermissionMapper.delete(roleWithPermissionWrapper)

        // 删除Role
        val roleWrapper = KtQueryWrapper(Role::class.java)
            .eq(Role::companyId, companyId)
        roleService.remove(roleWrapper)

        // 删除Company
        removeById(companyId)

        // 删除Employee
        val employeeWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, companyId)
        employeeMapper.delete(employeeWrapper)
        return ResponseResult.success(OkMessages.DELETE_SUCCESS)

    }

    override fun updateCompany(dto: CompanyUpdateDTO): ResponseResult {
        // 查询出Company对象
        val company = getById(dto.companyId) ?:
            return ResponseResult.error(ErrorMessages.COMPANY_NOT_EXIST)

        // 更新Company
        company.name = dto.name ?: company.name
        company.shortName = dto.shorNme ?: company.shortName
        company.description = dto.description ?: company.description
        company.phone = dto.phone ?: company.phone
        company.email = dto.email ?: company.email
        company.industry = dto.industry ?: company.industry
        company.status = dto.status ?: company.status
        updateById(company)

        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun getCompanyList(): ResponseResult {
        // 根据用户 查询到所有的员工对象，然后获取到所有的公司id
        val empWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::userId, UserContext.getUserId())
        val empList = employeeMapper.selectList(empWrapper)

        // 组装CompanyVO的list
        val voList = empList.map {
            // 查询对应的公司
            val company = getById(it.companyId)
            // 查询对应的部门
            val department = departmentService.getById(it.departmentId)
            // 查询任务列表
            val wrapper  = KtQueryWrapper(Mission::class.java)
                .eq(Mission::companyId, it.companyId)
                .eq(Mission::departmentId, it.departmentId)
                .eq(Mission::employeeId, it.id)
            val missionList = missionService.list(wrapper)
                .map(CompanyVO.ComMissionVO::fromMission)
            CompanyVO(
                id = company.id!!,
                name = company.name,
                status = company.status,
                department = department.name,
                missionList = missionList
            )
        }
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, voList)
    }

    override fun getCompanyStatistics(companyId: String): ResponseResult {
        // 查询公司信息
        val company = getById(companyId) ?: 
            return ResponseResult.error(ErrorMessages.COMPANY_NOT_EXIST)
        
        // 统计员工数量
        val empWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, companyId)
        val totalEmployees = employeeService.count(empWrapper)
        
        val activeEmpWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, companyId)
            .eq(Employee::status, Employee.STATUS_ACTIVE)
        val activeEmployees = employeeService.count(activeEmpWrapper)
        
        // 统计本月新增员工
        val currentMonthStart = java.time.LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        val newEmpWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, companyId)
            .ge(Employee::createTime, currentMonthStart)
        val newEmployeesThisMonth = employeeService.count(newEmpWrapper)
        
        // 统计部门数量
        val depWrapper = KtQueryWrapper(com.wuzhi.server.service_company.pojo.po.manage.Department::class.java)
            .eq(com.wuzhi.server.service_company.pojo.po.manage.Department::companyId, companyId)
        val totalDepartments = departmentService.count(depWrapper)
        
        // 统计任务数量
        val missionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::companyId, companyId)
        val totalMissions = missionService.count(missionWrapper)
        
        // 统计各状态任务数量
        val missionStatusCount = mutableMapOf<String, Long>()
        Mission::class.java.getDeclaredFields()
            .filter { it.name.startsWith("STATUS_") &&
                    Modifier.isStatic(it.modifiers) &&
                    Modifier.isFinal(it.modifiers)
            }
            .forEach { field ->
                field.isAccessible = true
                val statusValue = field.get(null) as String
                val statusMissionWrapper = KtQueryWrapper(Mission::class.java)
                    .eq(Mission::companyId, companyId)
                    .eq(Mission::status, statusValue)
                missionStatusCount[statusValue] = missionService.count(statusMissionWrapper)
            }
        
        // 统计本月新增任务
        val newMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::companyId, companyId)
            .ge(Mission::createTime, currentMonthStart)
        val newMissionsThisMonth = missionService.count(newMissionWrapper)
        
        // 统计逾期任务
        val now = java.time.LocalDateTime.now()
        val overdueMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::companyId, companyId)
            .ne(Mission::status, Mission.STATUS_COMPLETED)
            .lt(Mission::endTime, now)
        val overdueMissions = missionService.count(overdueMissionWrapper)
        
        val statisticsVO = CompanyStatisticsVO(
            companyId = companyId,
            companyName = company.name,
            totalEmployees = totalEmployees.toInt(),
            totalDepartments = totalDepartments.toInt(),
            totalMissions = totalMissions.toInt(),
            missionStatusCount = missionStatusCount,
            activeEmployees = activeEmployees.toInt(),
            newEmployeesThisMonth = newEmployeesThisMonth.toInt(),
            newMissionsThisMonth = newMissionsThisMonth.toInt(),
            overdueMissions = overdueMissions.toInt()
        )
        
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, statisticsVO)
    }

    override fun getCompanyTree(companyId: String): ResponseResult {
        // 查询出员工
        val empWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, companyId)
            .eq(Employee::userId, UserContext.getUserId())
        val employee = employeeMapper.selectOne(empWrapper)
            ?: return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)

        // 员工部门判断
        if(employee.departmentId == null){
            return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_IN_DEPARTMENT)
        }

        // 获取树，并返回
        val tree = companyNodeService.getTree(companyId, employee.departmentId)
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, tree)
    }
}