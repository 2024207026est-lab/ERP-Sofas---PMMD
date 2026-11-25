package pe.erp.sofas.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import pe.erp.sofas.model.EtapaPMMD;

import java.util.function.Consumer;



public class StepperPMMD extends HBox {

    public StepperPMMD(EtapaPMMD actualPMMD, Consumer<EtapaPMMD> alElegirPMMD) {
        getStyleClass().add("stepper");
        setAlignment(Pos.TOP_CENTER);
        EtapaPMMD[] etapasPMMD = EtapaPMMD.values();
        for (int iPMMD = 0; iPMMD < etapasPMMD.length; iPMMD++) {
            EtapaPMMD ePMMD = etapasPMMD[iPMMD];
            boolean hechoPMMD = ePMMD.ordinal() < actualPMMD.ordinal();
            boolean activoPMMD = ePMMD == actualPMMD;
            StackPane circulo = new StackPane(hechoPMMD ? IcoPMMD.of("CHECK", 13) : IcoPMMD.of(ePMMD.icono(), 13));
            circulo.getStyleClass().add("step-circle");
            if (hechoPMMD || activoPMMD) {
                circulo.setStyle("-fx-background-color: " + ePMMD.color() + "; -fx-border-color: " + ePMMD.color() + ";");
                circulo.getStyleClass().add("step-filled");
            }
            if (activoPMMD) circulo.getStyleClass().add("step-active");
            var nombre = UIPMMD.wrap(ePMMD.nombre(), "step-label");
            nombre.setAlignment(Pos.CENTER);
            nombre.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            if (activoPMMD) nombre.getStyleClass().add("step-label-active");
            VBox pasoPMMD = new VBox(5, circulo, nombre);
            pasoPMMD.setAlignment(Pos.TOP_CENTER);
            pasoPMMD.setPrefWidth(56);
            pasoPMMD.setMinWidth(44);
            Tooltip.install(pasoPMMD, new Tooltip((iPMMD + 1) + ". " + ePMMD.nombre()));
            if (alElegirPMMD != null) UIPMMD.onClick(pasoPMMD, evPMMD -> alElegirPMMD.accept(ePMMD));
            getChildren().add(pasoPMMD);
            if (iPMMD < etapasPMMD.length - 1) {
                Region lineaPMMD = new Region();
                lineaPMMD.getStyleClass().add("step-line");
                if (ePMMD.ordinal() < actualPMMD.ordinal()) lineaPMMD.setStyle("-fx-background-color: " + etapasPMMD[iPMMD + 1].color() + ";");
                HBox.setHgrow(lineaPMMD, Priority.ALWAYS);
                lineaPMMD.setTranslateY(13);
                getChildren().add(lineaPMMD);
            }
        }
    }
}
