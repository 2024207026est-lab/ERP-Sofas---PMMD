package pe.erp.sofas.ui.components;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;



public final class IcoPMMD {

    private IcoPMMD() {
    }

    public static Ikon ikon(String nombrePMMD) {
        for (String nPMMD : new String[]{nombrePMMD, "MDOAL_" + nombrePMMD}) {
            try {
                return Material2OutlinedAL.valueOf(nPMMD);
            } catch (IllegalArgumentException ignoredPMMD) {

            }
            try {
                return Material2OutlinedMZ.valueOf(nPMMD);
            } catch (IllegalArgumentException ignoredPMMD) {

            }
        }
        return Material2OutlinedAL.INFO;
    }

    public static FontIcon of(String nombrePMMD) {
        return of(nombrePMMD, 18);
    }

    public static FontIcon of(String nombrePMMD, int sizePMMD) {
        FontIcon iconPMMD = new FontIcon(ikon(nombrePMMD));
        iconPMMD.setIconSize(sizePMMD);
        iconPMMD.getStyleClass().add("ico");
        return iconPMMD;
    }

    public static FontIcon of(String nombrePMMD, int sizePMMD, String colorPMMD) {
        FontIcon iconPMMD = of(nombrePMMD, sizePMMD);
        iconPMMD.setStyle("-fx-icon-color: " + colorPMMD + ";");
        return iconPMMD;
    }
}
