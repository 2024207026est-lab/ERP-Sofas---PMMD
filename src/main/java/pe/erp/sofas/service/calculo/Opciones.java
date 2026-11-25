package pe.erp.sofas.service.calculo;

import pe.erp.sofas.model.LineaExtraPMMD;

import java.util.List;
import java.util.Map;






public record Opciones(
  MedidasPMMD medidas, 
    Map<String, String> sustituciones, 
       List<LineaExtraPMMD> extras,
                       
                       boolean incluirOpcionales, 
                         double recargoManoObraPct, 
                           double manoObraFija) {

    public static final Opciones NINGUNA = new Opciones(null, Map.of(), List.of(), false, 0, 0);

    public static Opciones medidas(MedidasPMMD m) {
        return new Opciones(m, Map.of(), List.of(), false, 0, 0);
    }

    public static Opciones sustituir(Map<String, String> sust) {
        return new Opciones(null, sust, List.of(), false, 0, 0);
    }

    public Opciones conMedidas(MedidasPMMD m) {
        return new Opciones(m, sustituciones, extras, incluirOpcionales, recargoManoObraPct, manoObraFija);
    }

    public Map<String, String> sust() {
        return sustituciones == null ? Map.of() : sustituciones;
    }

    public List<LineaExtraPMMD> ext() {
        return extras == null ? List.of() : extras;
    }
}
