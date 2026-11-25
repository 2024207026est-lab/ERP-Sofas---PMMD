package pe.erp.sofas.ui.three.parts.families;

import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.ui.three.parts.SofaPartPMMD;
import pe.erp.sofas.ui.three.parts.concrete.*;

import java.util.ArrayList;
import java.util.List;







public class SeccionalLChaiseFamilyPMMD implements FamiliaFactoryPMMD.FamiliaConstructor {

    private static final double ANCHO_CUERPOPMMD = 52;

    private static final double LARGO_CHAISEPMMD = 65;

    private static final double FONDO_ESTANDARPMMD = 92;

    private static final double ALTO_ESTANDARPMMD = 85;

    @Override
    public List<SofaPartPMMD> construir(ConfigSofaPMMD configPMMD, PhongMaterial materialTela, PhongMaterial materialRespaldoPMMD) {
        List<SofaPartPMMD> partesPMMD = new ArrayList<>();

        int cuerposPMMD = configPMMD.cuerpos();
        double largoSofaPMMD = cuerposPMMD * ANCHO_CUERPOPMMD;
        double largoTotal = largoSofaPMMD + LARGO_CHAISEPMMD;
        double fondo = configPMMD.profundidad();
        double altoPMMD = ALTO_ESTANDARPMMD;
        boolean chaiseDerechaPMMD = configPMMD.chaise() == ConfigSofaPMMD.Chaise.DERECHA;

        PhongMaterial matResp = materialRespaldoPMMD == null ? materialTela : materialRespaldoPMMD;


        partesPMMD.add(new BaseSeatFramePartPMMD(largoTotal, fondo, 13, materialTela));


        for (int iPMMD = 0; iPMMD < cuerposPMMD; iPMMD++) {
            partesPMMD.add(new SeatCushionPartPMMD(ANCHO_CUERPOPMMD - 1, fondo, materialTela));
        }


        partesPMMD.add(new BackrestPartPMMD(largoSofaPMMD - 34, altoPMMD - 18 - 3, matResp));



        if (chaiseDerechaPMMD) {

            partesPMMD.add(new ArmPartPMMD(ArmPartPMMD.TipoBrazo.RECTO, altoPMMD * 0.7, fondo, materialTela));
        } else {

            partesPMMD.add(new ArmPartPMMD(ArmPartPMMD.TipoBrazo.RECTO, altoPMMD * 0.7, fondo, materialTela));
        }


        partesPMMD.add(new ChaseLoungeModulePMMD(LARGO_CHAISEPMMD, materialTela));


        partesPMMD.add(new LegsPartPMMD(largoTotal, fondo, LegsPartPMMD.TipoPata.MADERA_CONICA, materialTela));


        if (configPMMD.cojines() > 0) {
            double tamCojin = Math.min(44, ANCHO_CUERPOPMMD * 0.75);
            for (int iPMMD = 0; iPMMD < configPMMD.cojines(); iPMMD++) {
                partesPMMD.add(new DecorativeCushionPartPMMD(tamCojin, materialTela));
            }
        }

        return partesPMMD;
    }
}
