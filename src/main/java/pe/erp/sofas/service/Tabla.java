package pe.erp.sofas.service;

import java.util.ArrayList;
import java.util.List;



public record Tabla(
  String titulo, 
    String subtitulo, 
      List<String> columnas, 
        boolean[] derecha, 
          List<List<String>> filas,
                    
                    List<String> totales) {

    public static Tabla de(String titulo, String subtitulo, List<String> columnas, boolean[] derecha) {
        return new Tabla(titulo, subtitulo, columnas, derecha, new ArrayList<>(), null);
    }

    public Tabla fila(Object... valoresPMMD) {
        List<String> fPMMD = new ArrayList<>();
        for (Object vPMMD : valoresPMMD) fPMMD.add(vPMMD == null ? "—" : vPMMD.toString());
        filas.add(fPMMD);
        return this;
    }

    public Tabla conTotales(String... valoresPMMD) {
        return new Tabla(titulo, subtitulo, columnas, derecha, filas, List.of(valoresPMMD));
    }
}
