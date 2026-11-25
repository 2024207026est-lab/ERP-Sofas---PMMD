package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;







public class ChaseLoungeModulePMMD extends BaseSofaPartPMMD {

    private final PhongMaterial material;

    public ChaseLoungeModulePMMD(double largoPMMD, PhongMaterial material) {
        super("Chaise lounge", largoPMMD, 92, 85);
        this.material = material == null ? materialPorDefecto() : material;
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();


        agregar(gPMMD, cajaRedondeada(ancho, 18, fondo, 3, material), 0, 18 / 2, 0);


        double largoCojinPMMD = ancho - 4;
        agregar(gPMMD, cajaRedondeada(largoCojinPMMD, 14, fondo - 4, 5, material),
                0, 18 + 14 / 2, 0);


        agregar(gPMMD, cajaRedondeada(17, 14, 20, 4, material),
                ancho / 2 - 17 / 2, 18 + 14 / 2, -fondo / 2 + 10);

        return gPMMD;
    }
}
