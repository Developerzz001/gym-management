package com.gymmanagement.branch;

import com.gymmanagement.audit.BranchAuditLog;
import com.gymmanagement.audit.BranchAuditLogRepository;
import com.gymmanagement.branch.dto.*;
import com.gymmanagement.client.Client;
import com.gymmanagement.client.ClientRepository;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.BadRequestException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.organization.Organization;
import com.gymmanagement.organization.OrganizationService;
import com.gymmanagement.security.TenantAccessService;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import com.gymmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchService {
    private final BranchRepository repository;
    private final BranchSettingsRepository settingsRepository;
    private final MemberBranchTransferRepository transferRepository;
    private final BranchAuditLogRepository auditRepository;
    private final OrganizationService organizationService;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TenantAccessService tenantAccess;

    @Transactional
    public BranchResponse create(BranchRequest request) {
        tenantAccess.assertOrganizationAccess(request.organizationId());
        Organization organization = organizationService.getEntity(request.organizationId());
        String code = "BR_%02d".formatted(repository.findNextGeneratedCodeNumber(request.organizationId()));
        Branch branch = apply(Branch.builder().organization(organization).branchCode(code).build(), request);
        branch = repository.save(branch);
        settingsRepository.save(BranchSettings.builder().branch(branch).workingHours("{}").timezone("UTC").build());
        audit(branch, "BRANCH_CREATED", null, branch.getBranchCode());
        return response(branch);
    }

    @Transactional
    public BranchResponse update(Long id, BranchRequest request) {
        Branch branch = getEntity(id);
        tenantAccess.assertBranchAccess(branch);
        if (!branch.getOrganization().getId().equals(request.organizationId())) {
            throw new BadRequestException("A branch cannot be moved to another organization");
        }
        String oldValue = branch.getBranchName() + "|" + branch.getStatus();
        apply(branch, request);
        audit(branch, "BRANCH_UPDATED", oldValue, branch.getBranchName() + "|" + branch.getStatus());
        return response(repository.save(branch));
    }

    @Transactional
    public BranchResponse setActive(Long id, boolean active) {
        Branch branch = getEntity(id);
        tenantAccess.assertBranchAccess(branch);
        BranchStatus old = branch.getStatus();
        branch.setStatus(active ? BranchStatus.ACTIVE : BranchStatus.INACTIVE);
        audit(branch, "BRANCH_STATUS_CHANGED", old.name(), branch.getStatus().name());
        return response(repository.save(branch));
    }

    public BranchResponse get(Long id) {
        Branch branch = getEntity(id);
        tenantAccess.assertBranchAccess(branch);
        return response(branch);
    }

    public PageResponse<BranchResponse> list(Long organizationId, String keyword, int page, int size) {
        User user = tenantAccess.currentUser();
        String search = keyword == null ? "" : keyword.trim();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("branchName"));
        Page<Branch> result;
        if (user.getRole() == Role.SUPER_ADMIN) {
            result = organizationId == null ? repository.findByBranchNameContainingIgnoreCase(search, pageable)
                    : repository.findByOrganizationIdAndBranchNameContainingIgnoreCase(organizationId, search, pageable);
        } else if (user.getRole() == Role.ORGANIZATION_ADMIN) {
            Long scopedOrganizationId = user.getOrganization().getId();
            if (organizationId != null && !organizationId.equals(scopedOrganizationId)) {
                throw new org.springframework.security.access.AccessDeniedException("Organization access denied");
            }
            result = repository.findByOrganizationIdAndBranchNameContainingIgnoreCase(scopedOrganizationId, search, pageable);
        } else {
            Branch branch = user.getBranch();
            result = branch == null || !branch.getBranchName().toLowerCase().contains(search.toLowerCase())
                    ? Page.empty(pageable) : new PageImpl<>(java.util.List.of(branch), pageable, 1);
        }
        return PageResponse.from(result.map(this::response));
    }

    public BranchSettingsResponse settings(Long branchId) {
        Branch branch = getEntity(branchId);
        tenantAccess.assertBranchAccess(branch);
        return settingsResponse(settingsRepository.findByBranchId(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("BranchSettings", "branchId", branchId)));
    }

    @Transactional
    public BranchSettingsResponse updateSettings(Long branchId, BranchSettingsRequest request) {
        Branch branch = getEntity(branchId);
        tenantAccess.assertBranchAccess(branch);
        BranchSettings settings = settingsRepository.findByBranchId(branchId).orElseGet(() -> BranchSettings.builder().branch(branch).build());
        String old = settings.getTimezone() + "|" + settings.getWorkingHours();
        settings.setWorkingHours(request.workingHours()); settings.setTimezone(request.timezone());
        settings.setMembershipRules(request.membershipRules()); settings.setNotificationPreferences(request.notificationPreferences());
        settings.setAttendanceRules(request.attendanceRules());
        audit(branch, "SETTINGS_CHANGED", old, settings.getTimezone() + "|" + settings.getWorkingHours());
        return settingsResponse(settingsRepository.save(settings));
    }

    @Transactional
    public TransferResponse transferMember(Long clientId, TransferRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));
        Branch source = client.getUser().getBranch();
        Branch destination = getEntity(request.destinationBranchId());
        if (source == null) throw new BadRequestException("Member is not assigned to a branch");
        tenantAccess.assertBranchAccess(source);
        tenantAccess.assertOrganizationAccess(destination.getOrganization().getId());
        if (!source.getOrganization().getId().equals(destination.getOrganization().getId())) {
            throw new BadRequestException("Members can only transfer within their organization");
        }
        if (source.getId().equals(destination.getId())) throw new BadRequestException("Destination must differ from current branch");
        User actor = tenantAccess.currentUser();
        client.getUser().setBranch(destination);
        client.setAssignedCoach(null);
        client.setAssignedDietician(null);
        userRepository.save(client.getUser());
        clientRepository.save(client);
        MemberBranchTransfer transfer = transferRepository.save(MemberBranchTransfer.builder().client(client)
                .fromBranch(source).toBranch(destination).transferredBy(actor).reason(request.reason()).build());
        audit(destination, "MEMBER_TRANSFERRED", source.getBranchCode(), destination.getBranchCode());
        return transferResponse(transfer);
    }

    @Transactional
    public void transferStaff(Long userId, TransferRequest request) {
        User staff = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (staff.getRole() != Role.COACH && staff.getRole() != Role.FITNESS_COACH && staff.getRole() != Role.DIETICIAN
                && staff.getRole() != Role.RECEPTIONIST && staff.getRole() != Role.BRANCH_MANAGER) {
            throw new BadRequestException("Only branch staff can be transferred");
        }
        Branch source = staff.getBranch();
        Branch destination = getEntity(request.destinationBranchId());
        if (source == null) throw new BadRequestException("Staff member is not assigned to a branch");
        tenantAccess.assertBranchAccess(source);
        if (!source.getOrganization().getId().equals(destination.getOrganization().getId())) {
            throw new BadRequestException("Staff can only transfer within their organization");
        }
        staff.setBranch(destination);
        staff.setOrganization(destination.getOrganization());
        userRepository.save(staff);
        audit(destination, "STAFF_TRANSFERRED", source.getBranchCode(), destination.getBranchCode() + ":" + request.reason());
    }

    public PageResponse<TransferResponse> memberHistory(Long clientId, int page, int size) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));
        if (client.getUser().getBranch() != null) tenantAccess.assertBranchAccess(client.getUser().getBranch());
        return PageResponse.from(transferRepository.findByClientIdOrderByCreatedAtDesc(clientId,
                PageRequest.of(page, Math.min(size, 100))).map(this::transferResponse));
    }

    public Branch getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
    }

    private Branch apply(Branch branch, BranchRequest request) {
        branch.setBranchName(request.branchName());
        branch.setAddress(request.address()); branch.setCity(request.city()); branch.setState(request.state());
        branch.setCountry(request.country()); branch.setPincode(request.pincode()); branch.setContactNumber(request.contactNumber());
        branch.setEmail(request.email());
        if (request.managerId() != null) {
            User manager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.managerId()));
            if (manager.getRole() != Role.BRANCH_MANAGER) throw new BadRequestException("Manager must have BRANCH_MANAGER role");
            if (manager.getOrganization() == null || !manager.getOrganization().getId().equals(branch.getOrganization().getId())) {
                throw new BadRequestException("Manager must belong to the branch organization");
            }
            manager.setBranch(branch.getId() == null ? null : branch);
            branch.setManager(manager);
        }
        return branch;
    }

    private void audit(Branch branch, String action, String oldValue, String newValue) {
        auditRepository.save(BranchAuditLog.builder().organization(branch.getOrganization()).branch(branch)
                .user(tenantAccess.currentUser()).action(action).oldValue(oldValue).newValue(newValue).build());
    }

    private BranchResponse response(Branch value) {
        User manager = value.getManager();
        return new BranchResponse(value.getId(), value.getOrganization().getId(), value.getOrganization().getName(),
                value.getBranchCode(), value.getBranchName(), value.getAddress(), value.getCity(), value.getState(),
                value.getCountry(), value.getPincode(), value.getContactNumber(), value.getEmail(),
                manager == null ? null : manager.getId(), manager == null ? null : manager.getFullName(),
                value.getStatus(), value.getCreatedAt(), value.getUpdatedAt());
    }

    private BranchSettingsResponse settingsResponse(BranchSettings value) {
        return new BranchSettingsResponse(value.getBranch().getId(), value.getWorkingHours(), value.getTimezone(),
                value.getMembershipRules(), value.getNotificationPreferences(), value.getAttendanceRules());
    }

    private TransferResponse transferResponse(MemberBranchTransfer value) {
        return new TransferResponse(value.getId(), value.getClient().getId(), value.getClient().getUser().getFullName(),
                value.getFromBranch().getId(), value.getFromBranch().getBranchName(), value.getToBranch().getId(),
                value.getToBranch().getBranchName(), value.getReason(), value.getTransferredBy().getFullName(), value.getCreatedAt());
    }
}