package com.example.QLTuyenDung.controller.admin;

import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.QLTuyenDung.dto.UserRoleDTO;
import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.model.UserRole;
import com.example.QLTuyenDung.service.CongTyService;
import com.example.QLTuyenDung.service.FileStorageService;
import com.example.QLTuyenDung.service.UserRoleService;
import com.example.QLTuyenDung.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class QLyUserController {
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final CongTyService congTyService;
    private final FileStorageService fileStorageService;
    @GetMapping("/users")
    public String showUser(Model model) {
        List<UserRoleDTO> userRoles = userService.getAllUsersWithRoles();
        model.addAttribute("userRoles", userRoles);
        return "admin/QLUser/index";
    }

    @GetMapping("/add-user")
    public String add(Model model) {
        User user = new User();
        user.setEnabled(true);
        List<CongTy> dSCongTy = congTyService.getAllCongTy();
        model.addAttribute("user", user);
        model.addAttribute("companies", dSCongTy);
        return "admin/QLUser/add";
    }
    
    @PostMapping("/add-user")
    public String addUser(@ModelAttribute User user,
                        @RequestParam String roleName,
                        @RequestParam(required = false) Long companyId,
                        @RequestParam(required = false) MultipartFile cvMultipartFile,
                        RedirectAttributes redirectAttributes) {
        try {
            if (!roleName.equals("CANDIDATE")) {
                user.setCvFile(null);
            }
            else if (cvMultipartFile != null && !cvMultipartFile.isEmpty()) {
                String cvFileName = fileStorageService.storeFile(cvMultipartFile);
                System.out.println("CV file name: " + cvFileName);
                user.setCvFile(cvFileName);
                System.out.println("CV file name: " + user.getCvFile());
            }
            if (roleName.equals("HR_STAFF") || roleName.equals("CV_STAFF")) {
                if (companyId == null) {
                    throw new RuntimeException("Vui lòng chọn công ty cho nhân viên");
                }
                CongTy congTy = congTyService.getCongTyById(companyId);
                user.setCongTy(congTy);
            } else {
                user.setCongTy(null); // Xóa công ty nếu không phải nhân viên
            }
            userService.addUserWithRole(user, roleName);
            redirectAttributes.addFlashAttribute("success", "Thêm người dùng thành công!");
            return "redirect:/admin/users";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/add-user";
        }
    }

    @GetMapping("/edit-user/{id}")
    public String updateUser(@PathVariable Long id, @RequestParam Long roleId, Model model) {
        try {
            User user = userService.getUserById(id);
            UserRole userRole = userRoleService.getUserRoleByUserIdAndRoleId(user.getId(), roleId); 
            List<CongTy> dSCongTy = congTyService.getAllCongTy();
            model.addAttribute("user", user);
            model.addAttribute("userRole", userRole);
            model.addAttribute("companies", dSCongTy);
            model.addAttribute("oldRoleId", roleId);
            return "admin/QLUser/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/edit-user/{id}")
    public String updateUser(@PathVariable Long id,
                           @ModelAttribute User user,
                           @RequestParam String roleName,
                           @RequestParam Long oldRoleId,
                           @RequestParam(name = "enabled", defaultValue = "false") boolean enabled,
                           @RequestParam(required = false) MultipartFile cvMultipartFile,
                           @RequestParam(required = false) Long companyId,
                           RedirectAttributes redirectAttributes) {
        try {
            user.setId(id);
            user.setEnabled(enabled);
            if (roleName.equals("HR_STAFF") || roleName.equals("CV_STAFF")) {
                if (companyId == null) {
                    throw new RuntimeException("Vui lòng chọn công ty cho nhân viên");
                }
                CongTy congTy = congTyService.getCongTyById(companyId);
                user.setCongTy(congTy);
            } else {
                user.setCongTy(null); // Xóa công ty nếu không phải nhân viên
            }
            // Clear CV if role is not CANDIDATE
            if (!roleName.equals("CANDIDATE")) {
                user.setCvFile(null);
            } 
            // Handle CV file upload for CANDIDATE
            else if (cvMultipartFile != null && !cvMultipartFile.isEmpty()) {
                String fileName = fileStorageService.storeFile(cvMultipartFile);
                User existingUser = userService.getUserById(id);
                String oldCvFile = existingUser.getCvFile();
                user.setCvFile(fileName);
                if (oldCvFile != null) {
                    try {
                        Path cvPath = Paths.get(fileStorageService.getUploadDir() + File.separator + oldCvFile);
                        Files.deleteIfExists(cvPath);
                    } catch (IOException e) {
                        System.err.println("Error deleting old CV file: " + e.getMessage());
                    }
                } 
            }
            // Keep existing CV if no new file uploaded for CANDIDATE
            else {
                User existingUser = userService.getUserById(id);
                user.setCvFile(existingUser.getCvFile());
            }
            userService.updateUser(user, roleName, oldRoleId);
            redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công!");
            return "redirect:/admin/users";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/edit-user/" + id + "?roleId=" + oldRoleId;
        }
    }

    @PostMapping("/delete-cv/{id}")
    public ResponseEntity<?> deleteCvFile(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user.getCvFile() != null) {
                // Delete physical file
                Path cvPath = Paths.get(fileStorageService.getUploadDir() 
                    + File.separator + user.getCvFile());
                Files.deleteIfExists(cvPath);
                
                // Update database
                user.setCvFile(null);
                userService.updateUser(user, "CANDIDATE", null);
                
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/delete-user/{id}/{roleId}")
    public String deleteUser(@PathVariable Long id, 
                           @PathVariable Long roleId,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUserRole(id, roleId);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }


}
