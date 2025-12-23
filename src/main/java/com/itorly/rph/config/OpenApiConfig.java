package com.itorly.rph.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Remote Project Hub API",
                description = "API documentation for the Remote Project Hub service.",
                version = "1.0.0",
                contact = @Contact(name = "Remote Project Hub")
        )
)
public class OpenApiConfig {
}
