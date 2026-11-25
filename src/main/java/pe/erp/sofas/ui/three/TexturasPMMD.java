package pe.erp.sofas.ui.three;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.*;
import pe.erp.sofas.model.TipoTelaPMMD;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;



public final class TexturasPMMD {

    private static final Map<String, Image> CACHEPMMD = new HashMap<>();

    private TexturasPMMD() {
    }



    public static Image tejido(TipoTelaPMMD tipoPMMD) {
        String kPMMD = "tejido-" + (tipoPMMD == null ? "x" : tipoPMMD.name());
        return CACHEPMMD.computeIfAbsent(kPMMD, xPMMD -> {
            int s = 128;
            WritableImage imgPMMD = new WritableImage(s, s);
            var pwPMMD = imgPMMD.getPixelWriter();
            Random rPMMD = new Random(7);
            for (int yPMMD = 0; yPMMD < s; yPMMD++) {
                for (int xxPMMD = 0; xxPMMD < s; xxPMMD++) {
                    double vPMMD = 0.93;
                    if (tipoPMMD != null) {
                        switch (tipoPMMD) {
                            case LINO -> vPMMD = 0.86 + 0.08 * (((xxPMMD / 2) % 2 == 0) ^ ((yPMMD / 2) % 2 == 0) ? 1 : 0) + rPMMD.nextDouble() * 0.06;
                            case CHENILLE -> vPMMD = 0.84 + 0.1 * Math.abs(Math.sin((xxPMMD + yPMMD) * 0.35)) + rPMMD.nextDouble() * 0.05;
                            case BOUCLE -> {
                                double cxPMMD = (xxPMMD % 8) - 4, cyPMMD = (yPMMD % 8) - 4;
                                vPMMD = 0.80 + 0.16 * Math.max(0, 1 - Math.sqrt(cxPMMD * cxPMMD + cyPMMD * cyPMMD) / 4) + rPMMD.nextDouble() * 0.04;
                            }
                            case TERCIOPELO -> vPMMD = 0.9 + rPMMD.nextDouble() * 0.06;
                            case CUERO -> vPMMD = 0.93 + rPMMD.nextDouble() * 0.04 - (rPMMD.nextDouble() > 0.985 ? 0.08 : 0);
                            case MICROFIBRA -> vPMMD = 0.9 + rPMMD.nextDouble() * 0.05;
                        }
                    }
                    vPMMD = Math.max(0, Math.min(1, vPMMD));
                    pwPMMD.setColor(xxPMMD, yPMMD, Color.gray(vPMMD));
                }
            }
            return imgPMMD;
        });
    }



    public static Image sombra() {
        return CACHEPMMD.computeIfAbsent("sombra", xPMMD -> {
            Canvas cPMMD = new Canvas(256, 256);
            GraphicsContext gPMMD = cPMMD.getGraphicsContext2D();
            gPMMD.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(15, 23, 42, 0.38)), new Stop(0.6, Color.rgb(15, 23, 42, 0.16)), new Stop(1, Color.rgb(15, 23, 42, 0))));
            gPMMD.fillRect(0, 0, 256, 256);
            var spPMMD = new javafx.scene.SnapshotParameters();
            spPMMD.setFill(Color.TRANSPARENT);
            return cPMMD.snapshot(spPMMD, null);
        });
    }



    public static Image piso(boolean maderaPMMD) {
        return CACHEPMMD.computeIfAbsent("piso-" + maderaPMMD, xPMMD -> {
            int s = 256;
            WritableImage imgPMMD = new WritableImage(s, s);
            var pwPMMD = imgPMMD.getPixelWriter();
            Random rPMMD = new Random(3);
            for (int yPMMD = 0; yPMMD < s; yPMMD++) {
                for (int xxPMMD = 0; xxPMMD < s; xxPMMD++) {
                    Color cPMMD;
                    if (maderaPMMD) {
                        int tablaPMMD = yPMMD / 32;
                        double tonoPMMD = 0.78 + (tablaPMMD % 3) * 0.03 + Math.sin(xxPMMD * 0.05 + tablaPMMD) * 0.02 + rPMMD.nextDouble() * 0.02;
                        cPMMD = yPMMD % 32 == 0 ? Color.color(0.62, 0.5, 0.38) : Color.color(Math.min(1, tonoPMMD + 0.08), tonoPMMD, tonoPMMD * 0.82);
                    } else {
                        double vPMMD = 0.95 + rPMMD.nextDouble() * 0.02;
                        cPMMD = Color.color(vPMMD, vPMMD, vPMMD * 0.99);
                    }
                    pwPMMD.setColor(xxPMMD, yPMMD, cPMMD);
                }
            }
            return imgPMMD;
        });
    }
}
