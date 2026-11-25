package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import pe.erp.sofas.ui.three.geom.PillowMeshPMMD;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;










public class SeatCushionPartPMMD extends BaseSofaPartPMMD {

    private final PhongMaterial material;

    public SeatCushionPartPMMD(double ancho, double fondo, PhongMaterial material) {
        super("Cojín de asiento", ancho, fondo, 14);
        this.material = material == null ? materialPorDefecto() : material;
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();


        MeshView cojinPMMD = new MeshView(PillowMeshPMMD.crearDefecto(ancho, alto, fondo, 4));
        cojinPMMD.setMaterial(material);
        cojinPMMD.setCullFace(javafx.scene.shape.CullFace.NONE);

        agregar(gPMMD, cojinPMMD, 0, alto / 2, 0);






        return gPMMD;
    }
}
