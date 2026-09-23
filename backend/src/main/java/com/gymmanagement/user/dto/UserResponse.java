package com.gymmanagement.user.dto;

import com.gymmanagement.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private Role role;
    private Long organizationId;
    private String organizationName;
    private Long branchId;
    private String branchName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
