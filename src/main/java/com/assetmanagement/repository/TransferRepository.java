package com.assetmanagement.repository;

import com.assetmanagement.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findByFromBaseIdOrToBaseIdOrderByTransferDateDesc(Long fromBaseId, Long toBaseId);

    List<Transfer> findAllByOrderByTransferDateDesc();
}
