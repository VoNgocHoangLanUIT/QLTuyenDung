package com.example.QLTuyenDung.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.model.UserRole;
import com.example.QLTuyenDung.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Attempting to authenticate with email: " + email);
    
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email is empty or null");
            throw new UsernameNotFoundException("Email cannot be empty");
        }
        
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        System.out.println("Found user: " + user.getEmail());
        System.out.println("User password hash: " + user.getPassword());
        Collection<GrantedAuthority> grantedAuthoritySet = new HashSet<>();
        Set<UserRole> roles = user.getUserRoles();
        UserRole adminRole = roles.stream()
            .filter(role -> "ADMIN".equals(role.getRole().getName()))
            .findFirst()
            .orElse(roles.iterator().next());
        for (UserRole userRole : roles) {
            grantedAuthoritySet.add(new SimpleGrantedAuthority(userRole.getRole().getName()));
        }
        return new CustomUserDetail(user, grantedAuthoritySet, adminRole);
    }

    
}
