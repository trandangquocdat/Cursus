package com.fpt.cursus.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class NotEmptyOrNullMultipartFileValidator implements ConstraintValidator<NotEmptyOrNullMultipartFile, MultipartFile> {

    @Override
    public void initialize(NotEmptyOrNullMultipartFile constraintAnnotation) {
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Return true if file is null or not empty
        return file == null || !file.isEmpty();
    }
}
