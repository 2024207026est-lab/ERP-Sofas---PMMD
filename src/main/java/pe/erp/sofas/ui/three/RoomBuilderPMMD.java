package pe.erp.sofas.ui.three;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

import java.util.List;






public final class RoomBuilderPMMD {
    


    public record Ubicado(
      Node nodo, 
        double x, 
          double z, 
            double rotacion) {
    }



    public record Abertura(
      int pared, 
        double posicion, 
          double ancho, 
            boolean puerta) {
    }

    private RoomBuilderPMMD() {
    }


    
    static PhongMaterial mate(String hexPMMD) {
        PhongMaterial mPMMD = new PhongMaterial(Color.web(hexPMMD));
        mPMMD.setSpecularColor(Color.gray(0.06));
        mPMMD.setSpecularPower(8);
        return mPMMD;
    }

    public static Group build(double ancho, double largoPMMD, double altoPMMD, List<Abertura> aberturasPMMD, List<Ubicado> mueblesPMMD) {
        Group gPMMD = new Group();
        MeshView pisoPMMD = new MeshView(MallasPMMD.plano(ancho, largoPMMD));
        PhongMaterial mpPMMD = new PhongMaterial(Color.web("#E9D9C2"));
        mpPMMD.setDiffuseMap(TexturasPMMD.piso(true));
        mpPMMD.setSpecularColor(Color.gray(0.08));
        pisoPMMD.setMaterial(mpPMMD);
        pisoPMMD.setCullFace(CullFace.NONE);
        pisoPMMD.setTranslateX(ancho / 2);
        pisoPMMD.setTranslateZ(largoPMMD / 2);
        gPMMD.getChildren().add(pisoPMMD);

        double hParedPMMD = altoPMMD / 2;
        PhongMaterial pared = new PhongMaterial(Color.web("#F4F1EC"));
        pared.setSpecularColor(Color.gray(0.04));
        PhongMaterial zocaloPMMD = mate("#D6CFC4");
        double ePMMD = 10;

        gPMMD.getChildren().add(muroPMMD(ancho + 2 * ePMMD, hParedPMMD, ePMMD, ancho / 2, largoPMMD + ePMMD / 2, pared, aberturasPMMD, 2, ancho));
        gPMMD.getChildren().add(muroPMMD(ePMMD, hParedPMMD, largoPMMD, -ePMMD / 2, largoPMMD / 2, pared, aberturasPMMD, 3, largoPMMD));
        gPMMD.getChildren().add(muroPMMD(ePMMD, hParedPMMD, largoPMMD, ancho + ePMMD / 2, largoPMMD / 2, pared, aberturasPMMD, 1, largoPMMD));
        Box z1PMMD = new Box(ancho, 8, 1.5);
        z1PMMD.setMaterial(zocaloPMMD);
        z1PMMD.setTranslateX(ancho / 2);
        z1PMMD.setTranslateY(-4);
        z1PMMD.setTranslateZ(largoPMMD - 1);
        gPMMD.getChildren().add(z1PMMD);

        for (Abertura aPMMD : aberturasPMMD) {
            Box panelPMMD;
            PhongMaterial mPMMD;
            if (aPMMD.puerta()) {
                panelPMMD = new Box(aPMMD.ancho(), Math.min(hParedPMMD + 40, 205), 4);
                mPMMD = mate("#A47148");
            } else {
                panelPMMD = new Box(aPMMD.ancho(), hParedPMMD * 0.55, 2);
                mPMMD = new PhongMaterial(Color.web("#93C5FD"));
                mPMMD.setSpecularColor(Color.WHITE);
                mPMMD.setSpecularPower(60);
            }
            panelPMMD.setMaterial(mPMMD);
            double yPMMD = aPMMD.puerta() ? -panelPMMD.getHeight() / 2 : -(hParedPMMD * 0.45 + panelPMMD.getHeight() / 2);
            switch (aPMMD.pared()) {
                case 2 -> {
                    panelPMMD.setTranslateX(aPMMD.posicion() + aPMMD.ancho() / 2);
                    panelPMMD.setTranslateZ(largoPMMD - 2);
                }
                case 1 -> {
                    panelPMMD.setRotationAxis(Rotate.Y_AXIS);
                    panelPMMD.setRotate(90);
                    panelPMMD.setTranslateX(ancho - 2);
                    panelPMMD.setTranslateZ(aPMMD.posicion() + aPMMD.ancho() / 2);
                }
                case 3 -> {
                    panelPMMD.setRotationAxis(Rotate.Y_AXIS);
                    panelPMMD.setRotate(90);
                    panelPMMD.setTranslateX(2);
                    panelPMMD.setTranslateZ(aPMMD.posicion() + aPMMD.ancho() / 2);
                }
                default -> {
                    panelPMMD.setTranslateX(aPMMD.posicion() + aPMMD.ancho() / 2);
                    panelPMMD.setTranslateZ(1);
                    panelPMMD.setHeight(4);
                    yPMMD = -2;
                }
            }
            panelPMMD.setTranslateY(yPMMD);
            gPMMD.getChildren().add(panelPMMD);
        }

        for (Ubicado uPMMD : mueblesPMMD) {
            Group contPMMD = new Group(uPMMD.nodo());
            contPMMD.setTranslateX(uPMMD.x());
            contPMMD.setTranslateZ(uPMMD.z());
            contPMMD.getTransforms().add(new Rotate(uPMMD.rotacion(), Rotate.Y_AXIS));
            gPMMD.getChildren().add(contPMMD);
        }
        return gPMMD;
    }

    private static Node muroPMMD(double w, double h, double d, double x, double z, PhongMaterial mPMMD, List<Abertura> abPMMD, int pared, double largoParedPMMD) {
        Box bPMMD = new Box(w, h, d);
        bPMMD.setMaterial(mPMMD);
        bPMMD.setTranslateX(x);
        bPMMD.setTranslateY(-h / 2);
        bPMMD.setTranslateZ(z);
        return bPMMD;
    }

    public static Node alfombra(double w, double d) {
        MeshView bPMMD = new MeshView(MallasPMMD.plano(w, d));
        PhongMaterial mPMMD = mate("#CBB89B");
        mPMMD.setDiffuseMap(TexturasPMMD.tejido(pe.erp.sofas.model.TipoTelaPMMD.BOUCLE));
        bPMMD.setMaterial(mPMMD);
        bPMMD.setCullFace(CullFace.NONE);
        bPMMD.setTranslateY(-0.8);
        return bPMMD;
    }

    public static Node planta() {
        Group gPMMD = new Group();
        Cylinder macetaPMMD = new Cylinder(18, 34, 24);
        macetaPMMD.setMaterial(mate("#E2E8F0"));
        macetaPMMD.setTranslateY(-17);
        gPMMD.getChildren().add(macetaPMMD);
        PhongMaterial hojaPMMD = mate("#4F8A5B");
        for (int iPMMD = 0; iPMMD < 7; iPMMD++) {
            Box h = new Box(10, 62 + iPMMD * 4, 2);
            h.setMaterial(hojaPMMD);
            h.setTranslateY(-(34 + 30 + iPMMD * 2));
            h.getTransforms().addAll(new Rotate(iPMMD * 51, Rotate.Y_AXIS), new Rotate(12 + (iPMMD % 3) * 6, Rotate.Z_AXIS));
            gPMMD.getChildren().add(h);
        }
        return gPMMD;
    }
}
