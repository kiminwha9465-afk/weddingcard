package org.example.weddingcard.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.weddingcard.entity.User;
import org.example.weddingcard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class KakaoAuthController {

    @Value("${kakao.rest-key}")
    private String kakaoRestKey;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestClient restClient = RestClient.create();

    public KakaoAuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/kakao/auth")
    public String kakaoAuth(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("kakaoState", state);
        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + kakaoRestKey
                + "&redirect_uri=" + redirectUri
                + "&state=" + state;
        return "redirect:" + url;
    }

    @GetMapping("/kakao/callback")
    public String kakaoCallback(@RequestParam(required = false) String code,
                                @RequestParam(required = false) String state,
                                @RequestParam(required = false) String error,
                                HttpSession session,
                                HttpServletRequest request) {
        if (error != null || code == null) {
            return "redirect:/login?error";
        }

        String savedState = (String) session.getAttribute("kakaoState");
        if (savedState == null || !savedState.equals(state)) {
            return "redirect:/login?error";
        }
        session.removeAttribute("kakaoState");

        // 토큰 발급
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("grant_type", "authorization_code");
        tokenParams.add("client_id", kakaoRestKey);
        tokenParams.add("redirect_uri", redirectUri);
        tokenParams.add("code", code);

        Map<String, Object> tokenBody;
        try {
            tokenBody = restClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(tokenParams)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return "redirect:/login?error";
        }

        String accessToken = (String) tokenBody.get("access_token");
        if (accessToken == null) return "redirect:/login?error";

        // 사용자 정보 조회
        Map<String, Object> userInfo;
        try {
            userInfo = restClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return "redirect:/login?error";
        }

        String kakaoId = String.valueOf(userInfo.get("id"));
        String username = "kakao_" + kakaoId + "@kakao.local";

        User user = userRepository.findByKakaoId(kakaoId).orElseGet(() -> {
            User existing = userRepository.findByUsername(username).orElse(null);
            if (existing != null) {
                existing.setKakaoId(kakaoId);
                return userRepository.save(existing);
            }
            User newUser = new User(username, passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setKakaoId(kakaoId);
            return userRepository.save(newUser);
        });

        List<SimpleGrantedAuthority> authorities = user.getRole() == User.Role.ADMIN
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return "redirect:/dashboard";
    }
}
