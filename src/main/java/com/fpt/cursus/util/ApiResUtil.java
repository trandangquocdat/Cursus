package com.fpt.cursus.util;

import com.fpt.cursus.dto.ApiRes;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

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
