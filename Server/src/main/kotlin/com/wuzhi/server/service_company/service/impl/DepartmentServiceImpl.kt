package com.wuzhi.server.service_company.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.common.constant.ErrorMessages
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.mapper.DepartmentMapper
import com.wuzhi.server.service_company.pojo.dto.department.DepAddDTO
import com.wuzhi.server.service_company.pojo.dto.department.DepChangeDTO
import com.wuzhi.server.service_company.pojo.dto.department.DepUpdateDTO
import com.wuzhi.server.service_company.pojo.po.manage.CompanyNode
import com.wuzhi.server.service_company.pojo.po.manage.Department
import com.wuzhi.server.service_company.pojo.po.manage.Employee
import com.wuzhi.server.service_company.pojo.vo.department.DepartmentStatisticsVO
import com.wuzhi.server.service_company.pojo.po.manage.Mission
import com.wuzhi.server.service_company.service.CompanyNodeService
import com.wuzhi.server.service_company.service.DepartmentService
import com.wuzhi.server.service_company.service.EmployeeService
import com.wuzhi.server.service_company.service.MissionService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.Modifier
import java.time.LocalDateTime

@Service
class DepartmentServiceImpl(
    @Lazy private val employeeService: EmployeeService,
    @Lazy private val missionService: MissionService,
    private val companyNodeService: CompanyNodeService
): DepartmentService, ServiceImpl<DepartmentMapper, Department>(){

    override fun addDepartment(dto: DepAddDTO): ResponseResult {
        // 查询当前员工的id
        val empWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, dto.companyId)
            .eq(Employee::userId, UserContext.getUserId())
        val emp = employeeService.getOne(empWrapper)
            ?: return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)
        val operatorEmpId = emp.id

        // 计算出部门的层级
        val nodeWrapper = KtQueryWrapper(CompanyNode::class.java)
            .eq(CompanyNode::companyId, dto.companyId)
            .eq(CompanyNode::id, dto.parentId)
        val parentNode = companyNodeService.getOne(nodeWrapper)
        val level = parentNode.level + 1

        // 保存部门
        val department = Department(
            name = dto.name,
            description = dto.description,
            companyId = dto.companyId,
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            operatorEmpId = operatorEmpId!!
        )
        save(department)

        // 保存节点
        val departmentNode = CompanyNode(
            companyId = dto.companyId,
            parentId = parentNode.id,
            targetId = department.id!!,
            name = dto.name,
            level = level,
            type = CompanyNode.TYPE_DEPARTMENT,
        )
        companyNodeService.save(departmentNode)
        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    @Transactional
    override fun deleteDepartment(departmentId: String): ResponseResult {
        // 查询节点对象
        val nodeWrapper = KtQueryWrapper(CompanyNode::class.java)
            .eq(CompanyNode::targetId, departmentId)
        val node = companyNodeService.getOne(nodeWrapper)

        // 如果子级节点大于0，则不允许删除
        val subNodeWrapper = KtQueryWrapper(CompanyNode::class.java)
            .eq(CompanyNode::parentId, node.id)
        val subNodeCount = companyNodeService.count(subNodeWrapper)
        if (subNodeCount > 0) {
            return ResponseResult.error(ErrorMessages.DEPARTMENT_HAS_SUB_DEPARTMENT)
        }

        // 删除部门
        removeById(departmentId)
        employeeService.update(KtUpdateWrapper(Employee::class.java)
            .eq(Employee::departmentId, departmentId)
            .set(Employee::departmentId, null))
        return ResponseResult.success(OkMessages.DELETE_SUCCESS)
    }

    @Transactional
    override fun updateDepartment(dto: DepUpdateDTO): ResponseResult {
        // 查询当前员工的id
        val empWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, dto.companyId)
            .eq(Employee::userId, UserContext.getUserId())
        val emp = employeeService.getOne(empWrapper)
            ?: return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)
        val operatorEmpId = emp.id

        // 更新节点
        val nodeWrapper = KtUpdateWrapper(CompanyNode::class.java)
            .eq(CompanyNode::targetId, dto.depId)
            .set(dto.name != null,CompanyNode::name, dto.name)
        companyNodeService.update(nodeWrapper)

        // 更新部门
        val updateWrapper = KtUpdateWrapper(Department::class.java)
            .eq(Department::id, dto.depId)
            .set(dto.name != null,Department::name, dto.name)
            .set(dto.description != null,Department::description, dto.description)
            .set(Department::updateTime, LocalDateTime.now())
            .set(Department::operatorEmpId, operatorEmpId)
        update(updateWrapper)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun getDepartmentStatistics(departmentId: String): ResponseResult {
        // 查询部门信息
        val department = getById(departmentId) ?: 
            return ResponseResult.error(ErrorMessages.DEPARTMENT_NOT_EXIST)
        
        // 统计员工数量
        val empWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::departmentId, departmentId)
        val totalEmployees = employeeService.count(empWrapper)
        
        // 统计子部门数量
        val depNode = companyNodeService.getOne(
            KtQueryWrapper(CompanyNode::class.java)
                .eq(CompanyNode::companyId, department.companyId)
                .eq(CompanyNode::targetId, departmentId)
        )
        val subDepNodeWrapper = KtQueryWrapper(CompanyNode::class.java)
            .eq(CompanyNode::companyId, department.companyId)
            .eq(CompanyNode::parentId, depNode.id)
        val totalSubDepartments = companyNodeService.count(subDepNodeWrapper)
        
        // 统计任务数量
        val missionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::departmentId, departmentId)
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
                    .eq(Mission::departmentId, departmentId)
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
            .eq(Mission::departmentId, departmentId)
            .eq(Mission::status, Mission.STATUS_COMPLETED)
            .ge(Mission::updateTime, currentMonthStart)
        val completedMissionsThisMonth = missionService.count(completedMissionWrapper)
        
        // 统计进行中任务数量
        val inProgressMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::departmentId, departmentId)
            .eq(Mission::status, Mission.STATUS_IN_PROGRESS)
        val inProgressMissions = missionService.count(inProgressMissionWrapper)
        
        // 统计逾期任务
        val now = java.time.LocalDateTime.now()
        val overdueMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::departmentId, departmentId)
            .ne(Mission::status, Mission.STATUS_COMPLETED)
            .lt(Mission::endTime, now)
        val overdueMissions = missionService.count(overdueMissionWrapper)
        
        val statisticsVO = DepartmentStatisticsVO(
            departmentId = departmentId,
            departmentName = department.name,
            totalEmployees = totalEmployees.toInt(),
            totalSubDepartments = totalSubDepartments.toInt(),
            totalMissions = totalMissions.toInt(),
            missionStatusCount = missionStatusCount,
            completedMissionsThisMonth = completedMissionsThisMonth.toInt(),
            inProgressMissions = inProgressMissions.toInt(),
            overdueMissions = overdueMissions.toInt()
        )
        
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, statisticsVO)
    }

@Transactional
override fun changeDepParent(dto: DepChangeDTO): ResponseResult {
    // 1. 查询当前节点（使用 id，不是 targetId）
    val currentWrapper = KtQueryWrapper(CompanyNode::class.java)
        .eq(CompanyNode::companyId, dto.companyId)
        .eq(CompanyNode::id, dto.nodeId)
    val currentNode = companyNodeService.getOne(currentWrapper)
        ?: return ResponseResult.error(ErrorMessages.DEPARTMENT_NOT_EXIST)
        
        // 2. 检查是否为根节点
        if (currentNode.parentId == null) {
            return ResponseResult.error("根部门不能移动")
        }
        
        // 3. 检查目标父节点是否存在
        if (dto.parentId != null) {
            val parentWrapper = KtQueryWrapper(CompanyNode::class.java)
                .eq(CompanyNode::companyId, dto.companyId)
                .eq(CompanyNode::id, dto.parentId)
            val parentNode = companyNodeService.getOne(parentWrapper)
                ?: return ResponseResult.error(ErrorMessages.DEPARTMENT_NOT_EXIST)
            
            // 4. 检查是否试图移动到自己或自己的子节点
            if (isDepartmentDescendant(dto.nodeId, dto.parentId!!, dto.companyId)) {
                return ResponseResult.error("不能将部门移动到自己的子部门下")
            }
        }
        
        // 5. 计算层级变化量
        val parentWrapper = KtQueryWrapper(CompanyNode::class.java)
            .eq(CompanyNode::companyId, dto.companyId)
            .eq(CompanyNode::id, dto.parentId)
        val newParentNode = companyNodeService.getOne(parentWrapper)
        val newLevel = newParentNode?.level?.plus(1) ?: 0
        val levelDelta = newLevel - currentNode.level
        
        // 6. 更新当前节点的父节点和层级
        val nodeWrapper = KtUpdateWrapper(CompanyNode::class.java)
            .eq(CompanyNode::companyId, dto.companyId)
            .eq(CompanyNode::id, dto.nodeId)
            .set(CompanyNode::parentId, dto.parentId)
            .set(CompanyNode::level, newLevel)
        companyNodeService.update(nodeWrapper)
        
        // 7. 级联更新所有子节点的层级
        if (levelDelta != 0) {
            updateChildrenLevel(currentNode.id!!, levelDelta, dto.companyId)
        }
        
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    /**
     * 级联更新所有子节点的层级
     */
    private fun updateChildrenLevel(parentId: String, levelDelta: Int, companyId: String) {
        // 查询直接子节点
        val childrenWrapper = KtQueryWrapper(CompanyNode::class.java)
            .eq(CompanyNode::companyId, companyId)
            .eq(CompanyNode::parentId, parentId)
        val children = companyNodeService.list(childrenWrapper)
        
        // 更新子节点的层级
        for (child in children) {
            val updateWrapper = KtUpdateWrapper(CompanyNode::class.java)
                .eq(CompanyNode::id, child.id)
                .set(CompanyNode::level, child.level + levelDelta)
            companyNodeService.update(updateWrapper)
            
            // 递归更新子节点的子节点
            updateChildrenLevel(child.id!!, levelDelta, companyId)
        }
    }

    /**
     * 检查 targetParentId 是否是 departmentId 的后代部门
     */
    private fun isDepartmentDescendant(departmentId: String, targetParentId: String, companyId: String): Boolean {
        var currentId: String? = targetParentId
        while (currentId != null) {
            if (currentId == departmentId) {
                return true
            }
            val wrapper = KtQueryWrapper(CompanyNode::class.java)
                .eq(CompanyNode::companyId, companyId)
                .eq(CompanyNode::id, currentId)
            val node = companyNodeService.getOne(wrapper)
            currentId = node?.parentId
        }
        return false
    }
}