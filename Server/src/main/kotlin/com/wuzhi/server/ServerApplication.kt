package com.wuzhi.server

import com.wuzhi.server.common.property.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableConfigurationProperties(JwtProperties::class)
@SpringBootApplication
@EnableTransactionManagement
class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}
