package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import pe.erp.sofas.ui.three.geom.TuftingMeshPMMD;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;












public class CapitonePartPMMD extends BaseSofaPartPMMD {

    private final PhongMaterial materialTela;

    private final PhongMaterial materialBotonPMMD;

    public CapitonePartPMMD(double ancho, double alto, double profundidadPMMD, PhongMaterial materialTela) {
        super("Capitoné", ancho, profundidadPMMD, alto);
        this.materialTela = materialTela == null ? materialPorDefecto() : materialTela;


        Color colBasePMMD = (Color) this.materialTela.getDiffuseColor();
        Color colBoton = colBasePMMD.deriveColor(0, 1, 0.8, 1);
        this.materialBotonPMMD = new PhongMaterial(colBoton);
        this.materialBotonPMMD.setSpecularPower(this.materialTela.getSpecularPower());
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();


        MeshView capitonePMMD = new MeshView(TuftingMeshPMMD.crearDefecto(ancho, alto, fondo));
        capitonePMMD.setMaterial(materialTela);
        capitonePMMD.setCullFace(javafx.scene.shape.CullFace.NONE);

        agregar(gPMMD, capitonePMMD, 0, alto / 2, 0);


        agregarBotonesPMMD(gPMMD);

        return gPMMD;
    }





    private void agregarBotonesPMMD(Group gPMMD) {
        double tamRomboPMMD = 13;
        double radioBoto = 1.4;


        int filasPMMD = (int) Math.ceil(alto / tamRomboPMMD) + 1;
        int colsPMMD = (int) Math.ceil(ancho / tamRomboPMMD) + 1;

        double hxPMMD = ancho / 2, hyPMMD = alto / 2;

        for (int rowPMMD = 0; rowPMMD < filasPMMD; rowPMMD++) {
            for (int colPMMD = 0; colPMMD < colsPMMD; colPMMD++) {
                double yPMMD = -hyPMMD + (rowPMMD / (double) (filasPMMD - 1)) * alto;
                double xPMMD = -hxPMMD + (colPMMD / (double) (colsPMMD - 1)) * ancho;


                Sphere botonPMMD = new Sphere(radioBoto, 8);
                botonPMMD.setMaterial(materialBotonPMMD);

                botonPMMD.setTranslateX(xPMMD);
                botonPMMD.setTranslateY(-yPMMD);
                botonPMMD.setTranslateZ(fondo / 2 - 0.2);

                gPMMD.getChildren().add(botonPMMD);
            }
        }
    }
}
