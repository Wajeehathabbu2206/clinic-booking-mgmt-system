package com.medibook.user.repository;

import com.medibook.user.entity.User;
import com.medibook.common.util.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleAndEnabledTrueOrderByFullNameAsc(Role role);
    Page<User> findByRoleOrderByCreatedAtDesc(Role role, Pageable pageable);
}
