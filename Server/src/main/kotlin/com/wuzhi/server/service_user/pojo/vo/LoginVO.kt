package com.wuzhi.server.service_user.pojo.vo

class LoginVO(
    var accessToken: String,
    var refreshToken: String,
    var name: String?,
    var avatar: String?,
) {
}