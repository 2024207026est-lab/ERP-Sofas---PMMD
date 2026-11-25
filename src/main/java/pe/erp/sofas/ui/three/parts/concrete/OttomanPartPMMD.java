package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;






public class OttomanPartPMMD extends BaseSofaPartPMMD {

    private final PhongMaterial material;

    public OttomanPartPMMD(double ancho, double fondo, PhongMaterial material) {
        super("Puff", ancho, fondo, 40);
        this.material = material == null ? materialPorDefecto() : material;
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();


        agregar(gPMMD, cajaRedondeada(ancho, alto, fondo, 6, material), 0, alto / 2, 0);

        return gPMMD;
    }
}
