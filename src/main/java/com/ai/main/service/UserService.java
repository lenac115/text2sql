package com.ai.main.service;

import com.ai.main.domain.Users;
import com.ai.main.dto.address.AddressRequest;
import com.ai.main.dto.auth.*;
import com.ai.main.dto.user.*;
import com.ai.main.repository.UsersRepository;
import com.ai.main.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String REFRESH_KEY_PREFIX = "refresh:";

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usersRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Users user = Users.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .createdAt(LocalDateTime.now())
                .build();

        usersRepository.save(user);

        return issueTokens(user.getEmail(), user.getRole().name(), user.getName());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Users user = usersRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return issueTokens(user.getEmail(), user.getRole().name(), user.getName());
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        if (!jwtProvider.isRefreshToken(token)) {
            throw new BadCredentialsException("유효하지 않은 리프레쉬 토큰입니다.");
        }

        String email = jwtProvider.getEmail(token);
        String stored = redisTemplate.opsForValue().get(REFRESH_KEY_PREFIX + email);

        if (!token.equals(stored)) {
            throw new BadCredentialsException("만료되었거나 이미 사용된 리프레쉬 토큰입니다.");
        }

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저입니다."));

        return issueTokens(email, user.getRole().name(), user.getName());
    }

    public void logout(Authentication authentication) {
        String email = authentication.getName();
        redisTemplate.delete(REFRESH_KEY_PREFIX + email);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UserUpdateRequest request) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        user.updateName(request.name());
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateDefaultAddress(String email, AddressRequest request) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        user.updateDefaultAddress(request.toEntity());
        return UserProfileResponse.from(user);
    }

    @Transactional
    public void updatePassword(String email, PasswordUpdateRequest request) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    private AuthResponse issueTokens(String email, String role, String name) {
        String accessToken = jwtProvider.generateAccessToken(email, role);
        String refreshToken = jwtProvider.generateRefreshToken(email, role);

        redisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + email,
                refreshToken,
                Duration.ofMillis(jwtProvider.getRefreshExpirationMs())
        );

        return AuthResponse.of(accessToken, refreshToken, email, name);
    }
}
