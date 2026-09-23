package com.gymmanagement.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.gymmanagement.organization.Organization;
import com.gymmanagement.organization.OrganizationStatus;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    @Test
    void rejectsExistingTokenAfterOrganizationIsDeactivated() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "test-secret-key-that-is-at-least-32-bytes-long");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 60_000L);
        UserPrincipal activePrincipal = principalFor(OrganizationStatus.ACTIVE);
        String token = jwtService.generateAccessToken(activePrincipal);

        assertThat(jwtService.isTokenValid(token, principalFor(OrganizationStatus.INACTIVE))).isFalse();
    }

    private UserPrincipal principalFor(OrganizationStatus status) {
        Organization organization = Organization.builder().status(status).build();
        User user = User.builder().email("owner@example.com").password("password")
                .firstName("Org").lastName("Owner").role(Role.ORGANIZATION_ADMIN)
                .organization(organization).active(true).build();
        return new UserPrincipal(user);
    }
}