package pe.erp.sofas.ui.three.parts;






public enum PartLayoutPMMD {




    
    LINEAL("Sofá lineal"),





    
    L_CHAISE_IZQ("L con chaise izquierda"),
    
    L_CHAISE_DER("L con chaise derecha"),






    
    L_ESQUINERO_IZQ("L con esquinero izquierdo"),
    
    L_ESQUINERO_DER("L con esquinero derecho"),





    
    U_ESQUINEROS("Forma U"),





    
    MODULAR("Constructor modular");

    private final String nombre;

    PartLayoutPMMD(String nombre) {
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
