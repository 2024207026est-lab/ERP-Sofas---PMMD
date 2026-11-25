package pe.erp.sofas.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayList;
import java.util.List;



public class DonutChartPMMD extends HBox {

    public record Segmento(
      String nombre, 
        double valor, 
          String color) {
    }

    private final Label centroValorPMMD = UIPMMD.label("", "donut-value");

    private final Label centroTextoPMMD = UIPMMD.label("", "donut-caption");

    public DonutChartPMMD(List<Segmento> segmentosPMMD, double diametroPMMD, String tituloCentroPMMD, String sufijoPMMD) {
        this(segmentosPMMD, diametroPMMD, tituloCentroPMMD, sufijoPMMD, false);
    }



    public DonutChartPMMD(List<Segmento> segmentosPMMD, double diametroPMMD, String tituloCentroPMMD, String sufijoPMMD, boolean verticalPMMD) {
        super(22);
        setAlignment(Pos.CENTER_LEFT);
        double totalPMMD = segmentosPMMD.stream().mapToDouble(Segmento::valor).sum();
        double grosorPMMD = diametroPMMD * 0.16;
        double r = (diametroPMMD - grosorPMMD) / 2;
        Pane lienzo = new Pane();
        lienzo.setMinSize(diametroPMMD, diametroPMMD);
        lienzo.setPrefSize(diametroPMMD, diametroPMMD);
        lienzo.setMaxSize(diametroPMMD, diametroPMMD);
        Circle fondoPMMD = new Circle(diametroPMMD / 2, diametroPMMD / 2, r);
        fondoPMMD.getStyleClass().add("donut-track");
        fondoPMMD.setStrokeWidth(grosorPMMD);
        lienzo.getChildren().add(fondoPMMD);

        Runnable restablecerPMMD = () -> {
            centroValorPMMD.setText(tituloCentroPMMD);
            centroTextoPMMD.setText(sufijoPMMD);
        };
        restablecerPMMD.run();

        List<HBox> leyendasPMMD = new ArrayList<>();
        double inicio = 90;
        for (Segmento sPMMD : segmentosPMMD) {
            double anguloPMMD = totalPMMD == 0 ? 0 : sPMMD.valor() / totalPMMD * 360;
            Arc arcPMMD = new Arc(diametroPMMD / 2, diametroPMMD / 2, r, r, inicio, -Math.max(0, anguloPMMD - 1.6));
            arcPMMD.setType(ArcType.OPEN);
            arcPMMD.setFill(null);
            arcPMMD.setStrokeWidth(grosorPMMD);
            arcPMMD.setStrokeLineCap(StrokeLineCap.BUTT);
            arcPMMD.setStyle("-fx-stroke: " + sPMMD.color() + ";");
            arcPMMD.getStyleClass().add("donut-arc");
            String pctPMMD = String.format("%.0f %%", totalPMMD == 0 ? 0 : sPMMD.valor() * 100 / totalPMMD);
            arcPMMD.setOnMouseEntered(ePMMD -> {
                arcPMMD.setStrokeWidth(grosorPMMD + 5);
                centroValorPMMD.setText(pctPMMD);
                centroTextoPMMD.setText(sPMMD.nombre());
            });
            arcPMMD.setOnMouseExited(ePMMD -> {
                arcPMMD.setStrokeWidth(grosorPMMD);
                restablecerPMMD.run();
            });
            lienzo.getChildren().add(arcPMMD);
            inicio -= anguloPMMD;

            javafx.scene.layout.Region punto = new javafx.scene.layout.Region();
            punto.getStyleClass().add("legend-dot");
            punto.setStyle("-fx-background-color: " + sPMMD.color() + ";");
            HBox filaPMMD = new HBox(8, punto, UIPMMD.label(sPMMD.nombre(), "legend-text"), UIPMMD.spacer(), UIPMMD.label(pctPMMD, "legend-value"));
            filaPMMD.setAlignment(Pos.CENTER_LEFT);
            filaPMMD.setOnMouseEntered(ePMMD -> arcPMMD.getOnMouseEntered().handle(null));
            filaPMMD.setOnMouseExited(ePMMD -> arcPMMD.getOnMouseExited().handle(null));
            leyendasPMMD.add(filaPMMD);
        }
        VBox centro = new VBox(0, centroValorPMMD, centroTextoPMMD);
        centro.setAlignment(Pos.CENTER);
        centro.setMouseTransparent(true);
        centro.setMaxWidth(r * 1.6);
        centroTextoPMMD.setWrapText(true);
        centroTextoPMMD.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        StackPane dona = new StackPane(lienzo, centro);
        dona.setMinSize(diametroPMMD, diametroPMMD);
        VBox leyenda = new VBox(9);
        leyenda.getChildren().addAll(leyendasPMMD);
        leyenda.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leyenda, javafx.scene.layout.Priority.ALWAYS);
        if (verticalPMMD) {
            VBox colPMMD = new VBox(14, dona, leyenda);
            colPMMD.setAlignment(Pos.TOP_CENTER);
            HBox.setHgrow(colPMMD, javafx.scene.layout.Priority.ALWAYS);
            leyenda.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(colPMMD);
        } else {
            getChildren().addAll(dona, leyenda);
        }
    }
}
