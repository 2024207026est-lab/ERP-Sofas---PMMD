package pe.erp.sofas.ui.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

import java.util.List;






public class HBarChartPMMD extends Region {

    public record Fila(
      String etiqueta, 
        double a, 
          double b, 
            String nota, 
              boolean alerta) {
    }

    private final Canvas canvasPMMD = new Canvas();

    private List<Fila> filas = List.of();

    private String colorAPMMD = "#14213D";

    private String colorBPMMD = "#16A34A";

    private String leyendaAPMMD, leyendaBPMMD;

    private boolean centrado;

    private boolean porFila;

    private double alturaFilaPMMD = 30;

    public HBarChartPMMD() {
        getChildren().add(canvasPMMD);
        getStyleClass().add("hbar-chart");
        widthProperty().addListener((oPMMD, a, b) -> dibujarPMMD());
        heightProperty().addListener((oPMMD, a, b) -> dibujarPMMD());
        sceneProperty().addListener((oPMMD, a, b) -> dibujarPMMD());
    }

    public HBarChartPMMD colores(String a, String b) {
        this.colorAPMMD = a;
        this.colorBPMMD = b;
        return this;
    }

    public HBarChartPMMD leyenda(String a, String b) {
        this.leyendaAPMMD = a;
        this.leyendaBPMMD = b;
        return this;
    }



    public HBarChartPMMD centrado(boolean cPMMD) {
        this.centrado = cPMMD;
        return this;
    }



    public HBarChartPMMD porFila(boolean pPMMD) {
        this.porFila = pPMMD;
        return this;
    }

    public void setFilas(List<Fila> filas) {
        this.filas = filas;
        double altoPMMD = filas.size() * alturaFilaPMMD + (leyendaAPMMD != null ? 28 : 6);
        setMinHeight(altoPMMD);
        setPrefHeight(altoPMMD);
        dibujarPMMD();
    }

    @Override
    protected void layoutChildren() {
        canvasPMMD.setWidth(getWidth());
        canvasPMMD.setHeight(getHeight());
        dibujarPMMD();
    }

    private boolean oscuroPMMD() {
        return getScene() != null && getScene().getRoot() != null && getScene().getRoot().getStyleClass().contains("dark");
    }

    private void dibujarPMMD() {
        double wPMMD = getWidth(), hPMMD = getHeight();
        if (wPMMD <= 0 || hPMMD <= 0) return;
        GraphicsContext gPMMD = canvasPMMD.getGraphicsContext2D();
        gPMMD.clearRect(0, 0, wPMMD, hPMMD);
        boolean darkPMMD = oscuroPMMD();
        Color textoPMMD = Color.web(darkPMMD ? "#E5E7EB" : "#0F172A");
        Color mutedPMMD = Color.web(darkPMMD ? "#94A3B8" : "#64748B");
        Color pistaPMMD = Color.web(darkPMMD ? "#17223A" : "#F1F5F9");
        Font normalPMMD = Font.font("Inter", 12);
        Font boldPMMD = Font.font("Inter", FontWeight.BOLD, 11.5);
        double etiquetaWPMMD = Math.min(190, wPMMD * 0.34);
        double notaWPMMD = 92;
        double x0PMMD = etiquetaWPMMD + 10, x1PMMD = wPMMD - notaWPMMD - 8;
        double maxPMMD = 0;
        for (Fila fPMMD : filas) maxPMMD = Math.max(maxPMMD, centrado ? Math.max(Math.abs(fPMMD.a()), Math.abs(fPMMD.b())) : Math.max(fPMMD.a(), fPMMD.b()));
        if (maxPMMD <= 0) maxPMMD = 1;
        double yPMMD = 4;
        for (Fila fPMMD : filas) {
            gPMMD.setFont(normalPMMD);
            gPMMD.setFill(textoPMMD);
            gPMMD.setTextAlign(TextAlignment.LEFT);
            gPMMD.setTextBaseline(VPos.CENTER);
            gPMMD.fillText(recortarPMMD(fPMMD.etiqueta(), normalPMMD, etiquetaWPMMD), 0, yPMMD + alturaFilaPMMD / 2 - 2);
            double anchoPMMD = x1PMMD - x0PMMD;
            gPMMD.setFill(pistaPMMD);
            gPMMD.fillRoundRect(x0PMMD, yPMMD + 6, anchoPMMD, alturaFilaPMMD - 14, 6, 6);
            if (centrado) {
                double midPMMD = x0PMMD + anchoPMMD / 2;
                double lenPMMD = Math.abs(fPMMD.a()) / maxPMMD * anchoPMMD / 2;
                gPMMD.setFill(Color.web(fPMMD.a() > 10 ? "#DC2626" : fPMMD.a() > 0 ? "#F59E0B" : "#16A34A"));
                if (fPMMD.a() >= 0) gPMMD.fillRoundRect(midPMMD, yPMMD + 6, lenPMMD, alturaFilaPMMD - 14, 6, 6);
                else gPMMD.fillRoundRect(midPMMD - lenPMMD, yPMMD + 6, lenPMMD, alturaFilaPMMD - 14, 6, 6);
                gPMMD.setStroke(mutedPMMD);
                gPMMD.setLineWidth(1);
                gPMMD.strokeLine(midPMMD, yPMMD + 3, midPMMD, yPMMD + alturaFilaPMMD - 5);
            } else {
                double maxFilaPMMD = porFila ? Math.max(1e-9, Math.max(fPMMD.a(), fPMMD.b())) : maxPMMD;
                double hbPMMD = (alturaFilaPMMD - 14) / 2;
                gPMMD.setFill(Color.web(colorAPMMD));
                gPMMD.fillRoundRect(x0PMMD, yPMMD + 6, Math.max(2, fPMMD.a() / maxFilaPMMD * anchoPMMD), hbPMMD, 4, 4);
                gPMMD.setFill(Color.web(colorBPMMD));
                gPMMD.fillRoundRect(x0PMMD, yPMMD + 6 + hbPMMD, Math.max(2, fPMMD.b() / maxFilaPMMD * anchoPMMD), hbPMMD, 4, 4);
            }
            gPMMD.setFont(boldPMMD);
            gPMMD.setFill(fPMMD.alerta() ? Color.web("#DC2626") : Color.web(darkPMMD ? "#4ADE80" : "#15803D"));
            gPMMD.setTextAlign(TextAlignment.RIGHT);
            gPMMD.fillText(fPMMD.nota(), wPMMD - 2, yPMMD + alturaFilaPMMD / 2 - 2);
            yPMMD += alturaFilaPMMD;
        }
        if (leyendaAPMMD != null) {
            yPMMD += 6;
            gPMMD.setFont(normalPMMD);
            gPMMD.setTextAlign(TextAlignment.LEFT);
            gPMMD.setFill(Color.web(colorAPMMD));
            gPMMD.fillRoundRect(x0PMMD, yPMMD + 4, 10, 10, 3, 3);
            gPMMD.setFill(mutedPMMD);
            gPMMD.fillText(leyendaAPMMD, x0PMMD + 16, yPMMD + 9);
            double offPMMD = x0PMMD + 30 + textoAnchoPMMD(leyendaAPMMD, normalPMMD);
            gPMMD.setFill(Color.web(colorBPMMD));
            gPMMD.fillRoundRect(offPMMD, yPMMD + 4, 10, 10, 3, 3);
            gPMMD.setFill(mutedPMMD);
            gPMMD.fillText(leyendaBPMMD, offPMMD + 16, yPMMD + 9);
        }
    }

    private static double textoAnchoPMMD(String s, Font fPMMD) {
        Text tPMMD = new Text(s);
        tPMMD.setFont(fPMMD);
        return tPMMD.getLayoutBounds().getWidth();
    }

    private static String recortarPMMD(String s, Font fPMMD, double maxPMMD) {
        if (textoAnchoPMMD(s, fPMMD) <= maxPMMD) return s;
        String rPMMD = s;
        while (rPMMD.length() > 3 && textoAnchoPMMD(rPMMD + "…", fPMMD) > maxPMMD) rPMMD = rPMMD.substring(0, rPMMD.length() - 1);
        return rPMMD + "…";
    }
}
