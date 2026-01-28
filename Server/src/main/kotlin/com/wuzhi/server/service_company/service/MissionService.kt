package com.wuzhi.server.service_company.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.mission.MissionAddAiDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionAddDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionMarkDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionQueryDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionStatusDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionUpdateDTO
import com.wuzhi.server.service_company.pojo.po.manage.Mission

interface MissionService: IService<Mission> {

    /**
     * 设置任务到部门
     */
    fun addMission(dto: MissionAddDTO): ResponseResult

    /**
     * AI 为部门自动分配任务
     */
    fun addMissionByAi(dto: MissionAddAiDTO): ResponseResult

    /**
     * 删除任务
     */
    fun deleteMission(missionIds: List<String>): ResponseResult

    /**
     * 修改任务
     */
    fun updateMission(dto: MissionUpdateDTO): ResponseResult

    /**
     * 员工或部门更新任务状态
     */
    fun updateMissionStatus(dto: MissionStatusDTO): ResponseResult

    /**
     * 获取任务列表
     */
    fun queryMissionList(dto: MissionQueryDTO): ResponseResult

    /**
     * 获取任务统计信息
     */
    fun getMissionStatistics(companyId: String): ResponseResult
}