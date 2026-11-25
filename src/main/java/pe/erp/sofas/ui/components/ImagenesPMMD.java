package pe.erp.sofas.ui.components;

import javafx.scene.image.Image;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;



public final class ImagenesPMMD {

    private static final Map<String, Image> CACHEPMMD = new HashMap<>();

    private ImagenesPMMD() {
    }

    public static Image cargar(String ruta) {
        if (ruta == null || ruta.isBlank()) return null;
        return CACHEPMMD.computeIfAbsent(ruta + "@" + new File(ruta).lastModified(), kPMMD -> {
            if (ruta.startsWith("classpath:")) {
                URL urlPMMD = ImagenesPMMD.class.getResource(ruta.substring("classpath:".length()));
                return urlPMMD == null ? null : new Image(urlPMMD.toExternalForm(), 1400, 0, true, true, false);
            }
            File fPMMD = new File(ruta);
            return fPMMD.exists() ? new Image(fPMMD.toURI().toString(), 1400, 0, true, true, false) : null;
        });
    }

    public static void olvidar(String ruta) {
        CACHEPMMD.keySet().removeIf(kPMMD -> kPMMD.startsWith(ruta + "@"));
    }
}
