package com.gymmanagement.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gymmanagement.organization.dto.OrganizationRequest;
import com.gymmanagement.organization.dto.OrganizationResponse;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import com.gymmanagement.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createsOrganizationAndAdminWithNextGeneratedCode() {
        when(repository.findNextGeneratedCodeNumber()).thenReturn(2L);
        when(repository.save(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OrganizationService service = new OrganizationService(repository, userRepository, passwordEncoder);

        OrganizationResponse response = service.create(request("Silver Fitness Club"));

        assertThat(response.code()).isEqualTo("ORG_02");
        assertThat(response.name()).isEqualTo("Silver Fitness Club");
        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("owner@example.com");
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.ORGANIZATION_ADMIN);
        assertThat(userCaptor.getValue().getOrganization().getCode()).isEqualTo("ORG_02");
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void preservesGeneratedCodeWhenUpdatingOrganization() {
        Organization organization = Organization.builder()
                .code("ORG_01")
                .name("MK Fitness Club")
                .ownerName("Original Owner")
                .email("original@example.com")
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(organization));
        when(repository.save(organization)).thenReturn(organization);
        OrganizationService service = new OrganizationService(repository, userRepository, passwordEncoder);

        OrganizationResponse response = service.update(1L, request("MK Fitness Club Updated"));

        assertThat(response.code()).isEqualTo("ORG_01");
        assertThat(response.name()).isEqualTo("MK Fitness Club Updated");
        verify(repository).save(organization);
    }

    private OrganizationRequest request(String name) {
        return new OrganizationRequest(name, "Owner", "9876543210", "owner@example.com", "Admin@123", "Address");
    }
}