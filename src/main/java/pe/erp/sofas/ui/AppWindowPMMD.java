package pe.erp.sofas.ui;

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.ThemeManagerPMMD;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;



@Component
@Lazy
@RequiredArgsConstructor
@Slf4j
public class AppWindowPMMD {

    private static final String[] FUENTESPMMD = {
            "Inter-Regular", "Inter-Medium", "Inter-SemiBold", "Inter-Bold", "Inter-ExtraBold",
            "JetBrainsMono-Regular", "JetBrainsMono-Medium", "JetBrainsMono-Bold"};

    public record Cargado<T>(
      Parent nodo, 
        T controlador) {
    }

    private final ApplicationContext contextPMMD;

    private final ThemeManagerPMMD themeManagerPMMD;

    private Stage stage;

    private Scene scene;

    private HostServices hostServices;

    public void iniciar(Stage stage, HostServices hostServices) {
        this.stage = stage;
        this.hostServices = hostServices;
        cargarFuentesPMMD();

        Rectangle2D pantallaPMMD = Screen.getPrimary().getVisualBounds();
        double ancho = Math.min(1440, pantallaPMMD.getWidth() - 40);
        double alto = Math.min(900, pantallaPMMD.getHeight() - 40);
        scene = new Scene(cargar("/fxml/login.fxml").nodo(), ancho, alto);
        themeManagerPMMD.aplicar(scene);

        stage.setTitle("SISTEMA ERP SOFAS · ERP de sofás · Juliaca");
        stage.setScene(scene);
        stage.setMinWidth(1180);
        stage.setMinHeight(720);
        try (InputStream icono = getClass().getResourceAsStream("/images/icono.png")) {
            if (icono != null) stage.getIcons().add(new Image(icono));
        } catch (IOException ignoredPMMD) {

        }
        stage.show();

        String tourPMMD = System.getProperty("innova.tour");
        if (tourPMMD != null) {
            contextPMMD.getBean(DemoTourPMMD.class).ejecutar(tourPMMD);
        }
    }

    public void mostrarLogin() {
        scene.setRoot(cargar("/fxml/login.fxml").nodo());
        themeManagerPMMD.reaplicar();
    }

    public void mostrarShell(String moduloInicioPMMD) {
        Cargado<ShellControllerPMMD> shellPMMD = cargar("/fxml/shell.fxml");
        scene.setRoot(shellPMMD.nodo());
        themeManagerPMMD.reaplicar();
        shellPMMD.controlador().arrancar(moduloInicioPMMD);
    }

    public <T> Cargado<T> cargar(String rutaPMMD) {
        FXMLLoader loaderPMMD = new FXMLLoader(getClass().getResource(rutaPMMD));
        loaderPMMD.setControllerFactory(contextPMMD::getBean);

        loaderPMMD.setClassLoader(getClass().getClassLoader());
        try {
            Parent nodo = loaderPMMD.load();
            return new Cargado<>(nodo, loaderPMMD.getController());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar " + rutaPMMD, e);
        }
    }

    public Stage stage() {
        return stage;
    }

    public Scene scene() {
        return scene;
    }

    public HostServices hostServices() {
        return hostServices;
    }

    private void cargarFuentesPMMD() {
        for (String fPMMD : FUENTESPMMD) {
            try (InputStream inPMMD = getClass().getResourceAsStream("/fonts/" + fPMMD + ".ttf")) {
                Font fontPMMD = inPMMD == null ? null : Font.loadFont(inPMMD, 13);
                if (fontPMMD != null) log.debug("Fuente {} → familia '{}'", fPMMD, fontPMMD.getFamily());
            } catch (IOException e) {
                log.warn("No se pudo cargar la fuente {}", fPMMD);
            }
        }
    }
}
