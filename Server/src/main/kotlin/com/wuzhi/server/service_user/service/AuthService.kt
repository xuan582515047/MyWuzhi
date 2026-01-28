package com.wuzhi.server.service_user.service

import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_user.pojo.dto.CaptchaDTO
import com.wuzhi.server.service_user.pojo.dto.LoginDTO
import com.wuzhi.server.service_user.pojo.dto.RegisterDTO
import com.wuzhi.server.service_user.pojo.dto.RefreshTokenDTO
import jakarta.servlet.http.HttpServletResponse

interface AuthService {

    /**
     * 登录
     */
    fun login(loginDTO: LoginDTO): ResponseResult;

    /**
     * 注册
     */
    fun register(registerDTO: RegisterDTO): ResponseResult

    /**
     * 发送验证码
     */
    fun sendCaptcha(captchaDTO: CaptchaDTO): ResponseResult

    /**
     * 刷新令牌
     */
    fun refreshToken(refreshTokenDTO: RefreshTokenDTO, response: HttpServletResponse): ResponseResult
}