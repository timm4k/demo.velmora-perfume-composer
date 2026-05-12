package velmora.composer.ui;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class UserSession {
  private Long userId;
  private String nickname;
  private String email;
  private String initials;

  public boolean isLoggedIn() {
    return userId != null;
  }

  public void clear() {
    userId = null;
    nickname = null;
    email = null;
    initials = null;
  }
}
