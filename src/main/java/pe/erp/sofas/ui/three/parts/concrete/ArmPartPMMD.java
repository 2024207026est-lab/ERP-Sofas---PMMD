package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;







public class ArmPartPMMD extends BaseSofaPartPMMD {

    public enum TipoBrazo {
        
        RECTO(17, 4),
        
        REDONDEADO(18, 9),
        
        DELGADO(9, 3);

        final double ancho;
        
        final double radio;

        TipoBrazo(double ancho, double radio) {
            this.ancho = ancho;
            this.radio = radio;
        }
    }

    private final TipoBrazo tipo;

    private final PhongMaterial material;

    private final double altoBrazo;

    public ArmPartPMMD(TipoBrazo tipo, double altoBrazo, double fondo, PhongMaterial material) {
        super("Brazo " + tipo.name(), tipo.ancho, fondo, altoBrazo);
        this.tipo = tipo;
        this.material = material == null ? materialPorDefecto() : material;
        this.altoBrazo = altoBrazo;
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();


        agregar(gPMMD, cajaRedondeada(ancho, altoBrazo, fondo, tipo.radio, material), 0, altoBrazo / 2, 0);


        if (tipo == TipoBrazo.REDONDEADO) {
            Cylinder rolloPMMD = new Cylinder(ancho / 2 + 1, fondo - 2, 28);
            rolloPMMD.setMaterial(material);
            rolloPMMD.getTransforms().addAll(
                new Translate(0, -(altoBrazo - 1), 0),
                new Rotate(90, Rotate.X_AXIS)
            );
            gPMMD.getChildren().add(rolloPMMD);
        }

        return gPMMD;
    }
}
