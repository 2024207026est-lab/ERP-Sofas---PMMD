package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import pe.erp.sofas.ui.three.geom.PillowMeshPMMD;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;









public class BackrestPartPMMD extends BaseSofaPartPMMD {

    private final PhongMaterial material;

    private final double altoRespaldo;

    public BackrestPartPMMD(double ancho, double altoRespaldo, PhongMaterial material) {
        super("Respaldo", ancho, 20, altoRespaldo);
        this.material = material == null ? materialPorDefecto() : material;
        this.altoRespaldo = altoRespaldo;
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();


        MeshView respaldoPMMD = new MeshView(PillowMeshPMMD.crearDefecto(ancho, altoRespaldo, fondo, 4));
        respaldoPMMD.setMaterial(material);
        respaldoPMMD.setCullFace(javafx.scene.shape.CullFace.NONE);


        respaldoPMMD.getTransforms().add(new Rotate(-8, 0, 0, 0, Rotate.X_AXIS));

        agregar(gPMMD, respaldoPMMD, 0, altoRespaldo / 2, fondo / 2 - 10);

        return gPMMD;
    }
}
