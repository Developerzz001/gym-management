package com.gymmanagement.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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
}
