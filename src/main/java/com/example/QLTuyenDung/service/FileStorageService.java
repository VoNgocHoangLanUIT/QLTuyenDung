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
    
    public String getUploadDir() {
        return uploadDir;
    }
    
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            Files.createDirectories(Paths.get(uploadDir + File.separator + "logos"));
            Files.createDirectories(Paths.get(uploadDir + File.separator + "cvs"));
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
    
    public String storeCompanyLogo(MultipartFile file) {
        return storeFile(file, "logos");
    }
    
    public String storeCV(MultipartFile file) {
        return storeFile(file, "cvs");
    }
}
