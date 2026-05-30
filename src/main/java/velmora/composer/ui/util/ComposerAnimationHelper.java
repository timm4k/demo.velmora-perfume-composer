package velmora.composer.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

public final class ComposerAnimationHelper {

  private static final double PERF_BAR_MAX = 160.0;

  private ComposerAnimationHelper() {}

  public static void drawBalanceCircle(Canvas harmonyCircle, int value) {
    if (harmonyCircle == null) return;
    double w = harmonyCircle.getWidth();
    double h = harmonyCircle.getHeight();
    double cx = w / 2;
    double cy = h / 2;
    double r = Math.min(cx, cy) - 3;
    double angle = 360.0 * Math.min(100, Math.max(0, value)) / 100.0;

    GraphicsContext gc = harmonyCircle.getGraphicsContext2D();
    gc.clearRect(0, 0, w, h);

    gc.setLineWidth(2.5);
    gc.setStroke(Color.rgb(234, 230, 223));
    gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 0, 360, ArcType.OPEN);

    Color fillColor = value >= 70 ? Color.rgb(177, 141, 184)
        : value >= 40 ? Color.rgb(200, 149, 74)
            : Color.rgb(217, 83, 79);
    gc.setStroke(fillColor);
    gc.setLineCap(StrokeLineCap.ROUND);
    gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 90, -angle, ArcType.OPEN);
  }

  public static void animatePerfBar(Rectangle bar, int value) {
    double target = PERF_BAR_MAX * value / 100.0;
    if (bar.getWidth() == target) return;
    Timeline anim = new Timeline(
        new KeyFrame(Duration.millis(400),
            new KeyValue(bar.widthProperty(), target, Interpolator.EASE_OUT))
    );
    anim.play();
  }

  public static void animatePyramidBar(Rectangle bar, double target) {
    if (bar.getWidth() == target) return;
    Timeline anim = new Timeline(
        new KeyFrame(Duration.millis(400),
            new KeyValue(bar.widthProperty(), target, Interpolator.EASE_OUT))
    );
    anim.play();
  }

  public static void initParticles(javafx.scene.layout.Pane particleLayer) {
    Random rand = new Random();
    List<Circle> particles = new ArrayList<>();
    for (int i = 0; i < 35; i++) {
      Circle c = new Circle(1 + rand.nextDouble() * 3);
      double t = rand.nextDouble();
      if (t < 0.33)      c.setFill(Color.rgb(214, 148, 120, 0.06 + rand.nextDouble() * 0.10));
      else if (t < 0.66) c.setFill(Color.rgb(177, 141, 184, 0.06 + rand.nextDouble() * 0.10));
      else                c.setFill(Color.rgb(109, 168, 158, 0.06 + rand.nextDouble() * 0.10));
      c.setCenterX(rand.nextDouble() * 600 + 20);
      c.setCenterY(rand.nextDouble() * 400 + 40);
      c.setOpacity(0);
      particleLayer.getChildren().add(c);
      particles.add(c);
    }

    Timeline tl = new Timeline();
    for (int i = 0; i < particles.size(); i++) {
      Circle p = particles.get(i);
      double delay = rand.nextDouble() * 15;
      double dur = 12 + rand.nextDouble() * 16;
      double dx = (rand.nextDouble() - 0.5) * 80;
      double dy = (rand.nextDouble() - 0.5) * 50;
      tl.getKeyFrames().addAll(
          new KeyFrame(Duration.seconds(delay), new KeyValue(p.opacityProperty(), 0)),
          new KeyFrame(Duration.seconds(delay + 3), new KeyValue(p.opacityProperty(), 0.15 + rand.nextDouble() * 0.12)),
          new KeyFrame(Duration.seconds(delay + 3 + dur),
              new KeyValue(p.translateXProperty(), dx, Interpolator.EASE_BOTH),
              new KeyValue(p.translateYProperty(), dy, Interpolator.EASE_BOTH),
              new KeyValue(p.opacityProperty(), 0.15 + rand.nextDouble() * 0.12)),
          new KeyFrame(Duration.seconds(delay + 3 + dur + 3),
              new KeyValue(p.opacityProperty(), 0, Interpolator.EASE_BOTH)));
    }
    tl.setCycleCount(Timeline.INDEFINITE);
    tl.play();
  }

  public static void initFloatingGradients(javafx.scene.layout.Pane floatingGradients) {
    Random rand = new Random();

    double[][] blobSpecs = {
        {260, 0.14, 214, 148, 120},
        {300, 0.14, 177, 141, 184},
        {240, 0.14, 109, 168, 158}
    };
    String[] classes = {"floating-blob-peach", "floating-blob-lavender", "floating-blob-teal"};

    Circle[] blobs = new Circle[3];

    for (int i = 0; i < 3; i++) {
      Circle blob = new Circle(blobSpecs[i][0]);
      blob.getStyleClass().addAll("floating-blob", classes[i]);
      blob.setCenterX(200 + rand.nextDouble() * 300);
      blob.setCenterY(150 + rand.nextDouble() * 200);
      blob.setOpacity(0);
      floatingGradients.getChildren().add(blob);
      blobs[i] = blob;
    }

    Timeline tl = new Timeline();
    for (int i = 0; i < 3; i++) {
      Circle blob = blobs[i];
      double delay = rand.nextDouble() * 5;
      double startX = blob.getCenterX();
      double startY = blob.getCenterY();
      double dx1 = (rand.nextDouble() - 0.5) * 120;
      double dy1 = (rand.nextDouble() - 0.5) * 80;
      double dx2 = (rand.nextDouble() - 0.5) * 120;
      double dy2 = (rand.nextDouble() - 0.5) * 80;
      double dx3 = (rand.nextDouble() - 0.5) * 120;
      double dy3 = (rand.nextDouble() - 0.5) * 80;

      tl.getKeyFrames().addAll(
          new KeyFrame(Duration.seconds(delay), new KeyValue(blob.opacityProperty(), 0)),
          new KeyFrame(Duration.seconds(delay + 3), new KeyValue(blob.opacityProperty(), 0.8 + rand.nextDouble() * 0.2)),
          new KeyFrame(Duration.seconds(delay + 10),
              new KeyValue(blob.centerXProperty(), startX + dx1, Interpolator.EASE_BOTH),
              new KeyValue(blob.centerYProperty(), startY + dy1, Interpolator.EASE_BOTH)),
          new KeyFrame(Duration.seconds(delay + 20),
              new KeyValue(blob.centerXProperty(), startX + dx1 + dx2, Interpolator.EASE_BOTH),
              new KeyValue(blob.centerYProperty(), startY + dy1 + dy2, Interpolator.EASE_BOTH)),
          new KeyFrame(Duration.seconds(delay + 30),
              new KeyValue(blob.centerXProperty(), startX + dx1 + dx2 + dx3, Interpolator.EASE_BOTH),
              new KeyValue(blob.centerYProperty(), startY + dy1 + dy2 + dy3, Interpolator.EASE_BOTH)),
          new KeyFrame(Duration.seconds(delay + 35),
              new KeyValue(blob.opacityProperty(), 0.6, Interpolator.EASE_BOTH)),
          new KeyFrame(Duration.seconds(delay + 40),
              new KeyValue(blob.centerXProperty(), startX, Interpolator.EASE_BOTH),
              new KeyValue(blob.centerYProperty(), startY, Interpolator.EASE_BOTH)),
          new KeyFrame(Duration.seconds(delay + 43),
              new KeyValue(blob.opacityProperty(), 0, Interpolator.EASE_BOTH)));
    }
    tl.setCycleCount(Timeline.INDEFINITE);
    tl.play();
  }

  public static void fadeInNode(Node node, Duration duration) {
    FadeTransition ft = new FadeTransition(duration, node);
    ft.setToValue(1.0);
    ft.play();
  }

  public static void slideIn(Node node, Duration fadeDuration, Duration slideDuration) {
    node.setOpacity(0);
    node.setTranslateY(10);
    FadeTransition ft = new FadeTransition(fadeDuration, node);
    ft.setToValue(1.0);
    TranslateTransition tt = new TranslateTransition(slideDuration, node);
    tt.setToY(0);
    tt.setInterpolator(Interpolator.EASE_OUT);
    ft.play();
    tt.play();
  }

  public static void scaleIn(Node node, Duration duration) {
    node.setScaleX(0.85);
    node.setScaleY(0.85);
    ScaleTransition st = new ScaleTransition(duration, node);
    st.setToX(1.0);
    st.setToY(1.0);
    st.setInterpolator(Interpolator.EASE_OUT);
    st.play();
  }

  public static void fadeLabels(Node... labels) {
    for (Node node : labels) {
      if (node != null) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), node);
        ft.setFromValue(0.6);
        ft.setToValue(1.0);
        ft.play();
      }
    }
  }
}
