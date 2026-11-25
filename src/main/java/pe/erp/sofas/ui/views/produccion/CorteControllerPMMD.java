package pe.erp.sofas.ui.views.produccion;

import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.service.CorteServicePMMD;
import pe.erp.sofas.service.ExportServicePMMD;
import pe.erp.sofas.service.calculo.CortePMMD;
import pe.erp.sofas.service.calculo.PiezaPMMD;
import pe.erp.sofas.service.calculo.TipoCortePMMD;
import pe.erp.sofas.ui.AppWindowPMMD;
import pe.erp.sofas.ui.components.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class CorteControllerPMMD implements VistaPMMD {

    private record FilaPieza(
      List<Integer> numeros, 
        PiezaPMMD pieza, 
          double ancho, 
            double alto, 
              String origen) {
        
        String medidas() {
            return alto <= 0 ? MoneyPMMD.numero(ancho) : MoneyPMMD.numero(ancho) + " × " + MoneyPMMD.numero(alto);
        }
    }

    private final CorteServicePMMD cortePMMD;

    private final ExportServicePMMD exportPMMD;

    private final CalculadoraMaterialesPMMD calculadoraPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final NavigatorPMMD navigatorPMMD;

    private final AppWindowPMMD appWindowPMMD;

    private final pe.erp.sofas.core.ThemeManagerPMMD temaPMMD;

    @FXML private VBox raiz;

    private ComboBox<CorteServicePMMD.Fuente> fuentePMMD;

    private CheckBox conjuntoPMMD;

    private SegmentedControlPMMD tiposPMMD;

    private final ListView<CorteServicePMMD.MaterialCorte> materialesPMMD = new ListView<>();

    private final VBox kpisPMMD = new VBox(8);

    private final StackPane planoHostPMMD = new StackPane();

    private final VBox listaPiezasPMMD = new VBox();

    private CortePMMD.Estrategia estrategiaPMMD = CortePMMD.Estrategia.MEJOR_LADO;

    private CortePMMD.Resultado resultadoPMMD;

    private CortePMMD.Resultado1D resultado1DPMMD;

    private List<CorteServicePMMD.MaterialCorte> todosPMMD = List.of();

    private final Set<Integer> cortadasPMMD = new HashSet<>();

    private Canvas canvasPMMD;

    @FXML
    public void initialize() {
        fuentePMMD = FormsPMMD.combo(List.of(), null, CorteServicePMMD.Fuente::titulo);
        fuentePMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> cargarFuentePMMD());
        conjuntoPMMD = new CheckBox("Optimizar junto con las demás OP activas que usan el mismo material");
        conjuntoPMMD.setWrapText(true);
        conjuntoPMMD.selectedProperty().addListener((oPMMD, aPMMD, bPMMD) -> cargarFuentePMMD());
        tiposPMMD = new SegmentedControlPMMD("Espuma", "Tela", "Madera", "Tablero");
        tiposPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> filtrarMaterialesPMMD());
        materialesPMMD.setPrefHeight(150);
        materialesPMMD.setCellFactory(lvPMMD -> new ListCell<>() {

            @Override
            protected void updateItem(CorteServicePMMD.MaterialCorte mPMMD, boolean emptyPMMD) {
                super.updateItem(mPMMD, emptyPMMD);
                setText(null);
                setGraphic(emptyPMMD || mPMMD == null ? null : UIPMMD.hbox(8, new VBox(0, UIPMMD.label(mPMMD.material().getNombre(), "cell-title"),
                        UIPMMD.label(mPMMD.material().getCodigo(), "cell-sub")), UIPMMD.spacer(), new StatusBadgePMMD(mPMMD.numeroPiezas() + " piezas", "gray")));
            }
        });
        materialesPMMD.getSelectionModel().selectedItemProperty().addListener((oPMMD, aPMMD, bPMMD) -> optimizar(false));
        Button recalcular = UIPMMD.secondary("Recalcular", "REFRESH");
        recalcular.setOnAction(ePMMD -> optimizar(true));
        Button retazo = UIPMMD.secondary("Guardar retazo", "SAVE");
        retazo.setOnAction(ePMMD -> guardarRetazo());
        Button imprimir = UIPMMD.primary("Imprimir hoja de corte", "PRINT");
        imprimir.setOnAction(ePMMD -> imprimir());
        recalcular.setMaxWidth(Double.MAX_VALUE);
        retazo.setMaxWidth(Double.MAX_VALUE);
        imprimir.setMaxWidth(Double.MAX_VALUE);
        VBox izqPMMD = new VBox(12, UIPMMD.field("OP activa o ficha (1 unidad)", fuentePMMD), conjuntoPMMD, tiposPMMD, UIPMMD.label("Materiales del tipo", "form-label"), materialesPMMD,
                kpisPMMD, recalcular, retazo, imprimir);
        izqPMMD.getStyleClass().add("card");
        izqPMMD.setPadding(new Insets(16));
        ScrollPane spIzq = UIPMMD.scroll(izqPMMD);
        spIzq.setPrefWidth(340);
        spIzq.setMinWidth(320);
        planoHostPMMD.getStyleClass().add("corte-canvas-host");
        planoHostPMMD.setAlignment(Pos.TOP_LEFT);
        VBox derPMMD = new VBox(14, planoHostPMMD, listaPiezasPMMD);
        ScrollPane spDer = UIPMMD.scroll(derPMMD);
        HBox.setHgrow(spDer, Priority.ALWAYS);
        spDer.widthProperty().addListener((oPMMD, aPMMD, bPMMD) -> redibujarPMMD());
        HBox cuerpoPMMD = new HBox(14, spIzq, spDer);
        VBox.setVgrow(cuerpoPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(new ProduccionTabsPMMD(ModuloPMMD.CORTE, navigatorPMMD, calculadoraPMMD::abrir), cuerpoPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        List<CorteServicePMMD.Fuente> fsPMMD = cortePMMD.fuentes();
        fuentePMMD.getItems().setAll(fsPMMD);
        CorteServicePMMD.Fuente elegidaPMMD = fsPMMD.isEmpty() ? null : fsPMMD.get(0);
        if (intentPMMD.dato() != null) {
            String dPMMD = intentPMMD.dato().toString();
            elegidaPMMD = fsPMMD.stream().filter(fPMMD -> (fPMMD.op() != null && fPMMD.op().getCodigo().equals(dPMMD)) || (fPMMD.ficha() != null && fPMMD.ficha().getUid().equals(dPMMD)))
                    .findFirst().orElse(elegidaPMMD);
        } else if (fuentePMMD.getValue() != null) {
            CorteServicePMMD.Fuente actualPMMD = fuentePMMD.getValue();
            elegidaPMMD = fsPMMD.stream().filter(fPMMD -> fPMMD.codigo().equals(actualPMMD.codigo())).findFirst().orElse(elegidaPMMD);
        }
        fuentePMMD.setValue(null);
        fuentePMMD.setValue(elegidaPMMD);
    }

    private void cargarFuentePMMD() {
        CorteServicePMMD.Fuente fPMMD = fuentePMMD.getValue();
        if (fPMMD == null) return;
        conjuntoPMMD.setDisable(fPMMD.op() == null);
        todosPMMD = cortePMMD.materiales(fPMMD, conjuntoPMMD.isSelected() && fPMMD.op() != null);
        cortadasPMMD.clear();
        Set<TipoCortePMMD> presentesPMMD = new HashSet<>();
        todosPMMD.forEach(mPMMD -> presentesPMMD.add(mPMMD.tipo()));
        int idxPMMD = 0;
        String primeroPMMD = null;
        for (Node nPMMD : tiposPMMD.getChildren()) {
            TipoCortePMMD t = TipoCortePMMD.values()[idxPMMD++];
            nPMMD.setDisable(!presentesPMMD.contains(t));
            if (primeroPMMD == null && presentesPMMD.contains(t)) primeroPMMD = t.nombre();
        }
        String actualPMMD = tiposPMMD.getValor();
        boolean actualValidoPMMD = presentesPMMD.stream().anyMatch(t -> t.nombre().equals(actualPMMD));
        if (!actualValidoPMMD && primeroPMMD != null) tiposPMMD.setValor(primeroPMMD);
        else filtrarMaterialesPMMD();
    }

    private TipoCortePMMD tipoActualPMMD() {
        for (TipoCortePMMD t : TipoCortePMMD.values()) if (t.nombre().equals(tiposPMMD.getValor())) return t;
        return TipoCortePMMD.ESPUMA;
    }

    private void filtrarMaterialesPMMD() {
        TipoCortePMMD t = tipoActualPMMD();
        materialesPMMD.getItems().setAll(todosPMMD.stream().filter(mPMMD -> mPMMD.tipo() == t).toList());
        if (!materialesPMMD.getItems().isEmpty()) materialesPMMD.getSelectionModel().select(0);
        else {
            planoHostPMMD.getChildren().setAll(UIPMMD.empty("CONTENT_CUT", "Sin piezas de " + t.nombre().toLowerCase(), "Esta ficha no usa ese tipo de material.", null));
            listaPiezasPMMD.getChildren().clear();
        }
    }



    private void optimizar(boolean alternarPMMD) {
        CorteServicePMMD.MaterialCorte mc = materialesPMMD.getSelectionModel().getSelectedItem();
        if (mc == null) return;
        if (alternarPMMD) estrategiaPMMD = estrategiaPMMD.otra();
        cortadasPMMD.clear();
        StackPane overlayPMMD = new StackPane(new VBox(10, new ProgressIndicator(), UIPMMD.label("Optimizando cortes…", "card-title")));
        ((VBox) overlayPMMD.getChildren().get(0)).setAlignment(Pos.CENTER);
        overlayPMMD.getStyleClass().add("overlay-busy");
        overlayPMMD.setMinHeight(220);
        planoHostPMMD.getChildren().add(overlayPMMD);
        CortePMMD.Estrategia est = estrategiaPMMD;
        Task<Object> t = new Task<>() {

            @Override
            protected Object call() {
                return mc.tipo() == TipoCortePMMD.MADERA ? cortePMMD.optimizarMadera(mc) : cortePMMD.optimizar(mc, est);
            }
        };
        t.setOnSucceeded(ePMMD -> {
            PauseTransition pPMMD = new PauseTransition(Duration.millis(alternarPMMD ? 450 : 0));
            pPMMD.setOnFinished(xPMMD -> {
                if (t.getValue() instanceof CortePMMD.Resultado1D r1) {
                    resultado1DPMMD = r1;
                    resultadoPMMD = null;
                } else {
                    resultadoPMMD = (CortePMMD.Resultado) t.getValue();
                    resultado1DPMMD = null;
                }
                mostrarKpisPMMD(mc);
                redibujarPMMD();
                if (alternarPMMD) toastPMMD.info("Estrategia: " + est.nombre() + (resultadoPMMD != null ? " · orden " + resultadoPMMD.orden() : "") + ".");
            });
            pPMMD.play();
        });
        t.setOnFailed(ePMMD -> toastPMMD.error("No se pudo optimizar: " + t.getException().getMessage()));
        Thread hPMMD = new Thread(t, "corte");
        hPMMD.setDaemon(true);
        hPMMD.start();
    }

    private void mostrarKpisPMMD(CorteServicePMMD.MaterialCorte mc) {
        double estimadoPMMD = cortePMMD.estimadoFicha(fuentePMMD.getValue(), mc.material());
        String unidadPMMD = mc.material().getUnidad();
        String usadoPMMD, sobrantePMMD;
        double aprovPMMD;
        if (resultado1DPMMD != null) {
            usadoPMMD = resultado1DPMMD.tablas().size() + " tablas de 300 cm";
            sobrantePMMD = MoneyPMMD.numero(resultado1DPMMD.sobranteReutilizable()) + " cm";
            aprovPMMD = resultado1DPMMD.aprovechamiento();
        } else if (mc.tipo() == TipoCortePMMD.TELA) {
            usadoPMMD = MoneyPMMD.numero(resultadoPMMD.metros()) + " m de rollo";
            sobrantePMMD = MoneyPMMD.numero(resultadoPMMD.sobranteReutilizable()) + " m²";
            aprovPMMD = resultadoPMMD.aprovechamiento();
        } else {
            usadoPMMD = resultadoPMMD.laminas() + (resultadoPMMD.laminas() == 1 ? " plancha" : " planchas");
            sobrantePMMD = MoneyPMMD.numero(resultadoPMMD.sobranteReutilizable()) + " m²";
            aprovPMMD = resultadoPMMD.aprovechamiento();
        }
        String color = aprovPMMD >= 75 ? "#16A34A" : aprovPMMD >= 60 ? "#F59E0B" : "#DC2626";
        Label pctPMMD = UIPMMD.label(String.format("%.0f %%", aprovPMMD), "mini-stat-value");
        pctPMMD.setStyle("-fx-text-fill: " + color + ";");
        ProgressBarMiniPMMD barra = new ProgressBarMiniPMMD(aprovPMMD / 100, color, 8);
        VBox apPMMD = new VBox(4, UIPMMD.hbox(8, UIPMMD.label("Aprovechamiento", "mini-stat-label"), UIPMMD.spacer(), pctPMMD), barra);
        apPMMD.getStyleClass().add("mini-stat");
        kpisPMMD.getChildren().setAll(UIPMMD.hbox(8, UIPMMD.miniStat("Cantidad usada", usadoPMMD), UIPMMD.miniStat("Estima la ficha", MoneyPMMD.numero(estimadoPMMD) + " " + unidadPMMD)),
                UIPMMD.miniStat("Sobrante reutilizable", sobrantePMMD), apPMMD,
                UIPMMD.label(resultadoPMMD != null ? "Estrategia: " + resultadoPMMD.estrategia().nombre() + " · " + resultadoPMMD.orden() : "FFD con kerf de 5 mm", "cell-sub"));
    }

    private boolean oscuro() {
        return temaPMMD.isOscuro();
    }

    private void redibujarPMMD() {
        if (resultadoPMMD == null && resultado1DPMMD == null) return;
        double wPMMD = Math.max(520, planoHostPMMD.getWidth() > 0 ? planoHostPMMD.getWidth() - 24 : 760);
        canvasPMMD = resultado1DPMMD != null ? PlanoCortePMMD.dibujar1D(resultado1DPMMD, wPMMD, cortadasPMMD, oscuro()) : PlanoCortePMMD.dibujar2D(resultadoPMMD, wPMMD, cortadasPMMD, oscuro());
        planoHostPMMD.getChildren().setAll(canvasPMMD);
        renderPiezasPMMD();
    }

    private List<FilaPieza> filasPMMD() {
        List<CortePMMD.Colocacion> colsPMMD = new ArrayList<>();
        if (resultadoPMMD != null) colsPMMD.addAll(resultadoPMMD.colocaciones());
        if (resultado1DPMMD != null) resultado1DPMMD.tablas().forEach(t -> colsPMMD.addAll(t.cortes()));
        Map<String, List<CortePMMD.Colocacion>> gPMMD = new LinkedHashMap<>();
        colsPMMD.stream().sorted(Comparator.comparingInt(CortePMMD.Colocacion::numero)).forEach(cPMMD -> gPMMD.computeIfAbsent(cPMMD.pieza().origen() + "|" + cPMMD.pieza().nombre() + "|"
                + cPMMD.pieza().ancho() + "|" + cPMMD.pieza().alto(), kPMMD -> new ArrayList<>()).add(cPMMD));
        List<FilaPieza> lPMMD = new ArrayList<>();
        gPMMD.values().forEach(vPMMD -> lPMMD.add(new FilaPieza(vPMMD.stream().map(CortePMMD.Colocacion::numero).toList(), vPMMD.get(0).pieza(), vPMMD.get(0).pieza().ancho(),
                vPMMD.get(0).pieza().alto(), vPMMD.get(0).pieza().origen())));
        lPMMD.sort(Comparator.comparingInt(fPMMD -> fPMMD.numeros().get(0)));
        return lPMMD;
    }

    private void renderPiezasPMMD() {
        GridPane gPMMD = new GridPane();
        gPMMD.setHgap(12);
        gPMMD.setVgap(2);
        ColumnConstraints c1PMMD = new ColumnConstraints();
        c1PMMD.setHgrow(Priority.ALWAYS);
        gPMMD.getColumnConstraints().addAll(new ColumnConstraints(70), c1PMMD, new ColumnConstraints(120), new ColumnConstraints(50), new ColumnConstraints(90), new ColumnConstraints(70));
        gPMMD.addRow(0, UIPMMD.label("N.º", "dato-label"), UIPMMD.label("PIEZA", "dato-label"), UIPMMD.label("MEDIDAS (CM)", "dato-label"), UIPMMD.label("CANT.", "dato-label"),
                UIPMMD.label("ORIGEN", "dato-label"), UIPMMD.label("CORTADA", "dato-label"));
        int filaPMMD = 1;
        for (FilaPieza fPMMD : filasPMMD()) {
            Region puntoPMMD = new Region();
            puntoPMMD.getStyleClass().add("legend-dot");
            puntoPMMD.setStyle("-fx-background-color: " + fPMMD.pieza().componente().color() + ";");
            CheckBox ckPMMD = new CheckBox();
            boolean todasPMMD = cortadasPMMD.containsAll(fPMMD.numeros());
            ckPMMD.setSelected(todasPMMD);
            List<Node> celdasPMMD = List.of(UIPMMD.label(PlanoCortePMMD.rango(fPMMD.numeros()), "mono"), UIPMMD.hbox(8, puntoPMMD, UIPMMD.label(fPMMD.pieza().nombre(), "cell-title")),
                    UIPMMD.label(fPMMD.medidas(), "cell-sub"), UIPMMD.label(String.valueOf(fPMMD.numeros().size()), "cell-title"), UIPMMD.label(fPMMD.origen(), "code"), ckPMMD);
            ckPMMD.selectedProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
                if (bPMMD) cortadasPMMD.addAll(fPMMD.numeros());
                else cortadasPMMD.removeAll(fPMMD.numeros());
                redibujarPMMD();
            });
            int rPMMD = filaPMMD++;
            for (int iPMMD = 0; iPMMD < celdasPMMD.size(); iPMMD++) {
                gPMMD.add(celdasPMMD.get(iPMMD), iPMMD, rPMMD);
                if (todasPMMD) celdasPMMD.get(iPMMD).getStyleClass().add("pieza-cortada");
            }
        }
        listaPiezasPMMD.getChildren().setAll(UIPMMD.card(UIPMMD.cardHeader("Lista de piezas", "Marca cada pieza cuando esté cortada"), gPMMD));
    }

    private void guardarRetazo() {
        CorteServicePMMD.MaterialCorte mc = materialesPMMD.getSelectionModel().getSelectedItem();
        if (mc == null) return;
        double cantPMMD;
        String detallePMMD;
        if (resultado1DPMMD != null) {
            double cmPMMD = resultado1DPMMD.sobranteReutilizable();
            cantPMMD = Math.round(cmPMMD / 300 * 5 * 100) / 100.0;
            detallePMMD = MoneyPMMD.numero(cmPMMD) + " cm de tabla";
        } else {
            double m2PMMD = resultadoPMMD.sobranteReutilizable();
            cantPMMD = switch (mc.tipo()) {
                case ESPUMA -> m2PMMD / 2.0;
                case TABLERO -> m2PMMD / 2.9768;
                default -> m2PMMD / 1.4;
            };
            cantPMMD = Math.round(cantPMMD * 100) / 100.0;
            detallePMMD = MoneyPMMD.numero(m2PMMD) + " m² de " + mc.tipo().nombre().toLowerCase();
        }
        double cPMMD = cantPMMD;
        String dPMMD = detallePMMD;
        if (toastPMMD.intentar(() -> cortePMMD.guardarRetazo(mc.material(), cPMMD, fuentePMMD.getValue().codigo(), dPMMD))) {
            toastPMMD.exito("Retazo guardado en el kardex de " + mc.material().getCodigo() + ": " + dPMMD + ".");
        }
    }

    private void imprimir() {
        CorteServicePMMD.MaterialCorte mc = materialesPMMD.getSelectionModel().getSelectedItem();
        if (mc == null || canvasPMMD == null) return;
        File fPMMD = ArchivosPMMD.guardar(raiz, "Hoja-de-corte-" + fuentePMMD.getValue().codigo() + "-" + mc.material().getCodigo(), "PDF", "pdf");
        if (fPMMD == null) return;
        boolean oscuro = oscuro();
        Canvas paraPdfPMMD = resultado1DPMMD != null ? PlanoCortePMMD.dibujar1D(resultado1DPMMD, 1000, Set.of(), false) : PlanoCortePMMD.dibujar2D(resultadoPMMD, 1000, Set.of(), false);
        SnapshotParameters spPMMD = new SnapshotParameters();
        spPMMD.setFill(Color.WHITE);
        byte[] pngPMMD;
        try (ByteArrayOutputStream outPMMD = new ByteArrayOutputStream()) {
            ImageIO.write(SnapshotPMMD.imagen(paraPdfPMMD.snapshot(spPMMD, null)), "png", outPMMD);
            pngPMMD = outPMMD.toByteArray();
        } catch (Exception ePMMD) {
            pngPMMD = null;
        }
        byte[] imagenPMMD = pngPMMD;
        List<ExportServicePMMD.FilaCorte> piezasPMMD = filasPMMD().stream().map(xPMMD -> new ExportServicePMMD.FilaCorte(PlanoCortePMMD.rango(xPMMD.numeros()), xPMMD.pieza().nombre(),
                xPMMD.medidas(), xPMMD.numeros().size(), xPMMD.origen())).toList();
        String resumenPMMD = (resultado1DPMMD != null ? resultado1DPMMD.tablas().size() + " tablas" : resultadoPMMD.tipo() == TipoCortePMMD.TELA
                ? MoneyPMMD.numero(resultadoPMMD.metros()) + " m de rollo" : resultadoPMMD.laminas() + " planchas")
                + String.format(" · aprovechamiento %.0f %%", resultado1DPMMD != null ? resultado1DPMMD.aprovechamiento() : resultadoPMMD.aprovechamiento());
        TareasPMMD.ejecutar(overlaysPMMD, toastPMMD, "Generando hoja de corte…", () -> {
            exportPMMD.hojaCorte("Hoja de corte · " + mc.material().getNombre(), fuentePMMD.getValue().titulo() + " · " + mc.tipo().formato(), resumenPMMD, imagenPMMD, piezasPMMD, fPMMD);
            return fPMMD;
        }, aPMMD -> {
            toastPMMD.exito("Hoja de corte guardada: " + aPMMD.getName());
            appWindowPMMD.hostServices().showDocument(aPMMD.toURI().toString());
        });
        if (oscuro) redibujarPMMD();
    }
}
