package pe.erp.sofas.ui.three.parts.families;

import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.ui.three.parts.SofaPartPMMD;
import pe.erp.sofas.ui.three.parts.concrete.*;
import java.util.ArrayList;
import java.util.List;






public class SeccionalUFamilyPMMD implements FamiliaFactoryPMMD.FamiliaConstructor {

    @Override
    public List<SofaPartPMMD> construir(ConfigSofaPMMD configPMMD, PhongMaterial materialTela, PhongMaterial materialRespaldoPMMD) {
        List<SofaPartPMMD> partesPMMD = new ArrayList<>();
        PhongMaterial matRespPMMD = materialRespaldoPMMD == null ? materialTela : materialRespaldoPMMD;


        partesPMMD.add(new BaseSeatFramePartPMMD(400, 92, 13, materialTela));
        return partesPMMD;
    }
}
