package velmora.composer.ui.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import velmora.composer.model.Note;
import velmora.composer.model.NoteType;
import velmora.composer.ui.util.UiUtils;

public class MaterialCardCell extends ListCell<Note> {

  @Override
  protected void updateItem(Note note, boolean empty) {
    super.updateItem(note, empty);
    if (empty || note == null) {
      setGraphic(null);
      setStyle("-fx-background-color: transparent; -fx-padding: 3 0;");
    } else {
      HBox root = new HBox(12);
      root.setAlignment(Pos.CENTER_LEFT);
      root.getStyleClass().add("material-mini-card");

      Circle dot = new Circle(6);
      dot.getStyleClass().add("mcard-color-dot");
      dot.setFill(UiUtils.parseColor(note.getColorCode()));

      VBox info = new VBox(3);
      VBox.setVgrow(info, Priority.ALWAYS);

      Label name = new Label(note.getName() != null ? note.getName() : "");
      name.getStyleClass().add("mcard-name");

      HBox tags = new HBox(8);
      tags.setAlignment(Pos.CENTER_LEFT);
      Label cat = new Label(note.getCategory() != null ? note.getCategory().toUpperCase() : "");
      cat.getStyleClass().add("mcard-tags");
      Label phase = new Label(note.getType() != null ? note.getType().name() : "");
      phase.getStyleClass().addAll("mcard-phase-tag",
          note.getType() == NoteType.TOP ? "mcard-tag-top"
              : note.getType() == NoteType.HEART ? "mcard-tag-heart"
                  : "mcard-tag-base");
      tags.getChildren().addAll(cat, phase);

      String descText = note.getDescription();
      if (descText != null && descText.length() > 55) descText = descText.substring(0, 52) + "...";
      Label desc = new Label(descText != null ? descText : "");
      desc.getStyleClass().add("mcard-desc");

      HBox meta = new HBox(8);
      meta.setAlignment(Pos.CENTER_LEFT);
      Label origin = new Label(note.getOrigin() != null ? note.getOrigin() : "");
      origin.getStyleClass().add("mcard-origin");
      HBox dots = new HBox(3);
      dots.setAlignment(Pos.CENTER_LEFT);
      int intensity = note.getIntensity() != null ? note.getIntensity() : 0;
      for (int i = 0; i < 5; i++) {
        Label d = new Label();
        d.getStyleClass().add("mcard-intense-dot");
        d.setStyle(String.format("-fx-background-color: %s;", i < intensity / 2 ? "#D4A89A" : "#EAE6DF"));
        dots.getChildren().add(d);
      }
      meta.getChildren().addAll(origin, dots);

      info.getChildren().addAll(name, tags, desc, meta);
      root.getChildren().addAll(dot, info);
      setGraphic(root);
      setStyle("-fx-background-color: transparent; -fx-padding: 3 20 3 4;");
    }
  }
}
