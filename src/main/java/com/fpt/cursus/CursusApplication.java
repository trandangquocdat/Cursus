package com.fpt.cursus;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Cursus API", version = "1.0", description = "The API for the website provides endpoints for" +
        " managing user profiles, accessing skill categories, and tracking user progress. It allows users to enroll in various skill courses, track their learning journey, " +
        "and interact with other users through discussion forums. " +
        "The API also includes authentication mechanisms to ensure secure access to user data and skill resources.",
        contact = @Contact(name = "Cursus education", email = "cursusedu@gmail.com"),
        license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0"),
        extensions = {
                @Extension(name = "securityLevel", properties = @ExtensionProperty(name = "internal", value = "high"))
        }))
@SecurityScheme(name = "api", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class CursusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CursusApplication.class, args);
    }

}
