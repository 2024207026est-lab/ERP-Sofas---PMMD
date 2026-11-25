package pe.erp.sofas.model;



public enum EstadoCompraPMMD {
    
    BORRADOR("Borrador"),
    
    SOLICITADA("Solicitada"),
    
    APROBADA("Aprobada"),
    
    RECIBIDA("Recibida"),
    
    CANCELADA("Cancelada");

    private final String nombre;

    EstadoCompraPMMD(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }



    public boolean enTransito() {
        return this == SOLICITADA || this == APROBADA;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
