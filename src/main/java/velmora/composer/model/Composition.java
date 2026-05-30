package velmora.composer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compositions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Composition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;

  @Column(name = "is_public")
  private boolean isPublic = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private CompositionStatus status = CompositionStatus.DRAFT;

  @Column(name = "is_favorite")
  private boolean favorite = false;

  @Column(name = "version_number")
  private int versionNumber = 1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "perfume_id")
  private Perfume perfume;

  @OneToMany(mappedBy = "composition", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CompositionItem> items = new ArrayList<>();

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  private LocalDateTime updatedAt = LocalDateTime.now();
}