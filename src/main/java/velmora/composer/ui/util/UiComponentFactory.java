package velmora.composer.ui.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public final class UiComponentFactory {

  private UiComponentFactory() {}

  public static Label createChip(String text, String color) {
    Label chip = new Label(text.toUpperCase());
    chip.setStyle(
        "-fx-background-color: " + color + "22;"
            + "-fx-text-fill: " + color + ";"
            + "-fx-font-size: 11;"
            + "-fx-font-weight: 700;"
            + "-fx-padding: 4 12;"
            + "-fx-background-radius: 12;"
            + "-fx-font-family: 'Libre Baskerville',serif;"
            + "-fx-letter-spacing: 1;"
    );
    return chip;
  }

  public static Label createMetaBadge(String label, String value) {
    VBox badge = new VBox(2);
    badge.setAlignment(Pos.CENTER_LEFT);
    badge.setPadding(new Insets(6, 14, 6, 14));
    badge.setStyle("-fx-background-color: rgba(177,141,184,0.06); -fx-background-radius: 2;");
    Label lbl = new Label(label);
    lbl.setStyle("-fx-font-size: 10; -fx-text-fill: #B0ADA8; -fx-font-weight: 600; -fx-font-family: 'Libre Baskerville',serif;");
    Label val = new Label(value);
    val.setStyle("-fx-font-size: 14; -fx-font-weight: 700; -fx-text-fill: #F5F0EB;");
    badge.getChildren().addAll(lbl, val);
    return new Label(label + ": " + value);
  }

  public static HBox createChipRow(String phase, String notes, String color) {
    HBox row = new HBox(8);
    row.setAlignment(Pos.CENTER_LEFT);
    Label phaseLabel = new Label(phase);
    phaseLabel.setStyle(
        "-fx-font-size: 11; -fx-font-weight: 700; -fx-text-fill: " + color + ";"
            + "-fx-min-width: 60; -fx-font-family: 'Libre Baskerville',serif;"
    );
    Label notesLabel = new Label(notes);
    notesLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #D4CFC9; -fx-font-family: 'Libre Baskerville',serif;");
    row.getChildren().addAll(phaseLabel, notesLabel);
    return row;
  }

  public static Label createAccordChip(String text) {
    Label chip = new Label(text.toUpperCase());
    chip.setStyle("-fx-background-color: rgba(109,168,158,0.12); -fx-text-fill: #6DA89E; -fx-font-size: 10; -fx-font-weight: 700; -fx-padding: 3 10; -fx-background-radius: 8; -fx-font-family: 'Libre Baskerville',serif;");
    return chip;
  }

  public static Label createOccasionBadge(String text) {
    Label badge = new Label(text.toUpperCase());
    badge.setStyle("-fx-background-color: rgba(177,141,184,0.08); -fx-text-fill: #b18db8; -fx-font-size: 10; -fx-font-weight: 700; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-family: 'Libre Baskerville',serif;");
    return badge;
  }

  public static VBox createScoreBadge(int score, String color) {
    VBox badge = new VBox(0);
    badge.setAlignment(Pos.CENTER);
    badge.setPadding(new Insets(4, 10, 4, 10));
    badge.setStyle("-fx-background-color: rgba(45,42,36,0.75); -fx-background-radius: 10;");
    Label pct = new Label(score + "%");
    pct.setStyle("-fx-font-size: 18; -fx-font-weight: 700; -fx-text-fill: " + color + "; -fx-font-family: 'Libre Baskerville',serif;");
    badge.getChildren().add(pct);
    return badge;
  }

  public static Rectangle createDivider() {
    Rectangle divider = new Rectangle();
    divider.setHeight(1);
    divider.setStyle("-fx-fill: rgba(177,141,184,0.2);");
    return divider;
  }
}
