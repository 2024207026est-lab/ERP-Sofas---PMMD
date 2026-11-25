package pe.erp.sofas.model;

import java.util.LinkedHashMap;
import java.util.Map;



public record ConfigSofaPMMD(
  Modelo modelo, 
    int cuerpos, 
      Chaise chaise, 
        int profundidad, 
          Long telaId, 
            Brazo brazo,
                         
                         Patas patas, 
                           AcabadoMadera acabado, 
                             Espuma espuma, 
                               int cojines, 
                                 Long telaCojinesId,
                         
                         Mecanismo mecanismo) {

    public enum Modelo {
        
        MODERNO("Sofá Moderno", "SF-001", FormaPMMD.MODERNO),
        
        SECCIONAL_L("Seccional L", "SF-005", FormaPMMD.SECCIONAL_L),
        
        SILLON("Sillón", "SF-003", FormaPMMD.SILLON),
        
        CHESTERFIELD("Chesterfield", "SF-006", FormaPMMD.CHESTERFIELD),
        
        SOFA_CAMA("Sofá cama", "SF-007", FormaPMMD.SOFA_CAMA),
        
        ESCANDINAVO("Escandinavo", "SF-009", FormaPMMD.ESCANDINAVO);

        private final String nombre;

        private final String producto;

        private final FormaPMMD forma;

        Modelo(String nombre, String producto, FormaPMMD forma) {
            this.nombre = nombre;
            this.producto = producto;
            this.forma = forma;
        }

        public String nombre() {
            return nombre;
        }



        public String producto() {
            return producto;
        }

        public FormaPMMD forma() {
            return forma;
        }
    }

    public enum Chaise {
        
        NINGUNA("Sin chaise"), 
          IZQUIERDA("Izquierda"), 
           DERECHA("Derecha");

        private final String nombre;

        Chaise(String nombre) {
            this.nombre = nombre;
        }

        public String nombre() {
            return nombre;
        }
    }

    public enum Brazo {
        
        RECTO("Recto"), 
         REDONDEADO("Redondeado"), 
          DELGADO("Delgado");

        private final String nombre;

        Brazo(String nombre) {
            this.nombre = nombre;
        }

        public String nombre() {
            return nombre;
        }
    }

    public enum Patas {
        
        MADERA("Madera cónica", null, 0, false),
        
        METAL_NEGRO("Metal negro", "MP-022", 14, false),
        
        METAL_DORADO("Metal dorado", "MP-023", 20, false),
        
        ZOCALO("Zócalo oculto", "MP-035", 0, true);

        private final String nombre;

        private final String material;

        private final double recargo;

        private final boolean bajoPedido;

        Patas(String nombre, String material, double recargo, boolean bajoPedido) {
            this.nombre = nombre;
            this.material = material;
            this.recargo = recargo;
            this.bajoPedido = bajoPedido;
        }

        public String nombre() {
            return nombre;
        }

        public String material() {
            return material;
        }



        public double recargo() {
            return recargo;
        }

        public boolean bajoPedido() {
            return bajoPedido;
        }
    }

    public enum AcabadoMadera {
        
        NATURAL("Natural", "#C69A6D"), 
          NOGAL("Nogal", "#7A4E2B"), 
            WENGUE("Wengué", "#3B2A20");

        private final String nombre;

        private final String hex;

        AcabadoMadera(String nombre, String hex) {
            this.nombre = nombre;
            this.hex = hex;
        }

        public String nombre() {
            return nombre;
        }

        public String hex() {
            return hex;
        }
    }

    public enum Espuma {
        
        D23("Espuma D23 estándar", null, false),
        
        D30("D30 alta densidad", "MP-020", false),
        
        VISCO("Viscoelástica", "MP-034", true);

        private final String nombre;

        private final String material;

        private final boolean bajoPedido;

        Espuma(String nombre, String material, boolean bajoPedido) {
            this.nombre = nombre;
            this.material = material;
            this.bajoPedido = bajoPedido;
        }

        public String nombre() {
            return nombre;
        }

        public String material() {
            return material;
        }

        public boolean bajoPedido() {
            return bajoPedido;
        }
    }

    public enum Mecanismo {
        
        FIJO("Fijo", false),
        
        RECLINABLE_MANUAL("Reclinable manual", false),
        
        RECLINABLE_ELECTRICO("Reclinable eléctrico", true),
        
        CAMA("Cama plegable", false);

        private final String nombre;

        private final boolean bajoPedido;

        Mecanismo(String nombre, boolean bajoPedido) {
            this.nombre = nombre;
            this.bajoPedido = bajoPedido;
        }

        public String nombre() {
            return nombre;
        }

        public boolean bajoPedido() {
            return bajoPedido;
        }
    }



    public static int largoPara(int cuerpos) {
        return switch (cuerpos) {
            case 1 -> 95;
            case 2 -> 160;
            case 3 -> 210;
            default -> 260;
        };
    }

    public int largo() {
        return largoPara(cuerpos);
    }

    public int alto() {
        return modelo == Modelo.CHESTERFIELD ? 78 : 85;
    }

    public boolean conChaise() {
        return chaise != null && chaise != Chaise.NINGUNA;
    }

    public ConfigSofaPMMD con(Modelo m) {
        return new ConfigSofaPMMD(m, cuerpos, chaise, profundidad, telaId, brazo, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofaPMMD conCuerpos(int n) {
        return new ConfigSofaPMMD(modelo, n, chaise, profundidad, telaId, brazo, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofaPMMD conChaise(Chaise c) {
        return new ConfigSofaPMMD(modelo, cuerpos, c, profundidad, telaId, brazo, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofaPMMD conProfundidad(int p) {
        return new ConfigSofaPMMD(modelo, cuerpos, chaise, p, telaId, brazo, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofaPMMD conTela(Long t) {
        return new ConfigSofaPMMD(modelo, cuerpos, chaise, profundidad, t, brazo, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofaPMMD conBrazo(Brazo b) {
        return new ConfigSofaPMMD(modelo, cuerpos, chaise, profundidad, telaId, b, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofaPMMD conPatas(Patas p) {
        return new ConfigSofaPMMD(modelo, cuerpos, chaise, profundidad, telaId, brazo, p, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofaPMMD conAcabado(AcabadoMadera a) {
        return new ConfigSofaPMMD(modelo, cuerpos, chaise, profundidad, telaId, brazo, patas, a, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofaPMMD conEspuma(Espuma e) {
        return new ConfigSofaPMMD(modelo, cuerpos, chaise, profundidad, telaId, brazo, patas, acabado, e, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofaPMMD conCojines(int n, Long tela) {
        return new ConfigSofaPMMD(modelo, cuerpos, chaise, profundidad, telaId, brazo, patas, acabado, espuma, n, tela, mecanismo);
    }

    public ConfigSofaPMMD conMecanismo(Mecanismo m) {
        return new ConfigSofaPMMD(modelo, cuerpos, chaise, profundidad, telaId, brazo, patas, acabado, espuma, cojines, telaCojinesId, m);
    }

    public String serializar() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("modelo", modelo.name());
        m.put("cuerpos", String.valueOf(cuerpos));
        m.put("chaise", chaise.name());
        m.put("prof", String.valueOf(profundidad));
        m.put("tela", telaId == null ? "" : telaId.toString());
        m.put("brazo", brazo.name());
        m.put("patas", patas.name());
        m.put("acabado", acabado.name());
        m.put("espuma", espuma.name());
        m.put("cojines", String.valueOf(cojines));
        m.put("telaCojines", telaCojinesId == null ? "" : telaCojinesId.toString());
        m.put("mec", mecanismo.name());
        StringBuilder sbPMMD = new StringBuilder();
        m.forEach((kPMMD, vPMMD) -> sbPMMD.append(kPMMD).append('=').append(vPMMD).append(';'));
        return sbPMMD.toString();
    }

    public static ConfigSofaPMMD leer(String sPMMD) {
        if (sPMMD == null || sPMMD.isBlank()) return null;
        Map<String, String> m = new LinkedHashMap<>();
        for (String parPMMD : sPMMD.split(";")) {
            int iPMMD = parPMMD.indexOf('=');
            if (iPMMD > 0) m.put(parPMMD.substring(0, iPMMD), parPMMD.substring(iPMMD + 1));
        }
        try {
            return new ConfigSofaPMMD(Modelo.valueOf(m.get("modelo")), Integer.parseInt(m.get("cuerpos")),
                    Chaise.valueOf(m.get("chaise")), Integer.parseInt(m.get("prof")), idPMMD(m.get("tela")),
                    Brazo.valueOf(m.get("brazo")), Patas.valueOf(m.get("patas")), AcabadoMadera.valueOf(m.get("acabado")),
                    Espuma.valueOf(m.get("espuma")), Integer.parseInt(m.get("cojines")), idPMMD(m.get("telaCojines")),
                    Mecanismo.valueOf(m.get("mec")));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Long idPMMD(String sPMMD) {
        return sPMMD == null || sPMMD.isBlank() ? null : Long.valueOf(sPMMD);
    }
}
