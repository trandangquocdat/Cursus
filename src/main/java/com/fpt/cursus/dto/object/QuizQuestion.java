package com.fpt.cursus.dto.object;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizQuestion {
    double questionScore;
    private int questionId;
    private String questionContent;
    private List<QuizAnswer> answers;
}
