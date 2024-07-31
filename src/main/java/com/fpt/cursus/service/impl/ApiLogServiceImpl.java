package com.fpt.cursus.service.impl;

import com.fpt.cursus.entity.ApiLog;
import com.fpt.cursus.repository.ApiLogRepo;
import com.fpt.cursus.service.ApiLogService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApiLogServiceImpl implements ApiLogService {
    private final ApiLogRepo apiLogRepo;

    public ApiLogServiceImpl(ApiLogRepo apiLogRepo) {
        this.apiLogRepo = apiLogRepo;
    }

    public static String extractIdValue(String queryString, String idParam) {
        int startIndex = queryString.indexOf(idParam) + idParam.length();
        int endIndex = queryString.indexOf("&", startIndex);
        if (endIndex == -1) { // If there is no "&" after the id parameter
            endIndex = queryString.length();
        }
        return queryString.substring(startIndex, endIndex);
    }

    @Override
    public void saveApiLog(String requestUrl, String queryString) {
        String[] possibleIdParams = {"courseId=", "id="};
        boolean foundIdParam = false;
        String idValue = null;

        for (String idParam : possibleIdParams) {
            if (queryString != null && queryString.contains(idParam)) {
                idValue = idParam.concat(extractIdValue(queryString, idParam));
                foundIdParam = true;
                break;
            }
        }

        if (foundIdParam) {
            List<ApiLog> apiLogs = apiLogRepo.findByRequestUrl(requestUrl);
            List<String> queryStringList = apiLogs.stream().map(ApiLog::getQueryString).toList();

            if (!queryStringList.contains(idValue)) {
                ApiLog apiLog = new ApiLog();
                apiLog.setRequestUrl(requestUrl);
                apiLog.setQueryString(queryString);
                apiLog.setCount(1);
                apiLog.setAccessTime(new Date());
                apiLogRepo.save(apiLog);
            } else {
                for (ApiLog apiLog : apiLogs) {
                    if (queryStringList.contains(idValue)) {
                        apiLog.setCount(apiLog.getCount() + 1);
                        apiLogRepo.save(apiLog);
                        break;
                    }
                }
            }
        }

    }

}
