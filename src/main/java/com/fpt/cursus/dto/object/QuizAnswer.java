package com.fpt.cursus.dto.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizAnswer {
    @JsonIgnore
    private int questionId;
    private String id;
    private String content;
    private Boolean isCorrect;
}
