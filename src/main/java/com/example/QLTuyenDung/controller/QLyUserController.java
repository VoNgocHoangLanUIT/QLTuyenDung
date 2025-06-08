package com.example.QLTuyenDung.controller;

import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.QLTuyenDung.dto.UserRoleDTO;
import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.model.UserRole;
import com.example.QLTuyenDung.service.CongTyService;
import com.example.QLTuyenDung.service.FileStorageService;
import com.example.QLTuyenDung.service.HocVanService;
import com.example.QLTuyenDung.service.KinhNghiemLamViecService;
import com.example.QLTuyenDung.service.ThanhTuuService;
import com.example.QLTuyenDung.service.UserRoleService;
import com.example.QLTuyenDung.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor

public class QLyUserController {
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final CongTyService congTyService;
    private final FileStorageService fileStorageService;
    private final KinhNghiemLamViecService kinhNghiemLamViecService;
    private final HocVanService hocVanService;
    private final ThanhTuuService thanhTuuService;
    
    @GetMapping("/dsungvien")
    public String showDSUngVien(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "hoTen") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        // Lấy danh sách ứng viên với phân trang
        Page<User> pageUngVien = userService.getAllCandidatesPaginated(page, size, sort, direction);
        List<User> dSUngVien = pageUngVien.getContent();
        
        // Thêm thông tin phân trang vào model
        model.addAttribute("dSUngVien", dSUngVien);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageUngVien.getTotalPages());
        model.addAttribute("totalItems", pageUngVien.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("reverseSortDir", direction.equals("asc") ? "desc" : "asc");
        
        // Thông tin cho phân trang
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(pageUngVien.getTotalPages() - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentUrl", "/dsungvien");
        
        return "user/UngVien/index";
    }
    
    @GetMapping("/admin/users")
    public String adminShowUser(Model model) {
        List<UserRoleDTO> userRoles = userService.getAllUsersWithRoles();
        model.addAttribute("userRoles", userRoles);
        return "admin/QLUser/index";
    }

    @GetMapping("/admin/add-user")
    public String adminAddUser(Model model) {
        User user = new User();
        user.setEnabled(true);
        List<CongTy> dSCongTy = congTyService.getAllCongTy();
        model.addAttribute("user", user);
        model.addAttribute("companies", dSCongTy);
        return "admin/QLUser/add";
    }
    
    @PostMapping("/admin/add-user")
    public String adminAddUser(@ModelAttribute User user,
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

    @GetMapping("/admin/edit-user/{id}")
    public String adminUpdateUser(@PathVariable Long id, @RequestParam Long roleId, Model model) {
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

    @PostMapping("/admin/edit-user/{id}")
    public String adminUpdateUser(@PathVariable Long id,
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
            if (roleName.equals("RECRUITER") || roleName.equals("HR_STAFF") || roleName.equals("CV_STAFF")) {
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

    @PostMapping("/admin/delete-cv/{id}")
    public ResponseEntity<?> adminDeleteCvFile(@PathVariable Long id) {
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

    @GetMapping("/admin/delete-user/{id}/{roleId}")
    public String adminDeleteUser(@PathVariable Long id, 
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

    @GetMapping("/chitiet-ungvien/{id}")
    public String showChiTietUngVien(@PathVariable Long id, Model model) {
        try {
            User ungVien = userService.getUserById(id);
            model.addAttribute("ungVien", ungVien);
            model.addAttribute("dSHocVan", hocVanService.getHocVanByUserId(id));
            model.addAttribute("dSKinhNghiem", kinhNghiemLamViecService.getKinhNghiemLamViecByUserId(id));
            model.addAttribute("dSThanhTuu", thanhTuuService.getThanhTuuByUserId(id));
            return "user/UngVien/ChiTiet";
        } catch (RuntimeException e) {
            return "redirect:/dsungvien";
        }
    }

    @GetMapping("/download-cv/{id}")
    public ResponseEntity<?> downloadCV(@PathVariable Long id) {
        try {
            User ungVien = userService.getUserById(id);
            if (ungVien != null && ungVien.getCvFile() != null) {
                Path cvPath = Paths.get(fileStorageService.getUploadDir() + File.separator + ungVien.getCvFile());
                if (Files.exists(cvPath)) {
                    byte[] data = Files.readAllBytes(cvPath);
                    return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + ungVien.getCvFile() + "\"")
                        .body(data);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error downloading file");
        }
    }

    @GetMapping("/ungvien/edit-user")
    public String ungVienUpdateThongTinCaNhan(Model model, Authentication authentication) {
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            model.addAttribute("user", userHienTai);
            model.addAttribute("dSHocVan", hocVanService.getHocVanByUserId(userHienTai.getId()));
            model.addAttribute("dSKinhNghiem", kinhNghiemLamViecService.getKinhNghiemLamViecByUserId(userHienTai.getId()));
            model.addAttribute("dSThanhTuu", thanhTuuService.getThanhTuuByUserId(userHienTai.getId()));
            return "ungvien/CapNhatThongTinCaNhan";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/ungvien/edit-user")
    public String ungVienUpdateThongTinCaNhan(@ModelAttribute User user, 
                                @RequestParam(required = false) MultipartFile avatarFile,
                                Authentication authentication, 
                                RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Cập nhật các thuộc tính cơ bản
            userHienTai.setHoTen(user.getHoTen());
            userHienTai.setSoDienThoai(user.getSoDienThoai());
            userHienTai.setGioiTinh(user.getGioiTinh());
            userHienTai.setTuoi(user.getTuoi());
            userHienTai.setChuyenNganh(user.getChuyenNganh());
            userHienTai.setNamKinhNghiem(user.getNamKinhNghiem());
            userHienTai.setLuongMongMuon(user.getLuongMongMuon());
            userHienTai.setNgonNgu(user.getNgonNgu());
            userHienTai.setGioiThieu(user.getGioiThieu());
            
            // Xử lý upload avatar
            if (avatarFile != null && !avatarFile.isEmpty()) {
                // Xóa file cũ nếu có
                if (userHienTai.getHinhAnh() != null) {
                    try {
                        Path staticPath = Paths.get("src/main/resources/static/fe/images/resource/avatar", userHienTai.getHinhAnh());
                        boolean deleted = Files.deleteIfExists(staticPath);

                        if (!deleted) {
                            // Nếu không tìm thấy trong static, thử xóa trong uploads
                            Path uploadPath = Paths.get(fileStorageService.getUploadDir() + File.separator + userHienTai.getHinhAnh());
                            Files.deleteIfExists(uploadPath);
                        }
                    } catch (IOException e) {
                        System.err.println("Lỗi khi xóa file cũ: " + e.getMessage());
                    }
                }
                
                String fileName = fileStorageService.storeAvatar(avatarFile);
                userHienTai.setHinhAnh(fileName);
            }
            
            // Lưu thay đổi
            userService.updateUser(userHienTai);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        
        return "redirect:/ungvien/edit-user";
    }

    @PostMapping("/ungvien/edit-mxh")
    public String ungVienUpdateMXH(@ModelAttribute User user, 
                                Authentication authentication, 
                                RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Cập nhật thông tin mạng xã hội
            userHienTai.setFaceBook(user.getFaceBook());
            userHienTai.setLinkedIn(user.getLinkedIn());
            
            // Lưu thay đổi
            userService.updateUser(userHienTai);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin mạng xã hội thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        
        return "redirect:/ungvien/edit-user";
    }

    @PostMapping("/ungvien/edit-diachi")
    public String ungVienUpdateDiaChi(@ModelAttribute User user, 
                                Authentication authentication, 
                                RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Cập nhật địa chỉ
            userHienTai.setDiaChi(user.getDiaChi());
            
            // Lưu thay đổi
            userService.updateUser(userHienTai);
            redirectAttributes.addFlashAttribute("success", "Cập nhật địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        
        return "redirect:/ungvien/edit-user";
    }

    @PostMapping("/ungvien/upload-cv")
    public String uploadCV(@RequestParam(required = false) MultipartFile cvMultipartFile,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra file
            if (cvMultipartFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file CV");
                return "redirect:/ungvien/edit-user";
            }
            
            // Kiểm tra định dạng file
            String contentType = cvMultipartFile.getContentType();
            if (!contentType.equals("application/pdf") && 
                !contentType.equals("application/msword") && 
                !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                redirectAttributes.addFlashAttribute("error", "Chỉ hỗ trợ file PDF, DOC, DOCX");
                return "redirect:/ungvien/edit-user";
            }
            
            // Kiểm tra kích thước file (tối đa 5MB)
            if (cvMultipartFile.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "Kích thước file không được vượt quá 5MB");
                return "redirect:/ungvien/edit-user";
            }
            
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            // Xóa file cũ nếu có
            if (userHienTai.getCvFile() != null) {
                try {
                    Path cvPath = Paths.get(fileStorageService.getUploadDir() + File.separator + userHienTai.getCvFile());
                    Files.deleteIfExists(cvPath);
                } catch (IOException e) {
                    System.err.println("Lỗi khi xóa file CV cũ: " + e.getMessage());
                }
            }
            
            // Lưu file mới
            String fileName = fileStorageService.storeCV(cvMultipartFile);
            userHienTai.setCvFile(fileName);
            // Lưu thay đổi
            userService.updateUser(userHienTai);
            
            redirectAttributes.addFlashAttribute("success", "Tải CV lên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Tải CV lên thất bại: " + e.getMessage());
        }
        
        return "redirect:/ungvien/edit-user";
    }

    @PostMapping("/ungvien/delete-cv")
    public ResponseEntity<?> deleteCV(Authentication authentication) {
        try {
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user có CV không
            if (userHienTai.getCvFile() == null || userHienTai.getCvFile().isEmpty()) {
                return ResponseEntity.badRequest().body("Không có CV để xóa");
            }
            
            // Lấy đường dẫn file CV
            String uploadDir = fileStorageService.getUploadDir();
            Path cvPath = Paths.get(uploadDir + File.separator + userHienTai.getCvFile());
            // Kiểm tra file có tồn tại không
            if (Files.exists(cvPath)) {

                Files.delete(cvPath);
            }
            
            // Cập nhật thông tin user
            userHienTai.setCvFile(null);
            userService.updateUser(userHienTai);
            
            return ResponseEntity.ok().body("Xóa CV thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Xóa CV thất bại: " + e.getMessage());
        }
    }

    @GetMapping("/user/edit-user")
    public String updateThongTinCaNhan(Model model, Authentication authentication) {
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            model.addAttribute("user", userHienTai);
            return "user/CapNhatThongTinCaNhan";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/user/edit-user")
    public String updateThongTinCaNhan(@ModelAttribute User user, 
                                @RequestParam(required = false) MultipartFile avatarFile,
                                Authentication authentication, 
                                RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Cập nhật các thuộc tính cơ bản
            userHienTai.setHoTen(user.getHoTen());
            userHienTai.setSoDienThoai(user.getSoDienThoai());
            userHienTai.setGioiTinh(user.getGioiTinh());
            userHienTai.setTuoi(user.getTuoi());
            userHienTai.setChuyenNganh(user.getChuyenNganh());
            userHienTai.setGioiThieu(user.getGioiThieu());
            
            // Xử lý upload avatar
            if (avatarFile != null && !avatarFile.isEmpty()) {
                // Xóa file cũ nếu có
                if (userHienTai.getHinhAnh() != null) {
                    try {
                        Path staticPath = Paths.get("src/main/resources/static/fe/images/resource/avatar", userHienTai.getHinhAnh());
                        boolean deleted = Files.deleteIfExists(staticPath);

                        if (!deleted) {
                            // Nếu không tìm thấy trong static, thử xóa trong uploads
                            Path uploadPath = Paths.get(fileStorageService.getUploadDir() + File.separator + userHienTai.getHinhAnh());
                            Files.deleteIfExists(uploadPath);
                        }
                    } catch (IOException e) {
                        System.err.println("Lỗi khi xóa file cũ: " + e.getMessage());
                    }
                }
                
                String fileName = fileStorageService.storeAvatar(avatarFile);
                userHienTai.setHinhAnh(fileName);
            }
            
            // Lưu thay đổi
            userService.updateUser(userHienTai);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        
        return "redirect:/user/edit-user";
    }

    @PostMapping("/user/edit-mxh")
    public String updateMXH(@ModelAttribute User user, 
                                Authentication authentication, 
                                RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Cập nhật thông tin mạng xã hội
            userHienTai.setFaceBook(user.getFaceBook());
            userHienTai.setLinkedIn(user.getLinkedIn());
            
            // Lưu thay đổi
            userService.updateUser(userHienTai);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin mạng xã hội thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        
        return "redirect:/user/edit-user";
    }

    @PostMapping("/user/edit-diachi")
    public String updateDiaChi(@ModelAttribute User user, 
                                Authentication authentication, 
                                RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Cập nhật địa chỉ
            userHienTai.setDiaChi(user.getDiaChi());
            
            // Lưu thay đổi
            userService.updateUser(userHienTai);
            redirectAttributes.addFlashAttribute("success", "Cập nhật địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        
        return "redirect:/user/edit-user";
    }
}
