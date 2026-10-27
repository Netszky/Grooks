package com.netszky.grooks.account;

import com.netszky.grooks.common.security.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;


    public AuthService(UserRepository userRepository,  AuthenticationManager authenticationManager,  TokenService tokenService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public TokenService.GeneratedToken login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        UserEntity.normalizeEmail(
                                email
                        ), password
                ));

        var user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new IllegalStateException("User not found"));
        return  tokenService.generateToken(user.getId(),user.getRole());
    }
}
