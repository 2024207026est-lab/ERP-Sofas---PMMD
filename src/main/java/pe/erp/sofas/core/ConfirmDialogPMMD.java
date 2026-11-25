package pe.erp.sofas.core;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.ui.components.IcoPMMD;
import pe.erp.sofas.ui.components.UIPMMD;



@Component
@Lazy
@RequiredArgsConstructor
public class ConfirmDialogPMMD {

    public enum Tipo { 
        PELIGRO, 
         AVISO, 
          INFO }

    private final OverlaysPMMD overlaysPMMD;

    public void mostrar(String tituloPMMD, String mensajePMMD, String accionPMMD, Runnable alConfirmarPMMD) {
        mostrar(Tipo.PELIGRO, tituloPMMD, mensajePMMD, accionPMMD, alConfirmarPMMD);
    }

    public void mostrar(Tipo tipoPMMD, String tituloPMMD, String mensajePMMD, String accionPMMD, Runnable alConfirmarPMMD) {
        String iconoPMMD = switch (tipoPMMD) {
            case PELIGRO -> "WARNING";
            case AVISO -> "REPORT_PROBLEM";
            case INFO -> "HELP_OUTLINE";
        };
        StackPane ic = new StackPane(IcoPMMD.of(iconoPMMD, 26));
        ic.getStyleClass().addAll("confirm-icon", "confirm-" + tipoPMMD.name().toLowerCase());
        VBox cuerpoPMMD = new VBox(12, ic, UIPMMD.label(tituloPMMD, "confirm-title"), UIPMMD.wrap(mensajePMMD, "confirm-text"));
        cuerpoPMMD.setAlignment(Pos.CENTER);
        cuerpoPMMD.getStyleClass().add("confirm-body");

        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = tipoPMMD == Tipo.PELIGRO ? UIPMMD.danger(accionPMMD, null) : UIPMMD.primary(accionPMMD, null);
        okPMMD.setDefaultButton(true);
        OverlaysPMMD.Handle hPMMD = overlaysPMMD.modal(null, null, cuerpoPMMD, 440, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            hPMMD.cerrar();
            alConfirmarPMMD.run();
        });
        cancelarPMMD.setCancelButton(true);
        javafx.application.Platform.runLater(okPMMD::requestFocus);
    }
}
