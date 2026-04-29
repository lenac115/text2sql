package com.ai.main.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id @Column(name = "users_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String name;

    @NotNull
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "recipient",    column = @Column(name = "default_recipient")),
            @AttributeOverride(name = "phone",        column = @Column(name = "default_phone")),
            @AttributeOverride(name = "zipCode",      column = @Column(name = "default_zip_code")),
            @AttributeOverride(name = "addressLine1", column = @Column(name = "default_address_line1")),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "default_address_line2"))
    })
    private Address defaultAddress;

    @OneToMany(mappedBy = "users")
    private List<Orders> ordersList = new ArrayList<>();

    public enum Role {
        USER, ADMIN
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateDefaultAddress(Address address) {
        this.defaultAddress = address;
    }
}
