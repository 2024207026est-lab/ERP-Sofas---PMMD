package pe.erp.sofas.model;






public enum DependenciaPMMD {
    
    F("Fija", "1"),
    
    L("× largo", "L / L0"),
    
    LF("× largo·fondo", "(L * F) / (L0 * F0)"),
    
    LA("× largo·alto", "(L * A) / (L0 * A0)"),
    
    T("× superficie tapizada", "(L * (F + A)) / (L0 * (F0 + A0))"),
    
    N("× cuerpos", "n / n0"),
    
    P("Patas 4/6 según largo", "patas(L) / patas(L0)");

    private final String nombre;

    private final String expresion;

    DependenciaPMMD(String nombre, String expresion) {
        this.nombre = nombre;
        this.expresion = expresion;
    }

    public String nombre() {
        return nombre;
    }

    public String expresion() {
        return expresion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
