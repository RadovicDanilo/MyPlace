package com.radovicdanilo.myplace.domain

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @Size(min = 3, max = 50) @Column(unique = true) @NotNull var username: String,
    @NotNull val passwordHash: String
)