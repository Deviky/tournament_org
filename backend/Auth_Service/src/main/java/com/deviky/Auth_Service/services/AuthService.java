package com.deviky.Auth_Service.services;

import com.deviky.Auth_Service.components.EmailConfirmationTokenStore;
import com.deviky.Auth_Service.components.PasswordResetTokenStore;
import com.deviky.Auth_Service.components.TokenBlacklist;
import com.deviky.Auth_Service.dto.ApiResponse;
import com.deviky.Auth_Service.dto.AuthResponse;
import com.deviky.Auth_Service.dto.CreateOrganizationRequest;
import com.deviky.Auth_Service.dto.CreatePlayerRequest;
import com.deviky.Auth_Service.dto.LoginRequest;
import com.deviky.Auth_Service.dto.ModeratorCreateRequest;
import com.deviky.Auth_Service.dto.Organization;
import com.deviky.Auth_Service.dto.Player;
import com.deviky.Auth_Service.dto.RegisterOrganizationRequest;
import com.deviky.Auth_Service.dto.RegisterPlayerRequest;
import com.deviky.Auth_Service.dto.ResetPasswordRequest;
import com.deviky.Auth_Service.models.Role;
import com.deviky.Auth_Service.models.User;
import com.deviky.Auth_Service.repositories.UserRepository;
import com.deviky.Auth_Service.security_core.SecurityUser;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklist blacklist;
    private final EmailConfirmationTokenStore emailConfirmationTokenStore;
    private final MailSender mailSender;
    private final PasswordResetTokenStore passwordResetTokenStore;
    private final ParticipantClientService participantClientService;

    @Transactional
    public <T extends LoginRequest> ApiResponse<String> register(T request, String appUrl) {
        try {
            if (userRepository.findByUsername(request.getEmail()).isPresent()) {
                return new ApiResponse<>("Пользователь с такой электронной почтой уже существует", null, true);
            }

            if (!((request instanceof RegisterOrganizationRequest) || (request instanceof RegisterPlayerRequest))) {
                return new ApiResponse<>("Некорректные данные регистрации", null, true);
            }

            Role role = (request instanceof RegisterOrganizationRequest) ? Role.ORGANIZER : Role.PLAYER;

            User user = User.builder()
                    .username(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(role)
                    .emailConfirmed(false)
                    .build();

            User userSaved = userRepository.save(user);
            Long userId = userSaved.getId();

            if (request instanceof RegisterOrganizationRequest organizationRequest) {
                CreateOrganizationRequest createOrganizationDto = CreateOrganizationRequest.builder()
                        .id(userId)
                        .organizerName(organizationRequest.getOrganizerName())
                        .description(organizationRequest.getDescription())
                        .build();
                ApiResponse<Organization> organizationApiResponse =
                        participantClientService.createOrganizationProfile(createOrganizationDto);
                if (organizationApiResponse.isError()) {
                    throw new Exception(organizationApiResponse.getMessage());
                }
            } else if (request instanceof RegisterPlayerRequest playerRequest) {
                CreatePlayerRequest createPlayerRequest = CreatePlayerRequest.builder()
                        .id(userId)
                        .nickname(playerRequest.getNickname())
                        .games(playerRequest.getGames())
                        .build();
                ApiResponse<Player> playerApiResponse =
                        participantClientService.createPlayerProfile(createPlayerRequest);
                if (playerApiResponse.isError()) {
                    throw new Exception(playerApiResponse.getMessage());
                }
            }

            if (sendConfirmationEmailSafely(user.getUsername(), appUrl)) {
                return new ApiResponse<>("Проверьте электронную почту для подтверждения регистрации", null, false);
            }

            return new ApiResponse<>(
                    "Регистрация завершена, но письмо с подтверждением пока не удалось отправить. Попробуйте запросить его позже.",
                    null,
                    false
            );
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public void sendConfirmationEmail(String email, String appUrl) {
        String token = UUID.randomUUID().toString();
        emailConfirmationTokenStore.storeToken(token, email);

        String link = appUrl + "/confirm?token=" + token;
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(email);
        mail.setSubject("Подтверждение электронной почты");
        mail.setText("Перейдите по ссылке, чтобы подтвердить электронную почту: " + link);
        mailSender.send(mail);
    }

    private boolean sendConfirmationEmailSafely(String email, String appUrl) {
        try {
            sendConfirmationEmail(email, appUrl);
            return true;
        } catch (Exception e) {
            log.warn("Не удалось отправить письмо подтверждения на {}: {}", email, e.getMessage());
            return false;
        }
    }

    public ApiResponse<String> resendConfirmationEmail(String email, String appUrl) {
        try {
            User user = userRepository.findByUsername(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (user.isEmailConfirmed()) {
                return new ApiResponse<>("Электронная почта уже подтверждена", null, true);
            }

            sendConfirmationEmail(email, appUrl);
            return new ApiResponse<>("Письмо с подтверждением отправлено повторно", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<AuthResponse> confirmEmailAndLogin(String token) {
        try {
            String email = emailConfirmationTokenStore.getEmail(token);
            if (email == null) {
                return new ApiResponse<>("Токен недействителен или уже истек", null, true);
            }

            User user = userRepository.findByUsername(email).orElseThrow();
            user.setEmailConfirmed(true);
            userRepository.save(user);

            emailConfirmationTokenStore.removeToken(token);

            String access = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
            String refresh = jwtService.generateRefreshToken(user.getUsername());

            return new ApiResponse<>("Электронная почта успешно подтверждена", new AuthResponse(access, refresh), false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<AuthResponse> login(LoginRequest request) {
        try {
            SecurityUser securityUser = (SecurityUser) authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            ).getPrincipal();

            User user = securityUser.getUser();

            if (!user.isEmailConfirmed()) {
                return new ApiResponse<>("Электронная почта еще не подтверждена", null, true);
            }

            String access = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
            String refresh = jwtService.generateRefreshToken(user.getUsername());

            return new ApiResponse<>("Вход выполнен успешно", new AuthResponse(access, refresh), false);
        } catch (DisabledException e) {
            return new ApiResponse<>("Электронная почта еще не подтверждена", null, true);
        } catch (BadCredentialsException e) {
            return new ApiResponse<>("Неверная электронная почта или пароль", null, true);
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка авторизации", null, true);
        }
    }

    public ApiResponse<AuthResponse> refresh(String refreshToken) {
        try {
            if (blacklist.isRevoked(refreshToken)) {
                return new ApiResponse<>("Refresh-токен отозван", null, true);
            }

            Claims claims = jwtService.parseToken(refreshToken);
            String username = claims.getSubject();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (!user.isEmailConfirmed()) {
                return new ApiResponse<>("Электронная почта еще не подтверждена", null, true);
            }

            String newAccess = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

            return new ApiResponse<>(
                    "Токен успешно обновлен",
                    new AuthResponse(newAccess, refreshToken),
                    false
            );
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<String> logout(String accessToken, String refreshToken) {
        try {
            if (accessToken != null) {
                long accessTtl = jwtService.getExpirySeconds(accessToken);
                if (accessTtl > 0) {
                    blacklist.revoke(accessToken, accessTtl);
                }
            }

            if (refreshToken != null) {
                long refreshTtl = jwtService.getExpirySeconds(refreshToken);
                if (refreshTtl > 0) {
                    blacklist.revoke(refreshToken, refreshTtl);
                }
            }

            return new ApiResponse<>("Выход выполнен успешно", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<String> sendPasswordResetEmail(String email, String appUrl) {
        try {
            User user = userRepository.findByUsername(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            String token = UUID.randomUUID().toString();
            passwordResetTokenStore.storeToken(token, email);

            String link = appUrl + "/reset?token=" + token;
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject("Сброс пароля");
            mail.setText("Перейдите по ссылке, чтобы сбросить пароль: " + link);
            mailSender.send(mail);

            return new ApiResponse<>("Письмо для сброса пароля отправлено", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<String> resetPassword(String token, ResetPasswordRequest request) {
        try {
            String email = passwordResetTokenStore.getEmail(token);
            if (email == null) {
                return new ApiResponse<>("Токен недействителен или уже истек", null, true);
            }

            User user = userRepository.findByUsername(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            passwordResetTokenStore.removeToken(token);
            return new ApiResponse<>("Пароль успешно изменен", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<String> createModerator(ModeratorCreateRequest request, String appUrl) {
        try {
            if (userRepository.findByUsername(request.getEmail()).isPresent()) {
                return new ApiResponse<>("Пользователь с такой электронной почтой уже существует", null, true);
            }

            User user = User.builder()
                    .username(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.MODERATOR)
                    .emailConfirmed(false)
                    .build();

            userRepository.save(user);

            if (sendConfirmationEmailSafely(user.getUsername(), appUrl)) {
                return new ApiResponse<>("Модератор создан. Письмо с подтверждением отправлено.", null, false);
            }

            return new ApiResponse<>(
                    "Модератор создан, но письмо с подтверждением пока не удалось отправить.",
                    null,
                    false
            );
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }
}
