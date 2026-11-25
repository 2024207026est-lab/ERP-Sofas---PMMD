package pe.erp.sofas.service.calculo;

import pe.erp.sofas.model.ComponentePMMD;

import java.util.*;







public final class DespiecePMMD {
    


    public static final String D23 = "MP-019";

    public static final String D18_4 = "MP-018";

    public static final String D18_2 = "MP-017";

    public static final String MADERA = "MP-001";

    public static final String TRIPLAY = "MP-002";

    public static final String TELA = "MP-003";

    private static final double COSTURAPMMD = 3;

    private static final double ANCHO_CHAISEPMMD = 90;

    private static final double ROLLOPMMD = 140;

    private static final double TABLAPMMD = 300;

    public record Sofa(
      double largo, 
        double fondo, 
          double alto, 
            int cuerpos, 
              boolean chaise, 
                boolean puff,
                       
                       boolean girarTela, 
                         String origen) {
    }

    private DespiecePMMD() {
    }



    public static Map<String, List<PiezaPMMD>> piezas(Sofa sPMMD) {
        Map<String, List<PiezaPMMD>> mPMMD = new LinkedHashMap<>();
        if (sPMMD.puff()) {
            puff(sPMMD, mPMMD);
        } else {
            sofaPMMD(sPMMD, mPMMD);
        }
        return mPMMD;
    }

    private static void sofaPMMD(Sofa sPMMD, Map<String, List<PiezaPMMD>> mPMMD) {
        int n = Math.max(1, sPMMD.cuerpos());
        double LPMMD = sPMMD.largo(), FPMMD = sPMMD.fondo(), APMMD = sPMMD.alto();
        double FmPMMD = sPMMD.chaise() ? FPMMD - 65 : FPMMD;
        double LmPMMD = sPMMD.chaise() ? LPMMD - ANCHO_CHAISEPMMD : LPMMD;
        double brazosPMMD = sPMMD.chaise() ? 16 : 32;
        double anchoCojinPMMD = (LmPMMD - brazosPMMD) / n;
        double fondoCojinPMMD = FmPMMD - 22;
        double altoRespaldoPMMD = APMMD - 40;
        String o = sPMMD.origen();
        boolean g = sPMMD.girarTela();


        List<PiezaPMMD> d23PMMD = listaPMMD(mPMMD, D23);
        d23PMMD.add(new PiezaPMMD("Cojín de asiento", ComponentePMMD.ASIENTO, rPMMD(anchoCojinPMMD), rPMMD(fondoCojinPMMD), n, true, o));
        if (sPMMD.chaise()) d23PMMD.add(new PiezaPMMD("Cojín de chaise", ComponentePMMD.ASIENTO, ANCHO_CHAISEPMMD - 16, rPMMD(FPMMD - 22), 1, true, o));


        listaPMMD(mPMMD, D18_4).add(new PiezaPMMD("Respaldo", ComponentePMMD.RESPALDO, rPMMD(anchoCojinPMMD), rPMMD(altoRespaldoPMMD), n, true, o));


        List<PiezaPMMD> d18PMMD = listaPMMD(mPMMD, D18_2);
        int nBrazos = sPMMD.chaise() ? 1 : 2;
        d18PMMD.add(new PiezaPMMD("Brazo interior", ComponentePMMD.BRAZOS, rPMMD(FmPMMD - 10), rPMMD(APMMD - 35), nBrazos, true, o));
        d18PMMD.add(new PiezaPMMD("Tapa de brazo", ComponentePMMD.BRAZOS, rPMMD(FmPMMD - 10), 16, nBrazos, true, o));
        d18PMMD.add(new PiezaPMMD("Frente de asiento", ComponentePMMD.ASIENTO, Math.min(rPMMD(LmPMMD - brazosPMMD), 200), 18, 1, true, o));
        if (sPMMD.chaise()) d18PMMD.add(new PiezaPMMD("Frente de chaise", ComponentePMMD.ASIENTO, ANCHO_CHAISEPMMD - 16, 18, 1, true, o));


        List<PiezaPMMD> telaPMMD = listaPMMD(mPMMD, TELA);
        double espesorAsientoPMMD = 12.7 + COSTURAPMMD, espesorRespaldoPMMD = 10.2 + COSTURAPMMD;
        telaPMMD.add(new PiezaPMMD("Cara de cojín de asiento", ComponentePMMD.ASIENTO, rPMMD(anchoCojinPMMD + COSTURAPMMD), rPMMD(fondoCojinPMMD + COSTURAPMMD), 2 * n, g, o));
        bandaPMMD(telaPMMD, "Banda de cojín de asiento", ComponentePMMD.ASIENTO, 2 * (anchoCojinPMMD + fondoCojinPMMD) + COSTURAPMMD, espesorAsientoPMMD, n, g, o);
        if (sPMMD.chaise()) {
            telaPMMD.add(new PiezaPMMD("Cara de cojín de chaise", ComponentePMMD.ASIENTO, ANCHO_CHAISEPMMD - 16 + COSTURAPMMD, rPMMD(FPMMD - 22 + COSTURAPMMD), 2, g, o));
            bandaPMMD(telaPMMD, "Banda de cojín de chaise", ComponentePMMD.ASIENTO, 2 * (ANCHO_CHAISEPMMD - 16 + FPMMD - 22) + COSTURAPMMD, espesorAsientoPMMD, 1, g, o);
        }
        telaPMMD.add(new PiezaPMMD("Cara de respaldo", ComponentePMMD.RESPALDO, rPMMD(anchoCojinPMMD + COSTURAPMMD), rPMMD(altoRespaldoPMMD + COSTURAPMMD), 2 * n, g, o));
        bandaPMMD(telaPMMD, "Banda de respaldo", ComponentePMMD.RESPALDO, 2 * (anchoCojinPMMD + altoRespaldoPMMD) + COSTURAPMMD, espesorRespaldoPMMD, n, g, o);
        telaPMMD.add(new PiezaPMMD("Brazo interior", ComponentePMMD.BRAZOS, rPMMD(FmPMMD - 10 + COSTURAPMMD), rPMMD(APMMD - 35 + 16 + COSTURAPMMD), nBrazos, g, o));
        telaPMMD.add(new PiezaPMMD("Brazo exterior", ComponentePMMD.BRAZOS, rPMMD(FmPMMD + COSTURAPMMD), rPMMD(APMMD - 15 + COSTURAPMMD), nBrazos, g, o));
        telaPMMD.add(new PiezaPMMD("Frente de brazo", ComponentePMMD.BRAZOS, 16 + COSTURAPMMD, rPMMD(APMMD - 15 + COSTURAPMMD), nBrazos, g, o));
        bandaPMMD(telaPMMD, "Frente de asiento", ComponentePMMD.TAPIZADO, LmPMMD - brazosPMMD + COSTURAPMMD, 25 + COSTURAPMMD, 1, g, o);
        bandaPMMD(telaPMMD, "Respaldo exterior", ComponentePMMD.TAPIZADO, LPMMD + COSTURAPMMD, APMMD - 15 + COSTURAPMMD, 1, g, o);
        bandaPMMD(telaPMMD, "Faldón", ComponentePMMD.TAPIZADO, LPMMD + 2 * FPMMD + COSTURAPMMD, 18 + COSTURAPMMD, 1, g, o);


        List<PiezaPMMD> maderaPMMD = listaPMMD(mPMMD, MADERA);
        listonPMMD(maderaPMMD, "Larguero de asiento", ComponentePMMD.ESTRUCTURA, LPMMD - 4, 2, o);
        listonPMMD(maderaPMMD, "Travesaño", ComponentePMMD.ESTRUCTURA, FPMMD - 10, n + 1 + (sPMMD.chaise() ? 2 : 0), o);
        maderaPMMD.add(new PiezaPMMD("Parante de respaldo", ComponentePMMD.ESTRUCTURA, rPMMD(APMMD - 15), 0, n + 1, false, o));
        maderaPMMD.add(new PiezaPMMD("Larguero de brazo", ComponentePMMD.BRAZOS, rPMMD(FmPMMD - 8), 0, 2 * nBrazos, false, o));
        listonPMMD(maderaPMMD, "Refuerzo", ComponentePMMD.ESTRUCTURA, LPMMD / 2 - 10, 2, o);
        maderaPMMD.add(new PiezaPMMD("Taco", ComponentePMMD.ENSAMBLAJE, 12, 0, 8 + (sPMMD.chaise() ? 2 : 0), false, o));


        List<PiezaPMMD> triPMMD = listaPMMD(mPMMD, TRIPLAY);
        tableroPMMD(triPMMD, "Fondo de asiento", ComponentePMMD.ASIENTO, LmPMMD - brazosPMMD, FmPMMD - 22, o);
        if (sPMMD.chaise()) tableroPMMD(triPMMD, "Fondo de chaise", ComponentePMMD.ASIENTO, ANCHO_CHAISEPMMD - 16, FPMMD - 22, o);
        tableroPMMD(triPMMD, "Tapa trasera", ComponentePMMD.RESPALDO, LPMMD - 4, APMMD - 40, o);
        triPMMD.add(new PiezaPMMD("Lateral", ComponentePMMD.BRAZOS, rPMMD(FmPMMD - 5), rPMMD(APMMD - 20), nBrazos, true, o));
    }

    private static void puff(Sofa sPMMD, Map<String, List<PiezaPMMD>> mPMMD) {
        double LPMMD = sPMMD.largo(), APMMD = sPMMD.alto();
        String o = sPMMD.origen();
        boolean g = sPMMD.girarTela();
        listaPMMD(mPMMD, D23).add(new PiezaPMMD("Tapa de puff", ComponentePMMD.ASIENTO, LPMMD - 4, LPMMD - 4, 1, true, o));
        listaPMMD(mPMMD, D18_2).add(new PiezaPMMD("Lateral de puff", ComponentePMMD.TAPIZADO, LPMMD - 4, APMMD - 12, 4, true, o));
        List<PiezaPMMD> telaPMMD = listaPMMD(mPMMD, TELA);
        telaPMMD.add(new PiezaPMMD("Tapa de puff", ComponentePMMD.ASIENTO, LPMMD + COSTURAPMMD, LPMMD + COSTURAPMMD, 1, g, o));
        telaPMMD.add(new PiezaPMMD("Lateral de puff", ComponentePMMD.TAPIZADO, LPMMD + COSTURAPMMD, APMMD + COSTURAPMMD, 4, g, o));
        List<PiezaPMMD> madPMMD = listaPMMD(mPMMD, MADERA);
        madPMMD.add(new PiezaPMMD("Marco de puff", ComponentePMMD.ESTRUCTURA, LPMMD - 4, 0, 4, false, o));
        madPMMD.add(new PiezaPMMD("Parante de puff", ComponentePMMD.ESTRUCTURA, APMMD - 14, 0, 4, false, o));
        List<PiezaPMMD> triPMMD = listaPMMD(mPMMD, TRIPLAY);
        triPMMD.add(new PiezaPMMD("Base y tapa de puff", ComponentePMMD.ESTRUCTURA, LPMMD - 4, LPMMD - 4, 2, true, o));
    }



    private static void bandaPMMD(List<PiezaPMMD> lPMMD, String nombre, ComponentePMMD c, double largo, double anchoPMMD, int n,
                              boolean girable, String o) {
        if (largo > ROLLOPMMD && !girable) {
            int partes = (int) Math.ceil(largo / (ROLLOPMMD - COSTURAPMMD));
            lPMMD.add(new PiezaPMMD(nombre + " (empalme)", c, rPMMD(largo / partes + COSTURAPMMD), rPMMD(anchoPMMD), partes * n, false, o));
        } else {
            lPMMD.add(new PiezaPMMD(nombre, c, rPMMD(largo), rPMMD(anchoPMMD), n, girable, o));
        }
    }

    private static void listonPMMD(List<PiezaPMMD> lPMMD, String nombre, ComponentePMMD c, double largo, int n, String o) {
        if (largo > TABLAPMMD) {
            lPMMD.add(new PiezaPMMD(nombre + " (empalme)", c, rPMMD(largo / 2 + 10), 0, 2 * n, false, o));
        } else {
            lPMMD.add(new PiezaPMMD(nombre, c, rPMMD(largo), 0, n, false, o));
        }
    }

    private static void tableroPMMD(List<PiezaPMMD> lPMMD, String nombre, ComponentePMMD c, double wPMMD, double hPMMD, String o) {
        double wwPMMD = wPMMD, hhPMMD = hPMMD;
        int partes = 1;
        while (Math.max(wwPMMD, hhPMMD) > 240 || Math.min(wwPMMD, hhPMMD) > 120) {
            partes++;
            wwPMMD = wPMMD / partes;
            hhPMMD = hPMMD;
        }
        lPMMD.add(new PiezaPMMD(nombre + (partes > 1 ? " (1/" + partes + ")" : ""), c, rPMMD(wwPMMD), rPMMD(hhPMMD), partes, true, o));
    }

    private static List<PiezaPMMD> listaPMMD(Map<String, List<PiezaPMMD>> mPMMD, String codigoPMMD) {
        return mPMMD.computeIfAbsent(codigoPMMD, kPMMD -> new ArrayList<>());
    }

    private static double rPMMD(double vPMMD) {
        return Math.round(vPMMD * 10) / 10.0;
    }
}
