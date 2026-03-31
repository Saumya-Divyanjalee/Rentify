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

    @Bean
    public UserDetailsService userDetailsService() {
        // lambda function -> takes username and returns UserDetails
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

            // 2.  If not admin  check USER table
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

    //Authentication process handle component
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        //Default Spring Security authentication class -  DaoAuthenticationProvider
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // set how to load user data (from DB)
        provider.setUserDetailsService(userDetailsService);
        // set how to compare passwords (BCrypt)
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        // main manager that handles login process
        return cfg.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt -> secure password hashing algorithm
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper() {
        // used to map DTO <-> Entity
        return new ModelMapper();
    }
}