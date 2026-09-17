package com.ticketing.platform.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dynamic QR Ticketing Platform API")
                        .version("1.0.0")
                        .description("REST API documentation for Dynamic QR Ticketing Platform (Modular Monolith)")
                        .contact(new Contact().name("Ticketing Dev Team")));
    }
}
