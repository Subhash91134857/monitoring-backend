package com.subhash.apimonitoring.security.service;


import com.subhash.apimonitoring.security.model.CustomUserPrincipal;
import com.subhash.apimonitoring.user.entity.User;
import com.subhash.apimonitoring.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.isActive()) {
            log.warn("User account is disabled: {}", email);
            throw new UsernameNotFoundException("User account is disabled");
        }

        if (user.isAccountLockedAndValid()) {
            log.warn("User account is locked: {}", email);
            throw new UsernameNotFoundException("User account is locked");
        }

        return new CustomUserPrincipal(user);
    }
}
