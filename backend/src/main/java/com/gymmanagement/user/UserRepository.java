package com.gymmanagement.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Collection;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    @Query("select u from User u where " +
            "(:keyword is null or lower(u.firstName) like lower(concat('%', :keyword, '%')) " +
            "or lower(u.lastName) like lower(concat('%', :keyword, '%')) " +
            "or lower(u.email) like lower(concat('%', :keyword, '%'))) " +
            "and (:role is null or u.role = :role)")
    Page<User> search(@Param("keyword") String keyword, @Param("role") Role role, Pageable pageable);

            @Query("select u from User u where u.organization.id = :organizationId and u.role in :roles and " +
                "(:keyword is null or lower(u.firstName) like lower(concat('%', :keyword, '%')) " +
                "or lower(u.lastName) like lower(concat('%', :keyword, '%')) " +
                "or lower(u.email) like lower(concat('%', :keyword, '%')))")
            Page<User> searchByOrganizationAndRoles(@Param("organizationId") Long organizationId,
                @Param("roles") Collection<Role> roles, @Param("keyword") String keyword, Pageable pageable);

            @Query("select u from User u where u.branch.id = :branchId and u.role in :roles and " +
                "(:keyword is null or lower(u.firstName) like lower(concat('%', :keyword, '%')) " +
                "or lower(u.lastName) like lower(concat('%', :keyword, '%')) " +
                "or lower(u.email) like lower(concat('%', :keyword, '%')))")
            Page<User> searchByBranchAndRoles(@Param("branchId") Long branchId,
                @Param("roles") Collection<Role> roles, @Param("keyword") String keyword, Pageable pageable);

    long countByBranchIdAndRoleInAndActiveTrue(Long branchId, Collection<Role> roles);
}
