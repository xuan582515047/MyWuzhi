package com.wuzhi.server.service_company.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.schedule.ScheduleAddDTO
import com.wuzhi.server.service_company.pojo.dto.schedule.ScheduleListDTO
import com.wuzhi.server.service_company.pojo.dto.schedule.ScheduleUpdateDTO
import com.wuzhi.server.service_company.pojo.po.Schedule

interface ScheduleService: IService<Schedule> {

    /**
     * 添加日程
     */
    fun addSchedule(dto: ScheduleAddDTO): ResponseResult

    /**
     * 删除日程
     */
    fun deleteSchedule(id: String): ResponseResult

    /**
     * 更新日程
     */
    fun updateSchedule(dto: ScheduleUpdateDTO): ResponseResult

    /**
     * 获取日程列表
     */
    fun getScheduleList(dto: ScheduleListDTO): ResponseResult
}