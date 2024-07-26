package com.fpt.cursus.dto.request;

import com.fpt.cursus.dto.object.UserAnswerDto;
import lombok.Data;

import java.util.List;

@Data
public class CheckAnswerReq {
    List<UserAnswerDto> answers;
    private long quizId;
}
