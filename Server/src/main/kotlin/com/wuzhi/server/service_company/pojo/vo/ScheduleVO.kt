package com.wuzhi.server.service_company.pojo.vo

class ScheduleVO(
    val date: String,
    val scheduleList: List<ScheduleItem>
) {
    class ScheduleItem(
        val id: String,
        val title: String,
        val description: String,
        val time: String
    ) {

    }
}