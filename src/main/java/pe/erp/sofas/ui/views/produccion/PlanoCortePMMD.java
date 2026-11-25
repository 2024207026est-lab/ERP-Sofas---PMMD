package pe.erp.sofas.ui.views.produccion;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import pe.erp.sofas.service.calculo.CortePMMD;
import pe.erp.sofas.service.calculo.TipoCortePMMD;

import java.util.List;
import java.util.Set;







public final class PlanoCortePMMD {

    private static final double MARGENPMMD = 24;

    private static final Font NUMPMMD = Font.font("Inter", FontWeight.BOLD, 11);

    private static final Font TXTPMMD = Font.font("Inter", 11);

    private static final Font TITPMMD = Font.font("Inter", FontWeight.BOLD, 12);

    private PlanoCortePMMD() {
    }

    public static Canvas dibujar2D(CortePMMD.Resultado rPMMD, double ancho, Set<Integer> cortadasPMMD, boolean oscuroPMMD) {
        if (rPMMD.tipo() == TipoCortePMMD.TELA) return rolloPMMD(rPMMD, ancho, cortadasPMMD, oscuroPMMD);
        double escalaPMMD = Math.min(3.2, (ancho - 2 * MARGENPMMD) / rPMMD.ancho());
        double altoLamPMMD = rPMMD.alto() * escalaPMMD;
        double h = MARGENPMMD + rPMMD.laminas() * (altoLamPMMD + 42) + 10;
        Canvas cPMMD = new Canvas(ancho, h);
        GraphicsContext gPMMD = cPMMD.getGraphicsContext2D();
        Color textoPMMD = Color.web(oscuroPMMD ? "#E5E7EB" : "#0F172A");
        Color mutedPMMD = Color.web(oscuroPMMD ? "#94A3B8" : "#64748B");
        for (int iPMMD = 0; iPMMD < rPMMD.laminas(); iPMMD++) {
            double y0PMMD = MARGENPMMD + iPMMD * (altoLamPMMD + 42) + 18;
            gPMMD.setFont(TITPMMD);
            gPMMD.setFill(textoPMMD);
            gPMMD.setTextAlign(TextAlignment.LEFT);
            gPMMD.setTextBaseline(VPos.BOTTOM);
            String nombrePMMD = rPMMD.tipo() == TipoCortePMMD.ESPUMA ? "Plancha de espuma" : "Plancha de triplay";
            gPMMD.fillText(nombrePMMD + " " + (iPMMD + 1) + " · " + (int) rPMMD.ancho() + " × " + (int) rPMMD.alto() + " cm", MARGENPMMD, y0PMMD - 4);
            gPMMD.setFill(mutedPMMD);
            gPMMD.setTextAlign(TextAlignment.RIGHT);
            gPMMD.fillText(String.format("aprovechamiento %.0f %%", rPMMD.aprovechamientoLamina(iPMMD)), MARGENPMMD + rPMMD.ancho() * escalaPMMD, y0PMMD - 4);
            gPMMD.setFill(Color.web(oscuroPMMD ? "#1E2A44" : "#FFFFFF"));
            gPMMD.fillRect(MARGENPMMD, y0PMMD, rPMMD.ancho() * escalaPMMD, altoLamPMMD);

            for (CortePMMD.Rect sPMMD : rPMMD.sobrantes().get(iPMMD)) rayadoPMMD(gPMMD, MARGENPMMD + sPMMD.x() * escalaPMMD, y0PMMD + sPMMD.y() * escalaPMMD, sPMMD.w() * escalaPMMD, sPMMD.h() * escalaPMMD,
                    sPMMD.w() >= CortePMMD.MIN_RETAZO && sPMMD.h() >= CortePMMD.MIN_RETAZO, oscuroPMMD);
            for (CortePMMD.Colocacion colPMMD : rPMMD.colocaciones()) {
                if (colPMMD.lamina() != iPMMD) continue;
                pieza(gPMMD, colPMMD, MARGENPMMD + colPMMD.x() * escalaPMMD, y0PMMD + colPMMD.y() * escalaPMMD, colPMMD.w() * escalaPMMD, colPMMD.h() * escalaPMMD, cortadasPMMD.contains(colPMMD.numero()));
            }
            gPMMD.setStroke(Color.web(oscuroPMMD ? "#475569" : "#94A3B8"));
            gPMMD.setLineWidth(1.2);
            gPMMD.strokeRect(MARGENPMMD, y0PMMD, rPMMD.ancho() * escalaPMMD, altoLamPMMD);
        }
        return cPMMD;
    }

    private static Canvas rolloPMMD(CortePMMD.Resultado rPMMD, double ancho, Set<Integer> cortadasPMMD, boolean oscuroPMMD) {
        double tramoPMMD = 400;
        int tramosPMMD = Math.max(1, (int) Math.ceil(rPMMD.largoUsado() / tramoPMMD));
        double escalaPMMD = (ancho - 2 * MARGENPMMD) / tramoPMMD;
        double altoRolloPMMD = rPMMD.ancho() * escalaPMMD;
        double h = MARGENPMMD + tramosPMMD * (altoRolloPMMD + 52) + 6;
        Canvas cPMMD = new Canvas(ancho, h);
        GraphicsContext gPMMD = cPMMD.getGraphicsContext2D();
        Color textoPMMD = Color.web(oscuroPMMD ? "#E5E7EB" : "#0F172A");
        Color mutedPMMD = Color.web(oscuroPMMD ? "#94A3B8" : "#64748B");
        for (int kPMMD = 0; kPMMD < tramosPMMD; kPMMD++) {
            double y0PMMD = MARGENPMMD + kPMMD * (altoRolloPMMD + 52) + 18;
            double desdePMMD = kPMMD * tramoPMMD, hastaPMMD = Math.min(rPMMD.largoUsado(), desdePMMD + tramoPMMD);
            gPMMD.setFont(TITPMMD);
            gPMMD.setFill(textoPMMD);
            gPMMD.setTextAlign(TextAlignment.LEFT);
            gPMMD.setTextBaseline(VPos.BOTTOM);
            gPMMD.fillText(String.format("Rollo de 140 cm · tramo %d (%.1f – %.1f m)", kPMMD + 1, desdePMMD / 100, hastaPMMD / 100), MARGENPMMD, y0PMMD - 4);
            gPMMD.setFill(Color.web(oscuroPMMD ? "#1E2A44" : "#FFFFFF"));
            gPMMD.fillRect(MARGENPMMD, y0PMMD, (hastaPMMD - desdePMMD) * escalaPMMD, altoRolloPMMD);
            gPMMD.save();
            gPMMD.beginPath();
            gPMMD.rect(MARGENPMMD, y0PMMD, (hastaPMMD - desdePMMD) * escalaPMMD, altoRolloPMMD);
            gPMMD.clip();
            for (CortePMMD.Colocacion colPMMD : rPMMD.colocaciones()) {
                if (colPMMD.y() + colPMMD.h() <= desdePMMD || colPMMD.y() >= hastaPMMD) continue;
                double xPMMD = MARGENPMMD + (colPMMD.y() - desdePMMD) * escalaPMMD;
                double yPMMD = y0PMMD + colPMMD.x() * escalaPMMD;
                pieza(gPMMD, colPMMD, xPMMD, yPMMD, colPMMD.h() * escalaPMMD, colPMMD.w() * escalaPMMD, cortadasPMMD.contains(colPMMD.numero()));
            }
            gPMMD.restore();
            gPMMD.setStroke(Color.web(oscuroPMMD ? "#475569" : "#94A3B8"));
            gPMMD.setLineWidth(1.2);
            gPMMD.strokeRect(MARGENPMMD, y0PMMD, (hastaPMMD - desdePMMD) * escalaPMMD, altoRolloPMMD);

            gPMMD.setStroke(mutedPMMD);
            gPMMD.setFill(mutedPMMD);
            gPMMD.setFont(TXTPMMD);
            gPMMD.setTextAlign(TextAlignment.CENTER);
            gPMMD.setTextBaseline(VPos.TOP);
            for (double mPMMD = desdePMMD; mPMMD <= hastaPMMD + 0.1; mPMMD += 50) {
                double xPMMD = MARGENPMMD + (mPMMD - desdePMMD) * escalaPMMD;
                boolean metroPMMD = Math.abs(mPMMD % 100) < 0.1;
                gPMMD.strokeLine(xPMMD, y0PMMD + altoRolloPMMD, xPMMD, y0PMMD + altoRolloPMMD + (metroPMMD ? 7 : 4));
                if (metroPMMD) gPMMD.fillText(String.format("%.0f m", mPMMD / 100), xPMMD, y0PMMD + altoRolloPMMD + 8);
            }
        }
        return cPMMD;
    }

    public static Canvas dibujar1D(CortePMMD.Resultado1D rPMMD, double ancho, Set<Integer> cortadasPMMD, boolean oscuroPMMD) {
        double escalaPMMD = (ancho - 2 * MARGENPMMD - 70) / rPMMD.largoTabla();
        double altoTablaPMMD = 30;
        double h = MARGENPMMD + rPMMD.tablas().size() * (altoTablaPMMD + 30) + 10;
        Canvas cPMMD = new Canvas(ancho, h);
        GraphicsContext gPMMD = cPMMD.getGraphicsContext2D();
        Color textoPMMD = Color.web(oscuroPMMD ? "#E5E7EB" : "#0F172A");
        Color mutedPMMD = Color.web(oscuroPMMD ? "#94A3B8" : "#64748B");
        int iPMMD = 0;
        for (CortePMMD.Tabla tPMMD : rPMMD.tablas()) {
            double y0PMMD = MARGENPMMD + iPMMD * (altoTablaPMMD + 30) + 12;
            gPMMD.setFont(TITPMMD);
            gPMMD.setFill(textoPMMD);
            gPMMD.setTextAlign(TextAlignment.LEFT);
            gPMMD.setTextBaseline(VPos.BOTTOM);
            gPMMD.fillText("Tabla " + (iPMMD + 1) + " · 300 cm", MARGENPMMD, y0PMMD - 3);
            gPMMD.setFill(Color.web("#E4C9A4"));
            gPMMD.fillRect(MARGENPMMD, y0PMMD, rPMMD.largoTabla() * escalaPMMD, altoTablaPMMD);
            for (CortePMMD.Colocacion colPMMD : tPMMD.cortes()) {
                pieza(gPMMD, colPMMD, MARGENPMMD + colPMMD.x() * escalaPMMD, y0PMMD, colPMMD.w() * escalaPMMD, altoTablaPMMD, cortadasPMMD.contains(colPMMD.numero()));
            }
            double usadoPMMD = rPMMD.largoTabla() - tPMMD.sobrante();
            rayadoPMMD(gPMMD, MARGENPMMD + usadoPMMD * escalaPMMD, y0PMMD, tPMMD.sobrante() * escalaPMMD, altoTablaPMMD, tPMMD.sobrante() >= 40, oscuroPMMD);
            gPMMD.setStroke(Color.web("#A47148"));
            gPMMD.strokeRect(MARGENPMMD, y0PMMD, rPMMD.largoTabla() * escalaPMMD, altoTablaPMMD);
            gPMMD.setFont(TXTPMMD);
            gPMMD.setFill(mutedPMMD);
            gPMMD.setTextAlign(TextAlignment.LEFT);
            gPMMD.setTextBaseline(VPos.CENTER);
            gPMMD.fillText(String.format("sobra %.1f cm", tPMMD.sobrante()), MARGENPMMD + rPMMD.largoTabla() * escalaPMMD + 8, y0PMMD + altoTablaPMMD / 2);
            iPMMD++;
        }
        return cPMMD;
    }

    private static void pieza(GraphicsContext gPMMD, CortePMMD.Colocacion colPMMD, double xPMMD, double yPMMD, double wPMMD, double h, boolean cortadaPMMD) {
        Color basePMMD = Color.web(colPMMD.pieza().componente().color());
        gPMMD.setFill(Color.color(basePMMD.getRed(), basePMMD.getGreen(), basePMMD.getBlue(), cortadaPMMD ? 0.18 : 0.78));
        gPMMD.fillRect(xPMMD + 0.5, yPMMD + 0.5, Math.max(1, wPMMD - 1), Math.max(1, h - 1));
        gPMMD.setStroke(Color.WHITE);
        gPMMD.setLineWidth(1);
        gPMMD.strokeRect(xPMMD + 0.5, yPMMD + 0.5, Math.max(1, wPMMD - 1), Math.max(1, h - 1));
        if (wPMMD > 14 && h > 12) {
            gPMMD.setFill(cortadaPMMD ? Color.web("#64748B") : Color.WHITE);
            gPMMD.setFont(NUMPMMD);
            gPMMD.setTextAlign(TextAlignment.CENTER);
            gPMMD.setTextBaseline(VPos.CENTER);
            gPMMD.fillText(String.valueOf(colPMMD.numero()), xPMMD + wPMMD / 2, yPMMD + h / 2 - (h > 34 && wPMMD > 50 ? 6 : 0));
            if (h > 34 && wPMMD > 50) {
                gPMMD.setFont(Font.font("Inter", 9.5));
                gPMMD.fillText(Math.round(colPMMD.pieza().ancho()) + "×" + Math.round(colPMMD.pieza().alto()) + (colPMMD.girada() ? " ↻" : ""), xPMMD + wPMMD / 2, yPMMD + h / 2 + 8);
            }
        }
    }

    private static void rayadoPMMD(GraphicsContext gPMMD, double xPMMD, double yPMMD, double wPMMD, double h, boolean reutilizablePMMD, boolean oscuroPMMD) {
        if (wPMMD <= 0 || h <= 0) return;
        gPMMD.save();
        gPMMD.beginPath();
        gPMMD.rect(xPMMD, yPMMD, wPMMD, h);
        gPMMD.clip();
        gPMMD.setFill(Color.web(reutilizablePMMD ? (oscuroPMMD ? "#2A2119" : "#F5ECE1") : (oscuroPMMD ? "#17223A" : "#F1F5F9")));
        gPMMD.fillRect(xPMMD, yPMMD, wPMMD, h);
        gPMMD.setStroke(Color.web(reutilizablePMMD ? "#C69A6D" : "#CBD5E1"));
        gPMMD.setLineWidth(1);
        for (double dPMMD = -h; dPMMD < wPMMD; dPMMD += 8) gPMMD.strokeLine(xPMMD + dPMMD, yPMMD + h, xPMMD + dPMMD + h, yPMMD);
        gPMMD.restore();
    }



    public static String rango(List<Integer> numsPMMD) {
        if (numsPMMD.isEmpty()) return "";
        StringBuilder sbPMMD = new StringBuilder();
        int iniPMMD = numsPMMD.get(0), prevPMMD = iniPMMD;
        for (int iPMMD = 1; iPMMD <= numsPMMD.size(); iPMMD++) {
            int nPMMD = iPMMD < numsPMMD.size() ? numsPMMD.get(iPMMD) : Integer.MIN_VALUE;
            if (nPMMD == prevPMMD + 1) {
                prevPMMD = nPMMD;
                continue;
            }
            if (sbPMMD.length() > 0) sbPMMD.append(", ");
            sbPMMD.append(iniPMMD == prevPMMD ? String.valueOf(iniPMMD) : iniPMMD + "–" + prevPMMD);
            iniPMMD = prevPMMD = nPMMD;
        }
        return sbPMMD.toString();
    }
}
