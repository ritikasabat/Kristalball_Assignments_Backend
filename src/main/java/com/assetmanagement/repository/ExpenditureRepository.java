package com.assetmanagement.repository;

import com.assetmanagement.entity.Expenditure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {
    List<Expenditure> findByBaseIdOrderByExpendedAtDesc(Long baseId);

    List<Expenditure> findAllByOrderByExpendedAtDesc();
}
