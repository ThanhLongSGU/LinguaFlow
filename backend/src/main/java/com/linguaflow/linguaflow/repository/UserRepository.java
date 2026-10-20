package com.linguaflow.linguaflow.repository;

import com.linguaflow.linguaflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}