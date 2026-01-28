package com.wuzhi.server.service_company.pojo.vo.Employee

class EmployeeStatisticsVO(
    /** 员工ID */
    val employeeId: String,
    
    /** 员工姓名 */
    val employeeName: String,
    
    /** 所属部门ID */
    val departmentId: String?,
    
    /** 所属部门名称 */
    val departmentName: String?,
    
    /** 任务总数 */
    val totalMissions: Int,
    
    /** 各状态任务数量 */
    val missionStatusCount: Map<String, Long>,
    
    /** 本月完成任务数量 */
    val completedMissionsThisMonth: Int,
    
    /** 进行中任务数量 */
    val inProgressMissions: Int,
    
    /** 逾期任务数量 */
    val overdueMissions: Int,
    
    /** 任务完成率 */
    val missionCompletionRate: Double
) {
}