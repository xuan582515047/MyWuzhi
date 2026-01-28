package com.wuzhi.server.service_user.pojo.dto

import jakarta.validation.constraints.NotBlank

data class RegisterDTO(
    @NotBlank
    var phone: String,

    @NotBlank
    var password: String,

    @NotBlank
    var captcha: String
) {}