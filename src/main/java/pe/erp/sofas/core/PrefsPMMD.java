package pe.erp.sofas.core;

import java.util.prefs.Preferences;



public final class PrefsPMMD {

    public static final String TEMA_OSCURO = "temaOscuro";

    public static final String DENSIDAD = "densidad";

    public static final String MENU_CONTRAIDO = "menuContraido";

    public static final String RECORDAR = "recordarSesion";

    public static final String ULTIMO_USUARIO = "ultimoUsuario";

    public static final String VISTA_MODELOS = "vistaModelos";

    public static final String VISTA_TABLERO = "vistaTablero";

    private static final Preferences NODOPMMD = Preferences.userRoot().node("pe/innovasur/sofas");

    private PrefsPMMD() {
    }

    public static boolean bool(String clavePMMD, boolean defectoPMMD) {
        return NODOPMMD.getBoolean(clavePMMD, defectoPMMD);
    }

    public static void setBool(String clavePMMD, boolean valorPMMD) {
        NODOPMMD.putBoolean(clavePMMD, valorPMMD);
    }

    public static String str(String clavePMMD, String defectoPMMD) {
        return NODOPMMD.get(clavePMMD, defectoPMMD);
    }

    public static void setStr(String clavePMMD, String valorPMMD) {
        NODOPMMD.put(clavePMMD, valorPMMD);
    }
}
