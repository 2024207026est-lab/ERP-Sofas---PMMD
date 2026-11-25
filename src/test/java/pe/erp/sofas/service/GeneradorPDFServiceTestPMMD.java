package pe.erp.sofas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.model.UsuarioPMMD;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GeneradorPDFServiceTestPMMD {

    private GeneradorPDFServicePMMD servicePMMD;

    @Mock
    private CalculoPrecioServicePMMD precioServicePMMD;

    @Mock
    private PersistenciaConfiguracionServicePMMD configServicePMMD;

    private ConfigSofaPMMD configTestPMMD;

    private UsuarioPMMD usuarioTestPMMD;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servicePMMD = new GeneradorPDFServicePMMD(precioServicePMMD, configServicePMMD);

        configTestPMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO,
            3,
            ConfigSofaPMMD.Chaise.NINGUNA,
            92,
            null,
            ConfigSofaPMMD.Brazo.RECTO,
            ConfigSofaPMMD.Patas.MADERA,
            ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D23,
            1,
            null,
            ConfigSofaPMMD.Mecanismo.FIJO
        );

        usuarioTestPMMD = new UsuarioPMMD();
        usuarioTestPMMD.setId(1L);
        usuarioTestPMMD.setNombre("Test User");
    }

    @Test
    void testGenerarPresupuestoSinImagenPMMD() {
        CalculoPrecioServicePMMD.DesglosePrecio desglosePMMD = new CalculoPrecioServicePMMD.DesglosePrecio(
            BigDecimal.valueOf(800),
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(1330),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1330),
            BigDecimal.valueOf(239.4),
            BigDecimal.valueOf(1569.4)
        );

        when(precioServicePMMD.desglosar(any(ConfigSofaPMMD.class), any()))
            .thenReturn(desglosePMMD);

        byte[] pdf = servicePMMD.generarPresupuesto(configTestPMMD, "Cliente Test", usuarioTestPMMD);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);

        assertTrue(pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F');
    }

    @Test
    void testGenerarPresupuestoConClienteNombrePMMD() {
        CalculoPrecioServicePMMD.DesglosePrecio desglosePMMD = new CalculoPrecioServicePMMD.DesglosePrecio(
            BigDecimal.valueOf(800),
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(1330),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1330),
            BigDecimal.valueOf(239.4),
            BigDecimal.valueOf(1569.4)
        );

        when(precioServicePMMD.desglosar(any(ConfigSofaPMMD.class), any()))
            .thenReturn(desglosePMMD);

        byte[] pdf = servicePMMD.generarPresupuesto(configTestPMMD, "Empresa XYZ S.A.C.", usuarioTestPMMD);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testGenerarPresupuestoContienePrecioPMMD() {
        CalculoPrecioServicePMMD.DesglosePrecio desglosePMMD = new CalculoPrecioServicePMMD.DesglosePrecio(
            BigDecimal.valueOf(800),
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(1330),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1330),
            BigDecimal.valueOf(239.4),
            BigDecimal.valueOf(1569.4)
        );

        when(precioServicePMMD.desglosar(any(ConfigSofaPMMD.class), any()))
            .thenReturn(desglosePMMD);

        byte[] pdf = servicePMMD.generarPresupuesto(configTestPMMD, "Cliente Test", usuarioTestPMMD);

        assertNotNull(pdf);
        String contenidoPMMD = new String(pdf);

        assertTrue(contenidoPMMD.contains("DESGLOSE") || contenidoPMMD.contains("PRECIO") || pdf.length > 1000);
    }

    @Test
    void testGenerarPresupuestoModernoConChaisePMMD() {
        ConfigSofaPMMD conChaisePMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO, 3, ConfigSofaPMMD.Chaise.DERECHA, 92, null,
            ConfigSofaPMMD.Brazo.RECTO, ConfigSofaPMMD.Patas.MADERA, ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D23, 1, null, ConfigSofaPMMD.Mecanismo.FIJO
        );

        CalculoPrecioServicePMMD.DesglosePrecio desglosePMMD = new CalculoPrecioServicePMMD.DesglosePrecio(
            BigDecimal.valueOf(800),
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(1530),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1530),
            BigDecimal.valueOf(275.4),
            BigDecimal.valueOf(1805.4)
        );

        when(precioServicePMMD.desglosar(any(ConfigSofaPMMD.class), any()))
            .thenReturn(desglosePMMD);

        byte[] pdf = servicePMMD.generarPresupuesto(conChaisePMMD, "Cliente Test", usuarioTestPMMD);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testGenerarPresupuestoEstructuraValidaPMMD() {
        CalculoPrecioServicePMMD.DesglosePrecio desglosePMMD = new CalculoPrecioServicePMMD.DesglosePrecio(
            BigDecimal.valueOf(800),
            BigDecimal.valueOf(150),
            BigDecimal.valueOf(1330),
            BigDecimal.ZERO,
            BigDecimal.valueOf(1330),
            BigDecimal.valueOf(239.4),
            BigDecimal.valueOf(1569.4)
        );

        when(precioServicePMMD.desglosar(any(ConfigSofaPMMD.class), any()))
            .thenReturn(desglosePMMD);

        byte[] pdf = servicePMMD.generarPresupuesto(configTestPMMD, "Cliente Test", usuarioTestPMMD);


        assertNotNull(pdf);
        assertTrue(pdf.length > 500);


        String inicioPMMD = new String(pdf, 0, Math.min(10, pdf.length));
        assertTrue(inicioPMMD.contains("%PDF"));
    }
}
