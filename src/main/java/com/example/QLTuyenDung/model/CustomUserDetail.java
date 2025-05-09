package com.example.QLTuyenDung.model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetail implements UserDetails {
    private User user;
    private Collection<? extends GrantedAuthority> authorities;
    private UserRole userRole;

    public CustomUserDetail(User user, Collection<? extends GrantedAuthority> authorities, UserRole userRole) {
        super();
        this.user = user;
        this.authorities = authorities;
        this.userRole = userRole;
    }

    public CustomUserDetail() {
        super();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }

    public UserRole getUserRole() {
        return userRole;
    }
}
