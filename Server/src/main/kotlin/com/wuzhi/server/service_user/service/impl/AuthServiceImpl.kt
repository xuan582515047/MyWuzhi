package com.wuzhi.server.service_user.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.wuzhi.server.common.constant.ErrorMessages
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_user.pojo.dto.CaptchaDTO
import com.wuzhi.server.service_user.pojo.dto.LoginDTO
import com.wuzhi.server.service_user.pojo.dto.RegisterDTO
import com.wuzhi.server.service_user.pojo.dto.RefreshTokenDTO
import com.wuzhi.server.service_user.pojo.po.User
import com.wuzhi.server.service_user.pojo.vo.LoginVO
import com.wuzhi.server.service_user.pojo.vo.RefreshTokenVO
import com.wuzhi.server.common.property.JwtProperties
import com.wuzhi.server.service_user.service.AuthService
import com.wuzhi.server.service_user.service.UserService
import com.wuzhi.server.common.util.JwtUtil
import com.wuzhi.server.common.util.PasswordUtil
import com.wuzhi.server.common.util.TokenUtil
import com.wuzhi.server.service_user.service.UserRecordService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthServiceImpl(
    private val userService: UserService,
    private val tokenUtil: TokenUtil,
    private val jwtProperties: JwtProperties,
    private val userRecordService: UserRecordService
) : AuthService {
    override fun login(loginDTO: LoginDTO): ResponseResult {
        // 获取登录信息
        val phone = loginDTO.phone
        val password = loginDTO.password
        // 查询用户
        val wrapper = KtQueryWrapper(User::class.java)
        wrapper.eq(User::phone, phone)
        val user = userService.getOne(wrapper)
        // 账户不存在
        if (user == null){
            return ResponseResult.fail(ErrorMessages.USER_NOT_FOUND)
        }
        // 对比密码
        if (PasswordUtil.verify(password, user.password)){
            val userId = user.id ?: return ResponseResult.error("用户ID异常")
            val claims = mapOf( jwtProperties.userIdKey to userId )
            val accessToken = tokenUtil.createAccessToken(claims)
            val refreshToken = tokenUtil.createRefreshToken(claims)
            val loginVO = LoginVO(accessToken, refreshToken, user.name, user.avatar)
            // 记录登录记录
            userRecordService.loginRecord(userId)
            return ResponseResult.success(OkMessages.LOGIN_SUCCESS, loginVO)
        }else{
            return ResponseResult.fail(ErrorMessages.PASSWORD_ERROR)
        }
    }

    override fun register(registerDTO: RegisterDTO): ResponseResult {
        //TODO("这里是直接注册")

        // 获取注册信息
        val phone = registerDTO.phone
        val password = registerDTO.password
        val captcha = registerDTO.captcha
        // 查询用户
        val wrapper = KtQueryWrapper(User::class.java)
        wrapper.eq(User::phone, phone)
        val user = userService.getOne(wrapper)
        // 判断用户是否已存在
        if (user != null){
            return ResponseResult.fail(ErrorMessages.USER_ALREADY_EXIST)
        }else{
            val newUser = User(
                name = phone,
                phone = phone,
                password = PasswordUtil.encrypt(password),
                createTime = LocalDateTime.now()
            )
            userService.save(newUser)
            return ResponseResult.success(OkMessages.REGISTER_SUCCESS)
        }


    }

    override fun sendCaptcha(captchaDTO: CaptchaDTO): ResponseResult {
        //TODO("发送验证码")

        return ResponseResult.success(OkMessages.SEND_CAPTCHA_SUCCESS)
    }

    override fun refreshToken(refreshTokenDTO: RefreshTokenDTO, response: HttpServletResponse): ResponseResult {
        val refreshToken = refreshTokenDTO.refreshToken
        try{
            val claims = JwtUtil.parseJwt(jwtProperties.refreshKey, refreshToken)
                ?: return ResponseResult.unauthorized(ErrorMessages.REFRESH_TOKEN_ERROR)
            val userId = claims[jwtProperties.userIdKey] as? String
                ?: return ResponseResult.unauthorized(ErrorMessages.REFRESH_TOKEN_ERROR)
            val accessToken = tokenUtil.createAccessToken(mapOf(jwtProperties.userIdKey to userId))
            val refreshTokenVO = RefreshTokenVO(accessToken)
            // 记录刷新令牌记录
            userRecordService.refreshTokenRecord(userId)
            return ResponseResult.success(OkMessages.REFRESH_TOKEN_SUCCESS, refreshTokenVO)
        }catch (e: Exception){
            response.status = 491
            return ResponseResult.tokenRefreshFailed(ErrorMessages.REFRESH_TOKEN_ERROR)
        }
    }

}
