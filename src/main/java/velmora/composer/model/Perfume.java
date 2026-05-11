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

  @Column(name = "image_url")
  private String imageUrl;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "is_available")
  private boolean available = true;
}
