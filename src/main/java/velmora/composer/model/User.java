package velmora.composer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nickname;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Builder.Default
  @Column(name = "is_confirmed")
  private boolean enabled = false;

  @Column(name = "verification_code_hash")
  private String verificationCode;

  @Column(name = "verification_expires_at")
  private LocalDateTime verificationExpiresAt;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private List<Composition> compositions;
}