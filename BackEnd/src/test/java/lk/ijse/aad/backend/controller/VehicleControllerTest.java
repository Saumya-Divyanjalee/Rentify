package lk.ijse.aad.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.ijse.aad.backend.dto.VehicleDTO;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.enums.VehicleType;
import lk.ijse.aad.backend.service.custom.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@DisplayName("VehicleController Web Layer Tests")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @Autowired
    private ObjectMapper objectMapper;

    private VehicleDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleDTO = new VehicleDTO();
        sampleDTO.setId(1L);
        sampleDTO.setModel("Toyota Prius");
        sampleDTO.setPlateNumber("CAB-1234");
        sampleDTO.setType(VehicleType.CAB);
        sampleDTO.setStatus(VehicleStatus.AVAILABLE);
        sampleDTO.setPricePerDay(5000.0);
        sampleDTO.setCapacity(4);
        sampleDTO.setYear(2022);
    }

    // ==================== GET ALL (Public) ====================

    @Test
    @DisplayName("✅ GET /api/vehicles - public access, returns vehicle list")
    void getAllVehicles_ShouldReturn200() throws Exception {
        when(vehicleService.getAllVehicles()).thenReturn(List.of(sampleDTO));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].model").value("Toyota Prius"))
                .andExpect(jsonPath("$[0].type").value("CAB"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("✅ GET /api/vehicles/available - returns available vehicles")
    void getAvailableVehicles_ShouldReturn200() throws Exception {
        when(vehicleService.getAvailableVehicles()).thenReturn(List.of(sampleDTO));

        mockMvc.perform(get("/api/vehicles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("✅ GET /api/vehicles/type/CAB - filter by type")
    void getByType_ShouldReturn200() throws Exception {
        when(vehicleService.getVehiclesByType(VehicleType.CAB)).thenReturn(List.of(sampleDTO));

        mockMvc.perform(get("/api/vehicles/type/CAB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("CAB"));
    }

    @Test
    @DisplayName("✅ GET /api/vehicles/{id} - returns single vehicle")
    void getById_ShouldReturn200() throws Exception {
        when(vehicleService.getVehicleById(1L)).thenReturn(sampleDTO);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.model").value("Toyota Prius"));
    }

    // ==================== ADMIN: ADD VEHICLE ====================

    @Test
    @DisplayName("✅ POST /api/vehicles - ADMIN can add vehicle")
    @WithMockUser(roles = "ADMIN")
    void addVehicle_AsAdmin_ShouldReturn200() throws Exception {
        when(vehicleService.addVehicle(any(VehicleDTO.class))).thenReturn(sampleDTO);

        mockMvc.perform(post("/api/vehicles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Toyota Prius"));
    }

    @Test
    @DisplayName("❌ POST /api/vehicles - USER cannot add vehicle (403)")
    @WithMockUser(roles = "USER")
    void addVehicle_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("❌ POST /api/vehicles - No auth returns 401/403")
    void addVehicle_NoAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andExpect(status().is4xxClientError());
    }

    // ==================== ADMIN: UPDATE VEHICLE ====================

    @Test
    @DisplayName("✅ PUT /api/vehicles/{id} - ADMIN can update vehicle")
    @WithMockUser(roles = "ADMIN")
    void updateVehicle_AsAdmin_ShouldReturn200() throws Exception {
        when(vehicleService.updateVehicle(eq(1L), any(VehicleDTO.class))).thenReturn(sampleDTO);

        mockMvc.perform(put("/api/vehicles/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andExpect(status().isOk());
    }

    // ==================== ADMIN: DELETE VEHICLE ====================

    @Test
    @DisplayName("✅ DELETE /api/vehicles/{id} - ADMIN can delete vehicle")
    @WithMockUser(roles = "ADMIN")
    void deleteVehicle_AsAdmin_ShouldReturn200() throws Exception {
        doNothing().when(vehicleService).deleteVehicle(1L);

        mockMvc.perform(delete("/api/vehicles/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Vehicle deleted successfully."));
    }

    @Test
    @DisplayName("❌ DELETE /api/vehicles/{id} - USER cannot delete (403)")
    @WithMockUser(roles = "USER")
    void deleteVehicle_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/vehicles/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
