package com.example.QLTuyenDung.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.QLTuyenDung.service.CustomUserDetailService;

import lombok.RequiredArgsConstructor;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailService customUserDetailService;
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((auth)-> auth
                    .requestMatchers("/*","/fe/**","/register", "/dsungvien", "/chitiet-ungvien/**", "/dscongty", "/chitiet-congty/**", "/dstintd/**", "/chitiet-tintd/**").permitAll()
                    .requestMatchers("/admin/**").hasAuthority("ADMIN")
                    .requestMatchers("/nhatd/**").hasAuthority("RECRUITER")
                    .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login")
                            .loginProcessingUrl("/login")
                            .usernameParameter("email")
                            .passwordParameter("password")
                            .successHandler((request, response, authentication) -> {
                                var authorities = authentication.getAuthorities();
                                if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
                                    response.sendRedirect("/admin");
                                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("RECRUITER"))) {
                                    response.sendRedirect("/nhatd");
                                } else {
                                    response.sendRedirect("/");
                                }
                            })
                            .failureUrl("/login?error=true"))
                .logout(logout -> logout.logoutUrl("/logout")
                            .logoutSuccessUrl("/login?logout=true")
                            .deleteCookies("JSESSIONID")
                            .invalidateHttpSession(true)
                            .clearAuthentication(true))
                .userDetailsService(customUserDetailService);               
        return http.build();
    }

    
}
