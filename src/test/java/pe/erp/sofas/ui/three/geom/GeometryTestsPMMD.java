package pe.erp.sofas.ui.three.geom;

import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;







class GeometryTestsPMMD {

    @Test
    void testPillowMeshCreationPMMD() {
        TriangleMesh mallaPMMD = PillowMeshPMMD.crear(52, 14, 92, 4, 0.8, 6);
        assertNotNull(mallaPMMD);
        assertTrue(mallaPMMD.getPoints().size() > 0);
        assertTrue(mallaPMMD.getNormals().size() > 0);
        assertTrue(mallaPMMD.getFaces().size() > 0);
    }

    @Test
    void testPillowMeshTriangleCountPMMD() {
        TriangleMesh mallaPMMD = PillowMeshPMMD.crear(52, 14, 92, 4, 0.8, 6);




        int numCarasPMMD = mallaPMMD.getFaces().size() / 9;
        assertTrue(numCarasPMMD > 1000, "Debe haber >1000 triángulos para buena calidad");
        assertTrue(numCarasPMMD < 5000, "Debe tener <5000 triángulos para rendimiento");
    }

    @Test
    void testPillowMeshVariousSizesPMMD() {

        PillowMeshPMMD.crear(100, 20, 120, 5, 1.0, 8);
        PillowMeshPMMD.crear(40, 10, 60, 3, 0.6, 4);
        PillowMeshPMMD.crear(200, 85, 200, 8, 1.2, 10);

        assertTrue(true);
    }

    @Test
    void testPillowMeshDefaultsPMMD() {
        TriangleMesh mallaPMMD = PillowMeshPMMD.crearDefecto(52, 14, 92, 4);
        assertNotNull(mallaPMMD);
        assertTrue(mallaPMMD.getPoints().size() > 0);
    }

    @Test
    void testPipingMeshCreationPMMD() {
        TriangleMesh mallaPMMD = PipingMeshPMMD.crearRibeteRectangular(52, 92, 0.4, 8);
        assertNotNull(mallaPMMD);
        assertTrue(mallaPMMD.getPoints().size() > 0);
        assertTrue(mallaPMMD.getNormals().size() > 0);
        assertTrue(mallaPMMD.getFaces().size() > 0);
    }

    @Test
    void testPipingMeshDefaultsPMMD() {
        TriangleMesh mallaPMMD = PipingMeshPMMD.crearDefecto(52, 92);
        assertNotNull(mallaPMMD);
        assertTrue(mallaPMMD.getPoints().size() > 100);
    }

    @Test
    void testCombinedGeometryPerformancePMMD() {




        int totalTriangulosPMMD = 0;


        for (int iPMMD = 0; iPMMD < 3; iPMMD++) {
            TriangleMesh cojinPMMD = PillowMeshPMMD.crearDefecto(52, 14, 92, 4);
            totalTriangulosPMMD += cojinPMMD.getFaces().size() / 9;
        }


        TriangleMesh respaldoPMMD = PillowMeshPMMD.crearDefecto(156, 85, 20, 4);
        totalTriangulosPMMD += respaldoPMMD.getFaces().size() / 9;

        System.out.println("Sofá Moderno 3 cuerpos: " + totalTriangulosPMMD + " triángulos");


        assertTrue(totalTriangulosPMMD < 60000,
            "Sofá Moderno 3 cuerpos debe tener <60k triángulos, tiene " + totalTriangulosPMMD);
        assertTrue(totalTriangulosPMMD > 2000,
            "Sofá debe tener >2k triángulos para buena calidad");
    }
}
