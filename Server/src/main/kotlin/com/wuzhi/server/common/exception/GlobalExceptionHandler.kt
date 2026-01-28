package com.wuzhi.server.common.exception

import com.wuzhi.server.common.pojo.ResponseResult
import org.slf4j.LoggerFactory
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * 全局异常处理器
 * 统一处理所有Controller抛出的异常，保证系统稳定性
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * 处理业务异常
     * 这是最常见的异常类型，由业务逻辑主动抛出
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseResult {
        log.warn("业务异常: code=${e.code}, message=${e.message}")
        return ResponseResult.fail(e.message ?: "业务处理失败")
    }

    /**
     * 处理参数校验异常（@Valid注解校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseResult {
        val errors = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        log.warn("参数校验失败: $errors")
        return ResponseResult.fail("参数错误: $errors")
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException::class)
    fun handleBindException(e: BindException): ResponseResult {
        val errors = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        log.warn("参数绑定失败: $errors")
        return ResponseResult.fail("参数格式错误: $errors")
    }

    /**
     * 处理请求参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameterException(e: MissingServletRequestParameterException): ResponseResult {
        log.warn("请求参数缺失: ${e.parameterName}")
        return ResponseResult.fail("缺少必要参数: ${e.parameterName}")
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseResult {
        log.warn("参数类型错误: ${e.name}=${e.value}, 期望类型: ${e.requiredType?.simpleName}")
        return ResponseResult.fail("参数格式错误: ${e.name}")
    }

    /**
     * 处理JSON解析异常
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseResult {
        log.warn("请求体格式错误: ${e.message}")
        return ResponseResult.fail("请求数据格式错误，请检查JSON格式")
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException::class)
    fun handleNullPointerException(e: NullPointerException): ResponseResult {
        log.error("空指针异常: ${e.message}", e)
        return ResponseResult.error("系统内部错误: 数据异常")
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseResult {
        log.warn("非法参数异常: ${e.message}")
        return ResponseResult.fail("参数值非法: ${e.message}")
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseResult {
        log.warn("非法状态异常: ${e.message}")
        return ResponseResult.fail("状态异常: ${e.message}")
    }

    /**
     * 兜底异常处理
     * 处理所有未预料到的异常，保证系统不会崩溃
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseResult {
        log.error("系统异常: ${e.message}", e)
        return ResponseResult.error("系统异常：${e.message}")
    }
}
