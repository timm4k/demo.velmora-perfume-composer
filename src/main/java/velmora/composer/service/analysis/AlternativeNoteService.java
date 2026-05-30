package velmora.composer.service.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;

@Service
public class AlternativeNoteService {

  public record NoteAlternative(
      String currentNote,
      String suggestedNote,
      String reason,
      NoteType type
  ) {}

  private static final Map<String, List<List<String>>> ALTERNATIVES = Map.ofEntries(
    Map.entry("lemon", List.of(List.of("Yuzu", "More modern citrus profile with better longevity"))),
    Map.entry("bergamot", List.of(List.of("Grapefruit", "Adds bitterness and contrast to the opening"))),
    Map.entry("grapefruit", List.of(List.of("Bergamot", "Classic citrus note with smoother transition"))),
    Map.entry("mandarin", List.of(List.of("Blood Orange", "Deeper, more complex citrus character"))),
    Map.entry("orange", List.of(List.of("Neroli", "Adds floral dimension to citrus opening"))),
    Map.entry("lavender", List.of(List.of("Rosemary", "Herbal twist with fresher profile"))),
    Map.entry("jasmine", List.of(List.of("Jasmine Sambac", "Richer indolic floral note"))),
    Map.entry("rose", List.of(List.of("Turkish Rose", "Deeper, more complex rose absolute"))),
    Map.entry("vanilla", List.of(List.of("Tonka Bean", "Adds complexity and depth with nutty warmth"), List.of("Benzoin", "Vanilla-like sweetness with resinous depth"))),
    Map.entry("cedarwood", List.of(List.of("Sandalwood", "Creamier woody base with better longevity"))),
    Map.entry("sandalwood", List.of(List.of("Cedarwood", "Drier wood profile with sharper edge"))),
    Map.entry("patchouli", List.of(List.of("Vetiver", "Earthier, greener alternative to patchouli"))),
    Map.entry("oakmoss", List.of(List.of("Tree Moss", "Similar earthy base with modern twist"))),
    Map.entry("amber", List.of(List.of("Ambroxan", "Modern ambergris alternative with clean finish"))),
    Map.entry("musk", List.of(List.of("White Musk", "Cleaner, more transparent musk profile"))),
    Map.entry("cinnamon", List.of(List.of("Cardamom", "Spicy warmth with fresher character"))),
    Map.entry("pepper", List.of(List.of("Pink Pepper", "Softer, fruitier peppery note"))),
    Map.entry("tobacco", List.of(List.of("Hay", "Drier, greener alternative with similar warmth"))),
    Map.entry("leather", List.of(List.of("Suede", "Softer leather note with creamy texture"))),
    Map.entry("coffee", List.of(List.of("Roasted Sesame", "Nutty warmth with coffee-like depth"))),
    Map.entry("honey", List.of(List.of("Propolis", "Darker, more complex honey profile"))),
    Map.entry("pear", List.of(List.of("Apple", "Crisper fruity note with better projection"))),
    Map.entry("apple", List.of(List.of("Pear", "Softer, juicier fruit profile"))),
    Map.entry("watermelon", List.of(List.of("Cucumber", "Fresher aquatic alternative"))),
    Map.entry("fig", List.of(List.of("Fig Leaf", "Greener, more vegetal fig character")))
  );

  public List<NoteAlternative> suggestAlternatives(List<Note> currentNotes) {
    List<NoteAlternative> result = new ArrayList<>();
    if (currentNotes == null) return result;
    for (Note note : currentNotes) {
      String cat = note.getCategory();
      if (cat == null) continue;
      List<List<String>> alts = ALTERNATIVES.get(cat.toLowerCase());
      if (alts != null) {
        for (List<String> alt : alts) {
          result.add(new NoteAlternative(note.getName(), alt.get(0), alt.size() > 1 ? alt.get(1) : "Alternative note option", note.getType()));
        }
      }
    }
    if (result.size() > 4) result = result.subList(0, 4);
    return result;
  }
}
