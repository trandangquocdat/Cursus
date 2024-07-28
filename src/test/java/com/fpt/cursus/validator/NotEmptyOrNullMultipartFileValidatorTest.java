package com.fpt.cursus.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class NotEmptyOrNullMultipartFileValidatorTest {

    @InjectMocks
    private NotEmptyOrNullMultipartFileValidator notEmptyOrNullMultipartFileValidator;

    @Test
    void testIsValid() {
        // Arrange
        MultipartFile file = null;

        // Act
        boolean result = notEmptyOrNullMultipartFileValidator.isValid(file, null);

        // Assert
        assertTrue(result);
    }
}
