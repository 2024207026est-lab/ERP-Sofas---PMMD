package pe.erp.sofas.service.calculo;

import pe.erp.sofas.model.*;

import java.util.*;







public final class ExplosionPMMD {
    


    public record Linea(
      MaterialPMMD material, 
        ComponentePMMD componente, 
          double base, 
            double factor, 
              double neta,
                        
                        double mermaPct, 
                          double bruta, 
                            double costo, 
                              boolean opcional, 
                                boolean extra,
                        
                        String origen) {
    }



    public record PorMaterial(
      MaterialPMMD material, 
        double cantidad, 
          double costo) {
    }

    public record Resultado(
      FichaTecnicaPMMD ficha, 
        int cantidad, 
          MedidasPMMD medidas, 
            List<Linea> lineas,
                            
                            double costoMateriales, 
                              double horasMO, 
                                double costoMO, 
                                  double indirectosPct,
                            
                            double indirectos) {

        public double costoEstandar() {
            return costoMateriales + costoMO + indirectos;
        }

        public double costoUnitario() {
            return cantidad == 0 ? 0 : costoEstandar() / cantidad;
        }



        public List<PorMaterial> porMaterial() {
            Map<String, double[]> accPMMD = new LinkedHashMap<>();
            Map<String, MaterialPMMD> matsPMMD = new HashMap<>();
            for (Linea lPMMD : lineas) {
                String cPMMD = lPMMD.material().getCodigo();
                matsPMMD.put(cPMMD, lPMMD.material());
                double[] aPMMD = accPMMD.computeIfAbsent(cPMMD, kPMMD -> new double[2]);
                aPMMD[0] += lPMMD.bruta();
                aPMMD[1] += lPMMD.costo();
            }
            List<PorMaterial> rPMMD = new ArrayList<>();
            accPMMD.forEach((cPMMD, aPMMD) -> {
                MaterialPMMD m = matsPMMD.get(cPMMD);
                double cant = m.isEntera() ? Math.ceil(aPMMD[0] - 1e-9) : aPMMD[0];
                rPMMD.add(new PorMaterial(m, cant, aPMMD[1]));
            });
            return rPMMD;
        }

        public Map<ComponentePMMD, Double> costoPorComponente() {
            Map<ComponentePMMD, Double> m = new EnumMap<>(ComponentePMMD.class);
            for (Linea lPMMD : lineas) m.merge(lPMMD.componente(), lPMMD.costo(), Double::sum);
            return m;
        }
    }



    public static final String QUITAR = "-";

    private ExplosionPMMD() {
    }

    public static Resultado explotar(FichaTecnicaPMMD ficha, int cantidad, Opciones opPMMD, CatalogoPMMD catPMMD) {
        if (opPMMD == null) opPMMD = Opciones.NINGUNA;
        if (Boolean.TRUE.equals(ficha.getCompuesta())) return compuestaPMMD(ficha, cantidad, opPMMD, catPMMD);

        MedidasPMMD base = MedidasPMMD.de(ficha);
        MedidasPMMD pedidas = opPMMD.medidas() == null ? base : opPMMD.medidas();
        List<Linea> lineas = new ArrayList<>();
        for (LineaBOMPMMD lPMMD : ficha.getLineas()) {
            if (Boolean.TRUE.equals(lPMMD.getOpcional()) && !opPMMD.incluirOpcionales()) continue;
            if (QUITAR.equals(opPMMD.sust().get(lPMMD.getMaterial().getCodigo()))) continue;
            MaterialPMMD m = sustituirPMMD(lPMMD.getMaterial(), opPMMD, catPMMD);
            double factor = FormulaPMMD.factor(lPMMD.getDependencia(), pedidas, base);
            double neta = lPMMD.getCantidad() * factor * cantidad;
            double bruta = neta * (1 + nzPMMD(lPMMD.getMermaPct()) / 100.0);
            if (m.isEntera()) bruta = Math.ceil(bruta - 1e-9);
            lineas.add(new Linea(m, lPMMD.getComponente(), lPMMD.getCantidad(), factor, neta, nzPMMD(lPMMD.getMermaPct()), bruta,
                    bruta * m.getCosto(), Boolean.TRUE.equals(lPMMD.getOpcional()), false, ficha.getEtiqueta()));
        }
        for (LineaExtraPMMD ePMMD : opPMMD.ext()) {
            MaterialPMMD m = catPMMD.material(ePMMD.getMaterialCodigo());
            if (m == null) continue;
            double cant = ePMMD.getCantidad() * cantidad;
            if (m.isEntera()) cant = Math.ceil(cant - 1e-9);
            ComponentePMMD comp = componente(ePMMD.getComponente());
            lineas.add(new Linea(m, comp, ePMMD.getCantidad(), 1, cant, 0, cant, cant * m.getCosto(), false, true, "Extra"));
        }
        double materiales = lineas.stream().mapToDouble(Linea::costo).sum();
        double horas = ficha.getHorasMO() * FormulaPMMD.factorManoObra(pedidas, base) * cantidad;
        double mo = costoManoObra(ficha) * FormulaPMMD.factorManoObra(pedidas, base) * cantidad;
        mo = mo * (1 + opPMMD.recargoManoObraPct() / 100.0) + opPMMD.manoObraFija() * cantidad;
        horas = horas * (1 + opPMMD.recargoManoObraPct() / 100.0);
        double pct = nzPMMD(ficha.getIndirectosPct());
        return new Resultado(ficha, cantidad, pedidas, lineas, materiales, horas, mo, pct, (materiales + mo) * pct / 100.0);
    }



    private static Resultado compuestaPMMD(FichaTecnicaPMMD ficha, int cantidad, Opciones opPMMD, CatalogoPMMD catPMMD) {
        List<Linea> lineas = new ArrayList<>();
        double horas = 0, mo = 0;
        Opciones hijaOpPMMD = opPMMD.conMedidas(null);
        Opciones sinExtrasPMMD = new Opciones(null, hijaOpPMMD.sust(), List.of(), hijaOpPMMD.incluirOpcionales(), 0, 0);
        for (String codigoPMMD : ficha.getHijos()) {
            FichaTecnicaPMMD hijaPMMD = catPMMD.fichaAprobada(codigoPMMD);
            if (hijaPMMD == null) continue;
            Resultado rPMMD = explotar(hijaPMMD, cantidad, sinExtrasPMMD, catPMMD);
            lineas.addAll(rPMMD.lineas());
            horas += rPMMD.horasMO();
            mo += rPMMD.costoMO();
        }
        for (LineaExtraPMMD ePMMD : opPMMD.ext()) {
            MaterialPMMD m = catPMMD.material(ePMMD.getMaterialCodigo());
            if (m == null) continue;
            double cant = ePMMD.getCantidad() * cantidad;
            if (m.isEntera()) cant = Math.ceil(cant - 1e-9);
            lineas.add(new Linea(m, componente(ePMMD.getComponente()), ePMMD.getCantidad(), 1, cant, 0, cant, cant * m.getCosto(), false, true, "Extra"));
        }
        mo = mo * (1 + opPMMD.recargoManoObraPct() / 100.0) + opPMMD.manoObraFija() * cantidad;
        double materiales = lineas.stream().mapToDouble(Linea::costo).sum();
        double pct = nzPMMD(ficha.getIndirectosPct());
        return new Resultado(ficha, cantidad, null, lineas, materiales, horas, mo, pct, (materiales + mo) * pct / 100.0);
    }

    public static double costoManoObra(FichaTecnicaPMMD fPMMD) {
        return fPMMD.getOperaciones().stream().mapToDouble(OperacionMOPMMD::getCosto).sum();
    }

    private static MaterialPMMD sustituirPMMD(MaterialPMMD originalPMMD, Opciones opPMMD, CatalogoPMMD catPMMD) {
        String destinoPMMD = opPMMD.sust().get(originalPMMD.getCodigo());
        if (destinoPMMD == null || destinoPMMD.equals(originalPMMD.getCodigo())) return originalPMMD;
        MaterialPMMD m = catPMMD.material(destinoPMMD);
        return m == null ? originalPMMD : m;
    }

    private static ComponentePMMD componente(String nombrePMMD) {
        if (nombrePMMD == null) return ComponentePMMD.ENSAMBLAJE;
        try {
            return ComponentePMMD.valueOf(nombrePMMD);
        } catch (IllegalArgumentException ePMMD) {
            return ComponentePMMD.ENSAMBLAJE;
        }
    }

    private static double nzPMMD(Double dPMMD) {
        return dPMMD == null ? 0 : dPMMD;
    }
}
