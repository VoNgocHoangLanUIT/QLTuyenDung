package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.userRoles ur WHERE u.email = :email AND ur.role.name = :roleName")
    boolean existsByEmailAndRole(@Param("email") String email, @Param("roleName") String roleName);
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    User findBySoDienThoai(String soDienThoai);
    User findByUsernameOrEmailOrSoDienThoai(String username, String email, String soDienThoai);
    User findByUsernameAndEmailAndSoDienThoai(String username, String email, String soDienThoai);
    @Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    List<User> findByCongTy(CongTy congTy);

    @Query("SELECT DISTINCT u FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE r.name = :roleName")
    Page<User> findByRoleNamePaginated(@Param("roleName") String roleName, Pageable pageable);
    
    
}
