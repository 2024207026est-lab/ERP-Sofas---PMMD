package pe.erp.sofas.ui.views.reportes;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.service.ExportServicePMMD;
import pe.erp.sofas.service.ReporteServicePMMD;
import pe.erp.sofas.service.Tabla;
import pe.erp.sofas.ui.AppWindowPMMD;
import pe.erp.sofas.ui.components.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ReportesControllerPMMD implements VistaPMMD {

    private final ReporteServicePMMD reportesPMMD;

    private final ExportServicePMMD exportPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final AppWindowPMMD appWindowPMMD;

    @FXML private VBox raiz;

    private final TilePane tarjetasPMMD = new TilePane(16, 16);

    @FXML
    public void initialize() {
        tarjetasPMMD.setPrefTileWidth(360);
        tarjetasPMMD.setTileAlignment(Pos.TOP_LEFT);
        ScrollPane spPMMD = UIPMMD.scroll(tarjetasPMMD);
        VBox.setVgrow(spPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(UIPMMD.label("Elige el rango de fechas de cada reporte. Vista previa antes de exportar; la exportación corre en segundo plano.", "cell-sub"), spPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        tarjetasPMMD.getChildren().clear();
        for (ReporteServicePMMD.Tipo tPMMD : ReporteServicePMMD.Tipo.values()) tarjetasPMMD.getChildren().add(tarjetaPMMD(tPMMD));
    }

    private Node tarjetaPMMD(ReporteServicePMMD.Tipo tPMMD) {
        LocalDate hoyPMMD = AppClockPMMD.hoy();
        boolean historicoPMMD = tPMMD == ReporteServicePMMD.Tipo.METROS_TELA || tPMMD == ReporteServicePMMD.Tipo.COSTO_ESTANDAR_REAL || tPMMD == ReporteServicePMMD.Tipo.MERMAS
                || tPMMD == ReporteServicePMMD.Tipo.PRODUCCION_TAPICERO;
        DatePicker desdePMMD = FormsPMMD.fecha(historicoPMMD ? hoyPMMD.minusMonths(1).withDayOfMonth(1) : hoyPMMD.withDayOfMonth(1));
        DatePicker hastaPMMD = FormsPMMD.fecha(hoyPMMD);
        StackPane icPMMD = new StackPane(IcoPMMD.of(tPMMD.icono(), 20));
        icPMMD.getStyleClass().addAll("kpi-icon", "kpi-blue");
        Button verPMMD = UIPMMD.button("Vista previa", "VISIBILITY", "btn-sm", "btn-secondary");
        Button pdfPMMD = UIPMMD.button("PDF", "PICTURE_AS_PDF", "btn-sm", "btn-secondary");
        Button xlsPMMD = UIPMMD.button("Excel", "TABLE_VIEW", "btn-sm", "btn-secondary");
        verPMMD.setOnAction(ePMMD -> vistaPMMD(tPMMD, desdePMMD.getValue(), hastaPMMD.getValue()));
        pdfPMMD.setOnAction(ePMMD -> exportar(tPMMD, desdePMMD.getValue(), hastaPMMD.getValue(), true));
        xlsPMMD.setOnAction(ePMMD -> exportar(tPMMD, desdePMMD.getValue(), hastaPMMD.getValue(), false));
        GridPane fechasPMMD = UIPMMD.grid(2);
        fechasPMMD.add(UIPMMD.field("Desde", desdePMMD), 0, 0);
        fechasPMMD.add(UIPMMD.field("Hasta", hastaPMMD), 1, 0);
        VBox vPMMD = UIPMMD.card(UIPMMD.hbox(12, icPMMD, new VBox(2, UIPMMD.label(tPMMD.nombre(), "card-title"), UIPMMD.wrap(tPMMD.descripcion(), "cell-sub"))), fechasPMMD, UIPMMD.hbox(8, verPMMD, UIPMMD.spacer(), pdfPMMD, xlsPMMD));
        vPMMD.setPrefWidth(360);
        return vPMMD;
    }

    private Tabla generar(ReporteServicePMMD.Tipo tPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        if (dPMMD == null || hPMMD == null || hPMMD.isBefore(dPMMD)) throw new NegocioExceptionPMMD("Revisa el rango de fechas.");
        return reportesPMMD.generar(tPMMD, dPMMD, hPMMD);
    }

    private void vistaPMMD(ReporteServicePMMD.Tipo tPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        Tabla[] tablaPMMD = new Tabla[1];
        if (!toastPMMD.intentar(() -> tablaPMMD[0] = generar(tPMMD, dPMMD, hPMMD))) return;
        TableView<List<String>> tvPMMD = new TableView<>();
        tvPMMD.getStyleClass().add("is-table");
        tvPMMD.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tvPMMD.setPlaceholder(UIPMMD.empty("INSERT_CHART", "Sin datos", "No hay registros en el rango elegido.", null));
        for (int iPMMD = 0; iPMMD < tablaPMMD[0].columnas().size(); iPMMD++) {
            int idxPMMD = iPMMD;
            TableColumn<List<String>, String> cPMMD = new TableColumn<>(tablaPMMD[0].columnas().get(iPMMD).toUpperCase(AppClockPMMD.PERU));
            cPMMD.setCellValueFactory(cdPMMD -> new ReadOnlyStringWrapper(idxPMMD < cdPMMD.getValue().size() ? cdPMMD.getValue().get(idxPMMD) : ""));
            if (tablaPMMD[0].derecha() != null && tablaPMMD[0].derecha()[iPMMD]) cPMMD.getStyleClass().add("col-right");
            tvPMMD.getColumns().add(cPMMD);
        }
        tvPMMD.getItems().setAll(tablaPMMD[0].filas());
        if (tablaPMMD[0].totales() != null) tvPMMD.getItems().add(tablaPMMD[0].totales());
        tvPMMD.setPrefHeight(Math.min(520, 44 * (tvPMMD.getItems().size() + 1) + 10));
        Button pdfPMMD = UIPMMD.secondary("Exportar PDF", "PICTURE_AS_PDF");
        Button xlsPMMD = UIPMMD.secondary("Exportar Excel", "TABLE_VIEW");
        Button cerrarPMMD = UIPMMD.primary("Cerrar", null);
        var hndPMMD = overlaysPMMD.modal(tablaPMMD[0].titulo(), tablaPMMD[0].subtitulo() + " · " + tablaPMMD[0].filas().size() + " filas", tvPMMD, 1000, pdfPMMD, xlsPMMD, UIPMMD.spacer(), cerrarPMMD);
        pdfPMMD.setOnAction(ePMMD -> exportar(tPMMD, dPMMD, hPMMD, true));
        xlsPMMD.setOnAction(ePMMD -> exportar(tPMMD, dPMMD, hPMMD, false));
        cerrarPMMD.setOnAction(ePMMD -> hndPMMD.cerrar());
    }

    private void exportar(ReporteServicePMMD.Tipo tPMMD, LocalDate dPMMD, LocalDate hPMMD, boolean pdfPMMD) {
        File fPMMD = ArchivosPMMD.guardar(tarjetasPMMD, tPMMD.nombre().replace(" ", "-") + "-" + AppClockPMMD.hoy(), pdfPMMD ? "PDF" : "Excel", pdfPMMD ? "pdf" : "xlsx");
        if (fPMMD == null) return;
        TareasPMMD.ejecutar(overlaysPMMD, toastPMMD, pdfPMMD ? "Generando PDF…" : "Generando Excel…", () -> {
            Tabla tablaPMMD = generar(tPMMD, dPMMD, hPMMD);
            if (pdfPMMD) exportPMMD.pdf(tablaPMMD, fPMMD);
            else exportPMMD.excel(tablaPMMD, fPMMD);
            return fPMMD;
        }, aPMMD -> {
            toastPMMD.exito(tPMMD.nombre() + " exportado: " + aPMMD.getName());
            appWindowPMMD.hostServices().showDocument(aPMMD.toURI().toString());
        });
    }
}
