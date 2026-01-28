package com.wuzhi.server.service_company.pojo.vo.mission

class MissionVO(
    val id: String,
    val name: String,
    val description: String? = null,
    val startTime: String,
    val endTime: String,
    val status: String,
    val departmentId: String,
    val departmentName: String,
    val employeeName: String,
    val operatorEmpName: String,
    val publisherEmpName: String,
    val marked: Boolean,
    val createTime: String,
    val updateTime: String,
) {
    override fun toString(): String {
        val sb = StringBuilder().append("任务名称：${name}，")
            .append("任务描述：${description}，")
            .append("任务状态：${status}，")
            .append("任务开始时间：${startTime}，")
            .append("任务结束时间：${endTime}，")
            .append("任务发布人：${publisherEmpName}，")
            .append("任务所属部门：${departmentName}")
            .append("任务执行人：${employeeName}")
        return sb.toString()
    }
}