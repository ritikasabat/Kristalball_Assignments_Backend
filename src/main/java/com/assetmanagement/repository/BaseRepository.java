package com.assetmanagement.repository;

import com.assetmanagement.entity.Base;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaseRepository extends JpaRepository<Base, Long> {
    Optional<Base> findByName(String name);
}
