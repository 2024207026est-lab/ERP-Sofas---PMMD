package pe.erp.sofas.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;






public class KpiCardPMMD extends VBox {

    private final Label valor;

    private final Label sub;

    public KpiCardPMMD(String iconoPMMD, String variantePMMD, String etiquetaPMMD, String valor, String sub) {
        super(10);
        getStyleClass().addAll("card", "kpi-card");
        StackPane chip = new StackPane(IcoPMMD.of(iconoPMMD, 19));
        chip.getStyleClass().addAll("kpi-icon", "kpi-" + variantePMMD);
        Label lbl = UIPMMD.wrap(etiquetaPMMD, "kpi-label");
        HBox topPMMD = new HBox(10, lbl, UIPMMD.spacer(), chip);
        topPMMD.setAlignment(Pos.TOP_LEFT);
        this.valor = UIPMMD.label(valor, "kpi-value");
        this.sub = UIPMMD.label(sub == null ? "" : sub, "kpi-sub");
        getChildren().addAll(topPMMD, this.valor, this.sub);
        HBox.setHgrow(this, Priority.ALWAYS);
        setMaxWidth(Double.MAX_VALUE);
        setMinWidth(150);
    }



    public KpiCardPMMD tendencia(boolean positivaPMMD) {
        sub.getStyleClass().add(positivaPMMD ? "trend-up" : "trend-down");
        sub.setGraphic(IcoPMMD.of(positivaPMMD ? "TRENDING_UP" : "TRENDING_DOWN", 15));
        return this;
    }



    public KpiCardPMMD bordeSuperior(String colorPMMD) {
        setStyle("-fx-border-color: " + colorPMMD + " transparent transparent transparent; -fx-border-width: 3 0 0 0;");
        return this;
    }

    public KpiCardPMMD alClic(Runnable accionPMMD) {
        UIPMMD.onClick(this, ePMMD -> accionPMMD.run());
        getStyleClass().add("kpi-clickable");
        return this;
    }

    public void setValor(String vPMMD) {
        valor.setText(vPMMD);
    }

    public void setSub(String sPMMD) {
        sub.setText(sPMMD);
    }
}
