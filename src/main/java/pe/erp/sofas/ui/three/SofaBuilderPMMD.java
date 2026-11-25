package pe.erp.sofas.ui.three;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.model.FormaPMMD;
import pe.erp.sofas.model.ProductoPMMD;
import pe.erp.sofas.model.TelaPMMD;
import pe.erp.sofas.model.TipoTelaPMMD;

import java.util.HashMap;
import java.util.Map;








public final class SofaBuilderPMMD {
    


    public record Opciones(
      FormaPMMD forma, 
        int cuerpos, 
          double largo, 
            double fondo, 
              double alto, 
                ConfigSofaPMMD.Chaise chaise,
                           
                           ConfigSofaPMMD.Brazo brazo, 
                             ConfigSofaPMMD.Patas patas, 
                               String colorPatas, 
                                 String colorTela, 
                                   TipoTelaPMMD tipoTela,
                           
                           int cojines, 
                             String colorCojines, 
                               TipoTelaPMMD tipoCojines) {

        public static Opciones de(ConfigSofaPMMD cPMMD, TelaPMMD tela, TelaPMMD telaCojinesPMMD) {
            return new Opciones(cPMMD.modelo().forma(), cPMMD.cuerpos(), cPMMD.largo(), cPMMD.profundidad(), cPMMD.alto(), cPMMD.chaise(), cPMMD.brazo(),
                    cPMMD.patas(), cPMMD.patas() == ConfigSofaPMMD.Patas.MADERA ? cPMMD.acabado().hex() : null,
                    tela == null ? "#B9B7B1" : tela.getHex(), tela == null ? TipoTelaPMMD.CHENILLE : tela.getTipo(), cPMMD.cojines(),
                    telaCojinesPMMD == null ? (tela == null ? "#C9972E" : tela.getHex()) : telaCojinesPMMD.getHex(),
                    telaCojinesPMMD == null ? (tela == null ? TipoTelaPMMD.CHENILLE : tela.getTipo()) : telaCojinesPMMD.getTipo());
        }



        public static Opciones de(ProductoPMMD pPMMD) {
            FormaPMMD f = pPMMD.getForma();
            TipoTelaPMMD tipo = tipoPorNombrePMMD(pPMMD.getMaterial());
            String color = pPMMD.getColorHex() == null ? "#B9B7B1" : pPMMD.getColorHex();
            int n = f == FormaPMMD.JUEGO ? 3 : Math.max(1, Math.min(4, pPMMD.getCuerpos()));
            double l = pPMMD.getLargo() == null || pPMMD.getLargo() == 0 ? ConfigSofaPMMD.largoPara(n) : pPMMD.getLargo();
            double fondo = pPMMD.getFondo() == null || pPMMD.getFondo() == 0 ? 92 : pPMMD.getFondo();
            ConfigSofaPMMD.Chaise ch = f == FormaPMMD.SECCIONAL_L || f == FormaPMMD.SECCIONAL_U ? ConfigSofaPMMD.Chaise.DERECHA : ConfigSofaPMMD.Chaise.NINGUNA;
            if (ch != ConfigSofaPMMD.Chaise.NINGUNA) fondo = 92;
            if (f == FormaPMMD.SECCIONAL_U) {
                n = 3;
                l = 300;
            }
            ConfigSofaPMMD.Brazo brazo = switch (f) {
                case CHESTERFIELD, SILLON -> ConfigSofaPMMD.Brazo.REDONDEADO;
                case ESCANDINAVO -> ConfigSofaPMMD.Brazo.DELGADO;
                default -> ConfigSofaPMMD.Brazo.RECTO;
            };
            ConfigSofaPMMD.Patas patas = f == FormaPMMD.SECCIONAL_L ? ConfigSofaPMMD.Patas.METAL_NEGRO
                    : f == FormaPMMD.PUFF || f == FormaPMMD.RECLINABLE ? ConfigSofaPMMD.Patas.ZOCALO : ConfigSofaPMMD.Patas.MADERA;
            return new Opciones(f, n, l, fondo, pPMMD.getAlto() == null || pPMMD.getAlto() == 0 ? 85 : pPMMD.getAlto(), ch, brazo, patas,
                    f == FormaPMMD.CHESTERFIELD ? "#3B2A20" : "#C69A6D", color, tipo, f == FormaPMMD.MODERNO || f == FormaPMMD.ESCANDINAVO ? 2 : 0,
                    "#C9972E", TipoTelaPMMD.CHENILLE);
        }

        private static TipoTelaPMMD tipoPorNombrePMMD(String materialPMMD) {
            if (materialPMMD == null) return TipoTelaPMMD.CHENILLE;
            for (TipoTelaPMMD tPMMD : TipoTelaPMMD.values()) if (tPMMD.nombre().startsWith(materialPMMD.split(" ")[0])) return tPMMD;
            return TipoTelaPMMD.CHENILLE;
        }
    }

    private final Map<String, PhongMaterial> materialesPMMD = new HashMap<>();

    public Group build(Opciones oPMMD) {
        Group gPMMD = new Group();
        if (oPMMD.forma() == FormaPMMD.PUFF) {
            puffPMMD(gPMMD, oPMMD);
            return gPMMD;
        }
        PhongMaterial tela = tela(oPMMD.colorTela(), oPMMD.tipoTela());
        PhongMaterial telaOscuraPMMD = tela(Color.web(oPMMD.colorTela()).deriveColor(0, 1, 0.9, 1).toString(), oPMMD.tipoTela());
        boolean chesPMMD = oPMMD.forma() == FormaPMMD.CHESTERFIELD;
        boolean escPMMD = oPMMD.forma() == FormaPMMD.ESCANDINAVO;
        boolean recPMMD = oPMMD.forma() == FormaPMMD.RECLINABLE;
        int n = Math.max(1, oPMMD.cuerpos());
        double LPMMD = oPMMD.largo(), FPMMD = oPMMD.fondo(), APMMD = oPMMD.alto();
        double hPatasPMMD = switch (oPMMD.patas()) {
            case ZOCALO -> 7;
            case METAL_NEGRO, METAL_DORADO -> 16;
            default -> escPMMD ? 18 : 13;
        };
        double armWPMMD = switch (oPMMD.brazo()) {
            case DELGADO -> 9;
            case REDONDEADO -> chesPMMD ? 22 : 18;
            default -> recPMMD ? 22 : 17;
        };
        double hBasePMMD = escPMMD ? 14 : 18;
        double yAsientoPMMD = hPatasPMMD + hBasePMMD;
        double hCojinPMMD = escPMMD ? 11 : 14;
        double fondoRespaldoPMMD = chesPMMD ? 22 : 20;
        double altoBrazoPMMD = chesPMMD ? APMMD - 6 : switch (oPMMD.brazo()) {
            case DELGADO -> APMMD * 0.62;
            default -> APMMD * 0.7;
        };
        boolean chaise = oPMMD.chaise() != null && oPMMD.chaise() != ConfigSofaPMMD.Chaise.NINGUNA;
        boolean chaiseDerechaPMMD = oPMMD.chaise() == ConfigSofaPMMD.Chaise.DERECHA;
        double extraPMMD = chaise ? 65 : 0;


        double interiorPMMD = LPMMD - 2 * armWPMMD;
        add(gPMMD, cajaPMMD(LPMMD, hBasePMMD, FPMMD, 3, tela), 0, hPatasPMMD + hBasePMMD / 2, 0);
        double anchoCojinPMMD = interiorPMMD / n;
        double zCojinPMMD = -fondoRespaldoPMMD / 2;
        double profCojinPMMD = FPMMD - fondoRespaldoPMMD - 2;
        for (int iPMMD = 0; iPMMD < n; iPMMD++) {
            double xPMMD = -interiorPMMD / 2 + anchoCojinPMMD * (iPMMD + 0.5);
            boolean esChaisePMMD = chaise && ((chaiseDerechaPMMD && iPMMD == n - 1) || (!chaiseDerechaPMMD && iPMMD == 0));
            double profPMMD = profCojinPMMD + (esChaisePMMD ? extraPMMD : 0);
            double zPMMD = zCojinPMMD - (esChaisePMMD ? extraPMMD / 2 : 0);

            add(gPMMD, cajaPMMD(anchoCojinPMMD - 1, hCojinPMMD, profPMMD, 5, tela), xPMMD, yAsientoPMMD + hCojinPMMD / 2, zPMMD);

            double hRespPMMD = APMMD - yAsientoPMMD - hCojinPMMD - (chesPMMD ? 0 : 3);
            if (!chesPMMD) {
                Node rPMMD = add(gPMMD, cajaPMMD(anchoCojinPMMD - 1, hRespPMMD, 16, 6, tela), xPMMD, yAsientoPMMD + hCojinPMMD + hRespPMMD / 2, FPMMD / 2 - fondoRespaldoPMMD - 4);
                rPMMD.getTransforms().add(new Rotate(-8, 0, 0, 0, Rotate.X_AXIS));
            }
        }

        if (chaise) {
            double xChPMMD = (chaiseDerechaPMMD ? 1 : -1) * (interiorPMMD / 2 - anchoCojinPMMD / 2 + armWPMMD / 2);
            add(gPMMD, cajaPMMD(anchoCojinPMMD + armWPMMD, hBasePMMD, extraPMMD + 4, 3, tela), xChPMMD, hPatasPMMD + hBasePMMD / 2, -FPMMD / 2 - extraPMMD / 2 + 2);
        }

        double hRespaldoFramePMMD = APMMD - hPatasPMMD - (chesPMMD ? 0 : 6);
        add(gPMMD, cajaPMMD(LPMMD, hRespaldoFramePMMD, fondoRespaldoPMMD, chesPMMD ? 7 : 4, chesPMMD ? telaOscuraPMMD : tela), 0, hPatasPMMD + hRespaldoFramePMMD / 2, FPMMD / 2 - fondoRespaldoPMMD / 2);
        if (chesPMMD) capitonePMMD(gPMMD, LPMMD - 2 * armWPMMD, hPatasPMMD + hBasePMMD + hCojinPMMD, APMMD - 6, FPMMD / 2 - fondoRespaldoPMMD, telaOscuraPMMD);


        for (int ladoPMMD = -1; ladoPMMD <= 1; ladoPMMD += 2) {
            boolean sinBrazoPMMD = chaise && ((chaiseDerechaPMMD && ladoPMMD == 1) || (!chaiseDerechaPMMD && ladoPMMD == -1));
            double xPMMD = ladoPMMD * (LPMMD / 2 - armWPMMD / 2);
            if (sinBrazoPMMD) {
                add(gPMMD, cajaPMMD(armWPMMD, hBasePMMD + 6, FPMMD, 3, tela), xPMMD, hPatasPMMD + (hBasePMMD + 6) / 2, 0);
                continue;
            }
            double alto = altoBrazoPMMD - hPatasPMMD;
            double radioPMMD = switch (oPMMD.brazo()) {
                case REDONDEADO -> Math.min(armWPMMD / 2 - 0.5, 9);
                case DELGADO -> 3;
                default -> 4;
            };
            add(gPMMD, cajaPMMD(armWPMMD, alto, FPMMD, radioPMMD, tela), xPMMD, hPatasPMMD + alto / 2, 0);
            if (chesPMMD || oPMMD.brazo() == ConfigSofaPMMD.Brazo.REDONDEADO) {

                Cylinder rolloPMMD = new Cylinder(armWPMMD / 2 + (chesPMMD ? 3 : 1), FPMMD - 2, 28);
                rolloPMMD.setMaterial(tela);
                rolloPMMD.getTransforms().addAll(new javafx.scene.transform.Translate(xPMMD + ladoPMMD * (chesPMMD ? 2 : 0), -(altoBrazoPMMD - 1), 0),
                        new Rotate(90, Rotate.X_AXIS));
                gPMMD.getChildren().add(rolloPMMD);
            }
        }


        patas(gPMMD, oPMMD, LPMMD, FPMMD + (chaise ? 0 : 0), hPatasPMMD, chaise, chaiseDerechaPMMD, anchoCojinPMMD + armWPMMD, extraPMMD);


        if (oPMMD.cojines() > 0) {
            PhongMaterial mcPMMD = tela(oPMMD.colorCojines(), oPMMD.tipoCojines());
            int cPMMD = oPMMD.cojines();
            double tamPMMD = Math.min(44, anchoCojinPMMD * 0.75);
            for (int iPMMD = 0; iPMMD < cPMMD; iPMMD++) {
                int ladoPMMD = iPMMD % 2 == 0 ? -1 : 1;
                int filaPMMD = iPMMD / 2;
                double xPMMD = ladoPMMD * (interiorPMMD / 2 - tamPMMD / 2 - 4 - filaPMMD * (tamPMMD * 0.8));
                Node cjPMMD = add(gPMMD, cajaPMMD(tamPMMD, tamPMMD, 12, 5, mcPMMD), xPMMD, yAsientoPMMD + hCojinPMMD + tamPMMD / 2 - 2, FPMMD / 2 - fondoRespaldoPMMD - 16 - filaPMMD * 3);
                cjPMMD.getTransforms().addAll(new Rotate(-12, Rotate.X_AXIS), new Rotate(ladoPMMD * (6 + filaPMMD * 6), Rotate.Z_AXIS));
            }
        }
        return gPMMD;
    }

    private void puffPMMD(Group gPMMD, Opciones oPMMD) {
        double LPMMD = oPMMD.largo(), APMMD = oPMMD.alto();
        PhongMaterial tela = tela(oPMMD.colorTela(), oPMMD.tipoTela());
        add(gPMMD, cajaPMMD(LPMMD, APMMD - 6, LPMMD, 6, tela), 0, 6 + (APMMD - 6) / 2, 0);
        add(gPMMD, cajaPMMD(LPMMD - 6, 6, LPMMD - 6, 1, color("#1F2937", 10)), 0, 3, 0);
    }

    private void capitonePMMD(Group gPMMD, double anchoPMMD, double y0PMMD, double y1PMMD, double zPMMD, PhongMaterial basePMMD) {
        PhongMaterial botonPMMD = color(Color.web(basePMMD.getDiffuseColor().toString()).deriveColor(0, 1, 0.6, 1).toString(), 20);
        int colsPMMD = Math.max(4, (int) (anchoPMMD / 22));
        int filasPMMD = 3;
        for (int f = 0; f < filasPMMD; f++) {
            for (int cPMMD = 0; cPMMD <= colsPMMD; cPMMD++) {
                double xPMMD = -anchoPMMD / 2 + anchoPMMD * cPMMD / colsPMMD + (f % 2 == 0 ? 0 : anchoPMMD / colsPMMD / 2);
                if (xPMMD > anchoPMMD / 2) continue;
                double yPMMD = y0PMMD + 8 + (y1PMMD - y0PMMD - 16) * f / (filasPMMD - 1.0);
                Sphere sPMMD = new Sphere(1.4, 8);
                sPMMD.setMaterial(botonPMMD);
                sPMMD.setTranslateX(xPMMD);
                sPMMD.setTranslateY(-yPMMD);
                sPMMD.setTranslateZ(zPMMD - 0.4);
                gPMMD.getChildren().add(sPMMD);
            }
        }
    }

    private void patas(Group gPMMD, Opciones oPMMD, double LPMMD, double FPMMD, double h, boolean chaise, boolean derechaPMMD, double anchoChaisePMMD, double extraPMMD) {
        if (oPMMD.patas() == ConfigSofaPMMD.Patas.ZOCALO) {
            add(gPMMD, cajaPMMD(LPMMD - 8, h, FPMMD - 8, 1, color("#1F2937", 12)), 0, h / 2, 0);
            if (chaise) add(gPMMD, cajaPMMD(anchoChaisePMMD - 8, h, extraPMMD, 1, color("#1F2937", 12)), (derechaPMMD ? 1 : -1) * (LPMMD / 2 - anchoChaisePMMD / 2), h / 2, -FPMMD / 2 - extraPMMD / 2 + 4);
            return;
        }
        int cuantasPMMD = LPMMD <= 160 ? 4 : 6;
        double[] xsPMMD = cuantasPMMD == 4 ? new double[]{-LPMMD / 2 + 7, LPMMD / 2 - 7} : new double[]{-LPMMD / 2 + 7, 0, LPMMD / 2 - 7};
        double[] zsPMMD = {-FPMMD / 2 + 7, FPMMD / 2 - 7};
        PhongMaterial mPMMD = switch (oPMMD.patas()) {
            case METAL_NEGRO -> color("#1F1F22", 30);
            case METAL_DORADO -> color("#C8A24A", 60);
            default -> color(oPMMD.colorPatas() == null ? "#C69A6D" : oPMMD.colorPatas(), 14);
        };
        for (double xPMMD : xsPMMD) for (double zPMMD : zsPMMD) pataPMMD(gPMMD, oPMMD.patas(), mPMMD, xPMMD, zPMMD, h);
        if (chaise) {
            double xPMMD = (derechaPMMD ? 1 : -1) * (LPMMD / 2 - 7);
            pataPMMD(gPMMD, oPMMD.patas(), mPMMD, xPMMD, -FPMMD / 2 - extraPMMD + 7, h);
            pataPMMD(gPMMD, oPMMD.patas(), mPMMD, xPMMD - (derechaPMMD ? 1 : -1) * (anchoChaisePMMD - 14), -FPMMD / 2 - extraPMMD + 7, h);
        }
    }

    private void pataPMMD(Group gPMMD, ConfigSofaPMMD.Patas tipo, PhongMaterial mPMMD, double xPMMD, double zPMMD, double h) {
        Node n;
        if (tipo == ConfigSofaPMMD.Patas.MADERA) {
            MeshView mvPMMD = new MeshView(MallasPMMD.tronco(1.6, 2.6, h, 18));
            mvPMMD.setMaterial(mPMMD);
            mvPMMD.setCullFace(javafx.scene.shape.CullFace.NONE);
            n = mvPMMD;
        } else {
            Cylinder cPMMD = new Cylinder(1.1, h, 14);
            cPMMD.setMaterial(mPMMD);
            n = cPMMD;
        }
        n.setTranslateX(xPMMD);
        n.setTranslateY(-h / 2);
        n.setTranslateZ(zPMMD);
        gPMMD.getChildren().add(n);
    }

    private Node add(Group gPMMD, Node n, double xPMMD, double yCentroPMMD, double zPMMD) {
        n.setTranslateX(xPMMD);
        n.setTranslateY(-yCentroPMMD);
        n.setTranslateZ(zPMMD);
        gPMMD.getChildren().add(n);
        return n;
    }

    private MeshView cajaPMMD(double w, double h, double d, double rPMMD, PhongMaterial mPMMD) {
        MeshView mvPMMD = new MeshView(MallasPMMD.cajaRedondeada(w, h, d, rPMMD, 4));
        mvPMMD.setMaterial(mPMMD);
        mvPMMD.setCullFace(javafx.scene.shape.CullFace.NONE);
        return mvPMMD;
    }

    public PhongMaterial tela(String hexPMMD, TipoTelaPMMD tipo) {
        String kPMMD = "t" + hexPMMD + tipo;
        return materialesPMMD.computeIfAbsent(kPMMD, xPMMD -> {
            PhongMaterial mPMMD = new PhongMaterial(ColorSwatchColor.parse(hexPMMD));
            double brilloPMMD = tipo == null ? 8 : tipo.brillo();
            mPMMD.setSpecularPower(brilloPMMD);
            double scPMMD = tipo == TipoTelaPMMD.CUERO ? 0.38 : tipo == TipoTelaPMMD.TERCIOPELO ? 0.22 : 0.06;
            mPMMD.setSpecularColor(Color.gray(scPMMD));
            mPMMD.setDiffuseMap(TexturasPMMD.tejido(tipo));
            return mPMMD;
        });
    }

    public PhongMaterial color(String hexPMMD, double brilloPMMD) {
        return materialesPMMD.computeIfAbsent("c" + hexPMMD + brilloPMMD, xPMMD -> {
            PhongMaterial mPMMD = new PhongMaterial(ColorSwatchColor.parse(hexPMMD));
            mPMMD.setSpecularPower(brilloPMMD);
            mPMMD.setSpecularColor(Color.gray(brilloPMMD > 20 ? 0.5 : 0.15));
            return mPMMD;
        });
    }


    
    static final class ColorSwatchColor {
        
        static Color parse(String hexPMMD) {
            try {
                return Color.web(hexPMMD == null ? "#B9B7B1" : hexPMMD);
            } catch (IllegalArgumentException ePMMD) {
                return Color.web("#B9B7B1");
            }
        }
    }

    @SuppressWarnings("unused")
    private static Box guia(double w, double h, double d) {
        return new Box(w, h, d);
    }
}
