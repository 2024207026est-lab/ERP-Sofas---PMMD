package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;







public class DecorativeCushionPartPMMD extends BaseSofaPartPMMD {

    private final PhongMaterial material;

    public DecorativeCushionPartPMMD(double ancho, PhongMaterial material) {
        super("Cojín decorativo", ancho, ancho, 12);
        this.material = material == null ? materialPorDefecto() : material;
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();


        var cojinPMMD = cajaRedondeada(ancho, alto, fondo, 5, material);


        cojinPMMD.getTransforms().add(new Rotate(-12, Rotate.X_AXIS));
        cojinPMMD.getTransforms().add(new Rotate(6, Rotate.Z_AXIS));

        agregar(gPMMD, cojinPMMD, 0, alto / 2, 0);

        return gPMMD;
    }
}
