package velmora.composer.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.dto.auth.LoginRequest;
import velmora.composer.dto.auth.RegistrationRequest;
import velmora.composer.dto.auth.UserDto;
import velmora.composer.model.User;
import velmora.composer.service.UserService;
import velmora.composer.ui.UserSession;

/**
 * Фасад автентифікації — єдина точка входу для UI.
 * Координує UserService (логіка) та UserSession (стан сесії).
 * Перетворює User -> UserDto для передачі в UI.
 */
@Component
@RequiredArgsConstructor
public class AuthFacade {

  private final UserService userService;
  private final UserSession userSession;

  public UserDto login(LoginRequest request) {
    User user = userService.authenticate(request.getEmail(), request.getPassword());
    setSession(user);
    return UserDto.from(user);
  }

  public UserDto register(RegistrationRequest request) {
    User user = userService.register(
        request.getNickname(),
        request.getEmail(),
        request.getPassword()
    );
    return UserDto.from(user);
  }

  public UserDto verifyEmail(String email, String code) {
    User user = userService.verifyEmail(email, code);
    setSession(user);
    return UserDto.from(user);
  }

  private void setSession(User user) {
    userSession.setUserId(user.getId());
    userSession.setNickname(user.getNickname());
    userSession.setEmail(user.getEmail());
    userSession.setRole(user.getRole());
    userSession.setInitials(
        user.getNickname() != null && !user.getNickname().isEmpty()
            ? user.getNickname().substring(0, 1).toUpperCase()
            : "?"
    );
  }
}
