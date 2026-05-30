package velmora.composer.service.analysis;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;

@Service
public class PerfumeStyleService {

  public String detectStyle(List<Note> allNotes, List<Note> topNotes, List<Note> heartNotes, List<Note> baseNotes) {
    if (allNotes == null || allNotes.isEmpty()) return "Unknown";

    var categoryCounts = allNotes.stream()
        .filter(n -> n.getCategory() != null)
        .collect(Collectors.groupingBy(n -> n.getCategory().toLowerCase(), Collectors.counting()));

    boolean hasCitrus = categoryCounts.containsKey("citrus");
    boolean hasFloral = categoryCounts.containsKey("floral");
    boolean hasWoody = categoryCounts.containsKey("woody");
    boolean hasSpicy = categoryCounts.containsKey("spicy");
    boolean hasOriental = categoryCounts.containsKey("oriental");
    boolean hasGreen = categoryCounts.containsKey("green");
    boolean hasAquatic = categoryCounts.containsKey("aquatic");
    boolean hasFruity = categoryCounts.containsKey("fruity");
    boolean hasGourmand = categoryCounts.containsKey("gourmand");
    boolean hasLeather = categoryCounts.containsKey("leather");
    boolean hasEarthy = categoryCounts.containsKey("earthy");
    boolean hasAromatic = categoryCounts.containsKey("aromatic");
    boolean hasMusk = categoryCounts.containsKey("musk");
    boolean hasAmber = categoryCounts.containsKey("amber");

    if (hasCitrus && hasAromatic && !hasOriental && !hasGourmand) return "Fresh Aromatic";
    if (hasCitrus && hasWoody && hasAromatic) return "Citrus Woody";
    if (hasCitrus && hasAquatic) return "Marine Aromatic";
    if (hasCitrus && hasGreen) return "Green Fresh";
    if (hasCitrus && hasFruity) return "Fresh Fruity";
    if (hasCitrus && !hasWoody && !hasSpicy) return "Fresh Citrus";

    if (hasFloral && hasOriental) return "Floral Oriental";
    if (hasFloral && hasWoody) return "Floral Woody";
    if (hasFloral && hasGourmand) return "Sweet Floral";
    if (hasFloral && hasFruity) return "Fruity Floral";
    if (hasFloral && hasGreen) return "Green Floral";
    if (hasFloral && hasMusk) return "Clean Floral";
    if (hasFloral) return "Soft Floral";

    if (hasOriental && hasGourmand) return "Oriental Gourmand";
    if (hasOriental && hasSpicy) return "Dark Oriental";
    if (hasOriental && hasAmber) return "Rich Oriental";

    if (hasWoody && hasSpicy) return "Woody Spicy";
    if (hasWoody && hasLeather) return "Leather Woody";
    if (hasWoody && hasEarthy) return "Earthy Woody";
    if (hasWoody && hasAromatic) return "Woody Aromatic";
    if (hasWoody && hasAmber) return "Warm Woody";
    if (hasWoody) return "Warm Woody";

    if (hasSpicy && hasLeather) return "Spicy Leather";
    if (hasSpicy && hasAromatic) return "Aromatic Spicy";

    if (hasGourmand && hasSpicy) return "Gourmand Spicy";
    if (hasGourmand) return "Sweet Gourmand";

    if (hasAquatic && hasAromatic) return "Aromatic Aquatic";
    if (hasAquatic) return "Fresh Aquatic";

    if (hasGreen && hasAromatic) return "Green Aromatic";

    if (hasLeather) return "Dark Leather";

    return "Balanced Composition";
  }
}
