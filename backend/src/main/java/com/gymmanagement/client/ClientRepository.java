package com.gymmanagement.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByUserId(Long userId);

    @Query("select c from Client c where c.assignedCoach.id = :coachId " +
            "and (c.registrationType is null or c.registrationType = com.gymmanagement.client.RegistrationType.REGISTERED)")
    List<Client> findRegisteredByAssignedCoachId(@Param("coachId") Long coachId);

    @Query("select c from Client c where c.assignedDietician.id = :dieticianId " +
            "and (c.registrationType is null or c.registrationType = com.gymmanagement.client.RegistrationType.REGISTERED)")
    List<Client> findRegisteredByAssignedDieticianId(@Param("dieticianId") Long dieticianId);

    List<Client> findByAssignedCoachIsNotNull();

    List<Client> findByAssignedDieticianIsNotNull();

    @Query("select c from Client c where (:registrationType is null or c.registrationType = :registrationType " +
            "or (:registrationType = com.gymmanagement.client.RegistrationType.REGISTERED and c.registrationType is null)) and (" +
            "lower(c.user.firstName) like lower(concat('%', :keyword, '%')) or " +
            "lower(c.user.lastName) like lower(concat('%', :keyword, '%')) or " +
            "lower(c.user.email) like lower(concat('%', :keyword, '%')))")
    Page<Client> search(@Param("keyword") String keyword,
                        @Param("registrationType") RegistrationType registrationType, Pageable pageable);

    @Query("select c from Client c where c.user.organization.id = :organizationId " +
            "and (:registrationType is null or c.registrationType = :registrationType " +
            "or (:registrationType = com.gymmanagement.client.RegistrationType.REGISTERED and c.registrationType is null)) and (" +
            "lower(c.user.firstName) like lower(concat('%', :keyword, '%')) or " +
            "lower(c.user.lastName) like lower(concat('%', :keyword, '%')) or " +
            "lower(c.user.email) like lower(concat('%', :keyword, '%')))" )
    Page<Client> searchByOrganization(@Param("organizationId") Long organizationId,
                                      @Param("keyword") String keyword,
                                      @Param("registrationType") RegistrationType registrationType, Pageable pageable);

    @Query("select c from Client c where c.user.branch.id = :branchId " +
            "and (:registrationType is null or c.registrationType = :registrationType " +
            "or (:registrationType = com.gymmanagement.client.RegistrationType.REGISTERED and c.registrationType is null)) and (" +
            "lower(c.user.firstName) like lower(concat('%', :keyword, '%')) or " +
            "lower(c.user.lastName) like lower(concat('%', :keyword, '%')) or " +
            "lower(c.user.email) like lower(concat('%', :keyword, '%')))" )
    Page<Client> searchByBranch(@Param("branchId") Long branchId,
                                @Param("keyword") String keyword,
                                @Param("registrationType") RegistrationType registrationType, Pageable pageable);

    long countByUserBranchId(Long branchId);
    long countByUserBranchIdAndUserActiveTrue(Long branchId);
}
