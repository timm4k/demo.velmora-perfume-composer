package velmora.composer.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "perfumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Perfume {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String brand;
  private BigDecimal price;
  private BigDecimal rating;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "availability")
  private boolean available = true;

  @Column(name = "top_notes", columnDefinition = "TEXT")
  private String topNotes;

  @Column(name = "heart_notes", columnDefinition = "TEXT")
  private String heartNotes;

  @Column(name = "base_notes", columnDefinition = "TEXT")
  private String baseNotes;

  @Column(name = "olfactory_family")
  private String olfactoryFamily;

  private String season;
  private String projection;

  @Column(name = "longevity_score")
  private Integer longevityScore;

  @Column(name = "gender_profile")
  private String genderProfile;
}