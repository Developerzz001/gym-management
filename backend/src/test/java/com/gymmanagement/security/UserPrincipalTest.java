package com.gymmanagement.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.gymmanagement.organization.Organization;
import com.gymmanagement.organization.OrganizationStatus;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import org.junit.jupiter.api.Test;

class UserPrincipalTest {

    @Test
    void disablesUserWhenOrganizationIsInactive() {
        Organization organization = Organization.builder().status(OrganizationStatus.INACTIVE).build();
        User user = User.builder().email("owner@example.com").password("password")
                .firstName("Org").lastName("Owner").role(Role.ORGANIZATION_ADMIN)
                .organization(organization).active(true).build();

        assertThat(new UserPrincipal(user).isEnabled()).isFalse();
    }

    @Test
    void enablesActiveUserWhenOrganizationIsActive() {
        Organization organization = Organization.builder().status(OrganizationStatus.ACTIVE).build();
        User user = User.builder().email("owner@example.com").password("password")
                .firstName("Org").lastName("Owner").role(Role.ORGANIZATION_ADMIN)
                .organization(organization).active(true).build();

        assertThat(new UserPrincipal(user).isEnabled()).isTrue();
    }
}