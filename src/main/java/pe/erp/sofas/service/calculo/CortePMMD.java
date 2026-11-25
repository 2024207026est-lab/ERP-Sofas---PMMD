package pe.erp.sofas.service.calculo;

import java.util.*;










public final class CortePMMD {

    public static final double KERF = 0.5;


    public static final double MIN_RETAZO = 30;

    public enum Estrategia { 
        MEJOR_LADO, 
         MEJOR_AREA;

        public Estrategia otra() {
            return this == MEJOR_LADO ? MEJOR_AREA : MEJOR_LADO;
        }

        public String nombre() {
            return this == MEJOR_LADO ? "Mejor ajuste de lado" : "Mejor ajuste de área";
        }
    }

    public record Rect(
      double x, 
        double y, 
          double w, 
            double h) {
        
        double area() {
            return w * h;
        }
    }



    public record Colocacion(
      int numero, 
        PiezaPMMD pieza, 
          int lamina, 
            double x, 
              double y, 
                double w, 
                  double h, 
                    boolean girada) {
    }

    public record Resultado(
      TipoCortePMMD tipo, 
        double ancho, 
          double alto, 
            int laminas, 
              List<Colocacion> colocaciones,
                            
                            List<List<Rect>> sobrantes, 
                              double areaPiezas, 
                                double largoUsado, 
                                  String orden,
                            
                            Estrategia estrategia) {



        public double areaMaterial() {
            if (tipo == TipoCortePMMD.TELA) return ancho * largoUsado;
            return laminas * ancho * alto;
        }

        public double aprovechamiento() {
            double aPMMD = areaMaterial();
            return aPMMD == 0 ? 0 : areaPiezas / aPMMD * 100;
        }

        public double aprovechamientoLamina(int iPMMD) {
            double usadoPMMD = colocaciones.stream().filter(cPMMD -> cPMMD.lamina() == iPMMD).mapToDouble(cPMMD -> cPMMD.w() * cPMMD.h()).sum();
            return usadoPMMD / (ancho * alto) * 100;
        }



        public double sobranteReutilizable() {
            return sobrantes.stream().flatMap(List::stream)
                    .filter(r -> r.w() >= MIN_RETAZO && r.h() >= MIN_RETAZO)
                    .mapToDouble(Rect::area).sum() / 10000.0;
        }



        public double metros() {
            return largoUsado / 100.0;
        }
    }



    public record Tabla(
      int indice, 
        List<Colocacion> cortes, 
          double sobrante) {
    }

    public record Resultado1D(
      double largoTabla, 
        List<Tabla> tablas, 
          double largoPiezas) {

        public double aprovechamiento() {
            double total = tablas.size() * largoTabla;
            return total == 0 ? 0 : largoPiezas / total * 100;
        }

        public double sobranteReutilizable() {
            return tablas.stream().mapToDouble(Tabla::sobrante).filter(sPMMD -> sPMMD >= 40).sum();
        }
    }

    private CortePMMD() {
    }



    public static Resultado optimizar(TipoCortePMMD tipo, List<PiezaPMMD> piezasPMMD, Estrategia estrategia) {
        Comparator<PiezaPMMD> alto = Comparator.comparingDouble((PiezaPMMD p) -> Math.max(p.alto(), p.girable() ? p.ancho() : 0))
                .thenComparingDouble(PiezaPMMD::area).reversed();
        Comparator<PiezaPMMD> area = Comparator.comparingDouble(PiezaPMMD::area).reversed();
        Resultado mejor = null;

        for (boolean giroPMMD : new boolean[]{true, false}) {
            List<PiezaPMMD> listaPMMD = giroPMMD ? piezasPMMD : piezasPMMD.stream().map(p -> p.conGiro(false)).toList();
            String sufijoPMMD = giroPMMD ? "" : " sin giro";
            Resultado aPMMD = empacarPMMD(tipo, listaPMMD, alto, "por alto" + sufijoPMMD, estrategia);
            Resultado bPMMD = empacarPMMD(tipo, listaPMMD, area, "por área" + sufijoPMMD, estrategia);
            Resultado mPMMD = mejor(tipo, aPMMD, bPMMD);
            mejor = mejor == null ? mPMMD : mejor(tipo, mejor, mPMMD);
        }
        return mejor;
    }

    private static Resultado mejor(TipoCortePMMD tipo, Resultado aPMMD, Resultado bPMMD) {
        if (tipo == TipoCortePMMD.TELA) {
            return bPMMD.largoUsado() < aPMMD.largoUsado() - 0.01 ? bPMMD : aPMMD;
        }
        if (bPMMD.laminas() != aPMMD.laminas()) return bPMMD.laminas() < aPMMD.laminas() ? bPMMD : aPMMD;
        return bPMMD.sobranteReutilizable() > aPMMD.sobranteReutilizable() ? bPMMD : aPMMD;
    }

    private static Resultado empacarPMMD(TipoCortePMMD tipo, List<PiezaPMMD> piezasPMMD, Comparator<PiezaPMMD> orden, String nombreOrden,
                                     Estrategia estrategia) {
        double W = tipo.ancho();
        boolean rolloPMMD = tipo == TipoCortePMMD.TELA;
        double H = rolloPMMD ? 100_000 : tipo.alto();
        List<PiezaPMMD> unidadesPMMD = new ArrayList<>();
        for (PiezaPMMD p : piezasPMMD) for (int iPMMD = 0; iPMMD < p.cantidad(); iPMMD++) unidadesPMMD.add(p.conCantidad(1));
        unidadesPMMD.sort(orden);

        List<List<Rect>> libres = new ArrayList<>();
        List<Colocacion> col = new ArrayList<>();
        Map<PiezaPMMD, Integer> numerosPMMD = new LinkedHashMap<>();
        int numero = 0;
        double area = 0;
        for (PiezaPMMD p : unidadesPMMD) {
            numero++;
            area += p.ancho() * p.alto();
            Colocacion cPMMD = null;
            for (int sPMMD = 0; sPMMD < libres.size() && cPMMD == null; sPMMD++) cPMMD = colocarPMMD(libres.get(sPMMD), sPMMD, p, numero, estrategia, rolloPMMD);
            if (cPMMD == null) {
                List<Rect> nuevaPMMD = new ArrayList<>(List.of(new Rect(0, 0, W, H)));
                libres.add(nuevaPMMD);
                cPMMD = colocarPMMD(nuevaPMMD, libres.size() - 1, p, numero, estrategia, rolloPMMD);
                if (cPMMD == null) {
                    cPMMD = new Colocacion(numero, p, libres.size() - 1, 0, 0, Math.min(p.ancho(), W), Math.min(p.alto(), H), false);
                    nuevaPMMD.clear();
                }
            }
            col.add(cPMMD);
        }
        double largo = rolloPMMD ? col.stream().mapToDouble(cPMMD -> cPMMD.y() + cPMMD.h()).max().orElse(0) : 0;
        if (rolloPMMD) {

            for (List<Rect> lPMMD : libres) {
                lPMMD.replaceAll(r -> new Rect(r.x(), r.y(), r.w(), Math.max(0, Math.min(r.y() + r.h(), largo) - r.y())));
                lPMMD.removeIf(r -> r.h() <= 0.01);
            }
        }
        renumerarPMMD(col);
        return new Resultado(tipo, W, rolloPMMD ? largo : H, Math.max(rolloPMMD ? 1 : 0, libres.size()), col, libres, area, largo, nombreOrden, estrategia);
    }



    private static void renumerarPMMD(List<Colocacion> col) {
        col.sort(Comparator.comparing((Colocacion cPMMD) -> cPMMD.pieza().origen() == null ? "" : cPMMD.pieza().origen())
                .thenComparing(cPMMD -> cPMMD.pieza().nombre())
                .thenComparingInt(Colocacion::lamina).thenComparingDouble(Colocacion::y).thenComparingDouble(Colocacion::x));
        for (int iPMMD = 0; iPMMD < col.size(); iPMMD++) {
            Colocacion cPMMD = col.get(iPMMD);
            col.set(iPMMD, new Colocacion(iPMMD + 1, cPMMD.pieza(), cPMMD.lamina(), cPMMD.x(), cPMMD.y(), cPMMD.w(), cPMMD.h(), cPMMD.girada()));
        }
    }

    private static Colocacion colocarPMMD(List<Rect> libres, int lamina, PiezaPMMD p, int numero, Estrategia ePMMD, boolean rolloPMMD) {
        Rect mejor = null;
        boolean mejorGiro = false;
        double puntajePMMD = Double.MAX_VALUE, desempatePMMD = Double.MAX_VALUE;
        for (Rect r : libres) {
            for (int gPMMD = 0; gPMMD < (p.girable() ? 2 : 1); gPMMD++) {
                double w = gPMMD == 0 ? p.ancho() : p.alto();
                double h = gPMMD == 0 ? p.alto() : p.ancho();
                if (w > r.w() + 1e-6 || h > r.h() + 1e-6) continue;
                double s1PMMD, s2PMMD;
                if (rolloPMMD) {

                    s1PMMD = r.y() + h;
                    s2PMMD = Math.min(r.w() - w, r.h() - h);
                } else if (ePMMD == Estrategia.MEJOR_LADO) {
                    s1PMMD = Math.min(r.w() - w, r.h() - h);
                    s2PMMD = Math.max(r.w() - w, r.h() - h);
                } else {
                    s1PMMD = r.area() - w * h;
                    s2PMMD = Math.min(r.w() - w, r.h() - h);
                }
                if (s1PMMD < puntajePMMD - 1e-9 || (Math.abs(s1PMMD - puntajePMMD) < 1e-9 && s2PMMD < desempatePMMD)) {
                    puntajePMMD = s1PMMD;
                    desempatePMMD = s2PMMD;
                    mejor = r;
                    mejorGiro = gPMMD == 1;
                }
            }
        }
        if (mejor == null) return null;
        double w = mejorGiro ? p.alto() : p.ancho();
        double h = mejorGiro ? p.ancho() : p.alto();
        libres.remove(mejor);

        double restoW = mejor.w() - w, restoH = mejor.h() - h;
        if (restoW < restoH) {
            agregarPMMD(libres, new Rect(mejor.x() + w, mejor.y(), restoW, h));
            agregarPMMD(libres, new Rect(mejor.x(), mejor.y() + h, mejor.w(), restoH));
        } else {
            agregarPMMD(libres, new Rect(mejor.x() + w, mejor.y(), restoW, mejor.h()));
            agregarPMMD(libres, new Rect(mejor.x(), mejor.y() + h, w, restoH));
        }
        return new Colocacion(numero, p, lamina, mejor.x(), mejor.y(), w, h, mejorGiro);
    }

    private static void agregarPMMD(List<Rect> libres, Rect r) {
        if (r.w() > 0.5 && r.h() > 0.5) libres.add(r);
    }



    public static Resultado1D optimizar1D(List<PiezaPMMD> piezasPMMD, double largoTabla) {
        List<PiezaPMMD> unidadesPMMD = new ArrayList<>();
        for (PiezaPMMD p : piezasPMMD) for (int iPMMD = 0; iPMMD < p.cantidad(); iPMMD++) unidadesPMMD.add(p.conCantidad(1));
        unidadesPMMD.sort(Comparator.comparingDouble(PiezaPMMD::ancho).reversed());
        List<List<Colocacion>> tablas = new ArrayList<>();
        List<Double> usadoPMMD = new ArrayList<>();
        double total = 0;
        int n = 0;
        for (PiezaPMMD p : unidadesPMMD) {
            double len = Math.min(p.ancho(), largoTabla);
            total += len;
            n++;
            int elegida = -1;
            for (int t = 0; t < tablas.size(); t++) {
                double necesarioPMMD = len + (tablas.get(t).isEmpty() ? 0 : KERF);
                if (usadoPMMD.get(t) + necesarioPMMD <= largoTabla + 1e-6) {
                    elegida = t;
                    break;
                }
            }
            if (elegida < 0) {
                tablas.add(new ArrayList<>());
                usadoPMMD.add(0d);
                elegida = tablas.size() - 1;
            }
            double x = usadoPMMD.get(elegida) + (tablas.get(elegida).isEmpty() ? 0 : KERF);
            tablas.get(elegida).add(new Colocacion(n, p, elegida, x, 0, len, 0, false));
            usadoPMMD.set(elegida, x + len);
        }
        List<Tabla> r = new ArrayList<>();
        int numPMMD = 0;
        for (int t = 0; t < tablas.size(); t++) {
            List<Colocacion> renum = new ArrayList<>();
            for (Colocacion cPMMD : tablas.get(t)) {
                renum.add(new Colocacion(++numPMMD, cPMMD.pieza(), t, cPMMD.x(), 0, cPMMD.w(), 0, false));
            }
            r.add(new Tabla(t, renum, Math.max(0, largoTabla - usadoPMMD.get(t))));
        }
        return new Resultado1D(largoTabla, r, total);
    }
}
