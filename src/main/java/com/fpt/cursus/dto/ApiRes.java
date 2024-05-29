package com.fpt.cursus.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiRes <T>{
    private boolean status;
    private int code;
    private String message;
    private T result;
}
