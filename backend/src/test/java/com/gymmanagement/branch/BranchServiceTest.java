package com.gymmanagement.branch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gymmanagement.audit.BranchAuditLog;
import com.gymmanagement.audit.BranchAuditLogRepository;
import com.gymmanagement.branch.dto.BranchRequest;
import com.gymmanagement.branch.dto.BranchResponse;
import com.gymmanagement.client.ClientRepository;
import com.gymmanagement.organization.Organization;
import com.gymmanagement.organization.OrganizationService;
import com.gymmanagement.security.TenantAccessService;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import com.gymmanagement.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock private BranchRepository repository;
    @Mock private BranchSettingsRepository settingsRepository;
    @Mock private MemberBranchTransferRepository transferRepository;
    @Mock private BranchAuditLogRepository auditRepository;
    @Mock private OrganizationService organizationService;
    @Mock private UserRepository userRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private TenantAccessService tenantAccess;

    @InjectMocks
    private BranchService service;

    @Test
    void createsBranchWithNextCodeForOrganization() {
        Organization organization = Organization.builder().code("ORG_01").name("MK Fitness Club").build();
        organization.setId(1L);
        User actor = User.builder().role(Role.ORGANIZATION_ADMIN).organization(organization).build();
        when(organizationService.getEntity(1L)).thenReturn(organization);
        when(repository.findNextGeneratedCodeNumber(1L)).thenReturn(2L);
        when(repository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(settingsRepository.save(any(BranchSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditRepository.save(any(BranchAuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tenantAccess.currentUser()).thenReturn(actor);

        BranchResponse response = service.create(new BranchRequest(1L, "Downtown", "Main Road", "Pune",
                "Maharashtra", "India", "411001", "9876543210", "branch@example.com", null));

        assertThat(response.branchCode()).isEqualTo("BR_02");
        assertThat(response.organizationId()).isEqualTo(1L);
    }
}