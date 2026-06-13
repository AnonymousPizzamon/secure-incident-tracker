package com.alden.logmonitoringapp.repository;
import com.alden.logmonitoringapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<User, Long> {

    // findBy + field returns the full entity — used by AppUserDetailsService which needs the User object
    Optional<User> findByUsername(String username);
    // Using Optional allows callers to handle the possibility of an absent value explicitly

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    // service only needs a true/false, using these methods makes DB ops less wasteful

}