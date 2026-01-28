package com.wuzhi.server.service_company.controller

import com.wuzhi.server.common.constant.BaseUrlConstant
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.mission.MissionAddAiDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionAddDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionDeleteDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionQueryDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionStatisticsDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionStatusDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionUpdateDTO
import com.wuzhi.server.service_company.pojo.po.manage.CompanyRecord
import com.wuzhi.server.service_company.service.CompanyRecordService
import com.wuzhi.server.service_company.service.MissionService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mission")
class MissionController(
    private val missionService: MissionService,
    private val companyRecordService: CompanyRecordService
) {

    /**
     * 为部门或员工分配任务
     */
    @PostMapping("/add")
    fun addMission(@RequestBody @Valid dto: MissionAddDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_MISSION,
            operateType = CompanyRecord.OPERATE_TYPE_ADD,
            targetId = dto.companyId,
            message = "为部门分配任务：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return missionService.addMission(dto)
    }


    /**
     * AI 为部门自动分配任务
     */
    @PostMapping("/add/ai")
    fun addMissionByAi(@RequestBody @Valid dto: MissionAddAiDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_MISSION,
            operateType = CompanyRecord.OPERATE_TYPE_ADD,
            targetId = dto.companyId,
            message = "使用AI为部门分配任务：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return missionService.addMissionByAi(dto)
    }

    /**
     * 删除任务
     */
    @PostMapping("/delete")
    fun deleteMission(dto: MissionDeleteDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_MISSION,
            operateType = CompanyRecord.OPERATE_TYPE_DELETE,
            targetId = dto.companyId,
            message = "删除任务：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return missionService.deleteMission(dto.missionIds)
    }

    /**
     * 修改任务
     */
    @PutMapping("/update")
    fun updateMission(@RequestBody @Valid dto: MissionUpdateDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_MISSION,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "修改任务：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return missionService.updateMission(dto)
    }

    /**
     * 员工或部门更新任务状态
     */
    @PutMapping("/update/status")
    fun updateMissionStatus(@RequestBody @Valid dto: MissionStatusDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_MISSION,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "更新任务状态：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return missionService.updateMissionStatus(dto)
    }

    /**
     * 获取任务列表
     * 1. 若分页参数为空，则返回所有
     * 2. 若部门ID为空，则代表所有直属部门
     * 3. 若部门ID不为空，则直接代表指定部门
     */
    @PostMapping("/list")
    fun getMissionList(@RequestBody @Valid dto: MissionQueryDTO): ResponseResult {
        return missionService.queryMissionList(dto)
    }

    /**
     * 获取任务统计信息
     */
    @PostMapping("/statistics")
    fun getMissionStatistics(@RequestBody @Valid dto: MissionStatisticsDTO): ResponseResult {
        return missionService.getMissionStatistics(dto.companyId)
    }
}