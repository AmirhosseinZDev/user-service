package com.ftgo.user.userservice.security.config;

import com.ftgo.user.userservice.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for setting up the authentication manager and authentication provider.
 */
@Configuration
public class AuthenticationManagerConfig {
    /**
     * Configures a `DaoAuthenticationProvider` bean.
     *
     * @param userDetailsService The custom user details service used to load user-specific data.
     * @param passwordEncoder The password encoder used to encode and verify passwords.
     * @return A configured `DaoAuthenticationProvider` instance.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Configures the `AuthenticationManager` bean.
     *
     * @param config The authentication configuration used to create the authentication manager.
     * @return A configured `AuthenticationManager` instance.
     * @throws Exception If an error occurs while creating the authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
