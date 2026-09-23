package com.gymmanagement.user;

import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.DuplicateResourceException;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.user.dto.UserProfileRequest;
import com.gymmanagement.user.dto.UserRequest;
import com.gymmanagement.user.dto.UserResponse;
import com.gymmanagement.branch.Branch;
import com.gymmanagement.branch.BranchRepository;
import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.coach.FitnessCoachRepository;
import com.gymmanagement.dietician.Dietician;
import com.gymmanagement.dietician.DieticianRepository;
import com.gymmanagement.organization.Organization;
import com.gymmanagement.organization.OrganizationRepository;
import com.gymmanagement.security.TenantAccessService;
import com.gymmanagement.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final BranchRepository branchRepository;
    private final FitnessCoachRepository coachRepository;
    private final DieticianRepository dieticianRepository;
    private final TenantAccessService tenantAccess;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new com.gymmanagement.common.exception.BadRequestException("Password is required");
        }
        Scope scope = resolveScope(request);
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .shiftStartTime(request.getShiftStartTime())
                .shiftEndTime(request.getShiftEndTime())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .organization(scope.organization())
                .branch(scope.branch())
                .active(request.getActive() == null || request.getActive())
                .build();
        user = userRepository.save(user);
        if (user.getRole() == Role.BRANCH_MANAGER) {
            if (scope.branch().getManager() != null) {
                throw new BadRequestException("Branch already has a manager");
            }
            scope.branch().setManager(user);
            branchRepository.save(scope.branch());
        } else if (user.getRole() == Role.COACH) {
            coachRepository.save(FitnessCoach.builder().user(user).build());
        } else if (user.getRole() == Role.DIETICIAN) {
            dieticianRepository.save(Dietician.builder().user(user).build());
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = getUserEntityById(id);
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setShiftStartTime(request.getShiftStartTime());
        user.setShiftEndTime(request.getShiftEndTime());
        user.setRole(request.getRole());
        Scope scope = resolveScope(request);
        user.setOrganization(scope.organization());
        user.setBranch(scope.branch());
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        userRepository.delete(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(getUserEntityById(id));
    }

    @Override
    public UserResponse getMyProfile() {
        return userMapper.toResponse(tenantAccess.currentUser());
    }

    @Override
    @Transactional
    public UserResponse updateMyProfile(UserProfileRequest request) {
        User user = tenantAccess.currentUser();
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public PageResponse<UserResponse> getUsers(String keyword, Role role, int page, int size) {
        User actor = tenantAccess.currentUser();
        String search = keyword == null ? "" : keyword.trim();
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("id").descending());
        Page<User> users;
        if (actor.getRole() == Role.ORGANIZATION_ADMIN) {
            users = userRepository.searchByOrganizationAndRoles(actor.getOrganization().getId(),
                    Set.of(Role.BRANCH_MANAGER), search, pageable);
        } else if (actor.getRole() == Role.BRANCH_MANAGER) {
            users = userRepository.searchByBranchAndRoles(actor.getBranch().getId(),
                    Set.of(Role.COACH, Role.DIETICIAN, Role.RECEPTIONIST), search, pageable);
        } else {
            users = userRepository.search(search, role, pageable);
        }
        return PageResponse.from(users.map(userMapper::toResponse));
    }

    @Override
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    private Scope resolveScope(UserRequest request) {
        User actor = tenantAccess.currentUser();
        assertRoleAssignmentAllowed(actor, request.getRole());
        Long organizationId = request.getOrganizationId();
        Long branchId = request.getBranchId();
        if (actor.getRole() == Role.ORGANIZATION_ADMIN) {
            organizationId = actor.getOrganization().getId();
        } else if (actor.getRole() == Role.BRANCH_MANAGER) {
            organizationId = actor.getOrganization().getId();
            branchId = actor.getBranch().getId();
        }
            Long resolvedOrganizationId = organizationId;
            Long resolvedBranchId = branchId;
            Organization organization = resolvedOrganizationId == null ? null : organizationRepository.findById(resolvedOrganizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", resolvedOrganizationId));
            Branch branch = resolvedBranchId == null ? null : branchRepository.findById(resolvedBranchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", resolvedBranchId));
        if (branch != null && (organization == null || !branch.getOrganization().getId().equals(organization.getId()))) {
            throw new BadRequestException("Branch must belong to the selected organization");
        }
        if (request.getRole() != Role.SUPER_ADMIN && request.getRole() != Role.ADMIN && organization == null) {
            throw new BadRequestException("organizationId is required for tenant users");
        }
        if (requiresBranch(request.getRole()) && branch == null) {
            throw new BadRequestException("branchId is required for branch users");
        }
        return new Scope(organization, branch);
    }

    private void assertRoleAssignmentAllowed(User actor, Role requestedRole) {
        if (actor.getRole() == Role.ORGANIZATION_ADMIN && requestedRole != Role.BRANCH_MANAGER) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Organization admins can only create branch managers");
        }
        if (actor.getRole() == Role.BRANCH_MANAGER
                && !Set.of(Role.COACH, Role.DIETICIAN, Role.RECEPTIONIST).contains(requestedRole)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Branch managers can only create coaches, dieticians, and receptionists");
        }
        if (requestedRole == Role.SUPER_ADMIN && actor.getRole() != Role.SUPER_ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException("Only super admins can assign SUPER_ADMIN");
        }
    }

    private boolean requiresBranch(Role role) {
        return role == Role.BRANCH_MANAGER || role == Role.COACH || role == Role.FITNESS_COACH
                || role == Role.DIETICIAN || role == Role.RECEPTIONIST || role == Role.CLIENT;
    }

    private record Scope(Organization organization, Branch branch) { }
}
