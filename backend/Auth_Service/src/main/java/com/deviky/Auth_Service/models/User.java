package com.deviky.Auth_Service.models;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(schema = "auth", name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "email_confirmed")
    private boolean emailConfirmed;
}
