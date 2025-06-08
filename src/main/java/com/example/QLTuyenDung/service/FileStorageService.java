package com.example.QLTuyenDung.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
public class FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    
    private String avatarDir;
    private String companyLogoDir;

    public String getUploadDir() {
        return uploadDir;
    }
    
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            Files.createDirectories(Paths.get(uploadDir + File.separator + "logos"));
            Files.createDirectories(Paths.get(uploadDir + File.separator + "cvs"));

            companyLogoDir = "src/main/resources/static/fe/images/resource/company-logo";
            Files.createDirectories(Paths.get(companyLogoDir));
            
            avatarDir = "src/main/resources/static/fe/images/resource/avatar";
            Files.createDirectories(Paths.get(avatarDir));
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload!", e);
        }
    }

    public String storeFile(MultipartFile file) {
        return storeFile(file, "");
    }
    
    public String storeFile(MultipartFile file, String subfolder) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File trống, không thể lưu!");
            }

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());

            if (fileName.contains("..")) {
                throw new RuntimeException("Tên file không hợp lệ: " + fileName);
            }
            
            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
            String targetPath = uploadDir;
            if (subfolder != null && !subfolder.isEmpty()) {
                targetPath = targetPath + File.separator + subfolder;
                // Đảm bảo thư mục tồn tại
                Files.createDirectories(Paths.get(targetPath));
            }
            
            Path targetLocation = Paths.get(targetPath + File.separator + uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            // Trả về đường dẫn tương đối đến file 
            return (subfolder != null && !subfolder.isEmpty() ? subfolder + "/" : "") + uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu trữ file. Vui lòng thử lại!", ex);
        }
    }
    
    public String storeCV(MultipartFile file) {
        return storeFile(file, "cvs");
    }

    public String copyCV(String originalCvFileName) throws IOException {
        if (originalCvFileName == null || originalCvFileName.isEmpty()) {
            throw new IOException("Không tìm thấy file CV gốc");
        }
        
        // Tạo tên file mới dựa trên timestamp hiện tại
        String uniquePrefix = System.currentTimeMillis() + "_";
        String originalFileNameOnly = Paths.get(originalCvFileName).getFileName().toString();
        String newFileName = uniquePrefix + originalFileNameOnly;
        
        // Đường dẫn file gốc và file đích
        Path sourcePath;
        // Kiểm tra xem file CV có trong thư mục cvs không
        if (originalCvFileName.startsWith("cvs/")) {
            sourcePath = Paths.get(uploadDir, originalCvFileName);
        } else {
            sourcePath = Paths.get(uploadDir, "cvs", originalCvFileName);
        }
        
        Path targetPath = Paths.get(uploadDir, "cvs", newFileName);
        
        try {
            // Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(targetPath.getParent());
            
            // Sao chép file
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return "cvs/" + newFileName;
        } catch (IOException e) {
            System.err.println("Lỗi khi sao chép CV: " + e.getMessage());
            throw e;
        }
    }

    public String storeCompanyLogo(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File trống, không thể lưu!");
            }

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());

            if (fileName.contains("..")) {
                throw new RuntimeException("Tên file không hợp lệ: " + fileName);
            }
            
            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
            Path targetLocation = Paths.get(companyLogoDir + File.separator + uniqueFileName);
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Logo đã được lưu tại: " + targetLocation);
            
            // Chỉ trả về tên file, không bao gồm đường dẫn thư mục
            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu trữ logo. Vui lòng thử lại!", ex);
        }
    }

    public String storeAvatar(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File trống, không thể lưu!");
            }

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());

            if (fileName.contains("..")) {
                throw new RuntimeException("Tên file không hợp lệ: " + fileName);
            }
            
            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
            Path targetLocation = Paths.get(avatarDir + File.separator + uniqueFileName);
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            
            // Chỉ trả về tên file, không bao gồm đường dẫn thư mục
            return uniqueFileName;
        } catch (IOException ex) {
            System.err.println("Lỗi khi lưu avatar: " + ex.getMessage());
            throw new RuntimeException("Không thể lưu trữ avatar. Vui lòng thử lại!", ex);
        }
    }

    public boolean deleteCV(String cvFileName) {
        try {
            if (cvFileName == null || cvFileName.isEmpty()) {
                return false;
            }
            
            Path cvPath;
            // Kiểm tra xem đường dẫn đã có prefix "cvs/" chưa
            if (cvFileName.startsWith("cvs/")) {
                cvPath = Paths.get(uploadDir, cvFileName);
            } else {
                cvPath = Paths.get(uploadDir, "cvs", cvFileName);
            }
            
            return Files.deleteIfExists(cvPath);
        } catch (IOException e) {
            System.err.println("Lỗi khi xóa CV: " + e.getMessage());
            return false;
        }
    }
}
