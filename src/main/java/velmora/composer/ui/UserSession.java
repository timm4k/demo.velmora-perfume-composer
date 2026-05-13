package velmora.composer.ui;

import lombok.Data;
import org.springframework.stereotype.Component;
import velmora.composer.model.Role;

@Component
@Data
public class UserSession {
  private Long userId;
  private String nickname;
  private String email;
  private String initials;
  private Role role = Role.USER;

  public boolean isLoggedIn() {
    return userId != null;
  }

  public void clear() {
    userId = null;
    nickname = null;
    email = null;
    initials = null;
    role = Role.USER;
  }
}
