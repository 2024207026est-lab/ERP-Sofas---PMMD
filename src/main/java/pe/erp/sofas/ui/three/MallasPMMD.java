package pe.erp.sofas.ui.three;

import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;






public final class MallasPMMD {

    private MallasPMMD() {
    }



    public static TriangleMesh cajaRedondeada(double wPMMD, double hPMMD, double dPMMD, double rPMMD, int seg) {
        rPMMD = Math.max(0.01, Math.min(rPMMD, Math.min(wPMMD, Math.min(hPMMD, dPMMD)) / 2 - 0.01));
        double hxPMMD = wPMMD / 2, hyPMMD = hPMMD / 2, hzPMMD = dPMMD / 2;
        double ixPMMD = hxPMMD - rPMMD, iyPMMD = hyPMMD - rPMMD, izPMMD = hzPMMD - rPMMD;
        TriangleMesh mPMMD = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);

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
                    mPMMD.getPoints().addAll(pxPMMD, pyPMMD, pzPMMD);
                    mPMMD.getNormals().addAll((float) nx, (float) ny, (float) nz);

                    double[] pPMMD = {pxPMMD, pyPMMD, pzPMMD};
                    mPMMD.getTexCoords().addAll((float) (pPMMD[eaPMMD] / 40.0), (float) (pPMMD[ebPMMD] / 40.0));
                }
            }
            for (int jPMMD = 0; jPMMD < ladoPMMD - 1; jPMMD++) {
                for (int iPMMD = 0; iPMMD < ladoPMMD - 1; iPMMD++) {
                    int aPMMD = basePMMD + jPMMD * ladoPMMD + iPMMD, bPMMD = aPMMD + 1, cPMMD = aPMMD + ladoPMMD, ddPMMD = cPMMD + 1;

                    boolean invertirPMMD = (sPMMD > 0) == (ejePMMD == 1);
                    if (invertirPMMD) {
                        triPMMD(mPMMD, aPMMD, cPMMD, bPMMD);
                        triPMMD(mPMMD, bPMMD, cPMMD, ddPMMD);
                    } else {
                        triPMMD(mPMMD, aPMMD, bPMMD, cPMMD);
                        triPMMD(mPMMD, bPMMD, ddPMMD, cPMMD);
                    }
                }
            }
            basePMMD += ladoPMMD * ladoPMMD;
        }
        return mPMMD;
    }

    private static void triPMMD(TriangleMesh mPMMD, int aPMMD, int bPMMD, int cPMMD) {
        mPMMD.getFaces().addAll(aPMMD, aPMMD, aPMMD, bPMMD, bPMMD, bPMMD, cPMMD, cPMMD, cPMMD);
    }



    public static TriangleMesh tronco(double r1PMMD, double r2PMMD, double hPMMD, int ladosPMMD) {
        TriangleMesh mPMMD = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
        mPMMD.getTexCoords().addAll(0, 0);
        double inclinacionPMMD = (r1PMMD - r2PMMD) / hPMMD;
        for (int iPMMD = 0; iPMMD <= ladosPMMD; iPMMD++) {
            double aPMMD = 2 * Math.PI * iPMMD / ladosPMMD;
            double cPMMD = Math.cos(aPMMD), sPMMD = Math.sin(aPMMD);
            double nlPMMD = Math.sqrt(1 + inclinacionPMMD * inclinacionPMMD);
            mPMMD.getPoints().addAll((float) (r1PMMD * cPMMD), (float) (hPMMD / 2), (float) (r1PMMD * sPMMD));
            mPMMD.getNormals().addAll((float) (cPMMD / nlPMMD), (float) (-inclinacionPMMD / nlPMMD), (float) (sPMMD / nlPMMD));
            mPMMD.getPoints().addAll((float) (r2PMMD * cPMMD), (float) (-hPMMD / 2), (float) (r2PMMD * sPMMD));
            mPMMD.getNormals().addAll((float) (cPMMD / nlPMMD), (float) (-inclinacionPMMD / nlPMMD), (float) (sPMMD / nlPMMD));
        }
        for (int iPMMD = 0; iPMMD < ladosPMMD; iPMMD++) {
            int aPMMD = 2 * iPMMD, bPMMD = aPMMD + 1, cPMMD = aPMMD + 2, dPMMD = aPMMD + 3;
            mPMMD.getFaces().addAll(aPMMD, aPMMD, 0, cPMMD, cPMMD, 0, bPMMD, bPMMD, 0);
            mPMMD.getFaces().addAll(bPMMD, bPMMD, 0, cPMMD, cPMMD, 0, dPMMD, dPMMD, 0);
        }

        int basePMMD = mPMMD.getPoints().size() / 3;
        mPMMD.getPoints().addAll(0, (float) (-hPMMD / 2), 0, 0, (float) (hPMMD / 2), 0);
        mPMMD.getNormals().addAll(0, -1, 0, 0, 1, 0);
        int arribaPMMD = basePMMD, abajoPMMD = basePMMD + 1;
        for (int iPMMD = 0; iPMMD < ladosPMMD; iPMMD++) {
            int aPMMD = 2 * iPMMD + 1, cPMMD = 2 * iPMMD + 3;
            mPMMD.getFaces().addAll(arribaPMMD, arribaPMMD, 0, aPMMD, arribaPMMD, 0, cPMMD, arribaPMMD, 0);
            int bPMMD = 2 * iPMMD, dPMMD = 2 * iPMMD + 2;
            mPMMD.getFaces().addAll(abajoPMMD, abajoPMMD, 0, dPMMD, abajoPMMD, 0, bPMMD, abajoPMMD, 0);
        }
        return mPMMD;
    }



    public static TriangleMesh plano(double wPMMD, double dPMMD) {
        TriangleMesh mPMMD = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);
        float xPMMD = (float) (wPMMD / 2), zPMMD = (float) (dPMMD / 2);
        mPMMD.getPoints().addAll(-xPMMD, 0, -zPMMD, xPMMD, 0, -zPMMD, -xPMMD, 0, zPMMD, xPMMD, 0, zPMMD);
        mPMMD.getNormals().addAll(0, -1, 0);
        mPMMD.getTexCoords().addAll(0, 0, 1, 0, 0, 1, 1, 1);
        mPMMD.getFaces().addAll(0, 0, 0, 2, 0, 2, 1, 0, 1);
        mPMMD.getFaces().addAll(1, 0, 1, 2, 0, 2, 3, 0, 3);
        return mPMMD;
    }
}
