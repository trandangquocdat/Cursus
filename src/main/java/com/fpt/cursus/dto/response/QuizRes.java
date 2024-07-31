package com.fpt.cursus.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fpt.cursus.dto.object.QuizQuestion;
import com.fpt.cursus.entity.Quiz;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizRes {
    Quiz quiz;
    List<QuizQuestion> questions;
}
