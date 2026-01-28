package com.wuzhi.server.service_company.pojo.vo.company

class CompanyStatisticsVO(
    /** 公司ID */
    val companyId: String,
    
    /** 公司名称 */
    val companyName: String,
    
    /** 员工总数 */
    val totalEmployees: Int,
    
    /** 部门总数 */
    val totalDepartments: Int,
    
    /** 任务总数 */
    val totalMissions: Int,
    
    /** 各状态任务数量 */
    val missionStatusCount: Map<String, Long>,
    
    /** 活跃员工数量 */
    val activeEmployees: Int,
    
    /** 本月新增员工数量 */
    val newEmployeesThisMonth: Int,
    
    /** 本月新增任务数量 */
    val newMissionsThisMonth: Int,
    
    /** 逾期任务数量 */
    val overdueMissions: Int
) {
}