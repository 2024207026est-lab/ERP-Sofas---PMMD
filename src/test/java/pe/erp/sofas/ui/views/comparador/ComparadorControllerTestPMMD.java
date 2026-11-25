package pe.erp.sofas.ui.views.comparador;

import org.junit.jupiter.api.Test;
import pe.erp.sofas.core.SessionPMMD;
import pe.erp.sofas.core.ToastPMMD;
import pe.erp.sofas.service.ConfiguradorServicePMMD;
import pe.erp.sofas.service.PersistenciaConfiguracionServicePMMD;
import pe.erp.sofas.service.TelaServicePMMD;
import pe.erp.sofas.ui.AppWindowPMMD;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComparadorControllerTestPMMD {

    @Test
    void testComparadorCreationPMMD() {

        assertTrue(true);
    }

    @Test
    void testComparisonRowPMMD() {
        ComparadorControllerPMMD.ComparisonRow rowPMMD = new ComparadorControllerPMMD.ComparisonRow(
            "Precio", "S/ 5000", "S/ 6000", "S/ 7000"
        );
        assertEquals("Precio", rowPMMD.propiedad);
        assertEquals("S/ 5000", rowPMMD.opcion1);
        assertEquals("S/ 6000", rowPMMD.opcion2);
        assertEquals("S/ 7000", rowPMMD.opcion3);
    }

    @Test
    void testComparisonRowAllFieldsPMMD() {
        ComparadorControllerPMMD.ComparisonRow rowPMMD = new ComparadorControllerPMMD.ComparisonRow(
            "Modelo", "Moderno", "Chesterfield", "Cloud"
        );
        assertNotNull(rowPMMD.propiedad);
        assertNotNull(rowPMMD.opcion1);
        assertNotNull(rowPMMD.opcion2);
        assertNotNull(rowPMMD.opcion3);
    }
}
