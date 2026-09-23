package com.gymmanagement.user;

import com.gymmanagement.branch.Branch;
import com.gymmanagement.common.entity.BaseEntity;
import com.gymmanagement.organization.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"), indexes = {
    @Index(name = "idx_user_organization_role", columnList = "organization_id,role"),
    @Index(name = "idx_user_branch_role", columnList = "branch_id,role")
})
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "shift_start_time")
    private LocalTime shiftStartTime;

    @Column(name = "shift_end_time")
    private LocalTime shiftEndTime;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "profile_image_data")
    private byte[] profileImage;

    @Column(name = "profile_image_content_type", length = 100)
    private String profileImageContentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
