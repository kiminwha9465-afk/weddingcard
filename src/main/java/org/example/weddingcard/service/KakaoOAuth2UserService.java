package org.example.weddingcard.service;

import org.example.weddingcard.entity.User;
import org.example.weddingcard.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public KakaoOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String kakaoId = String.valueOf(oAuth2User.getAttribute("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        String username = (email != null && !email.isBlank()) ? email : "kakao_" + kakaoId + "@kakao.local";

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

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("_email", user.getUsername());

        return new DefaultOAuth2User(authorities, attributes, "_email");
    }
}
