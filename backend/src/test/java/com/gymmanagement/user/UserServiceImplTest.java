package com.gymmanagement.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gymmanagement.branch.Branch;
import com.gymmanagement.branch.BranchRepository;
import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.coach.FitnessCoachRepository;
import com.gymmanagement.dietician.DieticianRepository;
import com.gymmanagement.organization.Organization;
import com.gymmanagement.organization.OrganizationRepository;
import com.gymmanagement.security.TenantAccessService;
import com.gymmanagement.user.dto.UserRequest;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private FitnessCoachRepository coachRepository;
    @Mock private DieticianRepository dieticianRepository;
    @Mock private TenantAccessService tenantAccess;

    @InjectMocks
    private UserServiceImpl service;

    private Organization organization;
    private Branch branch;

    @BeforeEach
    void setUp() {
        organization = Organization.builder().code("ORG_01").name("MK Fitness Club").build();
        organization.setId(1L);
        branch = Branch.builder().organization(organization).branchCode("BR_01").branchName("Main").build();
        branch.setId(2L);
        lenient().when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void organizationAdminCreatesManagerForOwnBranch() {
        when(tenantAccess.currentUser()).thenReturn(User.builder().role(Role.ORGANIZATION_ADMIN)
                .organization(organization).build());
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(branchRepository.findById(2L)).thenReturn(Optional.of(branch));

        service.createUser(request(Role.BRANCH_MANAGER, 2L));

        verify(branchRepository).save(branch);
        org.assertj.core.api.Assertions.assertThat(branch.getManager().getRole()).isEqualTo(Role.BRANCH_MANAGER);
        org.assertj.core.api.Assertions.assertThat(branch.getManager().getShiftStartTime()).isEqualTo(LocalTime.of(9, 0));
        org.assertj.core.api.Assertions.assertThat(branch.getManager().getShiftEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    void branchManagerCreatesCoachProfileInOwnBranch() {
        when(tenantAccess.currentUser()).thenReturn(User.builder().role(Role.BRANCH_MANAGER)
                .organization(organization).branch(branch).build());
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(branchRepository.findById(2L)).thenReturn(Optional.of(branch));

        service.createUser(request(Role.COACH, null));

        verify(coachRepository).save(any(FitnessCoach.class));
    }

        @Test
        void organizationAdminListingUsesEmptySearchWhenKeywordIsMissing() {
        when(tenantAccess.currentUser()).thenReturn(User.builder().role(Role.ORGANIZATION_ADMIN)
            .organization(organization).build());
        when(userRepository.searchByOrganizationAndRoles(eq(1L), eq(Set.of(Role.BRANCH_MANAGER)),
            eq(""), any(Pageable.class))).thenReturn(Page.empty());

        service.getUsers(null, null, 0, 20);

        verify(userRepository).searchByOrganizationAndRoles(eq(1L), eq(Set.of(Role.BRANCH_MANAGER)),
            eq(""), any(Pageable.class));
        }

    private UserRequest request(Role role, Long branchId) {
        return UserRequest.builder().firstName("New").lastName("User").email(role.name().toLowerCase() + "@example.com")
                .password("Password@123").role(role).organizationId(1L).branchId(branchId)
                .shiftStartTime(LocalTime.of(9, 0)).shiftEndTime(LocalTime.of(17, 0)).active(true).build();
    }
}