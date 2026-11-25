package pe.erp.sofas.core;






public record IntentPMMD(
  String accion, 
    Object dato) {

    public static final String ACCION_NUEVO = "NEW";

    public static final String ACCION_ABRIR = "OPEN";

    public static final String ACCION_PRESELECCIONAR = "PRESELECT";

    public static final IntentPMMD NONE = new IntentPMMD(null, null);

    public static final IntentPMMD NEW = new IntentPMMD(ACCION_NUEVO, null);

    public static IntentPMMD abrir(Object dato) {
        return new IntentPMMD(ACCION_ABRIR, dato);
    }

    public static IntentPMMD preseleccionar(Object dato) {
        return new IntentPMMD(ACCION_PRESELECCIONAR, dato);
    }

    public boolean es(String accion) {
        return accion.equals(this.accion);
    }
}
