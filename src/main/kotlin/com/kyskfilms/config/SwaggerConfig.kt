package com.kyskfilms.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "KyskFilms API",
        description = "A Netflix-like streaming platform API",
        version = "v1.0",
        contact = Contact(
            name = "KyskFilms Support",
            email = "support@kyskfilms.com"
        )
    ),
    servers = [
        Server(url = "http://localhost:8081", description = "Local server"),
        Server(url = "https://api.kyskfilms.com", description = "Production server")
    ]
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
class SwaggerConfig {
}