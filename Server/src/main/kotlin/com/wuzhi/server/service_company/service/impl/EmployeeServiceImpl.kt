package com.wuzhi.server.service_company.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.common.constant.ErrorMessages
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.PageResult
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.mapper.EmployeeMapper
import com.wuzhi.server.service_company.mapper.EmployeeWithRoleMapper
import com.wuzhi.server.service_company.mapper.PermissionMapper
import com.wuzhi.server.service_company.mapper.RoleWithPermissionMapper
import com.wuzhi.server.service_company.pojo.dto.employee.EmpAddDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpChangeDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpDetailDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpQueryDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpUpdateDTO
import com.wuzhi.server.service_company.pojo.po.auth.EmployeeWithRole
import com.wuzhi.server.service_company.pojo.po.auth.RoleWithPermission
import com.wuzhi.server.service_company.pojo.po.manage.CompanyNode
import com.wuzhi.server.service_company.pojo.po.manage.Employee
import com.wuzhi.server.service_company.pojo.vo.Employee.EmployeeVO
import com.wuzhi.server.service_company.pojo.vo.Employee.EmployeeStatisticsVO
import com.wuzhi.server.service_company.pojo.po.manage.Mission
import com.wuzhi.server.service_company.service.CompanyNodeService
import com.wuzhi.server.service_company.service.EmployeeService
import com.wuzhi.server.service_company.service.MissionService
import com.wuzhi.server.service_company.service.DepartmentService
import com.wuzhi.server.service_user.service.UserService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.Modifier
import java.time.LocalDateTime

@Service
class EmployeeServiceImpl(
    private val employeeWithRoleMapper: EmployeeWithRoleMapper,
    private val roleWithPermissionMapper: RoleWithPermissionMapper,
    private val permissionMapper: PermissionMapper,
    @Lazy private val missionService: MissionService,
    @Lazy private val departmentService: DepartmentService,
    private val companyNodeService: CompanyNodeService,
    private val userService: UserService
): EmployeeService, ServiceImpl<EmployeeMapper, Employee>(){
    override fun getEmpPermission(employeeId: String): ResponseResult {
        // 查询用户所有的roleId
        val roleIds = employeeWithRoleMapper.selectObjs<String>(
            KtQueryWrapper(EmployeeWithRole::class.java)
            .eq(EmployeeWithRole::employeeId, employeeId)
            .select(EmployeeWithRole::roleId)
        )

        // 查询所有的权限
        val permissionSet = HashSet<String>()
        val permissionIds = roleWithPermissionMapper.selectObjs<String>(
            KtQueryWrapper(RoleWithPermission::class.java)
            .`in`(RoleWithPermission::roleId, roleIds)
            .select(RoleWithPermission::permissionId)
        )
        if (permissionIds == null || permissionIds.isEmpty()) {
            return ResponseResult.success(OkMessages.QUERY_SUCCESS, permissionSet)
        }

        val permissionList = permissionMapper.selectBatchIds(permissionIds)
        permissionSet.addAll(permissionList.map { it.code })
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, permissionSet)
    }

    @Transactional
    override fun addEmployee(dto: EmpAddDTO): ResponseResult {
        /*
        //TODO 检查用户是否存在
        val user = userService.getById(dto.userId)
            ?: return ResponseResult.error(ErrorMessages.USER_NOT_EXIST)
        */

        // 检查用户是否已在公司内
        val wrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::userId, dto.userId)
            .eq(Employee::companyId, dto.companyId)
        val count = count(wrapper)
        if (count > 0) {
            return ResponseResult.error(ErrorMessages.USER_EXISTS)
        }

        // 查询部门对应的节点
        val depNode = companyNodeService.getById(dto.departmentNodeId)

        // 保存员工
        val employee = Employee(
            userId = dto.userId,
            companyId = dto.companyId,
            departmentId = depNode.targetId,
            name = dto.name,
            status = dto.status,
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            operatorEmpId = dto.operatorEmpId
        )
        save(employee)

        // 保存节点对象
        val node = CompanyNode(
            companyId = depNode.companyId,
            targetId = employee.id!!,
            parentId = depNode.id,
            name = dto.name,
            level = depNode.level + 1,
            type = CompanyNode.TYPE_EMPLOYEE,
        )
        companyNodeService.save(node)

        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    override fun deleteEmployee(employeeId: String): ResponseResult {
        removeById(employeeId)
        return ResponseResult.success(OkMessages.DELETE_SUCCESS)
    }

    @Transactional
    override fun updateEmployee(dto: EmpUpdateDTO): ResponseResult {
        // 查询当前员工的id
        val empWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, dto.companyId)
            .eq(Employee::userId, UserContext.getUserId())
        val emp = getOne(empWrapper)
            ?: return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)
        val operatorEmpId = emp.id

        // 更新节点
        val nodeWrapper = KtUpdateWrapper(CompanyNode::class.java)
            .eq(CompanyNode::companyId, dto.companyId)
            .eq(CompanyNode::targetId, dto.employeeId)
            .set(CompanyNode::name, dto.name)
        companyNodeService.update(nodeWrapper)

        val wrapper = KtUpdateWrapper(Employee::class.java)
            .eq(Employee::id, dto.employeeId)
            .set(dto.name != null, Employee::name, dto.name)
            .set(dto.status != null, Employee::status, dto.status)
            .set(Employee::updateTime, LocalDateTime.now())
            .set(Employee::operatorEmpId, operatorEmpId)
        update(wrapper)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun queryEmployeeList(dto: EmpQueryDTO): ResponseResult {
        // 分页查询员工
        val wrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, dto.companyId)
            .eq(dto.departmentId != null, Employee::departmentId, dto.departmentId)
            .eq(dto.status != null, Employee::status, dto.status)
            .like(dto.name != null, Employee::name, dto.name)
        if(dto.pageNum != null && dto.pageSize != null){
            // 有分页条件，分页查询
            val pageResult = page(Page(dto.pageNum, dto.pageSize), wrapper)
            val result = PageResult(
                total = pageResult.total,
                data = pageResult.records.map {
                    EmployeeVO.fromEmployee(it, it.departmentId, departmentService.getById(it.departmentId)?.name)
                }
            )
            return ResponseResult.success(OkMessages.QUERY_SUCCESS, result)
        }else{
            // 无分页条件，不分页查询
            val list = list(wrapper)
            val empList = list.map {
                EmployeeVO.fromEmployee(it, it.departmentId, departmentService.getById(it.departmentId)?.name)
            }
            val result = PageResult(
                total = empList.size.toLong(),
                data = empList
            )
            return ResponseResult.success(OkMessages.QUERY_SUCCESS, result)
        }


    }

    override fun getEmployeeStatistics(employeeId: String): ResponseResult {
        // 查询员工信息
        val employee = getById(employeeId) ?: 
            return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)
        
        // 查询部门信息
        val department = departmentService.getById(employee.departmentId)
        
        // 统计任务数量
        val missionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::employeeId, employeeId)
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
                    .eq(Mission::employeeId, employeeId)
                    .eq(Mission::status, statusValue)
                missionStatusCount[statusValue] = missionService.count(statusMissionWrapper)
            }
        
        // 统计本月完成任务数量
        val currentMonthStart = java.time.LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        val completedMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::employeeId, employeeId)
            .eq(Mission::status, Mission.STATUS_COMPLETED)
            .ge(Mission::updateTime, currentMonthStart)
        val completedMissionsThisMonth = missionService.count(completedMissionWrapper)
        
        // 统计进行中任务数量
        val inProgressMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::employeeId, employeeId)
            .eq(Mission::status, Mission.STATUS_IN_PROGRESS)
        val inProgressMissions = missionService.count(inProgressMissionWrapper)
        
        // 统计逾期任务
        val now = java.time.LocalDateTime.now()
        val overdueMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::employeeId, employeeId)
            .ne(Mission::status, Mission.STATUS_COMPLETED)
            .lt(Mission::endTime, now)
        val overdueMissions = missionService.count(overdueMissionWrapper)
        
        // 计算任务完成率
        val completedCount = missionService.count(
            KtQueryWrapper(Mission::class.java)
                .eq(Mission::employeeId, employeeId)
                .eq(Mission::status, Mission.STATUS_COMPLETED)
        )
        val completionRate = if (totalMissions > 0) {
            completedCount.toDouble() / totalMissions.toDouble() * 100
        } else 0.0
        
        val statisticsVO = EmployeeStatisticsVO(
            employeeId = employeeId,
            employeeName = employee.name,
            departmentId = employee.departmentId,
            departmentName = department.name,
            totalMissions = totalMissions.toInt(),
            missionStatusCount = missionStatusCount,
            completedMissionsThisMonth = completedMissionsThisMonth.toInt(),
            inProgressMissions = inProgressMissions.toInt(),
            overdueMissions = overdueMissions.toInt(),
            missionCompletionRate = completionRate
        )
        
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, statisticsVO)
    }

    @Transactional
    override fun changeEmpParent(dto: EmpChangeDTO): ResponseResult {
        // 1. 查询当前员工节点（使用 id，不是 targetId）
        val currentNodeWrapper = KtQueryWrapper(CompanyNode::class.java)
            .eq(CompanyNode::companyId, dto.companyId)
            .eq(CompanyNode::id, dto.nodeId)
        val currentNode = companyNodeService.getOne(currentNodeWrapper)
            ?: return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)
        
        // 2. 查询目标父节点（必须是部门节点）
        if (dto.parentId != null) {
            val parentNodeWrapper = KtQueryWrapper(CompanyNode::class.java)
                .eq(CompanyNode::companyId, dto.companyId)
                .eq(CompanyNode::id, dto.parentId)
            val parentNode = companyNodeService.getOne(parentNodeWrapper)
                ?: return ResponseResult.error(ErrorMessages.DEPARTMENT_NOT_EXIST)
            
            // 3. 检查父节点是否是部门类型
            if (parentNode.type != CompanyNode.TYPE_DEPARTMENT) {
                return ResponseResult.error("员工只能移动到部门下")
            }
        }
        
        // 4. 修改节点关系（使用 id，不是 targetId）
        val nodeWrapper = KtUpdateWrapper(CompanyNode::class.java)
            .eq(CompanyNode::companyId, dto.companyId)
            .eq(CompanyNode::id, dto.nodeId)
            .set(CompanyNode::parentId, dto.parentId)
        companyNodeService.update(nodeWrapper)
        
        // 5. 查询父级节点信息
        val parentNode = if (dto.parentId != null) {
            companyNodeService.getById(dto.parentId)
        } else null
        
        // 6. 修改员工的部门id（修复：使用 currentNode.targetId，不是 dto.nodeId）
        val empWrapper = KtUpdateWrapper(Employee::class.java)
            .eq(Employee::companyId, dto.companyId)
            .eq(Employee::id, currentNode.targetId)
            .set(Employee::departmentId, parentNode?.targetId)
        update(empWrapper)
        
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun getEmployeeDetail(dto: EmpDetailDTO): ResponseResult {
        // 查询节点
        val node = companyNodeService.getById(dto.nodeId)
            ?: return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)
        // 查询员工
        val employee = getById(node.targetId)
            ?: return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)
        // 返回员工详情
        val depName = departmentService.getById(employee.departmentId)?.name ?: "暂无部门"
        val detail = EmployeeVO.fromEmployee(employee, employee.departmentId, depName)
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, detail)
    }

}