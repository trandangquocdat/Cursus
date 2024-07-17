package com.fpt.cursus.util;

import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = PageUtil.class)
class PageUtilTest {

    @InjectMocks
    private PageUtil pageUtil;

    @Test
    void checkOffsetDoNotThrowException() {
        assertDoesNotThrow(() -> pageUtil.checkOffset(1));
    }

    @Test
    void checkOffsetThrowException() {
        assertThrows(AppException.class, () -> pageUtil.checkOffset(0),
                ErrorCode.INVALID_OFFSET.getMessage());
    }

    @Test
    void getPageable() {
        Pageable result = pageUtil.getPageable("name", 1, 10);
        assertEquals(1, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertTrue(result.getSort().isSorted());
    }

    @Test
    void getPageableWithoutSort() {
        Pageable result = pageUtil.getPageable(null, 1, 10);
        assertEquals(1, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertFalse(result.getSort().isSorted());
    }
}
