package velmora.composer.ui.util;

import javafx.scene.paint.Color;

public final class UiUtils {

  private UiUtils() {}

  public static Color parseColor(String colorCode) {
    if (colorCode == null || colorCode.isBlank()) {
      return Color.web("#7c6b6b");
    }
    try {
      return Color.web(colorCode);
    } catch (Exception e) {
      return Color.web("#7c6b6b");
    }
  }

  public static String formatPct(int value) {
    return value + "%";
  }

  public static String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
  }
}
