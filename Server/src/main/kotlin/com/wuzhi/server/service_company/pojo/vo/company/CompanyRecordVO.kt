package com.wuzhi.server.service_company.pojo.vo.company

import com.wuzhi.server.service_company.pojo.vo.Employee.EmployeeVO

class CompanyRecordVO(
    val id: String,
    val type: String,
    val operateType: String,
    // 这个的类型会根据type变化，都是用的对应的VO（展开后，在详情展示）
    val target: Any? = null,
    val message: String,
    //（展开后，在详情展示）
    val operatorEmployee: EmployeeVO,
    val operateTime: String
) {
}