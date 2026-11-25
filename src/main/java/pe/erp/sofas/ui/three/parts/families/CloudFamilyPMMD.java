package pe.erp.sofas.ui.three.parts.families;

import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.ui.three.parts.SofaPartPMMD;
import pe.erp.sofas.ui.three.parts.concrete.*;
import java.util.ArrayList;
import java.util.List;

public class CloudFamilyPMMD implements FamiliaFactoryPMMD.FamiliaConstructor {

    @Override
    public List<SofaPartPMMD> construir(ConfigSofaPMMD configPMMD, PhongMaterial materialTela, PhongMaterial materialRespaldoPMMD) {
        List<SofaPartPMMD> partesPMMD = new ArrayList<>();
        int cuerposPMMD = configPMMD.cuerpos();
        double largo = cuerposPMMD * 52;
        partesPMMD.add(new BaseSeatFramePartPMMD(largo, 92, 13, materialTela));
        return partesPMMD;
    }
}
