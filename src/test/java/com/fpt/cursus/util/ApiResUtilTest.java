package com.fpt.cursus.util;

import com.fpt.cursus.dto.response.ApiRes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApiResUtil.class)
class ApiResUtilTest {

    @Autowired
    private ApiResUtil apiResUtil;

    @MockBean
    private ApiRes<?> apiRes;

    @Test
    void testReturnApiRes() {
        boolean status = true;
        int code = 200;
        String message = "success";
        Object data = "Some data";

        apiRes = apiResUtil.returnApiRes(status, code, message, data);

        assertNotNull(apiRes);
        assertTrue(apiRes.getStatus());
        assertEquals(code, apiRes.getCode());
        assertEquals(message, apiRes.getMessage());
        assertEquals(data, apiRes.getData());
    }

    @Test
    void testReturnApiResWithNullData() {
        boolean status = true;
        int code = 200;
        String message = "success";

        apiRes = apiResUtil.returnApiRes(status, code, message, null);

        assertNotNull(apiRes);
        assertTrue(apiRes.getStatus());
        assertEquals(code, apiRes.getCode());
        assertEquals(message, apiRes.getMessage());
        assertNull(apiRes.getData());
    }
}
