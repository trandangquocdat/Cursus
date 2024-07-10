package com.fpt.cursus.dto.request;

import lombok.Data;

@Data
public class CreateFeedbackDto {

    private String content;
    private Float rating;

}
