package com.assetmanagement.repository;

import com.assetmanagement.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByBaseId(Long baseId);

    Optional<Asset> findByNameIgnoreCaseAndEquipmentTypeIgnoreCaseAndBaseId(String name, String equipmentType, Long baseId);

    boolean existsByAssetCode(String assetCode);
}
