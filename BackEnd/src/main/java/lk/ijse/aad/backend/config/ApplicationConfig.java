package lk.ijse.aad.backend.config;

import lk.ijse.aad.backend.repository.AdminRepository;
import lk.ijse.aad.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;



import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository  userRepository;
    private final AdminRepository adminRepository;

    /**
     * Checks ADMIN table first  →  authority = ROLE_ADMIN
     * Falls back to USER table  →  authority = ROLE_USER
     * This is how sign-in tells admin apart from user.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {

            // 1. Check admin table first
            var adminOpt = adminRepository.findByUsername(username);
            if (adminOpt.isPresent()) {
                var a = adminOpt.get();
                return new org.springframework.security.core.userdetails.User(
                        a.getUsername(),
                        a.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
            }

            // 2. Fall back to user table
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                var u = userOpt.get();
                return new org.springframework.security.core.userdetails.User(
                        u.getUsername(),
                        u.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
                );
            }

            throw new UsernameNotFoundException("User not found: " + username);
        };
    }

    // Inject UserDetailsService as parameter — fixes red underline in IntelliJ
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}