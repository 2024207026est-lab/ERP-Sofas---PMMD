package pe.erp.sofas.model;



public enum RolPMMD {
    
    GERENTE("Gerente"),
    
    ADMINISTRADOR("Administrador"),
    
    VENDEDOR("Vendedor"),
    
    SUPERVISOR("Supervisor de taller"),
    
    CARPINTERO("Carpintero"),
    
    COSTURERA("Costurera"),
    
    TAPICERO("Tapicero"),
    
    ALMACENERO("Almacenero");

    private final String nombre;

    RolPMMD(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }



    public boolean autoriza() {
        return this == GERENTE || this == SUPERVISOR || this == ADMINISTRADOR;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
