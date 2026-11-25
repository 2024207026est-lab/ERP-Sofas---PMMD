package pe.erp.sofas.ui.components;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;



public final class SnapshotPMMD {

    private SnapshotPMMD() {
    }

    public static BufferedImage imagen(WritableImage imgPMMD) {
        int w = (int) imgPMMD.getWidth(), h = (int) imgPMMD.getHeight();
        BufferedImage biPMMD = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader prPMMD = imgPMMD.getPixelReader();
        for (int yPMMD = 0; yPMMD < h; yPMMD++) {
            for (int xPMMD = 0; xPMMD < w; xPMMD++) biPMMD.setRGB(xPMMD, yPMMD, prPMMD.getArgb(xPMMD, yPMMD));
        }
        return biPMMD;
    }



    public static byte[] png(Node nodoPMMD) {
        if (nodoPMMD == null || nodoPMMD.getScene() == null) return null;
        SnapshotParameters spPMMD = new SnapshotParameters();
        spPMMD.setFill(Color.WHITE);
        spPMMD.setTransform(Transform.scale(2, 2));
        try (ByteArrayOutputStream outPMMD = new ByteArrayOutputStream()) {
            ImageIO.write(imagen(nodoPMMD.snapshot(spPMMD, null)), "png", outPMMD);
            return outPMMD.toByteArray();
        } catch (IOException ePMMD) {
            return null;
        }
    }
}
