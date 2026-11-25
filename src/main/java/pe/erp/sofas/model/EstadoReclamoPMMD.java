package pe.erp.sofas.model;

public enum EstadoReclamoPMMD {
    
    ABIERTO("Abierto"),
    
    EN_DIAGNOSTICO("En diagnóstico"),
    
    EN_REPARACION("En reparación"),
    
    RESUELTO("Resuelto"),
    
    RECHAZADO("Rechazado");

    private final String nombre;

    EstadoReclamoPMMD(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }

    public boolean cerrado() {
        return this == RESUELTO || this == RECHAZADO;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
