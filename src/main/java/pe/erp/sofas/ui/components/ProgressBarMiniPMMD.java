package pe.erp.sofas.ui.components;

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;



public class ProgressBarMiniPMMD extends StackPane {

    private final Region rellenoPMMD = new Region();

    private double valor;

    public ProgressBarMiniPMMD(double valor, String colorPMMD, double altoPMMD) {
        getStyleClass().add("progress-track");
        rellenoPMMD.getStyleClass().add("progress-fill");
        setMinHeight(altoPMMD);
        setPrefHeight(altoPMMD);
        setMaxHeight(altoPMMD);
        rellenoPMMD.setMaxHeight(altoPMMD);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        getChildren().add(rellenoPMMD);
        setStyle("-fx-background-radius: " + altoPMMD + ";");
        setColor(colorPMMD);
        setValor(valor);
        widthProperty().addListener((oPMMD, aPMMD, nPMMD) -> ajustarPMMD());
    }

    public void setColor(String colorPMMD) {
        rellenoPMMD.setStyle("-fx-background-color: " + colorPMMD + "; -fx-background-radius: " + getPrefHeight() + ";");
    }



    public void setValor(double vPMMD) {
        this.valor = Math.max(0, Math.min(1, vPMMD));
        ajustarPMMD();
    }

    private void ajustarPMMD() {
        double wPMMD = getWidth() * valor;
        rellenoPMMD.setMinWidth(wPMMD);
        rellenoPMMD.setPrefWidth(wPMMD);
        rellenoPMMD.setMaxWidth(wPMMD);
    }
}
