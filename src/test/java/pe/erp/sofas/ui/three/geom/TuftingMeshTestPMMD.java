package pe.erp.sofas.ui.three.geom;

import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;





class TuftingMeshTestPMMD {

    @Test
    void testTuftingMeshCreationPMMD() {
        TriangleMesh mallaPMMD = TuftingMeshPMMD.crear(156, 85, 20, 13, 1.2, 4);
        assertNotNull(mallaPMMD);
        assertTrue(mallaPMMD.getPoints().size() > 0);
        assertTrue(mallaPMMD.getNormals().size() > 0);
        assertTrue(mallaPMMD.getFaces().size() > 0);
    }

    @Test
    void testTuftingMeshDefaultsPMMD() {
        TriangleMesh mallaPMMD = TuftingMeshPMMD.crearDefecto(156, 85, 20);
        assertNotNull(mallaPMMD);
        assertTrue(mallaPMMD.getPoints().size() > 100);
    }

    @Test
    void testTuftingMeshTriangleCountPMMD() {

        TriangleMesh mallaPMMD = TuftingMeshPMMD.crearDefecto(156, 85, 20);
        int numTriangulosPMMD = mallaPMMD.getFaces().size() / 9;

        System.out.println("TuftingMesh triángulos: " + numTriangulosPMMD);


        assertTrue(numTriangulosPMMD > 100, "Capitoné debe tener >100 triángulos");
        assertTrue(numTriangulosPMMD < 5000, "Capitoné debe tener <5k triángulos");
    }

    @Test
    void testTuftingMeshVariousSizesPMMD() {

        TuftingMeshPMMD.crear(100, 70, 15, 12, 1.0, 3);
        TuftingMeshPMMD.crear(200, 100, 25, 14, 1.5, 5);
        TuftingMeshPMMD.crear(50, 50, 10, 10, 0.8, 2);

        assertTrue(true);
    }

    @Test
    void testTuftingMeshPerformancePMMD() {

        TriangleMesh respaldoPMMD = TuftingMeshPMMD.crearDefecto(156, 85, 20);

        int numTriangulosPMMD = respaldoPMMD.getFaces().size() / 9;
        System.out.println("Respaldo Chesterfield capitoné: " + numTriangulosPMMD + " triángulos");


        assertTrue(numTriangulosPMMD < 3000,
            "Respaldo Chesterfield debe tener <3000 triángulos para perf, tiene " + numTriangulosPMMD);
    }
}
