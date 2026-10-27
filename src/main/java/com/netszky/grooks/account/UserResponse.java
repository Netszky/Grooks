package com.netszky.grooks.account;

import java.util.UUID;

public record UserResponse(UUID id, String email, String role, String firstName, String lastName) {
}