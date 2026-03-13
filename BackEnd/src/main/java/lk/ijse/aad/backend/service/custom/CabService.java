package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.CabDTO;

import java.util.List;

public interface CabService {
    void saveCab(CabDTO cabDTO);
    List<CabDTO> getAllCabs();
    List<CabDTO> getAvailableCabs();
    CabDTO getCabById(Integer id);
    byte[] getCabImage(Integer id);
    String getCabImageType(Integer id);
    void updateCab(Integer id, CabDTO cabDTO);
    void deleteCab(Integer id);
}