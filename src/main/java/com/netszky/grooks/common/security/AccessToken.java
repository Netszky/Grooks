package com.netszky.grooks.common.security;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public final class AccessToken {
    private AccessToken() {}
    public static final String ACCESS_TOKEN_COOKIE = "access_token";

    private static ResponseCookie buildCookie(String value, Duration maxAge) {
        return ResponseCookie.from(
                        ACCESS_TOKEN_COOKIE,
                        value)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .secure(true)
                .maxAge(maxAge)
                .build();
    }


    public static ResponseCookie createCookie(TokenService.GeneratedToken token){
        return buildCookie(token.value(), token.maxAge());
    }

    public static ResponseCookie clearCookie(){
        return buildCookie("", Duration.ZERO);
    }
}
