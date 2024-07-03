package com.fpt.cursus.util;

import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PageUtil {
    public void checkOffset(int offset) {
        if (offset < 1) {
            throw new AppException(ErrorCode.INVALID_OFFSET);
        }
    }
    public Pageable getPageable(String sortBy, int offset, int pageSize) {
        return sortBy == null
                ? PageRequest.of(offset, pageSize)
                : PageRequest.of(offset, pageSize, Sort.by(sortBy));
    }

}
