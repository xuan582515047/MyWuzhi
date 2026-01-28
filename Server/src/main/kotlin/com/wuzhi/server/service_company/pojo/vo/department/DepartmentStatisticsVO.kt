package com.wuzhi.server.service_company.pojo.vo.department

class DepartmentStatisticsVO(
    /** 部门ID */
    val departmentId: String,
    
    /** 部门名称 */
    val departmentName: String,
    
    /** 员工总数 */
    val totalEmployees: Int,
    
    /** 子部门数量 */
    val totalSubDepartments: Int,
    
    /** 任务总数 */
    val totalMissions: Int,
    
    /** 各状态任务数量 */
    val missionStatusCount: Map<String, Long>,
    
    /** 本月完成任务数量 */
    val completedMissionsThisMonth: Int,
    
    /** 进行中任务数量 */
    val inProgressMissions: Int,
    
    /** 逾期任务数量 */
    val overdueMissions: Int
) {
}