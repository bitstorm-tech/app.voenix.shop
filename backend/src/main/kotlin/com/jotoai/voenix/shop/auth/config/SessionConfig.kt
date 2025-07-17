package com.jotoai.voenix.shop.auth.config

import org.springframework.context.annotation.Configuration
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession

@Configuration
@EnableJdbcHttpSession(
    maxInactiveIntervalInSeconds = 3600,
    tableName = "SPRING_SESSION",
)
class SessionConfig
