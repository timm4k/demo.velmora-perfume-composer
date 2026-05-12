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

  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "note_type")
  private NoteType type;

  private String category;

  private Integer intensity;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "color_code")
  private String colorCode;
}