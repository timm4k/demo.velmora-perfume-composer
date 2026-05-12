package velmora.composer.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import velmora.composer.dto.auth.LoginRequest;
import velmora.composer.dto.auth.RegistrationRequest;
import velmora.composer.dto.auth.UserDto;
import velmora.composer.model.User;
import velmora.composer.service.UserService;

@Component
@RequiredArgsConstructor
public class AuthFacade {

  private final UserService userService;

  public UserDto login(LoginRequest request) {
    User user = userService.authenticate(request.getEmail(), request.getPassword());
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
    return UserDto.from(user);
  }
}
