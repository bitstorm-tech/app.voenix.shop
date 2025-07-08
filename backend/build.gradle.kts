import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    kotlin("plugin.jpa") version "2.1.0"
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "11.10.1"
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
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

    // Database
    implementation("org.flywaydb:flyway-core:11.10.1")
    implementation("org.flywaydb:flyway-database-postgresql:11.10.1")
    implementation("org.postgresql:postgresql:42.7.7")

    // Environment variables (.env file support)
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

kotlin {
    jvmToolchain(21)
}

flyway {
    url = System.getenv("DATABASE_URL")
    user = System.getenv("DATABASE_USERNAME")
    password = System.getenv("DATABASE_PASSWORD")
    schemas = arrayOf("public")
    locations = arrayOf("classpath:db/migration")
}
