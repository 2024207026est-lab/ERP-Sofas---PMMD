package pe.erp.sofas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.repository.FichaTecnicaRepositoryPMMD;
import pe.erp.sofas.repository.TelaRepositoryPMMD;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CalculoPrecioServiceTestPMMD {

    private CalculoPrecioServicePMMD servicePMMD;

    @Mock
    private FichaTecnicaRepositoryPMMD fichaTecnicaRepoPMMD;

    @Mock
    private TelaRepositoryPMMD telaRepoPMMD;

    private ConfigSofaPMMD configTestPMMD;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servicePMMD = new CalculoPrecioServicePMMD(fichaTecnicaRepoPMMD, telaRepoPMMD);

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
    }

    @Test
    void testCalcularPrecioSinFichaPMMD() {
        when(fichaTecnicaRepoPMMD.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        BigDecimal precioPMMD = servicePMMD.calcularPrecio(configTestPMMD);

        assertNotNull(precioPMMD);
        assertTrue(precioPMMD.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalcularPrecioConDescuentoPMMD() {
        when(fichaTecnicaRepoPMMD.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        BigDecimal precioSinDescuentoPMMD = servicePMMD.calcularPrecio(configTestPMMD, BigDecimal.ZERO);
        BigDecimal precioConDescuento10PMMD = servicePMMD.calcularPrecio(configTestPMMD, BigDecimal.TEN);

        assertTrue(precioSinDescuentoPMMD.compareTo(precioConDescuento10PMMD) > 0);
    }

    @Test
    void testCalcularPrecioConEspumaD30PMMD() {
        when(fichaTecnicaRepoPMMD.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        ConfigSofaPMMD conD30PMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO, 3, ConfigSofaPMMD.Chaise.NINGUNA, 92, null,
            ConfigSofaPMMD.Brazo.RECTO, ConfigSofaPMMD.Patas.MADERA, ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D30, 1, null, ConfigSofaPMMD.Mecanismo.FIJO
        );

        BigDecimal precioD30PMMD = servicePMMD.calcularPrecio(conD30PMMD);

        assertTrue(precioD30PMMD.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalcularPrecioConReclinableManualPMMD() {
        when(fichaTecnicaRepoPMMD.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        ConfigSofaPMMD conReclinablePMMD = new ConfigSofaPMMD(
            ConfigSofaPMMD.Modelo.MODERNO, 3, ConfigSofaPMMD.Chaise.NINGUNA, 92, null,
            ConfigSofaPMMD.Brazo.RECTO, ConfigSofaPMMD.Patas.MADERA, ConfigSofaPMMD.AcabadoMadera.NATURAL,
            ConfigSofaPMMD.Espuma.D23, 1, null, ConfigSofaPMMD.Mecanismo.RECLINABLE_MANUAL
        );

        BigDecimal precioReclinablePMMD = servicePMMD.calcularPrecio(conReclinablePMMD);

        assertTrue(precioReclinablePMMD.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testDesglosePositivoPMMD() {
        when(fichaTecnicaRepoPMMD.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        CalculoPrecioServicePMMD.DesglosePrecio desglosePMMD = servicePMMD.desglosar(configTestPMMD, BigDecimal.TEN);

        assertNotNull(desglosePMMD);
        assertTrue(desglosePMMD.costoBOM().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(desglosePMMD.precioFinal().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testPrecioEsPositivoPMMD() {
        when(fichaTecnicaRepoPMMD.findByCodigoOrderByVersionDesc(anyString())).thenReturn(List.of());

        BigDecimal precioPMMD = servicePMMD.calcularPrecio(configTestPMMD);

        assertTrue(precioPMMD.compareTo(BigDecimal.ZERO) > 0);
    }
}
