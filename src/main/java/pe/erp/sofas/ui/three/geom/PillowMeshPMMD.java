package pe.erp.sofas.ui.three.geom;

import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;















public final class PillowMeshPMMD {

    public static TriangleMesh crear(double wPMMD, double hPMMD, double dPMMD, double rPMMD, double bulgePMMD, int seg) {

        TriangleMesh mallaPMMD = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);

        rPMMD = Math.max(0.01, Math.min(rPMMD, Math.min(wPMMD, Math.min(hPMMD, dPMMD)) / 2 - 0.01));
        double hxPMMD = wPMMD / 2, hyPMMD = hPMMD / 2, hzPMMD = dPMMD / 2;
        double ixPMMD = hxPMMD - rPMMD, iyPMMD = hyPMMD - rPMMD, izPMMD = hzPMMD - rPMMD;


        int nPMMD = seg + 1;
        double[] tPMMD = new double[2 * nPMMD];
        int[] signoPMMD = new int[2 * nPMMD];
        for (int iPMMD = 0; iPMMD < nPMMD; iPMMD++) {
            double aPMMD = Math.tan(Math.PI / 4 * (1 - iPMMD / (double) seg));
            tPMMD[iPMMD] = -aPMMD;
            signoPMMD[iPMMD] = -1;
            tPMMD[nPMMD + iPMMD] = Math.tan(Math.PI / 4 * (iPMMD / (double) seg));
            signoPMMD[nPMMD + iPMMD] = 1;
        }

        int ladoPMMD = 2 * nPMMD;
        int[][] carasPMMD = {{0, 1}, {0, -1}, {1, 1}, {1, -1}, {2, 1}, {2, -1}};
        int basePMMD = 0;

        for (int[] caraPMMD : carasPMMD) {
            int ejePMMD = caraPMMD[0], sPMMD = caraPMMD[1];
            int eaPMMD = (ejePMMD + 1) % 3, ebPMMD = (ejePMMD + 2) % 3;

            for (int jPMMD = 0; jPMMD < ladoPMMD; jPMMD++) {
                for (int iPMMD = 0; iPMMD < ladoPMMD; iPMMD++) {

                    double[] dirPMMD = new double[3];
                    int[] sgPMMD = new int[3];
                    dirPMMD[ejePMMD] = sPMMD;
                    sgPMMD[ejePMMD] = sPMMD;
                    dirPMMD[eaPMMD] = tPMMD[iPMMD];
                    sgPMMD[eaPMMD] = signoPMMD[iPMMD];
                    dirPMMD[ebPMMD] = tPMMD[jPMMD];
                    sgPMMD[ebPMMD] = signoPMMD[jPMMD];

                    double lenPMMD = Math.sqrt(dirPMMD[0] * dirPMMD[0] + dirPMMD[1] * dirPMMD[1] + dirPMMD[2] * dirPMMD[2]);
                    double nx = dirPMMD[0] / lenPMMD, ny = dirPMMD[1] / lenPMMD, nz = dirPMMD[2] / lenPMMD;

                    float pxPMMD = (float) (sgPMMD[0] * ixPMMD + rPMMD * nx);
                    float pyPMMD = (float) (sgPMMD[1] * iyPMMD + rPMMD * ny);
                    float pzPMMD = (float) (sgPMMD[2] * izPMMD + rPMMD * nz);



                    double uPMMD = Math.abs(tPMMD[iPMMD]) / Math.abs(tPMMD[0]);
                    double vPMMD = Math.abs(tPMMD[jPMMD]) / Math.abs(tPMMD[0]);
                    double deformacionPMMD = bulgePMMD * Math.sin(Math.PI * uPMMD) * Math.sin(Math.PI * vPMMD);


                    pxPMMD += (float) (deformacionPMMD * nx);
                    pyPMMD += (float) (deformacionPMMD * ny);
                    pzPMMD += (float) (deformacionPMMD * nz);

                    mallaPMMD.getPoints().addAll(pxPMMD, pyPMMD, pzPMMD);
                    mallaPMMD.getNormals().addAll((float) nx, (float) ny, (float) nz);
                    mallaPMMD.getTexCoords().addAll((float) (pxPMMD / 40.0), (float) (pzPMMD / 40.0));
                }
            }


            for (int jPMMD = 0; jPMMD < ladoPMMD - 1; jPMMD++) {
                for (int iPMMD = 0; iPMMD < ladoPMMD - 1; iPMMD++) {
                    int aPMMD = basePMMD + jPMMD * ladoPMMD + iPMMD, bPMMD = aPMMD + 1, cPMMD = aPMMD + ladoPMMD, ddPMMD = cPMMD + 1;
                    boolean invertirPMMD = (sPMMD > 0) == (ejePMMD == 1);
                    if (invertirPMMD) {
                        triPMMD(mallaPMMD, aPMMD, cPMMD, bPMMD);
                        triPMMD(mallaPMMD, bPMMD, cPMMD, ddPMMD);
                    } else {
                        triPMMD(mallaPMMD, aPMMD, bPMMD, cPMMD);
                        triPMMD(mallaPMMD, bPMMD, ddPMMD, cPMMD);
                    }
                }
            }
            basePMMD += ladoPMMD * ladoPMMD;
        }

        return mallaPMMD;
    }

    private static void triPMMD(TriangleMesh mPMMD, int aPMMD, int bPMMD, int cPMMD) {
        mPMMD.getFaces().addAll(aPMMD, aPMMD, aPMMD, bPMMD, bPMMD, bPMMD, cPMMD, cPMMD, cPMMD);
    }






    public static TriangleMesh crearDefecto(double wPMMD, double hPMMD, double dPMMD, double rPMMD) {
        return crear(wPMMD, hPMMD, dPMMD, rPMMD, 0.8, 6);
    }
}
