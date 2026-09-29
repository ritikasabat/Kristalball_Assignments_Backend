package com.assetmanagement.repository;

import com.assetmanagement.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByBaseIdOrderByPurchaseDateDesc(Long baseId);

    List<Purchase> findAllByOrderByPurchaseDateDesc();
}
