package com.wuzhi.server.service_user.pojo.dto

import jakarta.validation.constraints.NotBlank

data class RefreshTokenDTO(

    @NotBlank
    var refreshToken: String
) {}