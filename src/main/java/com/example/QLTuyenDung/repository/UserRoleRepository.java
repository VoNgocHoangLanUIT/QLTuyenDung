package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.model.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    UserRole findByUserIdAndRoleId(Long userId, Long roleId);
    List<UserRole> findByUser(User user);
}
