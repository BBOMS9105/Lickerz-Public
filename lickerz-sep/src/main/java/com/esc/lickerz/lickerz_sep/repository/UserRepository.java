package com.esc.lickerz.lickerz_sep.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.esc.lickerz.lickerz_sep.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    
    UserEntity findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsernameAndEmail(String username, String email);
}
