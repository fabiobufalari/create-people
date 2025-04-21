package com.bufalari.createpeople.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Como não temos um banco de usuários aqui, retornamos um user dummy.
        // No futuro, podemos consultar o MS de autenticação ou manter cache.

        return User.builder()
                .username(username)
                .password("") // Não é usado, pois estamos só autenticando via token JWT
                .authorities(Collections.emptyList())
                .build();
    }
}
