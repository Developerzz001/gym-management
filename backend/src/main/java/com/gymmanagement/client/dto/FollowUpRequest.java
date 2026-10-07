package com.gymmanagement.client.dto;

import com.gymmanagement.client.InquiryRating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpRequest {

    private LocalDate followUpDate;

    private String comment;

    private Long executiveId;

    private LocalDate nextFollowUpDate;

    private InquiryRating rating;
}
