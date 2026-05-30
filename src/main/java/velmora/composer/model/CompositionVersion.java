package velmora.composer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "composition_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompositionVersion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "composition_id", nullable = false)
  private Long compositionId;

  @Column(name = "version_number", nullable = false)
  private int versionNumber;

  @Column(name = "snapshot_json", columnDefinition = "JSONB", nullable = false)
  private String snapshotJson;

  @Column(name = "change_description", length = 500)
  private String changeDescription;

  @Column(name = "created_at")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}
