package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;
import pe.erp.sofas.ui.three.MallasPMMD;








public class LegsPartPMMD extends BaseSofaPartPMMD {

    public enum TipoPata {
        
        MADERA_CONICA(13),
        
        METAL_NEGRO(16),
        
        METAL_DORADO(16),
        
        ZOCALO(7);

        final double altura;

        TipoPata(double altura) {
            this.altura = altura;
        }
    }

    private final TipoPata tipo;

    private final PhongMaterial material;

    public LegsPartPMMD(double ancho, double fondo, TipoPata tipo, PhongMaterial material) {
        super("Patas " + tipo.name(), ancho, fondo, tipo.altura);
        this.tipo = tipo;
        this.material = material == null ? materialPorDefecto() : material;
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();


        double[] xsPMMD = {-ancho / 2 + 7, ancho / 2 - 7};
        double[] zsPMMD = {-fondo / 2 + 7, fondo / 2 - 7};

        for (double xPMMD : xsPMMD) {
            for (double zPMMD : zsPMMD) {
                pata(gPMMD, xPMMD, zPMMD, alto);
            }
        }

        return gPMMD;
    }

    private void pata(Group gPMMD, double xPMMD, double zPMMD, double h) {
        switch (tipo) {
            case ZOCALO -> {

                agregar(gPMMD, cajaRedondeada(2, h, 2, 0.5, material), xPMMD, h / 2, zPMMD);
            }
            default -> {

                Cylinder pata = new Cylinder(1.1, h, 14);
                pata.setMaterial(material);
                pata.setTranslateX(xPMMD);
                pata.setTranslateY(-h / 2);
                pata.setTranslateZ(zPMMD);
                gPMMD.getChildren().add(pata);
            }
        }
    }
}
