package com.wuzhi.server.service_company.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.mapper.ScheduleMapper
import com.wuzhi.server.service_company.pojo.dto.schedule.ScheduleAddDTO
import com.wuzhi.server.service_company.pojo.dto.schedule.ScheduleListDTO
import com.wuzhi.server.service_company.pojo.dto.schedule.ScheduleUpdateDTO
import com.wuzhi.server.service_company.pojo.po.Schedule
import com.wuzhi.server.service_company.pojo.vo.ScheduleVO
import com.wuzhi.server.service_company.service.ScheduleService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ScheduleServiceImpl: ScheduleService, ServiceImpl<ScheduleMapper, Schedule>() {
    override fun addSchedule(dto: ScheduleAddDTO): ResponseResult {
        val schedule = Schedule(
            companyId = dto.companyId,
            userId = UserContext.getUserId(),
            title = dto.title,
            description = dto.description,
            time = LocalDateTime.parse(dto.time),
        )
        save(schedule)
        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    override fun deleteSchedule(id: String): ResponseResult {
        removeById(id)
        return ResponseResult.success(OkMessages.DELETE_SUCCESS)
    }

    override fun updateSchedule(dto: ScheduleUpdateDTO): ResponseResult {
        val wrapper = KtUpdateWrapper(Schedule::class.java)
            .eq(Schedule::id, dto.id)
            .set(dto.title != null, Schedule::title, dto.title)
            .set(dto.description != null, Schedule::description, dto.description)
        if (dto.time != null) {
            wrapper.set(Schedule::time, LocalDateTime.parse(dto.time))
        }
        update(wrapper)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun getScheduleList(dto: ScheduleListDTO): ResponseResult {
        // 1. 解析日期字符串为 LocalDateTime
        val startTime = LocalDateTime.parse(dto.startDate)
        val endTime = LocalDateTime.parse(dto.endDate)

        // 2. 查询时间范围内的所有日程
        val wrapper = KtQueryWrapper(Schedule::class.java)
            .eq(Schedule::companyId, dto.companyId)
            .between(Schedule::time, startTime, endTime)
            .orderByAsc(Schedule::time)

        val scheduleList = list(wrapper)

        // 3. 按天分组
        val groupedByDate = scheduleList.groupBy { schedule ->
            schedule.time.toLocalDate()
        }

        // 4. 转换为 ScheduleVO 列表
        val result = groupedByDate.map { (date, schedules) ->
            ScheduleVO(
                date = date.format(DateTimeFormatter.ISO_DATE),
                scheduleList = schedules.map { schedule ->
                    ScheduleVO.ScheduleItem(
                        id = schedule.id!!,
                        title = schedule.title ?: "",
                        description = schedule.description ?: "",
                        time = schedule.time.format(DateTimeFormatter.ISO_DATE_TIME)
                    )
                }
            )
        }.sortedBy { it.date } // 按日期排序

        return ResponseResult.success("获取日程列表成功", result)

    }
}