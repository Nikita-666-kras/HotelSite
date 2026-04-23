package com.travelagency.repository;

import com.travelagency.domain.Role;
import com.travelagency.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRoleAndEnabledTrueOrderByIdAsc(Role role);
}
