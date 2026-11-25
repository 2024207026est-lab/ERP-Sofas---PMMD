package pe.erp.sofas.model;






public enum TipoTelaPMMD {
    
    CHENILLE("Chenille", 28, 0, 30000, true, false, false, "MP-003", "PR-03", 8),
    
    LINO("Lino", 34, 8, 20000, false, false, false, "MP-006", "PR-02", 8),
    
    CUERO("Cuero sintético", 35, 12, 100000, true, true, true, "MP-004", "PR-03", 40),
    
    TERCIOPELO("Terciopelo", 42, 15, 40000, false, false, false, "MP-007", "PR-02", 25),
    
    BOUCLE("Bouclé", 48, 20, 35000, false, false, false, "MP-008", "PR-02", 8),
    
    MICROFIBRA("Microfibra antimascotas", 32, 6, 60000, true, true, true, "MP-005", "PR-03", 8);

    private final String nombre;

    private final double costoMetro;

    private final double recargoPct;

    private final int martindale;

    private final boolean antimanchas;

    private final boolean mascotas;

    private final boolean impermeable;

    private final String materialCodigo;

    private final String proveedorCodigo;

    private final double brillo;

    TipoTelaPMMD(String nombre, double costoMetro, double recargoPct, int martindale, boolean antimanchas,
             boolean mascotas, boolean impermeable, String materialCodigo, String proveedorCodigo, double brillo) {
        this.nombre = nombre;
        this.costoMetro = costoMetro;
        this.recargoPct = recargoPct;
        this.martindale = martindale;
        this.antimanchas = antimanchas;
        this.mascotas = mascotas;
        this.impermeable = impermeable;
        this.materialCodigo = materialCodigo;
        this.proveedorCodigo = proveedorCodigo;
        this.brillo = brillo;
    }

    public String nombre() {
        return nombre;
    }

    public double costoMetro() {
        return costoMetro;
    }



    public double recargoPct() {
        return recargoPct;
    }

    public int martindale() {
        return martindale;
    }

    public boolean antimanchas() {
        return antimanchas;
    }

    public boolean mascotas() {
        return mascotas;
    }

    public boolean impermeable() {
        return impermeable;
    }

    public String materialCodigo() {
        return materialCodigo;
    }

    public String proveedorCodigo() {
        return proveedorCodigo;
    }



    public double brillo() {
        return brillo;
    }



    public boolean permiteGiro() {
        return this != TERCIOPELO;
    }

    public static TipoTelaPMMD porMaterial(String codigoMaterialPMMD) {
        for (TipoTelaPMMD tPMMD : values()) {
            if (tPMMD.materialCodigo.equals(codigoMaterialPMMD)) return tPMMD;
        }
        return null;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
