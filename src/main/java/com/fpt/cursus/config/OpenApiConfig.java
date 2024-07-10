package com.fpt.cursus.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration


public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI(@Value("${open.api.server.url}") String url,
                           @Value("${open.api.server.description}") String description) {
        return new OpenAPI().info(new Info().title("Cursus API").version("1.0")
                        .contact(new Contact().name("Cursus education").email("cursusedu@gmail.com"))
                        .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(new Server().url(url).description(description)));
    }

    @Bean
    public GroupedOpenApi userGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/**")
                .build();
    }

}
