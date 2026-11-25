package pe.erp.sofas.ui.three.parts;

import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.junit.jupiter.api.Test;
import pe.erp.sofas.ui.three.parts.concrete.*;

import static org.junit.jupiter.api.Assertions.*;









class SofaPartAssemblerTestPMMD {

    private PhongMaterial materialTelaPMMD() {
        PhongMaterial mPMMD = new PhongMaterial(Color.web("#B9B7B1"));
        mPMMD.setSpecularPower(8);
        return mPMMD;
    }

    private PhongMaterial materialMetalPMMD() {
        PhongMaterial mPMMD = new PhongMaterial(Color.web("#1F1F22"));
        mPMMD.setSpecularPower(30);
        return mPMMD;
    }

    @Test
    void testParteBaseSeatFramePMMD() {
        var basePMMD = new BaseSeatFramePartPMMD(210, 92, 13, materialTelaPMMD());
        var geomPMMD = basePMMD.build();
        var dimsPMMD = basePMMD.getDimensions();

        assertNotNull(geomPMMD);
        assertEquals(210, dimsPMMD.ancho());
        assertEquals(92, dimsPMMD.fondo());
        assertEquals(18, dimsPMMD.alto());
    }

    @Test
    void testParteSeatCushionPMMD() {
        var asientoPMMD = new SeatCushionPartPMMD(52, 92, materialTelaPMMD());
        var geomPMMD = asientoPMMD.build();
        var dimsPMMD = asientoPMMD.getDimensions();

        assertNotNull(geomPMMD);
        assertEquals(52, dimsPMMD.ancho());
        assertEquals(92, dimsPMMD.fondo());
        assertEquals(14, dimsPMMD.alto());
    }

    @Test
    void testParteBackrestPMMD() {
        var respaldoPMMD = new BackrestPartPMMD(52, 85, materialTelaPMMD());
        var geomPMMD = respaldoPMMD.build();
        var dimsPMMD = respaldoPMMD.getDimensions();

        assertNotNull(geomPMMD);
        assertEquals(52, dimsPMMD.ancho());
        assertEquals(20, dimsPMMD.fondo());
        assertEquals(85, dimsPMMD.alto());
    }

    @Test
    void testParteArmRectoPMMD() {
        var brazoPMMD = new ArmPartPMMD(ArmPartPMMD.TipoBrazo.RECTO, 60, 92, materialTelaPMMD());
        var geomPMMD = brazoPMMD.build();
        var dimsPMMD = brazoPMMD.getDimensions();

        assertNotNull(geomPMMD);
        assertEquals(17, dimsPMMD.ancho());
        assertEquals(92, dimsPMMD.fondo());
        assertEquals(60, dimsPMMD.alto());
    }

    @Test
    void testParteCornerPMMD() {
        var esquinaPMMD = new CornerPartPMMD(95, materialTelaPMMD(), materialTelaPMMD());
        var geomPMMD = esquinaPMMD.build();
        var dimsPMMD = esquinaPMMD.getDimensions();

        assertNotNull(geomPMMD);
        assertEquals(95, dimsPMMD.ancho());
        assertEquals(95, dimsPMMD.fondo());
        assertEquals(85, dimsPMMD.alto());
    }

    @Test
    void testParteLegsMaderaPMMD() {
        var patasPMMD = new LegsPartPMMD(210, 92, LegsPartPMMD.TipoPata.MADERA_CONICA, materialTelaPMMD());
        var geomPMMD = patasPMMD.build();
        var dimsPMMD = patasPMMD.getDimensions();

        assertNotNull(geomPMMD);
        assertEquals(210, dimsPMMD.ancho());
        assertEquals(92, dimsPMMD.fondo());
        assertEquals(13, dimsPMMD.alto());
    }

    @Test
    void testParteChaiseLoungeModulePMMD() {
        var chaisePMMD = new ChaseLoungeModulePMMD(65, materialTelaPMMD());
        var geomPMMD = chaisePMMD.build();
        var dimsPMMD = chaisePMMD.getDimensions();

        assertNotNull(geomPMMD);
        assertEquals(65, dimsPMMD.ancho());
        assertEquals(92, dimsPMMD.fondo());
    }

    @Test
    void testParteOttomanPMMD() {
        var puffPMMD = new OttomanPartPMMD(60, 60, materialTelaPMMD());
        var geomPMMD = puffPMMD.build();
        var dimsPMMD = puffPMMD.getDimensions();

        assertNotNull(geomPMMD);
        assertEquals(60, dimsPMMD.ancho());
        assertEquals(60, dimsPMMD.fondo());
        assertEquals(40, dimsPMMD.alto());
    }

    @Test
    void testParteDecorativeCushionPMMD() {
        var cojinPMMD = new DecorativeCushionPartPMMD(44, materialTelaPMMD());
        var geomPMMD = cojinPMMD.build();
        var dimsPMMD = cojinPMMD.getDimensions();

        assertNotNull(geomPMMD);
        assertEquals(44, dimsPMMD.ancho());
        assertEquals(44, dimsPMMD.fondo());
        assertEquals(12, dimsPMMD.alto());
    }

    @Test
    void testEnsambladorLineal3CuerposPMMD() {
        var material = materialTelaPMMD();
        var asiento1PMMD = new SeatCushionPartPMMD(52, 92, material);
        var asiento2PMMD = new SeatCushionPartPMMD(52, 92, material);
        var asiento3PMMD = new SeatCushionPartPMMD(52, 92, material);

        var ensambladorPMMD = new SofaPartAssemblerPMMD()
            .conLayout(PartLayoutPMMD.LINEAL)
            .agregar(asiento1PMMD)
            .agregar(asiento2PMMD)
            .agregar(asiento3PMMD);

        var resultadoPMMD = ensambladorPMMD.ensamblar();
        assertNotNull(resultadoPMMD.grupo());
        assertEquals(156, resultadoPMMD.largoTotal());
        assertEquals(92, resultadoPMMD.fondoTotal());
    }

    @Test
    void testEnsambladorLEsquineroPMMD() {
        var material = materialTelaPMMD();
        var basePMMD = new BaseSeatFramePartPMMD(343, 92, 13, material);
        var asientos1PMMD = new SeatCushionPartPMMD(52, 92, material);
        var asientos2PMMD = new SeatCushionPartPMMD(52, 92, material);
        var asientos3PMMD = new SeatCushionPartPMMD(52, 92, material);
        var esquinaPMMD = new CornerPartPMMD(95, material, material);
        var asientos4PMMD = new SeatCushionPartPMMD(52, 92, material);
        var asientos5PMMD = new SeatCushionPartPMMD(52, 92, material);

        var ensambladorPMMD = new SofaPartAssemblerPMMD()
            .conLayout(PartLayoutPMMD.L_ESQUINERO_DER)
            .agregar(basePMMD)
            .agregar(asientos1PMMD)
            .agregar(asientos2PMMD)
            .agregar(asientos3PMMD)
            .agregar(esquinaPMMD)
            .agregar(asientos4PMMD)
            .agregar(asientos5PMMD);

        var resultadoPMMD = ensambladorPMMD.ensamblar();
        assertNotNull(resultadoPMMD.grupo());
        assertTrue(resultadoPMMD.largoTotal() > 300);
    }
}
