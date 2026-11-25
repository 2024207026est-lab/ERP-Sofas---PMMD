package pe.erp.sofas.ui.three.parts.families;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.junit.jupiter.api.Test;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.ui.three.parts.PartLayoutPMMD;
import pe.erp.sofas.ui.three.parts.SofaPartAssemblerPMMD;

import static org.junit.jupiter.api.Assertions.*;






class ModernoFamilyTestPMMD {

    private PhongMaterial materialTelaPMMD() {
        PhongMaterial mPMMD = new PhongMaterial(Color.web("#B9B7B1"));
        mPMMD.setSpecularPower(8);
        return mPMMD;
    }

    @Test
    void testModerno1CuerpoPMMD() {
        var factoryPMMD = new FamiliaFactoryPMMD();
        var configPMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO, 1, ConfigSofaPMMD.Chaise.NINGUNA, 92, null,
            ConfigSofaPMMD.Brazo.RECTO, ConfigSofaPMMD.Patas.MADERA, ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D23, 0, null, ConfigSofaPMMD.Mecanismo.FIJO
        );

        var partesPMMD = factoryPMMD.construir(SofaFamiliaPMMD.MODERNO, configPMMD, materialTelaPMMD(), null);


        assertEquals(3, partesPMMD.size(), "Debe tener exactamente 3 partes");

        var ensambladorPMMD = new SofaPartAssemblerPMMD()
            .conLayout(PartLayoutPMMD.LINEAL)
            .agregarTodas(partesPMMD);

        var resultadoPMMD = ensambladorPMMD.ensamblar();
        assertEquals(85, resultadoPMMD.largoTotal());
        assertEquals(92, resultadoPMMD.fondoTotal());
    }

    @Test
    void testModerno3CuerposPMMD() {
        var factoryPMMD = new FamiliaFactoryPMMD();
        var configPMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO, 3, ConfigSofaPMMD.Chaise.NINGUNA, 92, null,
            ConfigSofaPMMD.Brazo.RECTO, ConfigSofaPMMD.Patas.MADERA, ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D23, 0, null, ConfigSofaPMMD.Mecanismo.FIJO
        );

        var partesPMMD = factoryPMMD.construir(SofaFamiliaPMMD.MODERNO, configPMMD, materialTelaPMMD(), null);


        assertEquals(5, partesPMMD.size(), "Debe tener exactamente 5 partes");

        var ensambladorPMMD = new SofaPartAssemblerPMMD()
            .conLayout(PartLayoutPMMD.LINEAL)
            .agregarTodas(partesPMMD);

        var resultadoPMMD = ensambladorPMMD.ensamblar();
        assertEquals(187, resultadoPMMD.largoTotal());
        assertEquals(92, resultadoPMMD.fondoTotal());
        assertEquals(59.5, resultadoPMMD.altoTotal(), 0.1);
    }

    @Test
    void testModerno4CuerposPMMD() {
        var factoryPMMD = new FamiliaFactoryPMMD();
        var configPMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO, 4, ConfigSofaPMMD.Chaise.NINGUNA, 92, null,
            ConfigSofaPMMD.Brazo.RECTO, ConfigSofaPMMD.Patas.MADERA, ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D23, 0, null, ConfigSofaPMMD.Mecanismo.FIJO
        );

        var partesPMMD = factoryPMMD.construir(SofaFamiliaPMMD.MODERNO, configPMMD, materialTelaPMMD(), null);


        var ensambladorPMMD = new SofaPartAssemblerPMMD()
            .conLayout(PartLayoutPMMD.LINEAL)
            .agregarTodas(partesPMMD);

        var resultadoPMMD = ensambladorPMMD.ensamblar();
        assertEquals(238, resultadoPMMD.largoTotal());
        assertEquals(92, resultadoPMMD.fondoTotal());
    }

    @Test
    void testFamiliaFactoryRegistradoModernoPMMD() {
        var factoryPMMD = new FamiliaFactoryPMMD();
        var configPMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO, 3, ConfigSofaPMMD.Chaise.NINGUNA, 92, null,
            ConfigSofaPMMD.Brazo.RECTO, ConfigSofaPMMD.Patas.MADERA, ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D23, 0, null, ConfigSofaPMMD.Mecanismo.FIJO
        );


        var partesPMMD = factoryPMMD.construir(SofaFamiliaPMMD.MODERNO, configPMMD, materialTelaPMMD(), null);
        assertNotNull(partesPMMD);
        assertTrue(partesPMMD.size() > 0, "Debe retornar al menos 1 parte");
    }

    @Test
    void testSeccionalLEsquineroPMMD() {
        var factoryPMMD = new FamiliaFactoryPMMD();
        var configPMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO, 3, ConfigSofaPMMD.Chaise.NINGUNA, 92, null,
            ConfigSofaPMMD.Brazo.RECTO, ConfigSofaPMMD.Patas.MADERA, ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D23, 0, null, ConfigSofaPMMD.Mecanismo.FIJO
        );


        var partesPMMD = factoryPMMD.construir(SofaFamiliaPMMD.SECCIONAL_L_ESQUINERO, configPMMD, materialTelaPMMD(), null);
        assertNotNull(partesPMMD);
        assertTrue(partesPMMD.size() > 5, "L esquinero debe tener varios módulos");
    }

    @Test
    void testTodas13FamiliasCompilanPMMD() {
        var factoryPMMD = new FamiliaFactoryPMMD();
        var configPMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO, 2, ConfigSofaPMMD.Chaise.NINGUNA, 92, null,
            ConfigSofaPMMD.Brazo.RECTO, ConfigSofaPMMD.Patas.MADERA, ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D23, 0, null, ConfigSofaPMMD.Mecanismo.FIJO
        );

        for (SofaFamiliaPMMD familiaPMMD : SofaFamiliaPMMD.values()) {
            var partesPMMD = factoryPMMD.construir(familiaPMMD, configPMMD, materialTelaPMMD(), null);
            assertNotNull(partesPMMD, "Familia " + familiaPMMD + " debe compilar");
            assertTrue(partesPMMD.size() > 0, "Familia " + familiaPMMD + " debe retornar partes");
        }
    }
}
