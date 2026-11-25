package pe.erp.sofas.ui.three;

import javafx.scene.image.Image;
import pe.erp.sofas.model.ProductoPMMD;

import java.util.HashMap;
import java.util.Map;



public final class Render3DPMMD {

    private static final Map<String, Image> CACHEPMMD = new HashMap<>();

    private Render3DPMMD() {
    }

    public static Image modelo(ProductoPMMD pPMMD) {
        return CACHEPMMD.computeIfAbsent(pPMMD.getCodigo() + pPMMD.getColorHex() + pPMMD.getLargo(), kPMMD ->
                Viewer3DPMMD.render(new SofaBuilderPMMD().build(SofaBuilderPMMD.Opciones.de(pPMMD)), 640, 420, -28, 16));
    }
}
