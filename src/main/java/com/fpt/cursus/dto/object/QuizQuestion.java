package com.fpt.cursus.dto.object;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizQuestion {
    private int questionId;
    private String questionContent;
    double questionScore;

    private List<QuizAnswer> answers;
}
