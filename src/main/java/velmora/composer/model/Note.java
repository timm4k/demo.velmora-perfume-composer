package velmora.composer.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Note {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  private NoteCategory category;

  @Enumerated(EnumType.STRING)
  private NoteType type;

  private Integer intensity;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "color_code")
  private String colorCode;
}
