package com.wuzhi.server.common.interceptor

import com.wuzhi.server.common.constant.BaseUrlConstant
import com.wuzhi.server.common.constant.ErrorMessages
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.property.JwtProperties
import com.wuzhi.server.common.util.JwtUtil
import com.wuzhi.server.service_user.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.Exception

@Component
class TokenInterceptor(
    private val jwtProperties: JwtProperties,
    private val userService: UserService
) : HandlerInterceptor {

    /**
     * 拦截请求，进行权限验证，添加用户ID到上下文
     */
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (handler !is HandlerMethod) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        // 解析请求头中的token，只要报错，就是没有权限
        try {
            val token = request.getHeader(jwtProperties.tokenHeader).removePrefix(BaseUrlConstant.TOKEN_PREFIX);
            val claims = JwtUtil.parseJwt(jwtProperties.accessKey, token)
                ?: throw Exception(ErrorMessages.NO_AUTHORIZATION);
            val userId = claims[jwtProperties.userIdKey].toString();
            // 查询用户，看用户是否存在
            userService.getById(userId)
                ?: throw Exception(ErrorMessages.NO_AUTHORIZATION);
            UserContext.setUserId(userId);
            return true;
        }catch (e: Exception){
            response.status = 401;
            response.contentType = "application/json;charset=UTF-8";
            response.writer.write("{\"code\":401,\"message\":\"未授权访问\",\"data\":null}");
            return false;
        }
    }

    /**
     * 请求处理之后，从上下文中删除用户ID
     */
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        UserContext.removeUserId();
    }

}
