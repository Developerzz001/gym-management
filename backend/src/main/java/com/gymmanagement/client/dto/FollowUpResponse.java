package com.gymmanagement.client.dto;

import com.gymmanagement.client.InquiryRating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpResponse {

    private Long id;
    private LocalDate followUpDate;
    private String comment;
    private Long executiveId;
    private String executiveName;
    private LocalDate nextFollowUpDate;
    private InquiryRating rating;
    private LocalDateTime createdAt;
}
