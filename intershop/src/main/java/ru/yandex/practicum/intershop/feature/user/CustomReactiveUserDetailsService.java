package ru.yandex.practicum.intershop.feature.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public CustomReactiveUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> CustomUserDetails.customUserDetailsBuilder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities(Arrays.stream(user.getRoles().split(","))
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList())
                        .accountNonExpired(user.isActive())
                        .credentialsNonExpired(user.isActive())
                        .accountNonLocked(user.isActive())
                        .enabled(user.isActive())
                        .build()
                );
    }
}
