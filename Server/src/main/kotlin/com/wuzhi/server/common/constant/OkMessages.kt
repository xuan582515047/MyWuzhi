package com.wuzhi.server.common.constant

class OkMessages {
    companion object {
        const val NO_TIP = "NO_TIP"

        // Common
        const val QUERY_SUCCESS = NO_TIP
        const val UPDATE_SUCCESS = "更新成功"
        const val DELETE_SUCCESS = "删除成功"
        const val ADD_SUCCESS = "创建成功"
        const val SET_SUCCESS = "设置成功"

        // Auth
        const val LOGIN_SUCCESS = "登录成功"
        const val REGISTER_SUCCESS = "注册成功"
        const val SEND_CAPTCHA_SUCCESS = "验证码发送成功"
        const val REFRESH_TOKEN_SUCCESS = NO_TIP

        // Chat
        const val MESSAGE_SEND_SUCCESS = NO_TIP

        // Company
        const val AUTH_SUCCESS = "公司认证成功"
    }
}