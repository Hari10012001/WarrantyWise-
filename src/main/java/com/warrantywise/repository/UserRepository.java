package com.warrantywise.repository;

import com.warrantywise.entity.User;
import com.warrantywise.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByIsActiveAndRole(Boolean isActive, Role role, Pageable pageable);

    long countByRole(Role role);

    long countByIsActive(Boolean isActive);
}
