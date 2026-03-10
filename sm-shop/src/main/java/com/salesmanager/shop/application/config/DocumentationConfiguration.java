package com.salesmanager.shop.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DocumentationConfiguration {

    @Value("${server.host:http://localhost}:${server.port:8080}")
    private String HOST;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shopizer REST API")
                        .description("API for Shopizer e-commerce. Contains public end points as well as private end "
                                + "points requiring basic authentication and remote authentication based on jwt bearer token. "
                                + "URL patterns containing /private/** use bearer token; those are authorized customer and "
                                + "administrators administration actions.")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Shopizer")
                                .url("https://www.shopizer.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(new Server().url(HOST)))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("shopizer-api")
                .pathsToMatch(
                        "/api/v1/**",
                        "/api/v2/**"
                )
                .build();
    }
}
