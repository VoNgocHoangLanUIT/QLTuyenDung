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
    
        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email cannot be empty");
        }
        
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        Collection<GrantedAuthority> grantedAuthoritySet = new HashSet<>();
        Set<UserRole> roles = user.getUserRoles();
        UserRole primaryRole = null;
        
        for (UserRole role : roles) {
            if ("ADMIN".equals(role.getRole().getName())) {
                primaryRole = role;
                break;
            }
        }
        
        if (primaryRole == null) {
            for (UserRole role : roles) {
                if ("RECRUITER".equals(role.getRole().getName())) {
                    primaryRole = role;
                    break;
                }
            }
        }
        
        if (primaryRole == null && !roles.isEmpty()) {
            primaryRole = roles.iterator().next();
        }
        for (UserRole userRole : roles) {
            grantedAuthoritySet.add(new SimpleGrantedAuthority(userRole.getRole().getName()));
        }
        return new CustomUserDetail(user, grantedAuthoritySet, primaryRole);
    }

    
}
