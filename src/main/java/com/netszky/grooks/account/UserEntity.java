package com.netszky.grooks.account;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String firstName;

    @Column(nullable = false, length = 255)
    private String lastName;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private Instant createdAt;

    protected UserEntity() {}

    private UserEntity(String email, String firstName, String lastName, String passwordHash, Role role) {
        this.email = normalizeEmail(email);
        this.firstName = requireNonBlank(firstName, "firstName");
        this.lastName =  requireNonBlank(lastName, "lastName");
        this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.createdAt = Instant.now();
    }

    public static UserEntity newMember(String email, String firstName, String lastName, String passwordHash) {
        return new UserEntity(email, firstName, lastName, passwordHash, Role.MEMBER);
    }
    public static UserEntity newCoach(String email, String firstName, String lastName, String passwordHash) {
        return new UserEntity(email, firstName, lastName, passwordHash, Role.COACH);
    }

    public static String normalizeEmail(String email) {
        return requireNonBlank(email, "email").trim().toLowerCase(Locale.ROOT);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(String.format("%s must not be null or blank", fieldName));
        }
        return value;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
