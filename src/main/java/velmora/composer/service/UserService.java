package velmora.composer.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velmora.composer.model.Role;
import velmora.composer.model.User;
import velmora.composer.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  @Transactional
  public User register(String nickname, String email, String rawPassword) {
    if (userRepository.findByEmail(email).isPresent()) {
      throw new IllegalArgumentException("Email already registered");
    }

    String code = emailService.generateCode();

    User user = User.builder()
        .nickname(nickname)
        .email(email)
        .password(passwordEncoder.encode(rawPassword))
        .role(Role.USER)
        .enabled(false)
        .inviteCode(code)
        .createdAt(LocalDateTime.now())
        .build();

    User saved = userRepository.save(user);

    emailService.sendVerificationEmail(email, code);

    return saved;
  }

  @Transactional(readOnly = true)
  public User authenticate(String email, String password) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException("Invalid email or password");
    }

    if (!user.isEnabled()) {
      throw new IllegalArgumentException("Please verify your email first");
    }

    return user;
  }

  @Transactional
  public User verifyEmail(String email, String code) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.isEnabled()) {
      return user;
    }

    if (code == null || !code.equals(user.getInviteCode())) {
      throw new IllegalArgumentException("Invalid verification code");
    }

    user.setEnabled(true);
    user.setInviteCode(null);
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }

  @Transactional(readOnly = true)
  public User findByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }
}
