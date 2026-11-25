package pe.erp.sofas.core;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;






@Component
@Lazy
public class ThemeManagerPMMD {

    public static final List<String> DENSIDADES = List.of("Compacta", "Normal", "Amplia");

    private final BooleanProperty oscuroPMMD = new SimpleBooleanProperty(PrefsPMMD.bool(PrefsPMMD.TEMA_OSCURO, false));

    private String densidad = PrefsPMMD.str(PrefsPMMD.DENSIDAD, "Normal");

    private Scene scene;

    public void aplicar(Scene scene) {
        this.scene = scene;
        refrescarPMMD();
    }

    public BooleanProperty oscuroProperty() {
        return oscuroPMMD;
    }

    public boolean isOscuro() {
        return oscuroPMMD.get();
    }

    public void alternar() {
        setOscuro(!isOscuro());
    }

    public void setOscuro(boolean valorPMMD) {
        oscuroPMMD.set(valorPMMD);
        PrefsPMMD.setBool(PrefsPMMD.TEMA_OSCURO, valorPMMD);
        refrescarPMMD();
    }

    public String getDensidad() {
        return densidad;
    }

    public void setDensidad(String densidad) {
        this.densidad = densidad;
        PrefsPMMD.setStr(PrefsPMMD.DENSIDAD, densidad);
        refrescarPMMD();
    }

    private void refrescarPMMD() {
        Application.setUserAgentStylesheet(isOscuro()
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
        if (scene == null) return;
        scene.getStylesheets().setAll(
                css("/css/base.css"),
                css(isOscuro() ? "/css/theme-dark.css" : "/css/theme-light.css"));
        if (scene.getRoot() != null) {
            var clasesPMMD = scene.getRoot().getStyleClass();
            clasesPMMD.removeAll("density-compacta", "density-normal", "density-amplia", "dark", "light");
            clasesPMMD.add("density-" + densidad.toLowerCase());
            clasesPMMD.add(isOscuro() ? "dark" : "light");
        }
    }



    public void reaplicar() {
        refrescarPMMD();
    }

    private String css(String rutaPMMD) {
        return Objects.requireNonNull(getClass().getResource(rutaPMMD), rutaPMMD).toExternalForm();
    }
}
