package com.gymmanagement.security;

import com.gymmanagement.branch.Branch;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.user.Role;
import com.gymmanagement.user.User;
import com.gymmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantAccessService {

    private final UserRepository userRepository;

    public User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public void assertOrganizationAccess(Long organizationId) {
        User user = currentUser();
        if (user.getRole() == Role.SUPER_ADMIN) {
            return;
        }
        if (user.getOrganization() == null || !user.getOrganization().getId().equals(organizationId)) {
            throw new AccessDeniedException("Organization access denied");
        }
    }

    public void assertBranchAccess(Branch branch) {
        User user = currentUser();
        if (user.getRole() == Role.SUPER_ADMIN) {
            return;
        }
        if (user.getOrganization() == null
                || !user.getOrganization().getId().equals(branch.getOrganization().getId())) {
            throw new AccessDeniedException("Branch access denied");
        }
        if (user.getRole() != Role.ORGANIZATION_ADMIN
                && (user.getBranch() == null || !user.getBranch().getId().equals(branch.getId()))) {
            throw new AccessDeniedException("Branch access denied");
        }
    }
}