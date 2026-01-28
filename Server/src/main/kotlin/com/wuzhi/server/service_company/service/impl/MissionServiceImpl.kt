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
import com.wuzhi.server.service_chat.service.ChatService
import com.wuzhi.server.service_company.mapper.MissionMapper
import com.wuzhi.server.service_company.pojo.dto.mission.MissionAddAiDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionAddDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionQueryDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionStatusDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionUpdateDTO
import com.wuzhi.server.service_company.pojo.po.manage.Employee
import com.wuzhi.server.service_company.pojo.po.manage.Mission
import com.wuzhi.server.service_company.pojo.vo.mission.MissionVO
import com.wuzhi.server.service_company.pojo.vo.mission.MissionStatisticsVO
import com.wuzhi.server.service_company.service.DepartmentService
import com.wuzhi.server.service_company.service.EmployeeService
import com.wuzhi.server.service_company.service.MissionService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.lang.reflect.Modifier
import java.time.LocalDateTime

@Service
class MissionServiceImpl(
    @Lazy private val departmentService: DepartmentService,
    @Lazy private val employeeService: EmployeeService,
    private val chatService: ChatService
): MissionService, ServiceImpl<MissionMapper, Mission>(){
    override fun addMission(dto: MissionAddDTO): ResponseResult {
        // 查询员工
        val empWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::companyId, dto.companyId)
            .eq(Employee::userId, UserContext.getUserId())
        val emp = employeeService.getOne(empWrapper)
            ?: return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)

        // 添加任务
        val mission = Mission(
            companyId = dto.companyId,
            name = dto.name,
            description = dto.description,
            status = dto.status,
            departmentId = dto.departmentId,
            employeeId = dto.employeeId,
            publisherEmpId = emp.id!!,
            operatorEmpId = emp.id,
            marked = false,
            startTime = LocalDateTime.parse(dto.startTime),
            endTime = LocalDateTime.parse(dto.endTime),
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now()
        )
        save(mission)
        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    override fun addMissionByAi(dto: MissionAddAiDTO): ResponseResult {
        // 获取部门信息
        val department = departmentService.getById(dto.departmentId)

        // 直接查询任务列表，获取前100条
        val pageData = queryMissionList(MissionQueryDTO(
            companyId = dto.companyId,
            page = 1,
            pageSize = 100
        )).data as PageResult
        val missions = pageData.data as List<MissionVO>

        // 调用AI
        val result = chatService.callMission(missions, department, dto.employeeId)
        return ResponseResult.success(OkMessages.ADD_SUCCESS, result)
    }

    override fun deleteMission(missionIds: List<String>): ResponseResult {
        removeByIds(missionIds)
        return ResponseResult.success(OkMessages.DELETE_SUCCESS)
    }

    override fun updateMission(dto: MissionUpdateDTO): ResponseResult {
        // 更新任务
        val wrapper = KtUpdateWrapper(Mission::class.java)
            .eq(Mission::id, dto.missionId)
            .eq(Mission::companyId, dto.companyId)
            .set(dto.name != null, Mission::name, dto.name)
            .set(dto.marked != null, Mission::marked, dto.marked)
            .set(dto.description != null, Mission::description, dto.description)
            .set(dto.status != null, Mission::status, dto.status)
            .set(dto.departmentId != null, Mission::departmentId, dto.departmentId)
            .set(dto.employeeId != null, Mission::employeeId, dto.employeeId)
            .set(dto.publisherEmpId != null, Mission::publisherEmpId, dto.publisherEmpId)
            .set(Mission::updateTime, LocalDateTime.now())
        if (dto.startTime != null){
            wrapper.set(Mission::startTime, LocalDateTime.parse(dto.startTime))
        }
        if (dto.endTime != null){
            wrapper.set(Mission::endTime, LocalDateTime.parse(dto.endTime))
        }
        update(wrapper)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun updateMissionStatus(dto: MissionStatusDTO): ResponseResult {
        val wrapper = KtUpdateWrapper(Mission::class.java)
            .eq(Mission::id, dto.missionId)
            .eq(Mission::companyId, dto.companyId)
            .set(Mission::status, dto.status)
            .set(Mission::operatorEmpId, dto.operatorEmpId)
            .set(Mission::updateTime, LocalDateTime.now())
        update(wrapper)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun queryMissionList(dto: MissionQueryDTO): ResponseResult {
        // 查询当前用户的Employee对象
        val empWrapper = KtQueryWrapper(Employee::class.java)
            .eq(Employee::userId, UserContext.getUserId())
            .eq(Employee::companyId, dto.companyId)
        val emp = employeeService.getOne(empWrapper) ?:
            return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)

        // 分页查询任务
        val missionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::companyId, dto.companyId)
            .like(dto.name != null, Mission::name, dto.name)
            .eq(dto.status != null, Mission::status, dto.status)
            .eq(dto.publisherEmpId != null, Mission::publisherEmpId, dto.publisherEmpId)
            .eq(dto.marked != null, Mission::marked, dto.marked)
            .eq(dto.employeeId != null, Mission::employeeId, dto.employeeId)

        // 如果传入了departmentId，则只查询该部门的任务
        // 如果传入了employeeId，则查询该员工所在部门的任务
        // 否则查询用户当前部门的任务
        if(dto.departmentId != null){
            missionWrapper.eq(Mission::departmentId, dto.departmentId)
        }else if(dto.employeeId != null){
            // 查询该员工的部门ID
            val employee = employeeService.getById(dto.employeeId)
            if (employee != null && employee.departmentId != null) {
                missionWrapper.eq(Mission::departmentId, employee.departmentId)
            }
        }else{
            missionWrapper.eq(Mission::departmentId, emp.departmentId)
        }

        // 如果传入了startTime和endTime，则查询startTime和endTime之间的任务
        if (dto.startTime != null){
            missionWrapper.ge(Mission::startTime, LocalDateTime.parse(dto.startTime))
        }
        if (dto.endTime != null){
            missionWrapper.le(Mission::endTime, LocalDateTime.parse(dto.endTime))
        }
        fun getMissionVO(it: Mission): MissionVO{
            // 查询各个字段的信息
            val employeeName = employeeService.getById(it.employeeId)?.name
            val publisherEmpName = employeeService.getById(it.publisherEmpId)?.name
            val operatorEmpName = employeeService.getById(it.operatorEmpId)?.name
            val departmentName = departmentService.getById(it.departmentId)?.name
            // 封装VO对象
            return MissionVO(
                id = it.id!!,
                name = it.name,
                description = it.description,
                status = it.status,
                departmentId = it.departmentId,
                departmentName = departmentName ?: "无",
                employeeName = employeeName ?: "无",
                publisherEmpName = publisherEmpName ?: "无",
                operatorEmpName = operatorEmpName ?: "无",
                marked = it.marked,
                startTime = it.startTime.toString(),
                endTime = it.endTime.toString(),
                createTime = it.createTime.toString(),
                updateTime = it.updateTime.toString()
            )
        }

        // 如果传入了page和pageSize，则分页查询，否则查询所有任务
        if (dto.page != null && dto.pageSize != null){
            val pageData = page(Page(dto.page, dto.pageSize), missionWrapper)
            val pageResult = pageData.records.map{getMissionVO(it)}
            val result = PageResult(
                total = pageData.total,
                data = pageResult
            )
            return ResponseResult.success(OkMessages.QUERY_SUCCESS, result)
        }else{
            val listData = list(missionWrapper)
            val listResult = listData.map{getMissionVO(it)}
            val result = PageResult(
                total = listData.size.toLong(),
                data = listResult
            )
            return ResponseResult.success(OkMessages.QUERY_SUCCESS, result)
        }

    }

    override fun getMissionStatistics(companyId: String): ResponseResult {
        // 统计任务数量
        val missionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::companyId, companyId)
        val totalMissions = count(missionWrapper)
        
        // 统计各状态任务数量
        val missionStatusCount = mutableMapOf<String, Long>()
        Mission::class.java.declaredFields
            .filter {
                it.name.startsWith("STATUS_") &&
                        Modifier.isStatic(it.modifiers) &&
                        Modifier.isFinal(it.modifiers)
            }
            .forEach { field ->
                field.isAccessible = true
                val statusValue = field.get(null) as String
                val statusMissionWrapper = KtQueryWrapper(Mission::class.java)
                    .eq(Mission::companyId, companyId)
                    .eq(Mission::status, statusValue)
                missionStatusCount[statusValue] = count(statusMissionWrapper)
            }
        
        // 统计本月新增任务
        val currentMonthStart = java.time.LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        val newMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::companyId, companyId)
            .ge(Mission::createTime, currentMonthStart)
        val newMissionsThisMonth = count(newMissionWrapper)
        
        // 统计本月完成任务
        val completedMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::companyId, companyId)
            .eq(Mission::status, Mission.STATUS_COMPLETED)
            .ge(Mission::updateTime, currentMonthStart)
        val completedMissionsThisMonth = count(completedMissionWrapper)
        
        // 统计逾期任务
        val now = java.time.LocalDateTime.now()
        val overdueMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::companyId, companyId)
            .ne(Mission::status, Mission.STATUS_COMPLETED)
            .lt(Mission::endTime, now)
        val overdueMissions = count(overdueMissionWrapper)
        
        // 统计高优先级任务（假设标记的任务为高优先级）
        val highPriorityMissionWrapper = KtQueryWrapper(Mission::class.java)
            .eq(Mission::companyId, companyId)
            .eq(Mission::marked, true)
        val highPriorityMissions = count(highPriorityMissionWrapper)
        
        // 统计各部门任务分布
        val departmentMissionCount = mutableMapOf<String, Int>()
        val departmentList = departmentService.list(
            KtQueryWrapper(com.wuzhi.server.service_company.pojo.po.manage.Department::class.java)
                .eq(com.wuzhi.server.service_company.pojo.po.manage.Department::companyId, companyId)
        )
        departmentList.forEach { dept ->
            val deptMissionCount = count(
                KtQueryWrapper(Mission::class.java)
                    .eq(Mission::companyId, companyId)
                    .eq(Mission::departmentId, dept.id)
            )
            departmentMissionCount[dept.name] = deptMissionCount.toInt()
        }
        
        val statisticsVO = MissionStatisticsVO(
            companyId = companyId,
            totalMissions = totalMissions.toInt(),
            missionStatusCount = missionStatusCount,
            newMissionsThisMonth = newMissionsThisMonth.toInt(),
            completedMissionsThisMonth = completedMissionsThisMonth.toInt(),
            overdueMissions = overdueMissions.toInt(),
            highPriorityMissions = highPriorityMissions.toInt(),
            departmentMissionCount = departmentMissionCount
        )
        
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, statisticsVO)
    }
}