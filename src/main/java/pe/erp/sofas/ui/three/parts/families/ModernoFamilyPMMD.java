package pe.erp.sofas.ui.three.parts.families;

import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.ui.three.parts.SofaPartPMMD;
import pe.erp.sofas.ui.three.parts.concrete.*;

import java.util.ArrayList;
import java.util.List;













public class ModernoFamilyPMMD implements FamiliaFactoryPMMD.FamiliaConstructor {

    private static final double ANCHO_CUERPOPMMD = 52;

    private static final double FONDO_ESTANDARPMMD = 92;

    private static final double ALTO_ESTANDARPMMD = 85;

    @Override
    public List<SofaPartPMMD> construir(ConfigSofaPMMD configPMMD, PhongMaterial materialTela, PhongMaterial materialRespaldoPMMD) {
        List<SofaPartPMMD> partesPMMD = new ArrayList<>();

        int cuerposPMMD = configPMMD.cuerpos();
        double fondo = configPMMD.profundidad();
        double altoPMMD = ALTO_ESTANDARPMMD;


        PhongMaterial matRespPMMD = materialRespaldoPMMD == null ? materialTela : materialRespaldoPMMD;






        partesPMMD.add(new ArmPartPMMD(ArmPartPMMD.TipoBrazo.RECTO, altoPMMD * 0.7, fondo, materialTela));


        for (int iPMMD = 0; iPMMD < cuerposPMMD; iPMMD++) {
            partesPMMD.add(new SeatCushionPartPMMD(ANCHO_CUERPOPMMD - 1, fondo, materialTela));
        }


        partesPMMD.add(new ArmPartPMMD(ArmPartPMMD.TipoBrazo.RECTO, altoPMMD * 0.7, fondo, materialTela));

        return partesPMMD;
    }





    public static double largoParaCuerpos(int cuerposPMMD) {
        return cuerposPMMD * ANCHO_CUERPOPMMD;
    }
}
