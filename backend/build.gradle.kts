import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    kotlin("plugin.jpa") version "2.2.0"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "11.10.5"
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
    id("io.gitlab.arturbosch.detekt") version ("1.23.8")
}

group = "com.jotoai.voenix"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.session:spring-session-jdbc")

    // Spring Modulith
    implementation("org.springframework.modulith:spring-modulith-starter-core:1.4.2")

    // Database
    implementation("org.flywaydb:flyway-core:11.10.5")
    implementation("org.flywaydb:flyway-database-postgresql:11.10.5")
    implementation("org.postgresql:postgresql:42.7.7")

    // Environment variables (.env file support)
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    // Image processing
    implementation("com.sksamuel.scrimage:scrimage-core:4.3.2")
    implementation("com.sksamuel.scrimage:scrimage-webp:4.3.2")

    // Ktor Client
    implementation("io.ktor:ktor-client-core:3.2.3")
    implementation("io.ktor:ktor-client-cio:3.2.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.2.3")
    implementation("io.ktor:ktor-serialization-jackson:3.2.3")
    implementation("io.ktor:ktor-client-logging:3.2.3")

    // PDF Generation
    implementation("com.github.librepdf:openpdf:2.0.3")

    // QR Code Generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.12")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test:1.4.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

flyway {
    url = System.getenv("DATABASE_URL")
    user = System.getenv("DATABASE_USERNAME")
    password = System.getenv("DATABASE_PASSWORD")
    schemas = arrayOf("public")
    locations = arrayOf("classpath:db/migration")
}

// Detekt configuration to handle Kotlin version mismatch
configurations.matching { it.name.contains("detekt") }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("2.0.21")
        }
    }
}
