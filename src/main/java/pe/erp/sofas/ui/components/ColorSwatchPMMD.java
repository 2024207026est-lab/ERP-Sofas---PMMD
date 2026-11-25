package pe.erp.sofas.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import pe.erp.sofas.model.TipoTelaPMMD;






public class ColorSwatchPMMD extends StackPane {

    private final Canvas canvasPMMD;

    private final double size;

    public ColorSwatchPMMD(String hexPMMD, TipoTelaPMMD tipoPMMD, double size) {
        this.size = size;
        canvasPMMD = new Canvas(size, size);
        getStyleClass().add("swatch");
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
        Circle clipPMMD = new Circle(size / 2, size / 2, size / 2);
        canvasPMMD.setClip(clipPMMD);
        InnerShadow sombraPMMD = new InnerShadow(size * 0.12, Color.rgb(0, 0, 0, 0.35));
        sombraPMMD.setOffsetY(size * 0.03);
        canvasPMMD.setEffect(sombraPMMD);
        getChildren().add(canvasPMMD);
        pintar(hexPMMD, tipoPMMD);
    }

    public void pintar(String hexPMMD, TipoTelaPMMD tipoPMMD) {
        GraphicsContext gPMMD = canvasPMMD.getGraphicsContext2D();
        Color basePMMD = parse(hexPMMD);
        gPMMD.clearRect(0, 0, size, size);
        gPMMD.setFill(basePMMD);
        gPMMD.fillOval(0, 0, size, size);
        if (tipoPMMD == null) return;
        Color claroPMMD = basePMMD.deriveColor(0, 1, 1.25, 1);
        Color oscuroPMMD = basePMMD.deriveColor(0, 1, 0.78, 1);
        switch (tipoPMMD) {
            case CHENILLE -> {
                gPMMD.setStroke(Color.color(claroPMMD.getRed(), claroPMMD.getGreen(), claroPMMD.getBlue(), 0.35));
                gPMMD.setLineWidth(Math.max(1, size / 26));
                for (double dPMMD = -size; dPMMD < size * 2; dPMMD += size / 7) gPMMD.strokeLine(dPMMD, 0, dPMMD + size, size);
            }
            case LINO -> {
                gPMMD.setStroke(Color.color(oscuroPMMD.getRed(), oscuroPMMD.getGreen(), oscuroPMMD.getBlue(), 0.35));
                gPMMD.setLineWidth(Math.max(0.6, size / 60));
                for (double dPMMD = 0; dPMMD < size; dPMMD += size / 14) {
                    gPMMD.strokeLine(dPMMD, 0, dPMMD, size);
                    gPMMD.strokeLine(0, dPMMD, size, dPMMD);
                }
            }
            case CUERO -> {
                gPMMD.setFill(new RadialGradient(0, 0, 0.32, 0.28, 0.6, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(255, 255, 255, 0.55)), new Stop(0.45, Color.rgb(255, 255, 255, 0.08)), new Stop(1, Color.TRANSPARENT)));
                gPMMD.fillOval(0, 0, size, size);
            }
            case TERCIOPELO -> {
                gPMMD.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(255, 255, 255, 0.0)), new Stop(0.45, Color.rgb(255, 255, 255, 0.32)),
                        new Stop(0.55, Color.rgb(255, 255, 255, 0.05)), new Stop(1, Color.rgb(0, 0, 0, 0.25))));
                gPMMD.fillOval(0, 0, size, size);
            }
            case BOUCLE -> {
                gPMMD.setFill(Color.color(oscuroPMMD.getRed(), oscuroPMMD.getGreen(), oscuroPMMD.getBlue(), 0.45));
                double pasoPMMD = size / 7;
                for (double yPMMD = pasoPMMD / 2; yPMMD < size; yPMMD += pasoPMMD) {
                    for (double xPMMD = (yPMMD / pasoPMMD) % 2 < 1 ? pasoPMMD / 2 : pasoPMMD; xPMMD < size; xPMMD += pasoPMMD) {
                        gPMMD.fillOval(xPMMD - size / 40, yPMMD - size / 40, size / 20, size / 20);
                    }
                }
            }
            case MICROFIBRA -> {
                gPMMD.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.color(claroPMMD.getRed(), claroPMMD.getGreen(), claroPMMD.getBlue(), 0.45)), new Stop(1, Color.color(oscuroPMMD.getRed(), oscuroPMMD.getGreen(), oscuroPMMD.getBlue(), 0.45))));
                gPMMD.fillOval(0, 0, size, size);
            }
        }
    }

    public static Color parse(String hexPMMD) {
        try {
            return Color.web(hexPMMD == null ? "#CBD5E1" : hexPMMD);
        } catch (IllegalArgumentException ePMMD) {
            return Color.web("#CBD5E1");
        }
    }

    public ColorSwatchPMMD seleccionada(boolean sPMMD) {
        pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("selected"), sPMMD);
        return this;
    }
}
