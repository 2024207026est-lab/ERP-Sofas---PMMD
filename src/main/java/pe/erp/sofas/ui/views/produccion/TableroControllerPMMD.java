package pe.erp.sofas.ui.views.produccion;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.EmpleadoServicePMMD;
import pe.erp.sofas.service.ProduccionServicePMMD;
import pe.erp.sofas.ui.components.*;

import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class TableroControllerPMMD implements VistaPMMD {

    private final ProduccionServicePMMD produccionPMMD;

    private final EmpleadoServicePMMD empleadosPMMD;

    private final OrdenesUIPMMD ordenesUIPMMD;

    private final CalculadoraMaterialesPMMD calculadoraPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private final VBox kpisPMMD = new VBox();

    private ComboBox<EmpleadoPMMD> cmbRespPMMD;

    private ComboBox<PrioridadPMMD> cmbPrioPMMD;

    private SegmentedControlPMMD vistaPMMD;

    private final StackPane contenidoPMMD = new StackPane();

    @FXML
    public void initialize() {
        List<EmpleadoPMMD> respPMMD = new ArrayList<>();
        respPMMD.add(null);
        respPMMD.addAll(empleadosPMMD.taller());
        cmbRespPMMD = FormsPMMD.combo(respPMMD, null, e -> e == null ? "Todos los responsables" : e.getNombre());
        cmbRespPMMD.setPrefWidth(230);
        cmbRespPMMD.valueProperty().addListener((o, aPMMD, bPMMD) -> renderPMMD());
        List<PrioridadPMMD> prPMMD = new ArrayList<>();
        prPMMD.add(null);
        prPMMD.addAll(List.of(PrioridadPMMD.values()));
        cmbPrioPMMD = FormsPMMD.combo(prPMMD, null, pPMMD -> pPMMD == null ? "Todas las prioridades" : "Prioridad " + pPMMD.nombre().toLowerCase());
        cmbPrioPMMD.setPrefWidth(200);
        cmbPrioPMMD.valueProperty().addListener((o, aPMMD, bPMMD) -> renderPMMD());
        vistaPMMD = SegmentedControlPMMD.conIconos(new String[]{"Kanban", "Tabla"}, new String[]{"VIEW_WEEK", "TABLE_VIEW"});
        vistaPMMD.setValor(PrefsPMMD.str(PrefsPMMD.VISTA_TABLERO, "Kanban"));
        vistaPMMD.valorProperty().addListener((o, aPMMD, bPMMD) -> {
            PrefsPMMD.setStr(PrefsPMMD.VISTA_TABLERO, bPMMD);
            renderPMMD();
        });
        Button nuevaPMMD = UIPMMD.primary("Nueva orden", "ADD");
        nuevaPMMD.setOnAction(e -> ordenesUIPMMD.nueva(this::renderPMMD));
        HBox barraPMMD = UIPMMD.hbox(10, cmbRespPMMD, cmbPrioPMMD, UIPMMD.label("Arrastra las tarjetas entre etapas o usa el detalle.", "cell-sub"), UIPMMD.spacer(), vistaPMMD, nuevaPMMD);
        VBox.setVgrow(contenidoPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(new ProduccionTabsPMMD(ModuloPMMD.PRODUCCION, navigatorPMMD, calculadoraPMMD::abrir), kpisPMMD, barraPMMD, contenidoPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        renderPMMD();
        if (intentPMMD.es(IntentPMMD.ACCION_NUEVO)) ordenesUIPMMD.nueva(this::renderPMMD);
        else if (intentPMMD.es(IntentPMMD.ACCION_ABRIR) && intentPMMD.dato() != null) {
            produccionPMMD.porCodigo(intentPMMD.dato().toString()).ifPresent(opPMMD -> ordenesUIPMMD.detalle(opPMMD, this::renderPMMD));
        }
    }

    private void renderPMMD() {
        if (vistaPMMD == null) return;
        var kPMMD = produccionPMMD.kpis();
        kpisPMMD.getChildren().setAll(UIPMMD.fila(
                new KpiCardPMMD("BUILD", "blue", "Órdenes activas", String.valueOf(kPMMD.activas()), "Pendientes y en producción"),
                new KpiCardPMMD("SCHEDULE", "green", "En tiempo", String.valueOf(kPMMD.enTiempo()), "Dentro de la fecha de entrega"),
                new KpiCardPMMD("ALARM", "red", "Retrasadas", String.valueOf(kPMMD.retrasadas()), "Con la entrega vencida"),
                new KpiCardPMMD("DONE_ALL", "teal", "Terminadas este mes", String.valueOf(kPMMD.terminadasMes()), "Pasaron a Listo en octubre"),
                new KpiCardPMMD("WEEKEND", "wood", "Sofás en proceso", String.valueOf(kPMMD.sofasEnProceso()), kPMMD.ordenesEnProceso() + " órdenes en etapas del taller")));
        List<OrdenProduccionPMMD> opsPMMD = produccionPMMD.ordenes().stream()
                .filter(o -> cmbRespPMMD.getValue() == null || (o.getResponsable() != null && o.getResponsable().getId().equals(cmbRespPMMD.getValue().getId())))
                .filter(o -> cmbPrioPMMD.getValue() == null || o.getPrioridad() == cmbPrioPMMD.getValue())
                .toList();
        if ("Tabla".equals(vistaPMMD.getValor())) contenidoPMMD.getChildren().setAll(tabla(opsPMMD));
        else contenidoPMMD.getChildren().setAll(kanban(opsPMMD));
    }

    private Node kanban(List<OrdenProduccionPMMD> opsPMMD) {
        HBox board = new HBox(14);
        board.getStyleClass().add("kanban-board");
        for (EtapaPMMD e : EtapaPMMD.values()) {
            List<OrdenProduccionPMMD> enEtapaPMMD = opsPMMD.stream().filter(o -> o.getEtapa() == e)
                    .sorted(Comparator.comparing(OrdenProduccionPMMD::getPrioridad).thenComparing(o -> o.getFechaEntrega() == null ? AppClockPMMD.hoy().plusYears(1) : o.getFechaEntrega()))
                    .toList();
            List<KanbanCardPMMD> cards = enEtapaPMMD.stream().map(o -> new KanbanCardPMMD(o, produccionPMMD.indicador(o), () -> ordenesUIPMMD.detalle(o, this::renderPMMD))).toList();
            board.getChildren().add(new KanbanColumnPMMD(e, cards, enEtapaPMMD.stream().mapToInt(OrdenProduccionPMMD::getCantidad).sum(),
                    (idPMMD, destinoPMMD) -> ordenesUIPMMD.mover(produccionPMMD.op(idPMMD), destinoPMMD, this::renderPMMD)));
        }
        ScrollPane spPMMD = new ScrollPane(board);
        spPMMD.setFitToHeight(true);
        spPMMD.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spPMMD.getStyleClass().add("page-scroll");
        return spPMMD;
    }

    private Node tabla(List<OrdenProduccionPMMD> opsPMMD) {
        TableView<OrdenProduccionPMMD> t = CellsPMMD.tabla("Sin órdenes con estos filtros.");
        TableColumn<OrdenProduccionPMMD, OrdenProduccionPMMD> etapaPMMD = CellsPMMD.nodo("Etapa", o -> {
            Region puntoPMMD = new Region();
            puntoPMMD.getStyleClass().add("legend-dot");
            puntoPMMD.setStyle("-fx-background-color: " + o.getEtapa().color() + ";");
            return UIPMMD.hbox(8, puntoPMMD, UIPMMD.label(o.getEtapa().nombre()));
        }, 170);
        etapaPMMD.setSortable(true);
        etapaPMMD.setComparator(Comparator.comparing(OrdenProduccionPMMD::getEtapa));
        t.getColumns().addAll(List.of(
                CellsPMMD.texto("Código", OrdenProduccionPMMD::getCodigo, 80, "cell-mono"),
                CellsPMMD.texto("Producto", o -> o.getProductoNombre() + " ×" + o.getCantidad(), 220, "cell-strong"),
                CellsPMMD.texto("Cliente", OrdenProduccionPMMD::getClienteNombre, 180),
                CellsPMMD.nodo("Tela", o -> o.getTela() == null ? UIPMMD.label("Según ficha", "cell-sub")
                        : UIPMMD.hbox(6, new ColorSwatchPMMD(o.getTela().getHex(), o.getTela().getTipo(), 16), UIPMMD.label(o.getTela().getNombre())), 170),
                etapaPMMD,
                CellsPMMD.texto("Responsable", o -> o.getResponsable() == null ? "—" : o.getResponsable().getNombre(), 170),
                CellsPMMD.texto("Entrega", o -> AppClockPMMD.fmt(o.getFechaEntrega()), 90),
                CellsPMMD.badge("Prioridad", o -> o.getPrioridad().nombre(), 80),
                CellsPMMD.nodo("Materiales", o -> {
                    var iPMMD = produccionPMMD.indicador(o);
                    return new StatusBadgePMMD(iPMMD.texto(), iPMMD.variante());
                }, 170),
                CellsPMMD.badge("Estado", OrdenProduccionPMMD::getEstado, 110)));
        t.getItems().setAll(opsPMMD);
        t.setRowFactory(tvPMMD -> {
            TableRow<OrdenProduccionPMMD> rPMMD = new TableRow<>();
            rPMMD.setOnMouseClicked(e -> {
                if (!rPMMD.isEmpty()) ordenesUIPMMD.detalle(rPMMD.getItem(), this::renderPMMD);
            });
            return rPMMD;
        });
        VBox cardPMMD = new VBox(t);
        VBox.setVgrow(t, Priority.ALWAYS);
        cardPMMD.getStyleClass().addAll("card", "table-card");
        return cardPMMD;
    }
}
