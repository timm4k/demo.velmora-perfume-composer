package velmora.composer.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {

  @NotBlank(message = "Nickname is required")
  @Size(min = 2, max = 50, message = "Nickname must be 2-50 characters")
  private String nickname;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
  private String password;
}
