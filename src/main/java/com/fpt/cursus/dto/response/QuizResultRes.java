package com.fpt.cursus.dto.response;

import lombok.Data;

@Data
public class QuizResultRes {
    private int correct;
    private int wrong;
    private int skipped;
    private double score;
}
