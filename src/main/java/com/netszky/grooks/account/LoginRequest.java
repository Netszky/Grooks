package com.netszky.grooks.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @Email(message = "Doit etre un email valide") String email, @NotBlank(message =  "Le password ne doit pas etre null") String password) {
}
