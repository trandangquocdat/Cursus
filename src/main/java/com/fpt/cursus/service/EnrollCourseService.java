package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface EnrollCourseService {
    void enrollCourseAfterPay(List<Long> ids, String username) throws JsonProcessingException;
}
