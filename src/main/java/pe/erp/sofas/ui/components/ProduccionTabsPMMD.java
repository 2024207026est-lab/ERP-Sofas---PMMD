package pe.erp.sofas.ui.components;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import pe.erp.sofas.core.ModuloPMMD;
import pe.erp.sofas.core.NavigatorPMMD;






public class ProduccionTabsPMMD extends HBox {

    public ProduccionTabsPMMD(ModuloPMMD actualPMMD, NavigatorPMMD navigatorPMMD, Runnable calculadoraPMMD) {
        getStyleClass().add("prod-tabs");
        SegmentedControlPMMD tabsPMMD = new SegmentedControlPMMD();
        tabsPMMD.getStyleClass().add("tabs");
        String[][] defsPMMD = {{"produccion", "Tablero", "VIEW_WEEK"}, {"fichas", "Fichas técnicas", "DESCRIPTION"},
                {"mrp", "Requerimiento de materiales", "FACT_CHECK"}, {"corte", "Hojas de corte", "CONTENT_CUT"},
                {"consumo", "Consumo y mermas", "ANALYTICS"}};
        for (String[] dPMMD : defsPMMD) tabsPMMD.agregar(dPMMD[1], dPMMD[2]);
        for (String[] dPMMD : defsPMMD) if (dPMMD[0].equals(actualPMMD.clave())) tabsPMMD.setValor(dPMMD[1]);
        tabsPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            for (String[] dPMMD : defsPMMD) if (dPMMD[1].equals(bPMMD) && !dPMMD[0].equals(actualPMMD.clave())) navigatorPMMD.goTo(dPMMD[0]);
        });
        Button calcPMMD = UIPMMD.secondary("Calculadora de materiales", "CALCULATE");
        calcPMMD.setOnAction(ePMMD -> calculadoraPMMD.run());
        getChildren().addAll(tabsPMMD, UIPMMD.spacer(), calcPMMD);
        setMinHeight(USE_PREF_SIZE);
    }
}
