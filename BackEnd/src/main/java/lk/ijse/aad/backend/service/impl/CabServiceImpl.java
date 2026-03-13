package lk.ijse.aad.backend.service.impl;

import lk.ijse.aad.backend.dto.CabDTO;
import lk.ijse.aad.backend.entity.Cab;
import lk.ijse.aad.backend.repository.CabRepository;
import lk.ijse.aad.backend.service.custom.CabService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CabServiceImpl implements CabService {

    private final CabRepository cabRepository;

    @Override
    public void saveCab(CabDTO cabDTO) {
        if (cabRepository.existsByCabPlate(cabDTO.getCabPlate())) {
            throw new RuntimeException(
                    "Cab with plate " + cabDTO.getCabPlate() + " already exists");
        }
        cabRepository.save(mapToEntity(new Cab(), cabDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CabDTO> getAllCabs() {
        return cabRepository.findAllWithoutImage()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CabDTO> getAvailableCabs() {
        return cabRepository.findByAvailableTrue()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CabDTO getCabById(Integer id) {
        Cab cab = findOrThrow(id);
        CabDTO dto = mapToDTO(cab);
        if (cab.getCabImage() != null) {
            dto.setCabImageBase64(
                    "data:" + cab.getImageType() + ";base64," +
                            Base64.getEncoder().encodeToString(cab.getCabImage()));
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getCabImage(Integer id) {
        return findOrThrow(id).getCabImage();
    }

    @Override
    @Transactional(readOnly = true)
    public String getCabImageType(Integer id) {
        String type = findOrThrow(id).getImageType();
        return type != null ? type : "image/jpeg";
    }

    @Override
    public void updateCab(Integer id, CabDTO cabDTO) {
        Cab existing = findOrThrow(id);
        cabRepository.findByCabPlate(cabDTO.getCabPlate())
                .filter(c -> !c.getCabId().equals(id))
                .ifPresent(c -> { throw new RuntimeException("Plate already used by another cab"); });
        cabRepository.save(mapToEntity(existing, cabDTO));
    }

    @Override
    public void deleteCab(Integer id) {
        if (!cabRepository.existsById(id)) {
            throw new RuntimeException("Cab not found with id: " + id);
        }
        cabRepository.deleteById(id);
    }

    // ─── HELPERS ────────────────────────────────────────────────────────────

    private Cab findOrThrow(Integer id) {
        return cabRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cab not found with id: " + id));
    }

    private Cab mapToEntity(Cab cab, CabDTO dto) {
        cab.setCabName(dto.getCabName());
        cab.setCabModel(dto.getCabModel());
        cab.setCabPlate(dto.getCabPlate().toUpperCase());
        cab.setCabType(dto.getCabType());
        cab.setPricePerKm(dto.getPricePerKm());
        cab.setSeatCount(dto.getSeatCount());
        cab.setCabDescription(dto.getCabDescription());
        cab.setAvailable(dto.isAvailable());

        String b64 = dto.getCabImageBase64();
        if (b64 != null && !b64.isBlank()) {
            if (b64.contains(",")) {
                String header    = b64.substring(0, b64.indexOf(','));
                String imageType = header.substring(header.indexOf(':') + 1, header.indexOf(';'));
                cab.setImageType(imageType);
                cab.setCabImage(Base64.getDecoder().decode(
                        b64.substring(b64.indexOf(',') + 1)));
            } else {
                cab.setImageType(dto.getImageType() != null ? dto.getImageType() : "image/jpeg");
                cab.setCabImage(Base64.getDecoder().decode(b64));
            }
        }
        return cab;
    }

    private CabDTO mapToDTO(Cab cab) {
        CabDTO dto = new CabDTO();
        dto.setCabId(cab.getCabId());
        dto.setCabName(cab.getCabName());
        dto.setCabModel(cab.getCabModel());
        dto.setCabPlate(cab.getCabPlate());
        dto.setCabType(cab.getCabType());
        dto.setPricePerKm(cab.getPricePerKm());
        dto.setSeatCount(cab.getSeatCount());
        dto.setCabDescription(cab.getCabDescription());
        dto.setAvailable(cab.isAvailable());
        dto.setImageType(cab.getImageType());
        return dto;
    }
}