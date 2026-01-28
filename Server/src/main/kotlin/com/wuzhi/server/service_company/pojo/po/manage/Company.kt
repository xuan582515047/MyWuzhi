package com.wuzhi.server.service_company.pojo.po.manage

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("data_company")
data class Company(

    // 主键
    @TableId("id")
    var id: String? = null,

    // 公司简称
    @TableField("short_name")
    var shortName: String? = null,

    // 公司名称
    @TableField("name")
    var name: String,

    // 描述
    @TableField("description")
    var description: String? = null,

    // 统一社会信用代码
    @TableField("credit_code")
    var creditCode: String? = null,

    // 法人代表
    @TableField("legal_representative")
    var legalRepresentative: String? = null,

    // 联系电话
    @TableField("phone")
    var phone: String,

    // 邮箱
    @TableField("email")
    var email: String,

    // 行业
    @TableField("industry")
    var industry: String,

    // 状态
    @TableField("status")
    var status: String,

    // 认证状态
    @TableField("approve_status")
    var approveStatus: String,

    // 创建时间
    @TableField("create_time")
    var createTime: LocalDateTime,
){
    companion object {
        const val STATUS_ACTIVE = "active"
        const val STATUS_INACTIVE = "inactive"

        const val APPROVE_STATUS_APPROVED = "approved"
        const val APPROVE_STATUS_NOT_APPROVED = "not_approved"
    }
}