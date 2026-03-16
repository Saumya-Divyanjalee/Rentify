package lk.ijse.aad.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


//http://localhost:8081/swagger-ui/index.html
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI rentifyOpenAPI() {
        return new OpenAPI()
                //   API Info
                .info(new Info()
                        .title("🚗 Rentify API")
                        .description("""
                            **Rentify** — Smart Cab Renting Platform
                            
                            ## Authentication
                            1. Call `POST /api/auth/login` to get your JWT token
                            2. Click **Authorize** button (top right)
                            3. Enter: `Bearer <your_token>`
                            
                            ## Roles
                            | Role  | Permissions                              |
                            |-------|------------------------------------------|
                            | USER  | Browse vehicles, create/cancel bookings  |
                            | ADMIN | Full CRUD on vehicles, manage bookings   |
                            
                            ## Vehicle Availability Checks
                            Before booking, system verifies:
                            - ✅ Vehicle status is AVAILABLE
                            - ✅ Insurance is active
                            - ✅ Insurance not expired before booking end date
                            - ✅ No overlapping bookings for selected dates
                            """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Rentify Team")
                                .email("saumisaumya529@gmail.com")
                                .url("https://rentify.lk"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                //   Servers
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local Development"),
                        new Server().url("https://api.rentify.lk").description("Production")
                ))

                //   JWT Bearer Auth
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token from /api/auth/login")));
    }
}
