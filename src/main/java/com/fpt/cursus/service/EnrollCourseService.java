package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.util.AccountUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EnrollCourseService {
    @Autowired
    private AccountUtil accountUtil;
    @Autowired
    private AccountRepo accountRepo;

    @Transactional
    public void enrollCourseAfterPay(List<Long> ids) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = accountUtil.getCurrentAccount();
        // Deserialize currently enrolled course IDs from JSON to List<Long>
        List<Long> enrolledCourse = mapper.readValue(account.getEnrolledCourseJson(), new TypeReference<>() {
        });
        // Use a Set to avoid duplicates
        Set<Long> enrolledCourseSet = new HashSet<>(enrolledCourse);
        // Add new course IDs to the Set (ensuring no duplicates)
        enrolledCourseSet.addAll(ids);
        // Convert Set back to List (if order matters, use ArrayList instead of HashSet)
        enrolledCourse = new ArrayList<>(enrolledCourseSet);
        // Update the Account object with the updated enrolled course list
        account.setEnrolledCourse(enrolledCourse);
        // Convert the updated list to JSON
        account.setEnrolledCourseJson(mapper.writeValueAsString(enrolledCourse));
        // Save the updated Account object
        accountRepo.save(account);
    }

}
