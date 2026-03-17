package lk.ijse.aad.backend.service.impl;

import jakarta.transaction.Transactional;
import lk.ijse.aad.backend.dto.InsuranceDTO;
import lk.ijse.aad.backend.entity.Insurance;
import lk.ijse.aad.backend.entity.Vehicle;
import lk.ijse.aad.backend.enums.InsuranceStatus;
import lk.ijse.aad.backend.exception.ResourceNotFoundException;
import lk.ijse.aad.backend.repository.InsuranceRepository;
import lk.ijse.aad.backend.repository.VehicleRepository;
import lk.ijse.aad.backend.service.custom.InsuranceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl implements InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final VehicleRepository vehicleRepository;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private InsuranceDTO toDTO(Insurance ins) {
        InsuranceDTO dto = new InsuranceDTO();
        dto.setId(ins.getId());
        dto.setVehicleId(ins.getVehicle().getId());
        dto.setVehiclePlateNumber(ins.getVehicle().getPlateNumber());
        dto.setPolicyNumber(ins.getPolicyNumber());
        dto.setProviderName(ins.getProviderName());
        dto.setCoverageType(ins.getCoverageType());
        dto.setPremiumAmount(ins.getPremiumAmount());
        dto.setCoverageAmount(ins.getCoverageAmount());
        dto.setStartDate(ins.getStartDate());
        dto.setExpiryDate(ins.getExpiryDate());
        dto.setStatus(ins.getStatus());
        dto.setNotes(ins.getNotes());
        return dto;
    }

    private Insurance toEntity(InsuranceDTO dto, Vehicle vehicle) {
        Insurance ins = new Insurance();
        ins.setVehicle(vehicle);
        ins.setPolicyNumber(dto.getPolicyNumber());
        ins.setProviderName(dto.getProviderName());
        ins.setCoverageType(dto.getCoverageType());
        ins.setPremiumAmount(dto.getPremiumAmount());
        ins.setCoverageAmount(dto.getCoverageAmount());
        ins.setStartDate(dto.getStartDate());
        ins.setExpiryDate(dto.getExpiryDate());
        // Auto-derive status from dates if not supplied
        ins.setStatus(dto.getStatus() != null ? dto.getStatus() : deriveStatus(dto));
        ins.setNotes(dto.getNotes());
        return ins;
    }

    /** Derive ACTIVE/EXPIRED based on expiry date when status is not explicitly set. */
    private InsuranceStatus deriveStatus(InsuranceDTO dto) {
        if (dto.getExpiryDate() != null && dto.getExpiryDate().isBefore(LocalDate.now())) {
            return InsuranceStatus.EXPIRED;
        }
        return InsuranceStatus.ACTIVE;
    }

    private Vehicle findVehicleOrThrow(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> {
                    log.error("Vehicle not found: {}", vehicleId);
                    return new ResourceNotFoundException("Vehicle not found: " + vehicleId);
                });
    }

    // ── Read operations ──────────────────────────────────────────────────────

    @Override
    public List<InsuranceDTO> getAllPolicies() {
        log.debug("Fetching all insurance policies");
        List<InsuranceDTO> list = insuranceRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
        log.info("getAllPolicies → {} results", list.size());
        return list;
    }

    @Override
    public InsuranceDTO getPolicyById(Long id) {
        log.debug("Fetching insurance policy id={}", id);
        Insurance ins = insuranceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Insurance policy not found: {}", id);
                    return new ResourceNotFoundException("Insurance policy not found: " + id);
                });
        return toDTO(ins);
    }

    @Override
    public List<InsuranceDTO> getPoliciesByVehicle(Long vehicleId) {
        log.debug("Fetching insurance policies for vehicle id={}", vehicleId);
        // Validate vehicle exists first
        findVehicleOrThrow(vehicleId);
        return insuranceRepository.findByVehicleId(vehicleId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<InsuranceDTO> getPoliciesByStatus(InsuranceStatus status) {
        log.debug("Fetching insurance policies by status={}", status);
        return insuranceRepository.findByStatus(status)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<InsuranceDTO> getActivePoliciesByVehicle(Long vehicleId) {
        log.debug("Fetching ACTIVE policies for vehicle id={}", vehicleId);
        findVehicleOrThrow(vehicleId);
        return insuranceRepository.findByVehicleIdAndStatus(vehicleId, InsuranceStatus.ACTIVE)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<InsuranceDTO> getExpiringPolicies(int daysAhead) {
        LocalDate cutoff = LocalDate.now().plusDays(daysAhead);
        log.debug("Fetching ACTIVE policies expiring before {}", cutoff);
        return insuranceRepository.findByExpiryDateBeforeAndStatus(cutoff, InsuranceStatus.ACTIVE)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Write operations ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public InsuranceDTO addPolicy(InsuranceDTO dto) {
        log.info("Adding insurance policy: policyNumber={}, vehicleId={}", dto.getPolicyNumber(), dto.getVehicleId());

        // Duplicate policy number check
        if (insuranceRepository.existsByPolicyNumber(dto.getPolicyNumber())) {
            throw new RuntimeException("Policy number already exists: " + dto.getPolicyNumber());
        }

        // Validate date range
        validateDates(dto);

        Vehicle vehicle = findVehicleOrThrow(dto.getVehicleId());
        Insurance saved = insuranceRepository.save(toEntity(dto, vehicle));
        log.info("Insurance policy saved id={}", saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public InsuranceDTO updatePolicy(Long id, InsuranceDTO dto) {
        log.info("Updating insurance policy id={}", id);

        Insurance existing = insuranceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance policy not found: " + id));

        // Allow policy number change only if not used by another record
        if (!existing.getPolicyNumber().equals(dto.getPolicyNumber())
                && insuranceRepository.existsByPolicyNumber(dto.getPolicyNumber())) {
            throw new RuntimeException("Policy number already exists: " + dto.getPolicyNumber());
        }

        validateDates(dto);

        Vehicle vehicle = findVehicleOrThrow(dto.getVehicleId());

        existing.setVehicle(vehicle);
        existing.setPolicyNumber(dto.getPolicyNumber());
        existing.setProviderName(dto.getProviderName());
        existing.setCoverageType(dto.getCoverageType());
        existing.setPremiumAmount(dto.getPremiumAmount());
        existing.setCoverageAmount(dto.getCoverageAmount());
        existing.setStartDate(dto.getStartDate());
        existing.setExpiryDate(dto.getExpiryDate());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : deriveStatus(dto));
        existing.setNotes(dto.getNotes());

        log.info("Insurance policy updated id={}", id);
        return toDTO(insuranceRepository.save(existing));
    }

    @Override
    @Transactional
    public InsuranceDTO cancelPolicy(Long id) {
        log.warn("Cancelling insurance policy id={}", id);
        Insurance existing = insuranceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance policy not found: " + id));
        existing.setStatus(InsuranceStatus.CANCELLED);
        log.info("Insurance policy cancelled id={}", id);
        return toDTO(insuranceRepository.save(existing));
    }

    @Override
    @Transactional
    public void deletePolicy(Long id) {
        log.warn("Deleting insurance policy id={}", id);
        if (!insuranceRepository.existsById(id)) {
            log.error("Insurance policy not found for delete, id={}", id);
            throw new ResourceNotFoundException("Insurance policy not found: " + id);
        }
        insuranceRepository.deleteById(id);
        log.info("Insurance policy deleted id={}", id);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    private void validateDates(InsuranceDTO dto) {
        if (dto.getStartDate() != null && dto.getExpiryDate() != null
                && !dto.getExpiryDate().isAfter(dto.getStartDate())) {
            throw new RuntimeException("Expiry date must be after start date");
        }
    }
}
