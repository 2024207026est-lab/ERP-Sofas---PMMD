package pe.erp.sofas.service.calculo;



public enum TipoCortePMMD {
    
    ESPUMA("Espuma", 200, 100, "plancha 200 × 100 cm"),
    
    TELA("Tela", 140, 0, "rollo de 140 cm de ancho"),
    
    MADERA("Madera", 300, 0, "tabla de 300 cm · kerf 5 mm"),
    
    TABLERO("Tablero", 244, 122, "plancha de triplay 244 × 122 cm");

    private final String nombre;

    private final double ancho;

    private final double alto;

    private final String formato;

    TipoCortePMMD(String nombre, double ancho, double alto, String formato) {
        this.nombre = nombre;
        this.ancho = ancho;
        this.alto = alto;
        this.formato = formato;
    }

    public String nombre() {
        return nombre;
    }

    public double ancho() {
        return ancho;
    }

    public double alto() {
        return alto;
    }

    public String formato() {
        return formato;
    }

    public static TipoCortePMMD paraCategoria(String categoriaMaterialPMMD, String codigoPMMD) {
        if (pe.erp.sofas.model.TipoTelaPMMD.porMaterial(codigoPMMD) != null) return TELA;
        return switch (categoriaMaterialPMMD) {
            case "Espuma" -> ESPUMA;
            case "Madera" -> MADERA;
            case "Tableros" -> TABLERO;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return nombre;
    }
}
