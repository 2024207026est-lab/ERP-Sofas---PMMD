package pe.erp.sofas.ui.three.parts.families;






public enum SofaFamiliaPMMD {




    
    MODERNO("Sofá Moderno", "SF-001", true),





    
    SECCIONAL_L_CHAISE("Seccional L con chaise", "SF-005", true),






    
    SECCIONAL_L_ESQUINERO("Seccional L con esquinero", "SF-012", false),






    
    SECCIONAL_U("Seccional U", "SF-011", false),






    
    MODULAR("Sofá Modular", "SF-013", false),





    
    CHESTERFIELD("Chesterfield", "SF-006", true),





    
    ESCANDINAVO("Escandinavo", "SF-009", true),





    
    SOFA_CAMA("Sofá cama", "SF-007", true),





    
    RECLINABLE("Reclinable", "SF-008", true),






    
    RETRO("Retro / Mid-century", "SF-014", false),






    
    CLOUD("Cloud / Modular profundo", "SF-015", false),






    
    TUXEDO("Tuxedo", "SF-016", false),





    
    SILLON("Sillón", "SF-003", true);

    private final String nombre;

    private final String codigoProducto;

    private final boolean yaExistia;

    SofaFamiliaPMMD(String nombre, String codigoProducto, boolean yaExistia) {
        this.nombre = nombre;
        this.codigoProducto = codigoProducto;
        this.yaExistia = yaExistia;
    }

    public String nombre() {
        return nombre;
    }

    public String codigoProducto() {
        return codigoProducto;
    }

    public boolean yaExistia() {
        return yaExistia;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
