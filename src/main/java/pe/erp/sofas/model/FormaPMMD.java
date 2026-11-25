package pe.erp.sofas.model;



public enum FormaPMMD {
    
    MODERNO("Sofá Moderno"),
    
    SECCIONAL_L("Seccional L"),
    
    SILLON("Sillón"),
    
    JUEGO("Juego de sala"),
    
    CHESTERFIELD("Chesterfield"),
    
    SOFA_CAMA("Sofá cama"),
    
    RECLINABLE("Reclinable"),
    
    ESCANDINAVO("Escandinavo"),
    
    PUFF("Puff"),
    
    SECCIONAL_U("Seccional U");

    private final String nombre;

    FormaPMMD(String nombre) {
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
