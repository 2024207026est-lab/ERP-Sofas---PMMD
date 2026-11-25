package pe.erp.sofas.ui.components;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;






public class LogoMarkPMMD extends HBox {

    public LogoMarkPMMD(double sizePMMD, String subtituloPMMD) {
        super(sizePMMD * 0.3);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("logo-mark");
        getChildren().add(icono(sizePMMD));
        if (subtituloPMMD != null) {
            var titulo = UIPMMD.label("SISTEMA ERP SOFAS", "logo-title");
            titulo.setStyle("-fx-font-size: " + Math.round(sizePMMD * 0.40) + "px;");
            var sub = UIPMMD.label(subtituloPMMD, "logo-subtitle");
            sub.setStyle("-fx-font-size: " + Math.max(9.5, Math.round(sizePMMD * 0.24)) + "px;");
            VBox textosPMMD = new VBox(Math.max(0, sizePMMD * 0.02), titulo, sub);
            textosPMMD.setAlignment(Pos.CENTER_LEFT);
            textosPMMD.getStyleClass().add("logo-texts");
            getChildren().add(textosPMMD);
        }
    }

    public static StackPane icono(double sizePMMD) {
        StackPane boxPMMD = new StackPane();
        boxPMMD.getStyleClass().add("logo-box");
        boxPMMD.setMinSize(sizePMMD, sizePMMD);
        boxPMMD.setPrefSize(sizePMMD, sizePMMD);
        boxPMMD.setMaxSize(sizePMMD, sizePMMD);
        boxPMMD.setStyle("-fx-background-radius: " + sizePMMD * 11 / 42 + ";");
        double uPMMD = sizePMMD / 42.0;
        Color maderaPMMD = Color.web("#C69A6D"), beigePMMD = Color.web("#E4C9A4"), oscuraPMMD = Color.web("#A47148");
        Pane sofaPMMD = new Pane();
        sofaPMMD.setMinSize(30 * uPMMD, 22 * uPMMD);
        sofaPMMD.setPrefSize(30 * uPMMD, 22 * uPMMD);
        sofaPMMD.setMaxSize(30 * uPMMD, 22 * uPMMD);
        sofaPMMD.getChildren().addAll(
                rectPMMD(4 * uPMMD, 2 * uPMMD, 22 * uPMMD, 10 * uPMMD, 3 * uPMMD, beigePMMD),
                rectPMMD(0, 7 * uPMMD, 6 * uPMMD, 11 * uPMMD, 3 * uPMMD, maderaPMMD),
                rectPMMD(24 * uPMMD, 7 * uPMMD, 6 * uPMMD, 11 * uPMMD, 3 * uPMMD, maderaPMMD),
                rectPMMD(5 * uPMMD, 11 * uPMMD, 20 * uPMMD, 6 * uPMMD, 2.5 * uPMMD, maderaPMMD),
                rectPMMD(14.6 * uPMMD, 11.5 * uPMMD, 0.8 * uPMMD, 5 * uPMMD, 0, oscuraPMMD),
                rectPMMD(2 * uPMMD, 18 * uPMMD, 2.2 * uPMMD, 3.5 * uPMMD, 1 * uPMMD, beigePMMD),
                rectPMMD(25.8 * uPMMD, 18 * uPMMD, 2.2 * uPMMD, 3.5 * uPMMD, 1 * uPMMD, beigePMMD));
        boxPMMD.getChildren().add(sofaPMMD);
        return boxPMMD;
    }

    private static Rectangle rectPMMD(double x, double y, double w, double h, double rPMMD, Color cPMMD) {
        Rectangle rePMMD = new Rectangle(x, y, w, h);
        rePMMD.setArcWidth(rPMMD * 2);
        rePMMD.setArcHeight(rPMMD * 2);
        rePMMD.setFill(cPMMD);
        return rePMMD;
    }
}
