package pe.erp.sofas.ui.three.parts.families;

import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.ui.three.parts.SofaPartPMMD;

import java.util.HashMap;
import java.util.List;
import java.util.Map;








public class FamiliaFactoryPMMD {

    private final Map<SofaFamiliaPMMD, FamiliaConstructor> constructoresPMMD = new HashMap<>();

    public FamiliaFactoryPMMD() {

        constructoresPMMD.put(SofaFamiliaPMMD.MODERNO, new ModernoFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.SECCIONAL_L_CHAISE, new SeccionalLChaiseFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.SECCIONAL_L_ESQUINERO, new SeccionalLEsquineroFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.SECCIONAL_U, new SeccionalUFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.MODULAR, new ModularFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.CHESTERFIELD, new ChesterfieldFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.ESCANDINAVO, new EscandinavoFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.SOFA_CAMA, new SofaCamaFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.RECLINABLE, new ReclinableFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.RETRO, new RetroFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.CLOUD, new CloudFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.TUXEDO, new TuxedoFamilyPMMD());
        constructoresPMMD.put(SofaFamiliaPMMD.SILLON, new SillonFamilyPMMD());
    }











    public List<SofaPartPMMD> construir(SofaFamiliaPMMD familiaPMMD, ConfigSofaPMMD configPMMD,
                                   PhongMaterial materialTelaPMMD, PhongMaterial materialRespaldoPMMD) {
        FamiliaConstructor constructorPMMD = constructoresPMMD.get(familiaPMMD);
        if (constructorPMMD == null) {
            throw new IllegalArgumentException("Familia no registrada: " + familiaPMMD);
        }
        return constructorPMMD.construir(configPMMD, materialTelaPMMD, materialRespaldoPMMD);
    }





    public interface FamiliaConstructor {



        
        List<SofaPartPMMD> construir(ConfigSofaPMMD configPMMD, PhongMaterial materialTelaPMMD, PhongMaterial materialRespaldoPMMD);
    }
}
