package com.netszky.grooks.common.error;

public record ApiError(String code, String message) {
    public static ApiError forbidden() {
        return new ApiError("FORBIDDEN", "Vous n'êtes pas autorisé à accéder à cette ressource.");
    }

    public static ApiError unauthorized() {
        return new ApiError("UNAUTHORIZED", "Authentification requise");
    }
}
