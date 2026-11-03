
package com.dianora.backend.user.repository;

import com.dianora.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findFirstByPhone(String phone);

    boolean existsByPhone(String phone);
}
