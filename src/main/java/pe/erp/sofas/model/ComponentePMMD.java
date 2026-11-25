package pe.erp.sofas.model;



public enum ComponentePMMD {
    
    ESTRUCTURA("Estructura", "#A47148"),
    
    ASIENTO("Asiento", "#0EA5E9"),
    
    RESPALDO("Respaldo", "#6366F1"),
    
    BRAZOS("Brazos", "#8B5CF6"),
    
    TAPIZADO("Tapizado", "#EC4899"),
    
    COJINES("Cojines", "#F59E0B"),
    
    ENSAMBLAJE("Ensamblaje", "#64748B"),
    
    PATAS("Patas", "#C69A6D"),
    
    ACABADO("Acabado", "#14B8A6"),
    
    EMBALAJE("Embalaje", "#94A3B8"),
    
    MECANISMO("Mecanismo", "#2563EB");

    private final String nombre;

    private final String color;

    ComponentePMMD(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    public String nombre() {
        return nombre;
    }

    public String color() {
        return color;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
