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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Cursus API", version = "1.0",
        contact = @Contact(name = "Cursus education", email = "cursusedu@gmail.com"),
        license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0"),
        extensions = {
                @Extension(name = "securityLevel", properties = @ExtensionProperty(name = "internal", value = "high"))
        }))
@SecurityScheme(name = "api", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@EnableAsync
@EnableScheduling
public class CursusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CursusApplication.class, args);
//        Account account = new Account();
//        account.setUsername("testUser");
//        account.setPassword("password");
//        account.setFullName("Test User");
//        account.setEmail("test@example.com");
//        account.setPhone("0123456789");
//        account.setRole(Role.STUDENT);
//        account.setStatus(UserStatus.ACTIVE);
//        List<EnrollCourseDto> enrolledCourses = new ArrayList<>();
//        EnrollCourseDto course1 = new EnrollCourseDto();
//        course1.setCourseName("Course 1");
//        EnrollCourseDto course2 = new EnrollCourseDto();
//        course2.setCourseName("Course 2");
//        enrolledCourses.add(course1);
//        enrolledCourses.add(course2);
//        account.setEnrolledCourse(enrolledCourses);
//        System.out.println("Enrolled Courses:");
//        account.getEnrolledCourse().forEach(course -> {
//                    System.out.println("Course Name: " + course.getCourseName());
//                }
//        );
    }

}
