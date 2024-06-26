package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface EnrollCourseService {
    void enrollCourseAfterPay(List<Long> ids) throws JsonProcessingException;
}
