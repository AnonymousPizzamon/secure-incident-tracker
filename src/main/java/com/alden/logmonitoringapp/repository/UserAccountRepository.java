package com.alden.logmonitoringapp.repository;
import com.alden.logmonitoringapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<User, Long> {
    //findBy + field (here the field is username from User) is a custom query method that JPA uses.
    Optional<User> findByUsername(String username);
    /**
     returning User may cause null pointer exception,
     so its better to make return type Optional.
     Optional never returns null.

    **/
}