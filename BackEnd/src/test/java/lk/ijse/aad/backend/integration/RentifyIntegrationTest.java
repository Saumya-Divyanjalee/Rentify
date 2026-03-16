//package lk.ijse.aad.backend.integration;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import lk.ijse.aad.backend.dto.AuthDTO;
//import lk.ijse.aad.backend.dto.BookingDTO;
//import lk.ijse.aad.backend.dto.VehicleDTO;
//import lk.ijse.aad.backend.entity.User;
//import lk.ijse.aad.backend.entity.Vehicle;
//import lk.ijse.aad.backend.enums.Role;
//import lk.ijse.aad.backend.enums.VehicleStatus;
//import lk.ijse.aad.backend.enums.VehicleType;
//import lk.ijse.aad.backend.repository.BookingRepository;
//import lk.ijse.aad.backend.repository.UserRepository;
//import lk.ijse.aad.backend.repository.VehicleRepository;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import java.time.LocalDate;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")  // Uses application-test.properties (H2)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@DisplayName("🔗 Integration Tests - Full Flow")
//class RentifyIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired private UserRepository userRepository;
//    @Autowired private VehicleRepository vehicleRepository;
//    @Autowired private BookingRepository bookingRepository;
//    @Autowired private PasswordEncoder passwordEncoder;
//
//    private final ObjectMapper mapper = new ObjectMapper()
//            .registerModule(new JavaTimeModule());
//
//    // Shared state across tests (ordered)
//    private static String userToken;
//    private static String adminToken;
//    private static Long vehicleId;
//    private static Long bookingId;
//
//    // ==================== SETUP: Seed admin user ====================
//
//    @BeforeEach
//    void seedAdmin() {
//        if (!userRepository.existsByEmail("admin@rentify.com")) {
//            User admin = new User();
//            admin.setName("Admin");
//            admin.setEmail("admin@rentify.com");
//            admin.setPassword(passwordEncoder.encode("admin123"));
//            admin.setRole(Role.ADMIN);
//            userRepository.save(admin);
//        }
//    }
//
//    // ==================== 1. REGISTER ====================
//
//    @Test
//    @Order(1)
//    @DisplayName("✅ Step 1: Register new user")
//    void step1_RegisterUser() throws Exception {
//        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest();
//        request.setName("Saumyaa");
//        request.setEmail("saumyaa@rentify.com");
//        request.setPassword("pass123");
//        request.setPhone("0771234567");
//
//        mockMvc.perform(post("/api/auth/register")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Registration successful."));
//    }
//
//    @Test
//    @Order(2)
//    @DisplayName("❌ Step 2: Register same email again → should fail")
//    void step2_RegisterDuplicateEmail_ShouldFail() throws Exception {
//        AuthDTO.RegisterRequest request = new AuthDTO.RegisterRequest();
//        request.setName("Duplicate");
//        request.setEmail("saumyaa@rentify.com"); // Already exists
//        request.setPassword("pass123");
//
//        mockMvc.perform(post("/api/auth/register")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    // ==================== 2. LOGIN ====================
//
//    @Test
//    @Order(3)
//    @DisplayName("✅ Step 3: User login → receives JWT token")
//    void step3_UserLogin_ShouldReturnToken() throws Exception {
//        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest();
//        request.setEmail("saumyaa@rentify.com");
//        request.setPassword("pass123");
//
//        MvcResult result = mockMvc.perform(post("/api/auth/login")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").exists())
//                .andExpect(jsonPath("$.role").value("USER"))
//                .andReturn();
//
//        String body = result.getResponse().getContentAsString();
//        userToken = mapper.readTree(body).get("token").asText();
//        assertThat(userToken).isNotBlank();
//    }
//
//    @Test
//    @Order(4)
//    @DisplayName("✅ Step 4: Admin login → receives JWT token")
//    void step4_AdminLogin_ShouldReturnToken() throws Exception {
//        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest();
//        request.setEmail("admin@rentify.com");
//        request.setPassword("admin123");
//
//        MvcResult result = mockMvc.perform(post("/api/auth/login")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.role").value("ADMIN"))
//                .andReturn();
//
//        String body = result.getResponse().getContentAsString();
//        adminToken = mapper.readTree(body).get("token").asText();
//        assertThat(adminToken).isNotBlank();
//    }
//
//    @Test
//    @Order(5)
//    @DisplayName("❌ Step 5: Wrong password login → 401")
//    void step5_WrongPassword_ShouldReturn401() throws Exception {
//        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest();
//        request.setEmail("saumyaa@rentify.com");
//        request.setPassword("wrongpass");
//
//        mockMvc.perform(post("/api/auth/login")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized());
//    }
//
//    // ==================== 3. VEHICLE CRUD ====================
//
//    @Test
//    @Order(6)
//    @DisplayName("✅ Step 6: Admin adds a vehicle")
//    void step6_AdminAddsVehicle() throws Exception {
//        VehicleDTO dto = new VehicleDTO();
//        dto.setModel("Toyota Prius");
//        dto.setPlateNumber("CAB-0001");
//        dto.setCapacity(4);
//        dto.setYear(2022);
//        dto.setType(VehicleType.CAB);
//        dto.setStatus(VehicleStatus.AVAILABLE);
//        dto.setPricePerDay(5000.0);
//        dto.setDescription("Test cab");
//
//        MvcResult result = mockMvc.perform(post("/api/vehicles")
//                        .with(csrf())
//                        .header("Authorization", "Bearer " + adminToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(dto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.model").value("Toyota Prius"))
//                .andExpect(jsonPath("$.status").value("AVAILABLE"))
//                .andReturn();
//
//        String body = result.getResponse().getContentAsString();
//        vehicleId = mapper.readTree(body).get("id").asLong();
//        assertThat(vehicleId).isPositive();
//    }
//
//    @Test
//    @Order(7)
//    @DisplayName("✅ Step 7: Anyone can browse vehicles (public)")
//    void step7_GetAllVehicles_NoAuth() throws Exception {
//        mockMvc.perform(get("/api/vehicles"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isArray());
//    }
//
//    @Test
//    @Order(8)
//    @DisplayName("✅ Step 8: Filter vehicles by type CAB")
//    void step8_FilterByType_CAB() throws Exception {
//        mockMvc.perform(get("/api/vehicles/type/CAB"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].type").value("CAB"));
//    }
//
//    // ==================== 4. BOOKING FLOW ====================
//
//    @Test
//    @Order(9)
//    @DisplayName("✅ Step 9: User creates a booking")
//    void step9_UserCreatesBooking() throws Exception {
//        Long userId = userRepository.findByEmail("saumyaa@rentify.com")
//                .orElseThrow().getId();
//
//        BookingDTO dto = new BookingDTO();
//        dto.setUserId(userId);
//        dto.setVehicleId(vehicleId);
//        dto.setStartDate(LocalDate.now().plusDays(1));
//        dto.setEndDate(LocalDate.now().plusDays(4)); // 3 days
//        dto.setPickupLocation("Colombo");
//        dto.setDropLocation("Kandy");
//
//        MvcResult result = mockMvc.perform(post("/api/bookings")
//                        .with(csrf())
//                        .header("Authorization", "Bearer " + userToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(dto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("PENDING"))
//                .andExpect(jsonPath("$.totalPrice").value(15000.0)) // 3 × 5000
//                .andReturn();
//
//        String body = result.getResponse().getContentAsString();
//        bookingId = mapper.readTree(body).get("id").asLong();
//
//        // Verify vehicle is now BOOKED in DB
//        Vehicle v = vehicleRepository.findById(vehicleId).orElseThrow();
//        assertThat(v.getStatus()).isEqualTo(VehicleStatus.BOOKED);
//    }
//
//    @Test
//    @Order(10)
//    @DisplayName("❌ Step 10: Cannot book same vehicle again (already BOOKED)")
//    void step10_DoubleBooking_ShouldFail() throws Exception {
//        Long userId = userRepository.findByEmail("saumyaa@rentify.com")
//                .orElseThrow().getId();
//
//        BookingDTO dto = new BookingDTO();
//        dto.setUserId(userId);
//        dto.setVehicleId(vehicleId);
//        dto.setStartDate(LocalDate.now().plusDays(5));
//        dto.setEndDate(LocalDate.now().plusDays(7));
//        dto.setPickupLocation("Galle");
//        dto.setDropLocation("Colombo");
//
//        mockMvc.perform(post("/api/bookings")
//                        .with(csrf())
//                        .header("Authorization", "Bearer " + userToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(mapper.writeValueAsString(dto)))
//                .andExpect(status().is5xxServerError()); // Vehicle not available
//    }
//
//    @Test
//    @Order(11)
//    @DisplayName("✅ Step 11: Admin confirms booking")
//    void step11_AdminConfirmsBooking() throws Exception {
//        mockMvc.perform(put("/api/bookings/" + bookingId + "/status")
//                        .with(csrf())
//                        .header("Authorization", "Bearer " + adminToken)
//                        .param("status", "CONFIRMED"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("CONFIRMED"));
//    }
//
//    @Test
//    @Order(12)
//    @DisplayName("✅ Step 12: Admin completes booking → vehicle becomes AVAILABLE again")
//    void step12_AdminCompletesBooking_VehicleFree() throws Exception {
//        mockMvc.perform(put("/api/bookings/" + bookingId + "/status")
//                        .with(csrf())
//                        .header("Authorization", "Bearer " + adminToken)
//                        .param("status", "COMPLETED"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("COMPLETED"));
//
//        // Verify vehicle is AVAILABLE again
//        Vehicle v = vehicleRepository.findById(vehicleId).orElseThrow();
//        assertThat(v.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
//    }
//
//    // ==================== 5. CLEANUP / SECURITY ====================
//
//    @Test
//    @Order(13)
//    @DisplayName("❌ Step 13: USER cannot access admin-only endpoint (GET all bookings)")
//    void step13_UserCannotAccessAdminEndpoints() throws Exception {
//        mockMvc.perform(get("/api/bookings")
//                        .header("Authorization", "Bearer " + userToken))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @Order(14)
//    @DisplayName("✅ Step 14: Admin can delete vehicle")
//    void step14_AdminDeletesVehicle() throws Exception {
//        mockMvc.perform(delete("/api/vehicles/" + vehicleId)
//                        .with(csrf())
//                        .header("Authorization", "Bearer " + adminToken))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Vehicle deleted successfully."));
//
//        // Vehicle should be gone from DB
//        assertThat(vehicleRepository.findById(vehicleId)).isEmpty();
//    }
//}
