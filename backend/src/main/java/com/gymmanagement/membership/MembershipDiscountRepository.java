package com.gymmanagement.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipDiscountRepository extends JpaRepository<MembershipDiscount, Long> {

    List<MembershipDiscount> findAllByOrderByIdDesc();

    List<MembershipDiscount> findByActiveTrueOrderByIdDesc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}