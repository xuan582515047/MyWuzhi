package com.wuzhi.server.service_company.controller

import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.schedule.ScheduleAddDTO
import com.wuzhi.server.service_company.pojo.dto.schedule.ScheduleListDTO
import com.wuzhi.server.service_company.pojo.dto.schedule.ScheduleUpdateDTO
import com.wuzhi.server.service_company.service.ScheduleService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/schedule")
class ScheduleController(
    private val scheduleService: ScheduleService
) {

    /**
     * 添加日程
     */
    @PostMapping("/add")
    fun addSchedule(@RequestBody @Valid dto: ScheduleAddDTO): ResponseResult {
        return scheduleService.addSchedule(dto)
    }


    /**
     * 删除日程
     */
    @DeleteMapping("/delete/{id}")
    fun deleteSchedule(@PathVariable id: String): ResponseResult {
        return scheduleService.deleteSchedule(id)
    }

    /**
     * 更新日程
     */
    @PutMapping("/update")
    fun updateSchedule(@RequestBody @Valid dto: ScheduleUpdateDTO): ResponseResult {
        return scheduleService.updateSchedule(dto)
    }

    /**
     * 获取日程列表
     */
    @PostMapping("/list")
    fun getScheduleList(@RequestBody @Valid dto: ScheduleListDTO): ResponseResult {
        return scheduleService.getScheduleList(dto)
    }
}