package traqueur_flotte.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import traqueur_flotte.DTO.ApiErrorResponse;
import traqueur_flotte.services.UserApiException;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserApiException.class)
    public ResponseEntity<ApiErrorResponse> handleUserApiException(UserApiException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ApiErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation() {
        return ResponseEntity.status(409)
                .body(new ApiErrorResponse("Une valeur unique est déjà utilisée."));
    }
}