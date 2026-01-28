package com.wuzhi.server.common.pojo

data class ResponseResult(
    var code: Int = 200,
    var message: String = "success",
    var data: Any? = null
){
    companion object{
        const val SUCCESS = 200
        const val FAIL = 500
        const val UNAUTHORIZED = 401
        const val REFRESH_TOKEN_FAILED = 431
        fun success(message: String, data: Any?): ResponseResult{
            return ResponseResult(code = SUCCESS, message = message, data = data)
        }
        fun success(message: String): ResponseResult{
            return ResponseResult(code = SUCCESS, message = message)
        }
        fun fail(message: String): ResponseResult{
            return ResponseResult(code = FAIL, message = message)
        }
        fun unauthorized(message: String): ResponseResult{
            return ResponseResult(code = UNAUTHORIZED, message = message)
        }
        fun error(message: String): ResponseResult{
            return ResponseResult(code = FAIL, message = message)
        }
        fun error(message: String, data: Any?): ResponseResult{
            return ResponseResult(code = FAIL, message = message, data = data)
        }

        fun tokenRefreshFailed(message: String): ResponseResult {
            return ResponseResult(code = UNAUTHORIZED, message = message)
        }
    }
}
