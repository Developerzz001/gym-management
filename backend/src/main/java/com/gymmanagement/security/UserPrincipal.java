package com.gymmanagement.security;

import com.gymmanagement.organization.OrganizationStatus;
import com.gymmanagement.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String fullName;
    private final String role;
    private final boolean active;
    private final Long organizationId;
    private final Long branchId;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.role = user.getRole().name();
        this.active = user.isActive() && (user.getOrganization() == null
            || user.getOrganization().getStatus() == OrganizationStatus.ACTIVE);
        this.organizationId = user.getOrganization() == null ? null : user.getOrganization().getId();
        this.branchId = user.getBranch() == null ? null : user.getBranch().getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
