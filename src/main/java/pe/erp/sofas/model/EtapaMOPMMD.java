package pe.erp.sofas.model;



public enum EtapaMOPMMD {
    
    CORTE("Corte", 8),
    
    CARPINTERIA("Carpintería", 8),
    
    COSTURA("Costura", 8),
    
    TAPIZADO("Tapizado", 9),
    
    ACABADO("Acabado", 7),
    
    CONTROL_CALIDAD("Control de calidad", 7);

    private final String nombre;

    private final double costoHora;

    EtapaMOPMMD(String nombre, double costoHora) {
        this.nombre = nombre;
        this.costoHora = costoHora;
    }

    public String nombre() {
        return nombre;
    }

    public double costoHora() {
        return costoHora;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
