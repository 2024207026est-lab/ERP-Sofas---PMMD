package pe.erp.sofas.service.calculo;

import java.time.LocalDate;
import java.util.Set;






public final class CoberturaPMMD {

    private static final Set<String> ESTRUCTURAPMMD = Set.of("Pata floja", "Estructura quebrada", "Mecanismo trabado");

    public record Resultado(
      boolean cubierto, 
        int meses, 
          LocalDate vence, 
            String tipo) {

        public String texto() {
            return cubierto ? "En garantía hasta " + pe.erp.sofas.core.AppClockPMMD.fmt(vence) : "Fuera de garantía";
        }
    }

    private CoberturaPMMD() {
    }

    public static int meses(String fallaPMMD) {
        return ESTRUCTURAPMMD.contains(fallaPMMD) ? 12 : 6;
    }

    public static Resultado evaluar(String fallaPMMD, LocalDate fechaCompraPMMD, LocalDate fechaReclamoPMMD) {
        int meses = meses(fallaPMMD);
        if (fechaCompraPMMD == null) return new Resultado(false, meses, null, tipo(meses));
        LocalDate vence = fechaCompraPMMD.plusMonths(meses);
        LocalDate refPMMD = fechaReclamoPMMD == null ? pe.erp.sofas.core.AppClockPMMD.hoy() : fechaReclamoPMMD;
        return new Resultado(!refPMMD.isAfter(vence), meses, vence, tipo(meses));
    }

    private static String tipo(int meses) {
        return meses == 12 ? "Estructura · 12 meses" : "Tapiz y espuma · 6 meses";
    }
}
