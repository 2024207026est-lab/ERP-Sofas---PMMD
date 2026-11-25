package pe.erp.sofas.ui.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;



public class SegmentedControlPMMD extends HBox {

    private final ToggleGroup grupoPMMD = new ToggleGroup();

    private final ObjectProperty<String> valorPMMD = new SimpleObjectProperty<>();

    public SegmentedControlPMMD(String... opcionesPMMD) {
        getStyleClass().add("segmented");
        setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        for (String opPMMD : opcionesPMMD) {
            agregar(opPMMD, null);
        }
        grupoPMMD.selectedToggleProperty().addListener((oPMMD, aPMMD, nPMMD) -> {
            if (nPMMD == null) {
                grupoPMMD.selectToggle(aPMMD);
                return;
            }
            valorPMMD.set((String) nPMMD.getUserData());
        });
        if (!grupoPMMD.getToggles().isEmpty()) grupoPMMD.selectToggle(grupoPMMD.getToggles().get(0));
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    }



    public SegmentedControlPMMD agregar(String texto, String iconoPMMD) {
        ToggleButton tPMMD = new ToggleButton(texto);
        tPMMD.setUserData(texto);
        tPMMD.setToggleGroup(grupoPMMD);
        tPMMD.getStyleClass().add("segment");
        tPMMD.setMnemonicParsing(false);
        tPMMD.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        if (iconoPMMD != null) tPMMD.setGraphic(IcoPMMD.of(iconoPMMD, 16));
        getChildren().add(tPMMD);
        return this;
    }

    public static SegmentedControlPMMD conIconos(String[] textosPMMD, String[] iconosPMMD) {
        SegmentedControlPMMD sPMMD = new SegmentedControlPMMD();
        for (int iPMMD = 0; iPMMD < textosPMMD.length; iPMMD++) sPMMD.agregar(textosPMMD[iPMMD], iconosPMMD[iPMMD]);
        sPMMD.grupoPMMD.selectToggle(sPMMD.grupoPMMD.getToggles().get(0));
        return sPMMD;
    }

    public ObjectProperty<String> valorProperty() {
        return valorPMMD;
    }

    public String getValor() {
        return valorPMMD.get();
    }

    public void setValor(String vPMMD) {
        grupoPMMD.getToggles().stream().filter(tPMMD -> vPMMD.equals(tPMMD.getUserData())).findFirst().ifPresent(grupoPMMD::selectToggle);
    }
}
