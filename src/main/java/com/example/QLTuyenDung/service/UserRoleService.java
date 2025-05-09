package com.example.QLTuyenDung.service;

import org.springframework.stereotype.Service;

import com.example.QLTuyenDung.model.UserRole;
import com.example.QLTuyenDung.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;
    public UserRole getUserRoleByUserIdAndRoleId(Long userId, Long roleId) {
        return userRoleRepository.findByUserIdAndRoleId(userId, roleId);
    }
}
