package com.itorly.rph.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI selfOpenAPI() {
        return new OpenAPI().info(new Info()
                        .title("My API Documents")
                        .description("Spring Boot 3 API Documentation")
                        .version("v1.0.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("More documents")
                        .url("https://springdoc.org"));
    }
}
