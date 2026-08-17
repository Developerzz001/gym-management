package com.gymmanagement.medicine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByClientId(Long clientId);

    List<Medicine> findByDieticianId(Long dieticianId);
}
