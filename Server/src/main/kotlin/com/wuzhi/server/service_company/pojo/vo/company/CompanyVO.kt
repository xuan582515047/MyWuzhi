package com.wuzhi.server.service_company.pojo.vo.company

import com.wuzhi.server.service_company.pojo.po.manage.Mission

class CompanyVO(
    var id: String,
    var name: String,
    var status: String,
    var department: String,
    val missionList: List<ComMissionVO>? = null
) {
    class ComMissionVO(
        val description: String? = null,
        val status: String,
    ){
        companion object{
            fun fromMission(mission: Mission): ComMissionVO {
                return ComMissionVO(
                    description = mission.description,
                    status = mission.status,
                )
            }
        }
    }

}