package pe.erp.sofas.ui.three.geom;

import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;















public final class TuftingMeshPMMD {
    












    public static TriangleMesh crear(double wPMMD, double hPMMD, double dPMMD, double tamRomboPMMD,
                                     double profundidadHundimientoPMMD, int segPMMD) {
        TriangleMesh mallaPMMD = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);




        double hxPMMD = wPMMD / 2, hyPMMD = hPMMD / 2, hzPMMD = dPMMD / 2;
        int nPMMD = segPMMD + 1;


        int filasPMMD = (int) Math.ceil(hPMMD / tamRomboPMMD) + 1;
        int colsPMMD = (int) Math.ceil(wPMMD / tamRomboPMMD) + 1;


        for (int rowPMMD = 0; rowPMMD < filasPMMD; rowPMMD++) {
            for (int colPMMD = 0; colPMMD < colsPMMD; colPMMD++) {
                double y = -hyPMMD + (rowPMMD / (double) (filasPMMD - 1)) * hPMMD;
                double x = -hxPMMD + (colPMMD / (double) (colsPMMD - 1)) * wPMMD;



                double distRomboPMMD = calcularDistanciaRomboPMMD(x, y, tamRomboPMMD);
                double deformacionPMMD = 0;
                if (distRomboPMMD < tamRomboPMMD / 2) {

                    double tPMMD = distRomboPMMD / (tamRomboPMMD / 2);
                    deformacionPMMD = -profundidadHundimientoPMMD * (1 - tPMMD * tPMMD);
                }

                float pxPMMD = (float) x;
                float pyPMMD = (float) y;
                float pzPMMD = (float) (hzPMMD + deformacionPMMD);

                mallaPMMD.getPoints().addAll(pxPMMD, pyPMMD, pzPMMD);
                mallaPMMD.getNormals().addAll(0, 0, 1);
                mallaPMMD.getTexCoords().addAll((float) ((x + hxPMMD) / wPMMD), (float) ((y + hyPMMD) / hPMMD));
            }
        }


        for (int rowPMMD = 0; rowPMMD < filasPMMD - 1; rowPMMD++) {
            for (int colPMMD = 0; colPMMD < colsPMMD - 1; colPMMD++) {
                int aPMMD = rowPMMD * colsPMMD + colPMMD;
                int bPMMD = aPMMD + 1;
                int cPMMD = aPMMD + colsPMMD;
                int ddPMMD = cPMMD + 1;

                mallaPMMD.getFaces().addAll(aPMMD, aPMMD, aPMMD, bPMMD, bPMMD, bPMMD, cPMMD, cPMMD, cPMMD);
                mallaPMMD.getFaces().addAll(bPMMD, bPMMD, bPMMD, ddPMMD, ddPMMD, ddPMMD, cPMMD, cPMMD, cPMMD);
            }
        }

        return mallaPMMD;
    }






    private static double calcularDistanciaRomboPMMD(double x, double y, double tamRomboPMMD) {

        double cxPMMD = Math.round(x / tamRomboPMMD) * tamRomboPMMD;
        double cyPMMD = Math.round(y / tamRomboPMMD) * tamRomboPMMD;

        double dxPMMD = Math.abs(x - cxPMMD);
        double dyPMMD = Math.abs(y - cyPMMD);


        return dxPMMD + dyPMMD;
    }






    public static TriangleMesh crearDefecto(double wPMMD, double hPMMD, double dPMMD) {
        return crear(wPMMD, hPMMD, dPMMD, 13, 1.2, 4);
    }
}
