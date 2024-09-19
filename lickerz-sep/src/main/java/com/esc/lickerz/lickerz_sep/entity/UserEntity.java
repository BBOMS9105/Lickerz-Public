package com.esc.lickerz.lickerz_sep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "password_salt", nullable = false, length = 255)
    private String passwordSalt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "role", nullable = false)
    private String role;

    // 기본 생성자
    public UserEntity() {}

    // 생성자
    public UserEntity(String username, String email, String passwordHash, String passwordSalt) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    @PrePersist
    protected void onCreate(){
        createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = new Date();
    }
}
