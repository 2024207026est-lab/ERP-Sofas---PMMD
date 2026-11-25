package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import pe.erp.sofas.ui.three.geom.PipingMeshPMMD;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;









public class RibetePartPMMD extends BaseSofaPartPMMD {

    private final PhongMaterial materialRibetePMMD;

    public RibetePartPMMD(double ancho, double profundidadPMMD, PhongMaterial materialBasePMMD) {
        super("Ribete", ancho, profundidadPMMD, 1);

        if (materialBasePMMD != null) {
            Color colBasePMMD = (Color) materialBasePMMD.getDiffuseColor();
            Color colOscuro = colBasePMMD.deriveColor(0, 1, 0.85, 1);
            this.materialRibetePMMD = new PhongMaterial(colOscuro);
            this.materialRibetePMMD.setSpecularPower(materialBasePMMD.getSpecularPower());
        } else {
            this.materialRibetePMMD = new PhongMaterial(Color.web("#8B8B8B"));
            this.materialRibetePMMD.setSpecularPower(8);
        }
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();


        MeshView ribetePMMD = new MeshView(PipingMeshPMMD.crearDefecto(ancho, fondo));
        ribetePMMD.setMaterial(materialRibetePMMD);
        ribetePMMD.setCullFace(javafx.scene.shape.CullFace.NONE);

        agregar(gPMMD, ribetePMMD, 0, 0, 0);

        return gPMMD;
    }
}
