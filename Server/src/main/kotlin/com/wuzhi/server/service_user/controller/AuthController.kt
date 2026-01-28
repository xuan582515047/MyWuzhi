package com.wuzhi.server.service_user.controller

import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_user.pojo.dto.CaptchaDTO
import com.wuzhi.server.service_user.pojo.dto.LoginDTO
import com.wuzhi.server.service_user.pojo.dto.RefreshTokenDTO
import com.wuzhi.server.service_user.pojo.dto.RegisterDTO
import com.wuzhi.server.service_user.service.AuthService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
){
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginDTO: LoginDTO): ResponseResult {
        return authService.login(loginDTO)
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerDTO: RegisterDTO): ResponseResult {
        return authService.register(registerDTO)
    }

    @PostMapping("/captcha")
    fun sendCaptcha(@Valid @RequestBody captchaDTO: CaptchaDTO): ResponseResult {
        return authService.sendCaptcha(captchaDTO)
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody refreshTokenDTO: RefreshTokenDTO, response: HttpServletResponse): ResponseResult {
        return authService.refreshToken(refreshTokenDTO, response)
    }
}