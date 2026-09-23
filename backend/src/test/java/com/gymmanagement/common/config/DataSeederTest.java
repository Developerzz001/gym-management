package com.gymmanagement.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import com.gymmanagement.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createsDefaultSuperAdminForNewDatabase() {
        when(userRepository.findByEmailIgnoreCase("superadmin@gymmanagement.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("SuperAdmin@123")).thenReturn("encoded-password");

        new DataSeeder(userRepository, passwordEncoder).run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.SUPER_ADMIN);
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("superadmin@gymmanagement.com");
        verify(userRepository, never()).findByEmailIgnoreCase("admin@gymmanagement.com");
    }

    @Test
    void preservesExistingSuperAdmin() {
        User superAdmin = User.builder().role(Role.SUPER_ADMIN).build();
        when(userRepository.findByEmailIgnoreCase("superadmin@gymmanagement.com")).thenReturn(Optional.of(superAdmin));

        new DataSeeder(userRepository, passwordEncoder).run();

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
        assertThat(superAdmin.getRole()).isEqualTo(Role.SUPER_ADMIN);
    }
}