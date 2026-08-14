package com.warrantywise.entity;

import com.warrantywise.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
    @Index(name="idx_users_email", columnList="email"),
    @Index(name="idx_users_role_active", columnList="role, is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password hash cannot exceed 255 characters")
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    @Column(name = "phone", length = 15)
    private String phone;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserSettings settings;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Product> products = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Notification> notifications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<Tag> tags = new ArrayList<>();
}

