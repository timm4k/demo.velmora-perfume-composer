package velmora.composer.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "composition_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CompositionItemId.class)
public class CompositionItem {

  @Id
  @Column(name = "composition_id")
  private Long compositionId;

  @Id
  @Column(name = "note_id")
  private Long noteId;

  @Column(nullable = false)
  private Integer percentage = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "composition_id", insertable = false, updatable = false)
  private Composition composition;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "note_id", insertable = false, updatable = false)
  private Note note;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CompositionItemId implements Serializable {
  private Long compositionId;
  private Long noteId;
}