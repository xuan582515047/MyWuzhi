package com.wuzhi.server.service_company.pojo.vo.mission

class MissionStatisticsVO(
    /** 公司ID */
    val companyId: String,
    
    /** 任务总数 */
    val totalMissions: Int,
    
    /** 各状态任务数量 */
    val missionStatusCount: Map<String, Long>,
    
    /** 本月新增任务数量 */
    val newMissionsThisMonth: Int,
    
    /** 本月完成任务数量 */
    val completedMissionsThisMonth: Int,
    
    /** 逾期任务数量 */
    val overdueMissions: Int,
    
    /** 高优先级任务数量 */
    val highPriorityMissions: Int,
    
    /** 各部门任务分布 */
    val departmentMissionCount: Map<String, Int>
) {
}