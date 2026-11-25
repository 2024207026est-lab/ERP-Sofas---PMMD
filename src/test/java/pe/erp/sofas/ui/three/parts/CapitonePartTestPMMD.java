package pe.erp.sofas.ui.three.parts;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.junit.jupiter.api.Test;
import pe.erp.sofas.ui.three.parts.concrete.CapitonePartPMMD;

import static org.junit.jupiter.api.Assertions.*;





class CapitonePartTestPMMD {

    private PhongMaterial materialTelaPMMD() {
        PhongMaterial mPMMD = new PhongMaterial(Color.web("#5B4135"));
        mPMMD.setSpecularPower(40);
        return mPMMD;
    }

    @Test
    void testCapitonePartCreationPMMD() {
        var capitonePMMD = new CapitonePartPMMD(156, 85, 20, materialTelaPMMD());
        assertNotNull(capitonePMMD);
        assertEquals("Capitoné", capitonePMMD.getName());
    }

    @Test
    void testCapitonePartDimensionsPMMD() {
        var capitonePMMD = new CapitonePartPMMD(156, 85, 20, materialTelaPMMD());
        var dimsPMMD = capitonePMMD.getDimensions();

        assertEquals(156, dimsPMMD.ancho());
        assertEquals(20, dimsPMMD.fondo());
        assertEquals(85, dimsPMMD.alto());
    }

    @Test
    void testCapitonePartBuildPMMD() {
        var capitonePMMD = new CapitonePartPMMD(156, 85, 20, materialTelaPMMD());
        Group geomPMMD = capitonePMMD.build();

        assertNotNull(geomPMMD);
        assertTrue(geomPMMD.getChildren().size() > 1, "Debe tener malla + botones");
    }

    @Test
    void testCapitonePartAnchorsPMMD() {
        var capitonePMMD = new CapitonePartPMMD(156, 85, 20, materialTelaPMMD());
        var anchorsPMMD = capitonePMMD.getAnchors();

        assertNotNull(anchorsPMMD);
        assertTrue(anchorsPMMD.containsKey("left"));
        assertTrue(anchorsPMMD.containsKey("right"));
    }

    @Test
    void testCapitonePartChesterfieldPMMD() {

        var material = new PhongMaterial(Color.web("#8B4513"));
        var capitonePMMD = new CapitonePartPMMD(156, 85, 20, material);

        Group geomPMMD = capitonePMMD.build();
        assertNotNull(geomPMMD);


        int numNodosPMMD = geomPMMD.getChildren().size();
        assertTrue(numNodosPMMD > 10, "Chesterfield debe tener muchos botones");
    }

    @Test
    void testCapitonePartNullMaterialPMMD() {

        var capitonePMMD = new CapitonePartPMMD(100, 70, 15, null);
        Group geomPMMD = capitonePMMD.build();
        assertNotNull(geomPMMD);
    }
}
