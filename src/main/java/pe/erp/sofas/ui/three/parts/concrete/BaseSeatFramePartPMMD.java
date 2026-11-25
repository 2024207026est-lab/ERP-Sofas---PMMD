package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;






public class BaseSeatFramePartPMMD extends BaseSofaPartPMMD {

    private final PhongMaterial material;

    private final double altoPatas;

    public BaseSeatFramePartPMMD(double ancho, double fondo, double altoPatas, PhongMaterial material) {
        super("Base de asiento", ancho, fondo, 18);
        this.material = material == null ? materialPorDefecto() : material;
        this.altoPatas = altoPatas;
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();

        agregar(gPMMD, cajaRedondeada(ancho, alto, fondo, 3, material), 0, alto / 2, 0);
        return gPMMD;
    }
}
