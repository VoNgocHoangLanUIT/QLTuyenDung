package com.example.QLTuyenDung.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("")
public class HomeController {
    private final UserService userService;
    @GetMapping("")
    public String user(){
        return "index";
    }

    @GetMapping("/login")
    public String userLogin(){
        return "logon";
    }

    @PostMapping("/register")
    public String userRegister(@RequestParam String username, @RequestParam String email, @RequestParam String password, @RequestParam String roleName, RedirectAttributes redirectAttributes)  {
        try {
            userService.registerUser(username, email, password, roleName);
            return "redirect:/login?register=true";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("registerError", true);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login?register=false";
        }
    }
}
