package com.example.QLTuyenDung.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       Authentication authentication) 
                                       throws IOException, ServletException {
        
        // Kiểm tra redirect parameter
        String redirectParam = request.getParameter("redirect");
        
        // Nếu có parameter redirect
        if (StringUtils.hasText(redirectParam)) {
            // Đảm bảo URL bắt đầu với / để tránh open redirect
            if (redirectParam.startsWith("/")) {
                // Chuyển hướng đến URL yêu cầu
                getRedirectStrategy().sendRedirect(request, response, redirectParam);
                return;
            }
        }
        
        // Xử lý theo role nếu không có redirect
        var authorities = authentication.getAuthorities();
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
            getRedirectStrategy().sendRedirect(request, response, "/admin");
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("RECRUITER"))) {
            getRedirectStrategy().sendRedirect(request, response, "/nhatd");
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("CANDIDATE"))) {
            getRedirectStrategy().sendRedirect(request, response, "/ungvien");
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("CV_STAFF"))) {
            getRedirectStrategy().sendRedirect(request, response, "/nvhs");
        } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("HR_STAFF"))) {
            getRedirectStrategy().sendRedirect(request, response, "/nvtd");
        } else {
            getRedirectStrategy().sendRedirect(request, response, "/");
        }
    }
}