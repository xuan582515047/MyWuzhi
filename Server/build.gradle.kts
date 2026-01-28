plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "com.wuzhi"
version = "0.0.1-SNAPSHOT"
description = "Server"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    mavenCentral()
}

extra["springAiVersion"] = "1.1.0"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // SpringMVC
    implementation("org.springframework:spring-webmvc")
    // SpringWeb
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Spring配置文件解释器
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    // MySQL驱动
    implementation("com.mysql:mysql-connector-j")
    // MyBatis-plus
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.7")
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    // BCrypt加密工具
    implementation("org.mindrot:jbcrypt:0.4")
    // Druid数据库连接池
    implementation("com.alibaba:druid-spring-boot-3-starter:1.2.21")
    // Redis向量库
    implementation("org.springframework.ai:spring-ai-starter-vector-store-redis")
    implementation("org.springframework.ai:spring-ai-advisors-vector-store")
    // lombok
    implementation("org.projectlombok:lombok:1.18.26")
    // 非空校验
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Tika文件处理器
    implementation("org.springframework.ai:spring-ai-tika-document-reader")
    implementation("org.springframework.ai:spring-ai-pdf-document-reader")


}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
