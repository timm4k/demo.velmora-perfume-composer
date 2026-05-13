package velmora.composer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_synergy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FamilySynergy {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "family_a", nullable = false, length = 50)
  private String familyA;

  @Column(name = "family_b", nullable = false, length = 50)
  private String familyB;

  @Column(nullable = false)
  private Integer score;

  @Column(columnDefinition = "TEXT")
  private String comment;
}
