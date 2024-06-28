package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.service.impl.EnrollCourseServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollCourseServiceTest {

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private AccountRepo accountRepo;

    @InjectMocks
    private EnrollCourseServiceImpl enrollCourseService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }
}
