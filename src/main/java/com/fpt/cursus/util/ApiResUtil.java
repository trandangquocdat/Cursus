package com.fpt.cursus.util;

import com.fpt.cursus.dto.response.ApiRes;
import org.springframework.stereotype.Component;

@Component
public class ApiResUtil {
    public ApiRes<?> returnApiRes(boolean status, int code, String message, Object data) {
        var apiRes = new ApiRes<>();
        apiRes.setStatus(status);
        apiRes.setCode(code);
        apiRes.setMessage(message);
        apiRes.setResult(data);
        return apiRes;
    }
}
