package pe.erp.sofas.model;

public enum EstadoFichaPMMD {
    
    BORRADOR("Borrador"),
    
    APROBADA("Aprobada"),
    
    OBSOLETA("Obsoleta");

    private final String nombre;

    EstadoFichaPMMD(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
