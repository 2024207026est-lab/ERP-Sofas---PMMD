package pe.erp.sofas.ui.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;



public final class ChartsPMMD {

    private ChartsPMMD() {
    }

    public record Serie(
      String nombre, 
        List<String> categorias, 
          List<Double> valores) {
    }

    private static NumberAxis ejeYPMMD(Function<Double, String> formato) {
        NumberAxis yPMMD = new NumberAxis();
        yPMMD.setForceZeroInRange(true);
        yPMMD.setMinorTickVisible(false);
        yPMMD.setTickLabelFormatter(new StringConverter<>() {

            @Override
            public String toString(Number nPMMD) {
                return formato.apply(nPMMD.doubleValue());
            }

            @Override
            public Number fromString(String sPMMD) {
                return 0;
            }
        });
        return yPMMD;
    }





    public static BarChart<String, Number> barras(List<Serie> seriesPMMD, Function<Double, String> formato,
                                                  BiConsumer<String, Double> alClicPMMD) {
        CategoryAxis x = new CategoryAxis();
        x.setTickMarkVisible(false);
        BarChart<String, Number> chart = new BarChart<>(x, ejeYPMMD(formato));
        chart.setAnimated(false);
        chart.setLegendVisible(seriesPMMD.size() > 1);
        chart.setVerticalGridLinesVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setBarGap(3);
        chart.setCategoryGap(seriesPMMD.size() > 1 ? 14 : 18);
        for (Serie sPMMD : seriesPMMD) {
            XYChart.Series<String, Number> seriePMMD = new XYChart.Series<>();
            seriePMMD.setName(sPMMD.nombre());
            for (int iPMMD = 0; iPMMD < sPMMD.categorias().size(); iPMMD++) {
                seriePMMD.getData().add(new XYChart.Data<>(sPMMD.categorias().get(iPMMD), sPMMD.valores().get(iPMMD)));
            }
            chart.getData().add(seriePMMD);
        }
        for (XYChart.Series<String, Number> seriePMMD : chart.getData()) {
            for (XYChart.Data<String, Number> dPMMD : seriePMMD.getData()) {
                Node nPMMD = dPMMD.getNode();
                if (nPMMD == null) continue;
                Tooltip tPMMD = new Tooltip(seriePMMD.getName() + " · " + dPMMD.getXValue() + "\n" + formato.apply(dPMMD.getYValue().doubleValue()));
                tPMMD.setShowDelay(Duration.millis(80));
                Tooltip.install(nPMMD, tPMMD);
                nPMMD.setOnMouseEntered(ePMMD -> nPMMD.getStyleClass().add("bar-hover"));
                nPMMD.setOnMouseExited(ePMMD -> nPMMD.getStyleClass().remove("bar-hover"));
                if (alClicPMMD != null) {
                    nPMMD.setStyle("-fx-cursor: hand;");
                    nPMMD.setOnMouseClicked(ePMMD -> {
                        chart.getData().forEach(srPMMD -> srPMMD.getData().forEach(ddPMMD -> {
                            if (ddPMMD.getNode() != null) ddPMMD.getNode().getStyleClass().remove("bar-selected");
                        }));
                        nPMMD.getStyleClass().add("bar-selected");
                        alClicPMMD.accept(dPMMD.getXValue(), dPMMD.getYValue().doubleValue());
                    });
                }
            }
        }
        return chart;
    }



    public static XYChart<String, Number> lineas(List<Serie> seriesPMMD, Function<Double, String> formato, boolean areaPMMD) {
        CategoryAxis x = new CategoryAxis();
        x.setTickMarkVisible(false);
        XYChart<String, Number> chart = areaPMMD ? new AreaChart<>(x, ejeYPMMD(formato)) : new LineChart<>(x, ejeYPMMD(formato));
        chart.setAnimated(false);
        chart.setLegendVisible(seriesPMMD.size() > 1);
        chart.setVerticalGridLinesVisible(false);
        chart.setAlternativeRowFillVisible(false);
        if (chart instanceof LineChart<String, Number> lc) lc.setCreateSymbols(true);
        if (chart instanceof AreaChart<String, Number> ac) ac.setCreateSymbols(true);
        for (Serie sPMMD : seriesPMMD) {
            XYChart.Series<String, Number> seriePMMD = new XYChart.Series<>();
            seriePMMD.setName(sPMMD.nombre());
            for (int iPMMD = 0; iPMMD < sPMMD.categorias().size(); iPMMD++) {
                seriePMMD.getData().add(new XYChart.Data<>(sPMMD.categorias().get(iPMMD), sPMMD.valores().get(iPMMD)));
            }
            chart.getData().add(seriePMMD);
        }
        for (XYChart.Series<String, Number> seriePMMD : chart.getData()) {
            for (XYChart.Data<String, Number> dPMMD : seriePMMD.getData()) {
                if (dPMMD.getNode() == null) continue;
                Tooltip tPMMD = new Tooltip(seriePMMD.getName() + " · " + dPMMD.getXValue() + "\n" + formato.apply(dPMMD.getYValue().doubleValue()));
                tPMMD.setShowDelay(Duration.millis(60));
                Tooltip.install(dPMMD.getNode(), tPMMD);
            }
        }
        return chart;
    }



    public static HBox barraHorizontal(String etiquetaPMMD, double valorPMMD, double maximoPMMD, String color, String textoPMMD) {
        var lbl = UIPMMD.label(etiquetaPMMD, "hbar-label");
        lbl.setMinWidth(130);
        lbl.setPrefWidth(130);
        ProgressBarMiniPMMD barra = new ProgressBarMiniPMMD(maximoPMMD <= 0 ? 0 : valorPMMD / maximoPMMD, color, 10);
        HBox.setHgrow(barra, Priority.ALWAYS);
        barra.setMaxWidth(Double.MAX_VALUE);
        var val = UIPMMD.label(textoPMMD, "hbar-value");
        val.setMinWidth(Region.USE_PREF_SIZE);
        val.setPrefWidth(56);
        val.setAlignment(Pos.CENTER_RIGHT);
        HBox hPMMD = new HBox(12, lbl, barra, val);
        hPMMD.setAlignment(Pos.CENTER_LEFT);
        return hPMMD;
    }



    public static VBox apilada(String etiquetaPMMD, String detallePMMD, double aPMMD, double bPMMD, double maximoPMMD, String colorAPMMD, String colorBPMMD) {
        HBox barra = new HBox();
        barra.setMinHeight(14);
        barra.setPrefHeight(14);
        barra.getStyleClass().add("progress-track");
        barra.setStyle("-fx-background-radius: 7;");
        Region raPMMD = new Region();
        raPMMD.setStyle("-fx-background-color: " + colorAPMMD + "; -fx-background-radius: 7 0 0 7;");
        Region rbPMMD = new Region();
        rbPMMD.setStyle("-fx-background-color: " + colorBPMMD + "; -fx-background-radius: 0 7 7 0;");
        barra.getChildren().addAll(raPMMD, rbPMMD);
        barra.widthProperty().addListener((oPMMD, x, wPMMD) -> {
            double totalPMMD = Math.max(maximoPMMD, 1);
            raPMMD.setPrefWidth(wPMMD.doubleValue() * aPMMD / totalPMMD);
            raPMMD.setMinWidth(wPMMD.doubleValue() * aPMMD / totalPMMD);
            rbPMMD.setPrefWidth(wPMMD.doubleValue() * bPMMD / totalPMMD);
            rbPMMD.setMinWidth(wPMMD.doubleValue() * bPMMD / totalPMMD);
        });
        HBox cab = UIPMMD.hbox(8, UIPMMD.label(etiquetaPMMD, "cell-title"), UIPMMD.spacer(), UIPMMD.label(detallePMMD, "cell-sub"));
        return new VBox(6, cab, barra);
    }
}
