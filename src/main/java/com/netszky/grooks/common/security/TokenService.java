package com.netszky.grooks.common.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TokenService {

    public record GeneratedToken(String value, Duration maxAge) {}
    private final JwtEncoder jwtEncoder;
    private final Duration jwtDuration;

    public TokenService(JwtEncoder jwtEncoder, @Value("${grooks.jwt.duration:1h}") Duration jwtDuration) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDuration = jwtDuration;
    }


    public GeneratedToken generateToken(UUID id, Role role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("grooks")
                .issuedAt(now)
                .expiresAt(now.plus(jwtDuration))
                .subject(String.valueOf(id))
                .claim("roles", List.of(role.name()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return new GeneratedToken(jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), jwtDuration );
    }
}
