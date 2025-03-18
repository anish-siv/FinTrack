package com.finance.expensetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Automatically generate the id and increment
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // Variable to store username

    @Column(nullable = false)
    private String password; // Variable to store password

    @Column(nullable = false)
    private String fullName; // Variable to store full name

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Expense> expenses; // Variable to store expenses

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // Returning the authorities for the user
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() { // Returning true if the account is not expired
        return true;
    }

    @Override
    public boolean isAccountNonLocked() { // Returning true if the account is not locked
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() { // Returning true if the credentials are not expired
        return true;
    }

    @Override
    public boolean isEnabled() { // Returning true if the user is enabled
        return true;
    }
}
