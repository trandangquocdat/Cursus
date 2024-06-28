package com.fpt.cursus.dto.request;

import com.fpt.cursus.enums.type.FeedbackType;
import lombok.Data;

@Data
public class CreateFeedbackDto {

    private String content;
    private float rating;

}
