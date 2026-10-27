package com.netszky.grooks.account;

import com.netszky.grooks.common.security.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;


    public AuthService(UserRepository userRepository,  AuthenticationManager authenticationManager,  TokenService tokenService,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public TokenService.GeneratedToken login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        UserEntity.normalizeEmail(
                                request.email()
                        ), request.password()
                ));

        var user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new IllegalStateException("User not found"));
        return  tokenService.generateToken(user.getId(),user.getRole());
    }


    public void register(RegisterRequest request) {

        if(userRepository.existsByEmail(UserEntity.normalizeEmail(request.email()))) {
            throw new EmailAlreadyUsedException();
        }

        var encodedPassword = passwordEncoder.encode(request.password());
        UserEntity user = UserEntity.newMember(request.email(), request.firstName(), request.lastName(), encodedPassword);
        userRepository.save(user);

    }
}
