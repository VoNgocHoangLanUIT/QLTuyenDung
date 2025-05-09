package com.example.QLTuyenDung.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.QLTuyenDung.model.Role;
import com.example.QLTuyenDung.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            System.out.println("Initializing roles...");
            
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);

            Role recruiterRole = new Role();
            recruiterRole.setName("RECRUITER");
            roleRepository.save(recruiterRole);

            Role candidateRole = new Role();
            candidateRole.setName("CANDIDATE");
            roleRepository.save(candidateRole);

            Role hrStaffRole = new Role();
            hrStaffRole.setName("HR_STAFF");
            roleRepository.save(hrStaffRole);

            Role cvStaffRole = new Role();
            cvStaffRole.setName("CV_STAFF");
            roleRepository.save(cvStaffRole);

            System.out.println("Roles initialized successfully!");
        }
    }
}