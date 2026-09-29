package com.assetmanagement.repository;

import com.assetmanagement.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByBaseIdOrderByAssignedAtDesc(Long baseId);

    List<Assignment> findAllByOrderByAssignedAtDesc();
}
