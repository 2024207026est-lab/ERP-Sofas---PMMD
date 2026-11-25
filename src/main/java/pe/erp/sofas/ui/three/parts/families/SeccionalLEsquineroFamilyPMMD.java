package pe.erp.sofas.ui.three.parts.families;

import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.ui.three.parts.SofaPartPMMD;
import pe.erp.sofas.ui.three.parts.concrete.*;

import java.util.ArrayList;
import java.util.List;







public class SeccionalLEsquineroFamilyPMMD implements FamiliaFactoryPMMD.FamiliaConstructor {

    private static final double ANCHO_CUERPOPMMD = 52;

    private static final double TAMANIO_ESQUINEROPMMD = 95;

    private static final double FONDO_ESTANDARPMMD = 92;

    private static final double ALTO_ESTANDARPMMD = 85;

    @Override
    public List<SofaPartPMMD> construir(ConfigSofaPMMD configPMMD, PhongMaterial materialTela, PhongMaterial materialRespaldoPMMD) {
        List<SofaPartPMMD> partesPMMD = new ArrayList<>();

        int cuerposAPMMD = 3;
        int cuerposBPMMD = 2;
        double largoAPMMD = cuerposAPMMD * ANCHO_CUERPOPMMD;
        double largoBPMMD = cuerposBPMMD * ANCHO_CUERPOPMMD;
        double largoTotal = largoAPMMD + TAMANIO_ESQUINEROPMMD + largoBPMMD;
        double fondo = configPMMD.profundidad();
        double altoPMMD = ALTO_ESTANDARPMMD;

        PhongMaterial matResp = materialRespaldoPMMD == null ? materialTela : materialRespaldoPMMD;


        partesPMMD.add(new BaseSeatFramePartPMMD(largoTotal, fondo, 13, materialTela));


        for (int iPMMD = 0; iPMMD < cuerposAPMMD; iPMMD++) {
            partesPMMD.add(new SeatCushionPartPMMD(ANCHO_CUERPOPMMD - 1, fondo, materialTela));
        }


        partesPMMD.add(new CornerPartPMMD(TAMANIO_ESQUINEROPMMD, materialTela, matResp));


        for (int iPMMD = 0; iPMMD < cuerposBPMMD; iPMMD++) {
            partesPMMD.add(new SeatCushionPartPMMD(ANCHO_CUERPOPMMD - 1, fondo, materialTela));
        }


        partesPMMD.add(new BackrestPartPMMD(largoAPMMD - 34, altoPMMD - 18 - 3, matResp));


        partesPMMD.add(new ArmPartPMMD(ArmPartPMMD.TipoBrazo.RECTO, altoPMMD * 0.7, fondo, materialTela));


        partesPMMD.add(new ArmPartPMMD(ArmPartPMMD.TipoBrazo.RECTO, altoPMMD * 0.7, fondo, materialTela));


        partesPMMD.add(new LegsPartPMMD(largoTotal, fondo, LegsPartPMMD.TipoPata.MADERA_CONICA, materialTela));

        return partesPMMD;
    }
}
