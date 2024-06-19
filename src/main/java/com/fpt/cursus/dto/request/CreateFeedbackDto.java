package com.fpt.cursus.dto.request;

import com.fpt.cursus.enums.FeedbackType;
import lombok.Data;

import java.util.Date;

@Data
public class CreateFeedbackDto {

    private String content;
    private FeedbackType type;
    private Long courseId;
}
