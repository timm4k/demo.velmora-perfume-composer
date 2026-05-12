package velmora.composer.dto.auth;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import velmora.composer.model.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

  private Long id;
  private String nickname;
  private String email;
  private String role;
  private LocalDateTime createdAt;

  public static UserDto from(User user) {
    return UserDto.builder()
        .id(user.getId())
        .nickname(user.getNickname())
        .email(user.getEmail())
        .role(user.getRole().name())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
