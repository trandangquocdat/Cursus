package com.fpt.cursus.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotEmptyOrNullMultipartFileValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyOrNullMultipartFile {
    String message() default "AVATAR_EMPTY";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
