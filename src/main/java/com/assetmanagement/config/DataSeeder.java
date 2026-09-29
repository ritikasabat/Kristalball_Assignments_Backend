package com.assetmanagement.config;

import com.assetmanagement.entity.Asset;
import com.assetmanagement.entity.Assignment;
import com.assetmanagement.entity.Base;
import com.assetmanagement.entity.Expenditure;
import com.assetmanagement.entity.Purchase;
import com.assetmanagement.entity.Role;
import com.assetmanagement.entity.Transfer;
import com.assetmanagement.entity.User;
import com.assetmanagement.repository.AssetRepository;
import com.assetmanagement.repository.AssignmentRepository;
import com.assetmanagement.repository.BaseRepository;
import com.assetmanagement.repository.ExpenditureRepository;
import com.assetmanagement.repository.PurchaseRepository;
import com.assetmanagement.repository.RoleRepository;
import com.assetmanagement.repository.TransferRepository;
import com.assetmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Component
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final BaseRepository baseRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final PurchaseRepository purchaseRepository;
    private final TransferRepository transferRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExpenditureRepository expenditureRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            RoleRepository roleRepository,
            BaseRepository baseRepository,
            UserRepository userRepository,
            AssetRepository assetRepository,
            PurchaseRepository purchaseRepository,
            TransferRepository transferRepository,
            AssignmentRepository assignmentRepository,
            ExpenditureRepository expenditureRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.baseRepository = baseRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.purchaseRepository = purchaseRepository;
        this.transferRepository = transferRepository;
        this.assignmentRepository = assignmentRepository;
        this.expenditureRepository = expenditureRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = role("ADMIN");
        Role commanderRole = role("BASE_COMMANDER");
        Role logisticsRole = role("LOGISTICS_OFFICER");

        Base baseA = base("Base A", "Location A");
        Base baseB = base("Base B", "Location B");
        Base baseC = base("Base C", "Location C");

        user("System Administrator", "admin@example.com", "Admin@123", adminRole, null);
        user("Base Commander", "commander@example.com", "Commander@123", commanderRole, baseA);
        user("Logistics Officer", "logistics@example.com", "Logistics@123", logisticsRole, null);

        if (assetRepository.count() > 0) {
            return;
        }

        Asset vehiclesA = asset("AST-VEH-A01", "Tactical Vehicle", "VEHICLE", 40, baseA);
        Asset equipmentA = asset("AST-EQP-A01", "Field Equipment", "EQUIPMENT", 80, baseA);
        Asset commsA = asset("AST-COM-A01", "Radio Set", "COMMUNICATION", 25, baseA);
        Asset vehiclesB = asset("AST-VEH-B01", "Tactical Vehicle", "VEHICLE", 18, baseB);
        Asset supplyC = asset("AST-SUP-C01", "Ration Pack", "SUPPLY", 120, baseC);

        purchase(vehiclesA, baseA, 20, LocalDate.of(2026, 9, 10), "1500000.00", "Defense Supplier Co");
        purchase(equipmentA, baseA, 40, LocalDate.of(2026, 9, 12), "220000.00", "Field Gear Ltd");
        purchase(vehiclesB, baseB, 10, LocalDate.of(2026, 9, 15), "800000.00", "Defense Supplier Co");
        purchase(commsA, baseA, 15, LocalDate.of(2026, 9, 18), "95000.00", "Comms India");
        purchase(supplyC, baseC, 50, LocalDate.of(2026, 9, 20), "45000.00", "Supply Chain Unit");

        Instant t1 = LocalDate.of(2026, 9, 22).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant t2 = LocalDate.of(2026, 9, 24).atStartOfDay().toInstant(ZoneOffset.UTC);
        transfer(vehiclesA, baseA, baseB, 5, t1, "Operational reallocation");
        transfer(equipmentA, baseA, baseC, 10, t2, "Support for Base C exercise");

        assignment(vehiclesA, baseA, "John Carter", 1, Instant.now().minus(4, ChronoUnit.DAYS), "Patrol assignment");
        assignment(commsA, baseA, "Priya Nair", 2, Instant.now().minus(2, ChronoUnit.DAYS), "Communications duty");
        expenditure(equipmentA, baseA, 8, "Expended during field drill", Instant.now().minus(1, ChronoUnit.DAYS));
        expenditure(supplyC, baseC, 20, "Consumed rations", Instant.now().minus(1, ChronoUnit.DAYS));
    }

    private Role role(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            return roleRepository.save(role);
        });
    }

    private Base base(String name, String location) {
        return baseRepository.findByName(name).orElseGet(() -> {
            Base base = new Base();
            base.setName(name);
            base.setLocation(location);
            return baseRepository.save(base);
        });
    }

    private void user(String name, String email, String password, Role role, Base base) {
        userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setBase(base);
            return userRepository.save(user);
        });
    }

    private Asset asset(String code, String name, String type, int qty, Base base) {
        Asset asset = new Asset();
        asset.setAssetCode(code);
        asset.setName(name);
        asset.setEquipmentType(type);
        asset.setQuantity(qty);
        asset.setBase(base);
        asset.setStatus("AVAILABLE");
        return assetRepository.save(asset);
    }

    private void purchase(Asset asset, Base base, int qty, LocalDate date, String cost, String supplier) {
        Purchase purchase = new Purchase();
        purchase.setAsset(asset);
        purchase.setBase(base);
        purchase.setQuantity(qty);
        purchase.setPurchaseDate(date);
        purchase.setCost(new BigDecimal(cost));
        purchase.setSupplier(supplier);
        purchase.setDescription("Seeded purchase");
        purchaseRepository.save(purchase);
    }

    private void transfer(Asset asset, Base from, Base to, int qty, Instant when, String remarks) {
        Transfer transfer = new Transfer();
        transfer.setAsset(asset);
        transfer.setFromBase(from);
        transfer.setToBase(to);
        transfer.setQuantity(qty);
        transfer.setTransferDate(when);
        transfer.setRemarks(remarks);
        transferRepository.save(transfer);
    }

    private void assignment(Asset asset, Base base, String personnel, int qty, Instant when, String remarks) {
        Assignment assignment = new Assignment();
        assignment.setAsset(asset);
        assignment.setBase(base);
        assignment.setPersonnelName(personnel);
        assignment.setQuantity(qty);
        assignment.setAssignedAt(when);
        assignment.setRemarks(remarks);
        assignmentRepository.save(assignment);
    }

    private void expenditure(Asset asset, Base base, int qty, String reason, Instant when) {
        Expenditure expenditure = new Expenditure();
        expenditure.setAsset(asset);
        expenditure.setBase(base);
        expenditure.setQuantity(qty);
        expenditure.setReason(reason);
        expenditure.setExpendedAt(when);
        expenditureRepository.save(expenditure);
    }
}
