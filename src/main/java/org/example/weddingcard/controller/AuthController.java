package org.example.weddingcard.controller;

import org.example.weddingcard.entity.User;
import org.example.weddingcard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-username}")
    private String adminUsername;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam String passwordConfirm,
                            Model model) {
        username = username == null ? "" : username.trim();
        if (username.isBlank() || password == null || password.length() < 8) {
            model.addAttribute("error", "아이디와 8자 이상의 비밀번호를 입력해주세요.");
            model.addAttribute("username", username);
            return "register";
        }
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("username", username);
            return "register";
        }
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            model.addAttribute("username", username);
            return "register";
        }

        User user = new User(username, passwordEncoder.encode(password));
        if (username.equalsIgnoreCase(adminUsername)) {
            user.setRole(User.Role.ADMIN);
        }
        userRepository.save(user);
        return "redirect:/login?registered";
    }
}
