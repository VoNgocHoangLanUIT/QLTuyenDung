package com.example.QLTuyenDung.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.example.QLTuyenDung.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
    boolean existsByName(String name);
}
