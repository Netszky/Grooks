package com.netszky.grooks.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException e) {
        ApiError error = new ApiError(
                "INVALID_CREDENTIALS",
                "Email ou mot de passe incorrect"
        );
        log.warn("Échec d'authentification : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException e) {
        ApiError error = new ApiError(
                "FORBIDDEN",
                "Vous n'êtes pas autorisé à accéder à cette ressource."
        );
        log.warn("Accès non autorisé : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleInvalidArgumentException(MethodArgumentNotValidException e) {
        String message = "Le corps de la requête est invalide";

        FieldError fieldError = e.getBindingResult().getFieldError();

        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            message = fieldError.getDefaultMessage();
        }
        ApiError error = new ApiError(
                "VALIDATION_ERROR",
                message
        );
        log.warn("Argument Invalide : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedRequestException(HttpMessageNotReadableException e) {
        ApiError error = new ApiError(
                "MALFORMED_REQUEST",
                "La requête est incorrecte."
        );
        log.warn("Requete mal formée : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception e) {
        ApiError error = new ApiError(
                "INTERNAL_ERROR",
                "Une erreur interne est survenue."
        );
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
