package com.example.QLTuyenDung.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.QLTuyenDung.dto.UserRoleDTO;
import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.PhongVan;
import com.example.QLTuyenDung.model.Role;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.model.UserRole;
import com.example.QLTuyenDung.repository.RoleRepository;
import com.example.QLTuyenDung.repository.TinTuyenDungRepository;
import com.example.QLTuyenDung.repository.UserRepository;
import com.example.QLTuyenDung.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final TinTuyenDungRepository tinTuyenDungRepository;
    private final DonUngTuyenService donUngTuyenService;
    private final PhongVanService phongVanService;
    private final FileStorageService fileStorageService;
    private final BCryptPasswordEncoder passwordEncoder;
    public User getUserByUserName(String userName){
        return userRepository.findByUsername(userName);
    }
    public void registerUser(String username, String email, String password, String roleName) {
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            if (userRepository.existsByEmailAndRole(email, roleName)) {
                throw new RuntimeException("Email đã được đăng ký cho vai trò này!");
            }
        
            Role role = roleRepository.findByName(roleName);
            if (role == null) {
                throw new RuntimeException("Khong tim thay role: " + roleName);
            }
            
            UserRole userRole = new UserRole();
            userRole.setUser(existingUser);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        } else {
            // Create and save new user
            Role role = roleRepository.findByName(roleName);
            if (role == null) {
                throw new RuntimeException("Khong tim thay role: " + roleName);
            }
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user = userRepository.save(user);  // Save and get the saved user with ID

            // Create and save user role
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
    }

    public List<User> getAll(){
        try {
            List<User> users = userRepository.findAll();
            System.out.println("Found " + users.size() + " users");
            users.forEach(user -> {
                System.out.println("User: " + user.getEmail() + ", Enabled: " + user.getEnabled());
            });
            return users;
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<User> getAllCandidates() {
        return userRepository.findByRoleName("CANDIDATE");
    }

    public List<UserRoleDTO> getAllUsersWithRoles() {
        try {
            List<User> users = userRepository.findAll();
            List<UserRoleDTO> userRoleDTOs = new ArrayList<>();
    
            for (User user : users) {
                List<UserRole> userRoles = userRoleRepository.findByUser(user);
                for (UserRole userRole : userRoles) {
                    UserRoleDTO dto = new UserRoleDTO();
                    dto.setId(user.getId());
                    dto.setEmail(user.getEmail());
                    dto.setUsername(user.getUsername());
                    dto.setEnabled(user.getEnabled());
                    dto.setHoTen(user.getHoTen());
                    dto.setSoDienThoai(user.getSoDienThoai());
                    dto.setDiaChi(user.getDiaChi());
                    dto.setRoleId(userRole.getRole().getId());
                    dto.setRoleName(userRole.getRole().getName());
                    userRoleDTOs.add(dto);
                }
            }
            return userRoleDTOs;
        } catch (Exception e) {
            System.err.println("Error fetching users: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void addUserWithRole(User user, String roleName) {
        // Check if email already exists
        User existingUser = userRepository.findByEmail(user.getEmail());
        
        if (existingUser != null) {
            // User exists, check if role combination exists
            if (userRepository.existsByEmailAndRole(existingUser.getEmail(), roleName)) {
                throw new RuntimeException("Email đã được đăng ký cho vai trò này!");
            }
        
            // Add new role to existing user
            Role role = roleRepository.findByName(roleName);
            if (role == null) {
                throw new RuntimeException("Role không tồn tại!");
            }
            else if (roleName.equals("HR_STAFF") || roleName.equals("CV_STAFF")) {
                existingUser.setCongTy(user.getCongTy());
            }
            UserRole userRole = new UserRole();
            userRole.setUser(existingUser);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
            
        } else {
            // Create new user
            Role role = roleRepository.findByName(roleName);
            if (role == null) {
                throw new RuntimeException("Role không tồn tại!");
            }
        
            // Encode password and set enabled
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getEnabled() == null) {
                user.setEnabled(true);
            }
        
            // Save user
            User savedUser = userRepository.save(user);
        
            // Create user role relationship
            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
    }


    public void updateUser(User user, String roleName, Long oldRoleId) {
        User existingUser = getUserById(user.getId());
        Role newRole = roleRepository.findByName(roleName);
        
        if (newRole == null) {
            throw new RuntimeException("Role không tồn tại!");
        }
        // Check if email changed and already exists for this role
        if (!existingUser.getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Không được phép thay đổi email!");
        }

        // Update basic info
        existingUser.setEmail(user.getEmail());
        existingUser.setUsername(user.getUsername());
        existingUser.setHoTen(user.getHoTen());
        existingUser.setSoDienThoai(user.getSoDienThoai());
        existingUser.setDiaChi(user.getDiaChi());
        existingUser.setEnabled(user.getEnabled() != null ? user.getEnabled() : false);
        existingUser.setCongTy(user.getCongTy());

        // Update password if provided
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // Update specific role
        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(existingUser.getId(), oldRoleId);
        if (userRole != null) {
            userRole.setRole(newRole);
            userRoleRepository.save(userRole);
        }

        existingUser.setCvFile(user.getCvFile());

        // Save user
        userRepository.save(existingUser);
        
    }

    public void deleteUserRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(user.getId(), roleId);
        if (userRole == null) {
            throw new RuntimeException("Không tìm thấy quyền này của user!");
        }

        // Check if deleting a CANDIDATE role and user has CV file
        if (userRole.getRole().getName().equals("CANDIDATE") && user.getCvFile() != null) {
            try {
                // Delete physical CV file
                Path cvPath = Paths.get(fileStorageService.getUploadDir() + File.separator + user.getCvFile());
                Files.deleteIfExists(cvPath);
            } catch (IOException e) {
                System.err.println("Error deleting CV file: " + e.getMessage());
            }
        }

        // Delete UserRole
        userRoleRepository.delete(userRole);

        // Check if user has any roles left
        List<UserRole> remainingRoles = userRoleRepository.findByUser(user);
        if (remainingRoles.isEmpty()) {
            // If no roles left, delete the user
            userRepository.delete(user);
        }
    }

    public List<User> getNhanVienTuyenDungByCongTyId(Long tinTuyenDungId) {
        TinTuyenDung tin = tinTuyenDungRepository.findById(tinTuyenDungId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng"));


        return userRepository.findByCongTy(tin.getCongty()).stream()
            .filter(user -> user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getName().equals("HR_STAFF") 
                    || userRole.getRole().getName().equals("CV_STAFF")))
            .toList();
    }

    // Thêm phương thức để lấy danh sách nhân viên chưa được phân công phỏng vấn
    public List<User> getNhanVienChuaPhanCongPhongVan(Long donUngTuyenId, Long congtyId) {
        // Lấy đơn ứng tuyển
        DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(donUngTuyenId);
        
        // Lấy danh sách nhân viên đã được phân công phỏng vấn
        List<PhongVan> dsPhongVan = phongVanService.getPhongVanByDonUngTuyen(donUngTuyen);
        List<Long> nhanVienDaPhanCongIds = dsPhongVan.stream()
            .filter(pv -> pv.getNhanVienTD() != null)
            .map(pv -> pv.getNhanVienTD().getId())
            .distinct()
            .collect(Collectors.toList());
        
        // Lấy tất cả nhân viên tuyển dụng của công ty
        List<User> allStaff = getNhanVienTuyenDungByCongTyId(congtyId);
        // Lọc ra những nhân viên chưa được phân công
        return allStaff.stream()
            .filter(staff -> !nhanVienDaPhanCongIds.contains(staff.getId()))
            .collect(Collectors.toList());
    }

    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("Không tìm thấy người dùng!");
        }
        return userRepository.save(user);
    }
    
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        
        // Kiểm tra mật khẩu cũ có chính xác không
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false; // Mật khẩu cũ không chính xác
        }
        
        // Mã hóa mật khẩu mới và cập nhật
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
        
        return true; // Đổi mật khẩu thành công
    }
}
