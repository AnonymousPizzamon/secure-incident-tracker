package com.alden.logmonitoringapp.repository;
import com.alden.logmonitoringapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<User, Long> {
}