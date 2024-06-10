package com.fpt.cursus.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapperUtil {
    public <T> List<T> deserializeCourseList(String courseJson, Class<T> valueType) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, valueType);
            return objectMapper.readValue(courseJson, listType);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String serializeCourseList(List<?> courseList) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(courseList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
