package com.example.QLTuyenDung.dto;

import lombok.Data;

@Data
public class UserRoleDTO {
    private Long id;
    private String email;
    private String username;
    private Boolean enabled;
    private String hoTen;
    private String soDienThoai;
    private String diaChi;
    private Long roleId;
    private String roleName;
}