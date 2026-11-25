package pe.erp.sofas.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;



public class SkeletonPMMD extends VBox {

    public SkeletonPMMD() {
        super(16);
        setPadding(new Insets(4, 0, 0, 0));
        HBox kpisPMMD = new HBox(16);
        for (int iPMMD = 0; iPMMD < 4; iPMMD++) kpisPMMD.getChildren().add(bloquePMMD(-1, 104));
        HBox medioPMMD = new HBox(16, bloquePMMD(-1, 260), bloquePMMD(360, 260));
        VBox filasPMMD = new VBox(10);
        for (int iPMMD = 0; iPMMD < 5; iPMMD++) filasPMMD.getChildren().add(bloquePMMD(-1, 22));
        filasPMMD.getStyleClass().add("skeleton-card");
        filasPMMD.setPadding(new Insets(18));
        getChildren().addAll(kpisPMMD, medioPMMD, filasPMMD);

        FadeTransition pulsoPMMD = new FadeTransition(Duration.millis(700), this);
        pulsoPMMD.setFromValue(1);
        pulsoPMMD.setToValue(0.55);
        pulsoPMMD.setAutoReverse(true);
        pulsoPMMD.setCycleCount(Timeline.INDEFINITE);
        pulsoPMMD.play();
    }

    private static Region bloquePMMD(double anchoPMMD, double altoPMMD) {
        Region rPMMD = new Region();
        rPMMD.getStyleClass().add("skeleton-block");
        rPMMD.setMinHeight(altoPMMD);
        rPMMD.setPrefHeight(altoPMMD);
        if (anchoPMMD > 0) {
            rPMMD.setMinWidth(anchoPMMD);
            rPMMD.setPrefWidth(anchoPMMD);
        } else {
            HBox.setHgrow(rPMMD, Priority.ALWAYS);
            rPMMD.setMaxWidth(Double.MAX_VALUE);
        }
        return rPMMD;
    }
}
