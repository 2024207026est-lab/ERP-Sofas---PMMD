package pe.erp.sofas.ui.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;

import java.util.List;



public class ChipFilterBarPMMD extends FlowPane {

    public record Chip(
      String clave, 
        String texto, 
          long conteo) {
    }

    private final ToggleGroup grupoPMMD = new ToggleGroup();

    private final ObjectProperty<String> seleccionPMMD = new SimpleObjectProperty<>();

    private boolean reconstruyendoPMMD;

    public ChipFilterBarPMMD() {
        super(8, 8);
        getStyleClass().add("chip-bar");
        setPrefWrapLength(1150);
        setMinWidth(0);
        setMinHeight(USE_PREF_SIZE);
        grupoPMMD.selectedToggleProperty().addListener((oPMMD, aPMMD, nPMMD) -> {
            if (reconstruyendoPMMD) return;
            if (nPMMD == null) {
                if (aPMMD != null) grupoPMMD.selectToggle(aPMMD);
                return;
            }
            seleccionPMMD.set((String) nPMMD.getUserData());
        });
    }



    public void setChips(List<Chip> chipsPMMD) {
        String actualPMMD = seleccionPMMD.get();
        reconstruyendoPMMD = true;
        getChildren().clear();
        grupoPMMD.getToggles().clear();
        ToggleButton aSeleccionarPMMD = null;
        for (Chip cPMMD : chipsPMMD) {
            ToggleButton tPMMD = new ToggleButton(cPMMD.texto());
            tPMMD.setUserData(cPMMD.clave());
            tPMMD.setToggleGroup(grupoPMMD);
            tPMMD.getStyleClass().add("chip");
            tPMMD.setMnemonicParsing(false);
            if (cPMMD.conteo() >= 0) {
                Label nPMMD = UIPMMD.label(String.valueOf(cPMMD.conteo()), "chip-count");
                tPMMD.setGraphic(nPMMD);
                tPMMD.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
            }
            getChildren().add(tPMMD);
            if (aSeleccionarPMMD == null || cPMMD.clave().equals(actualPMMD)) aSeleccionarPMMD = tPMMD;
        }
        if (aSeleccionarPMMD != null) grupoPMMD.selectToggle(aSeleccionarPMMD);
        reconstruyendoPMMD = false;

        if (aSeleccionarPMMD != null) seleccionPMMD.set((String) aSeleccionarPMMD.getUserData());
    }

    public ObjectProperty<String> seleccionProperty() {
        return seleccionPMMD;
    }

    public String getSeleccion() {
        return seleccionPMMD.get();
    }

    public void seleccionar(String clave) {
        grupoPMMD.getToggles().stream().filter(tPMMD -> clave.equals(tPMMD.getUserData())).findFirst().ifPresent(grupoPMMD::selectToggle);
    }
}
