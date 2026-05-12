package velmora.composer.service;

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

  @Transactional
  public User registerUser(String email, String rawPassword) {
    if (userRepository.findByEmail(email).isPresent()) {
      throw new IllegalArgumentException("User with this email already exists");
    }

    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setRole(Role.USER);
    user.setEnabled(false);

    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public User findByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }
}