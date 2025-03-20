package com.finance.expensetracker.repository;

import com.finance.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> { // The UserRepository extends JpaRepository<User, Long> to connect to the User entity
    Optional<User> findByUsername(String username); // The findByUsername method helps find a user by their username
}
