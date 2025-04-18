package com.ftgo.user.userservice.security.service;

import com.ftgo.user.userservice.entity.BaseUser;
import com.ftgo.user.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // Inject your repository for user data (for example, a JPA repository)
    private final UserRepository userRepository;

    /**
     * Load user by username.
     *
     * @param username the username
     * @return the user details
     * @throws UsernameNotFoundException the username not found exception
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Retrieve user from database
        BaseUser baseUser = userRepository.findByUsername(username);
        if (baseUser == null) {
            throw new UsernameNotFoundException("User not found with username " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                baseUser.getUsername(),
                baseUser.getPassword(),
                baseUser.getAuthorities()  // assuming authorities is defined as a collection of GrantedAuthority
        );
    }
}

