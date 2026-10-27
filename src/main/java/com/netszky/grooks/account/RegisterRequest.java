package com.netszky.grooks.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide") @Size(max = 255, message = "L'email doit faire au maximum 255 caractères.")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères.")
        String password,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 255, message = "Le prénom doit faire au maximum 255 caractères.")
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 255, message = "Le nom doit faire au maximum 255 caractères.")
        String lastName
)
{
}