package pe.erp.sofas.ui.three.geom;

import javafx.geometry.Point3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import java.util.ArrayList;
import java.util.List;












public final class PipingMeshPMMD {
    










    public static TriangleMesh crearRibeteRectangular(double wPMMD, double dPMMD, double radioTuboPMMD, int lados) {
        TriangleMesh mallaPMMD = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);




        List<Point3D> puntosPMMD = new ArrayList<>();


        for (double x = -wPMMD / 2; x <= wPMMD / 2; x += wPMMD / 10.0) {
            puntosPMMD.add(new Point3D(x, 0, -dPMMD / 2));
        }


        for (double z = -dPMMD / 2; z <= dPMMD / 2; z += dPMMD / 10.0) {
            puntosPMMD.add(new Point3D(wPMMD / 2, 0, z));
        }


        for (double x = wPMMD / 2; x >= -wPMMD / 2; x -= wPMMD / 10.0) {
            puntosPMMD.add(new Point3D(x, 0, dPMMD / 2));
        }


        for (double z = dPMMD / 2; z >= -dPMMD / 2; z -= dPMMD / 10.0) {
            puntosPMMD.add(new Point3D(-wPMMD / 2, 0, z));
        }


        int n = puntosPMMD.size();
        for (int iPMMD = 0; iPMMD < n; iPMMD++) {
            Point3D pPMMD = puntosPMMD.get(iPMMD);


            Point3D siguientePMMD = puntosPMMD.get((iPMMD + 1) % n);
            Point3D tangentePMMD = siguientePMMD.subtract(pPMMD).normalize();


            Point3D normalPMMD = new Point3D(0, 1, 0);
            if (Math.abs(tangentePMMD.getY()) > 0.9) {
                normalPMMD = new Point3D(1, 0, 0);
            }


            for (int jPMMD = 0; jPMMD < lados; jPMMD++) {
                double anguloPMMD = 2 * Math.PI * jPMMD / lados;
                double cosPMMD = Math.cos(anguloPMMD);
                double sinPMMD = Math.sin(anguloPMMD);


                Point3D offsetPMMD = normalPMMD.multiply(radioTuboPMMD * cosPMMD).add(
                    normalPMMD.crossProduct(tangentePMMD).multiply(radioTuboPMMD * sinPMMD)
                );
                Point3D ptuboPMMD = pPMMD.add(offsetPMMD);

                mallaPMMD.getPoints().addAll((float) ptuboPMMD.getX(), (float) ptuboPMMD.getY(), (float) ptuboPMMD.getZ());
                mallaPMMD.getNormals().addAll((float) offsetPMMD.getX(), (float) offsetPMMD.getY(), (float) offsetPMMD.getZ());
                mallaPMMD.getTexCoords().addAll(jPMMD / (float) lados, iPMMD / (float) n);
            }
        }


        for (int iPMMD = 0; iPMMD < n - 1; iPMMD++) {
            for (int jPMMD = 0; jPMMD < lados; jPMMD++) {
                int aPMMD = iPMMD * lados + jPMMD;
                int bPMMD = iPMMD * lados + (jPMMD + 1) % lados;
                int cPMMD = (iPMMD + 1) * lados + jPMMD;
                int ddPMMD = (iPMMD + 1) * lados + (jPMMD + 1) % lados;

                mallaPMMD.getFaces().addAll(aPMMD, aPMMD, aPMMD, bPMMD, bPMMD, bPMMD, cPMMD, cPMMD, cPMMD);
                mallaPMMD.getFaces().addAll(bPMMD, bPMMD, bPMMD, ddPMMD, ddPMMD, ddPMMD, cPMMD, cPMMD, cPMMD);
            }
        }

        return mallaPMMD;
    }






    public static TriangleMesh crearDefecto(double wPMMD, double dPMMD) {
        return crearRibeteRectangular(wPMMD, dPMMD, 0.4, 8);
    }
}
