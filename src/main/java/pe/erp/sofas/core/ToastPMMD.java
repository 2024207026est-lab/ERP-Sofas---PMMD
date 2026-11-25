package pe.erp.sofas.core;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.ui.components.IcoPMMD;
import pe.erp.sofas.ui.components.UIPMMD;



@Component
@Lazy
public class ToastPMMD {

    private static final Duration DURACIONPMMD = Duration.seconds(4);

    private VBox zona;

    public void registrar(VBox zona) {
        this.zona = zona;
    }

    public void exito(String mensajePMMD) {
        mostrarPMMD(mensajePMMD, "success", "CHECK_CIRCLE");
    }

    public void error(String mensajePMMD) {
        mostrarPMMD(mensajePMMD, "error", "ERROR");
    }

    public void advertencia(String mensajePMMD) {
        mostrarPMMD(mensajePMMD, "warn", "WARNING");
    }

    public void info(String mensajePMMD) {
        mostrarPMMD(mensajePMMD, "info", "INFO");
    }



    public boolean intentar(Runnable accionPMMD) {
        try {
            accionPMMD.run();
            return true;
        } catch (NegocioExceptionPMMD ePMMD) {
            error(ePMMD.getMessage());
        } catch (RuntimeException ePMMD) {
            Throwable causa = ePMMD;
            while (causa.getCause() != null && !(causa instanceof NegocioExceptionPMMD)) causa = causa.getCause();
            if (causa instanceof NegocioExceptionPMMD ne) {
                error(ne.getMessage());
            } else {
                org.slf4j.LoggerFactory.getLogger(ToastPMMD.class).error("Error inesperado", ePMMD);
                error("Ocurrió un error inesperado: " + ePMMD.getMessage());
            }
        }
        return false;
    }

    private void mostrarPMMD(String mensajePMMD, String tipoPMMD, String iconoPMMD) {
        if (zona == null) return;
        StackPane ic = new StackPane(IcoPMMD.of(iconoPMMD, 18));
        ic.getStyleClass().add("toast-icon");
        Label texto = UIPMMD.wrap(mensajePMMD, "toast-text");
        texto.setMaxWidth(300);
        var cerrar = UIPMMD.iconButton("CLOSE", "Cerrar", "btn-ghost", "btn-xs");
        HBox t = new HBox(12, ic, texto, UIPMMD.spacer(), cerrar);
        t.setAlignment(Pos.CENTER_LEFT);
        t.getStyleClass().addAll("toast", "toast-" + tipoPMMD);
        t.setMaxWidth(380);
        t.setPrefWidth(380);
        zona.getChildren().add(t);

        t.setOpacity(0);
        t.setTranslateX(24);
        FadeTransition in = new FadeTransition(Duration.millis(220), t);
        in.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), t);
        slide.setToX(0);
        new ParallelTransition(in, slide).play();

        Runnable quitarPMMD = () -> {
            FadeTransition outPMMD = new FadeTransition(Duration.millis(200), t);
            outPMMD.setToValue(0);
            outPMMD.setOnFinished(ePMMD -> zona.getChildren().remove(t));
            outPMMD.play();
        };
        cerrar.setOnAction(ePMMD -> quitarPMMD.run());
        PauseTransition esperaPMMD = new PauseTransition(DURACIONPMMD);
        esperaPMMD.setOnFinished(ePMMD -> quitarPMMD.run());
        esperaPMMD.play();
        if (zona.getChildren().size() > 4) zona.getChildren().remove(0);
    }
}
