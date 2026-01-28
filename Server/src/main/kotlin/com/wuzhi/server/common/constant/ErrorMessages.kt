package com.wuzhi.server.common.constant

class ErrorMessages {
    companion object {
        const val DECISION_TREE_NOT_FOUND = "决策树不存在"
        const val NO_AUTHORIZATION = "没有权限"
        const val REQUEST_DATA_LOSS = "请求数据缺失"

        // Auth 报错
        const val USER_NOT_FOUND = "用户不存在"
        const val PASSWORD_ERROR = "密码错误"
        const val USER_ALREADY_EXIST = "用户已存在"
        const val REFRESH_TOKEN_ERROR = "刷新Token错误"
        
        // Database 报错
        const val LOCAL_BUT_NO_FILE = "选择本地数据库但没有上传文件"
        const val TEXT_BUT_NO_TEXT = "选择文本数据库但没有上传文本"
        const val UNKNOWN_DATABASE_TYPE = "未知的数据库类型"
        const val DATABASE_NOT_EXIST = "数据库不存在"
        const val DATABASE_NODE_NOT_EXIST = "数据库节点不存在"

        // Tool 报错
        const val EXTERNAL_LINK_PRICE_NOT_ZERO = "外部链接类型的工具，价格必须为0"

        // Company 报错
        const val COMPANY_NOT_EXIST = "公司不存在"
        const val EMPLOYEE_NOT_EXIST = "员工不存在"
        const val USER_EXISTS = "用户已存在"
        const val DEPARTMENT_NOT_EXIST = "部门不存在"
        const val EMPLOYEE_NOT_IN_DEPARTMENT = "员工不在部门内"
        const val DEPARTMENT_HAS_SUB_DEPARTMENT = "部门下有子部门"
        const val USER_NOT_EXIST = "用户不存在"
    }
}