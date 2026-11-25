package pe.erp.sofas.ui.views.dashboard;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.EtapaPMMD;
import pe.erp.sofas.model.PedidoPMMD;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.components.*;

import java.util.List;
import java.util.Map;

@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class DashboardControllerPMMD implements VistaPMMD {

    private final DashboardServicePMMD dashboardPMMD;

    private final ProduccionServicePMMD produccionPMMD;

    private final NotificacionServicePMMD notificacionesPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox contenido;

    @FXML
    public void initialize() {
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        contenido.getChildren().setAll(kpis(), UIPMMD.columnas(new double[]{1.75, 1}, ventas12(), porModeloPMMD()),
                UIPMMD.columnas(new double[]{1.75, 1}, porEtapa(), alertas()),
                UIPMMD.columnas(new double[]{1, 1.45, 0.8}, entregas(), ultimosPedidos(), accesosPMMD()));
    }

    private Node kpis() {
        DashboardServicePMMD.Kpis kPMMD = dashboardPMMD.kpis();
        KpiCardPMMD ventas = new KpiCardPMMD("POINT_OF_SALE", "blue", "Ventas del mes", MoneyPMMD.fmt0(kPMMD.ventasMes()),
                String.format("%+.0f %% frente a septiembre", kPMMD.variacionPct())).tendencia(kPMMD.variacionPct() >= 0)
                .alClic(() -> navigatorPMMD.goTo("ventas"));
        KpiCardPMMD vendidosPMMD = new KpiCardPMMD("WEEKEND", "wood", "Sofás vendidos en el mes", String.valueOf(kPMMD.sofasVendidos()),
                "En tienda (POS)").alClic(() -> navigatorPMMD.goTo("ventas"));
        KpiCardPMMD fabPMMD = new KpiCardPMMD("BUILD", "violet", "Sofás en fabricación", String.valueOf(kPMMD.sofasFabricacion()),
                kPMMD.ordenesFabricacion() + " órdenes en el taller").alClic(() -> navigatorPMMD.goTo("produccion"));
        KpiCardPMMD pedPMMD = new KpiCardPMMD("ASSIGNMENT", "amber", "Pedidos a medida pendientes", String.valueOf(kPMMD.pedidosPendientesPMMD()),
                "Por fabricar o entregar").alClic(() -> navigatorPMMD.goTo("pedidos"));
        KpiCardPMMD telaPMMD = new KpiCardPMMD("PALETTE", "teal", "Metros de tela en stock", MoneyPMMD.numero(kPMMD.metrosTelaPMMD()) + " m",
                kPMMD.colores() + " colores activos").alClic(() -> navigatorPMMD.goTo("telas"));
        return UIPMMD.fila(ventas, vendidosPMMD, fabPMMD, pedPMMD, telaPMMD);
    }

    private Node ventas12() {
        List<FinanzasServicePMMD.Mes> mesesPMMD = dashboardPMMD.ventas12();
        var chartPMMD = ChartsPMMD.lineas(List.of(new ChartsPMMD.Serie("Ventas", mesesPMMD.stream().map(FinanzasServicePMMD.Mes::etiqueta).toList(),
                mesesPMMD.stream().map(FinanzasServicePMMD.Mes::ventas).toList())), MoneyPMMD::corto, true);
        chartPMMD.setPrefHeight(250);
        chartPMMD.setMinHeight(230);
        double totalPMMD = mesesPMMD.stream().mapToDouble(FinanzasServicePMMD.Mes::ventas).sum();
        VBox cardPMMD = UIPMMD.card(UIPMMD.cardHeader("Ventas de los últimos 12 meses", "Total " + MoneyPMMD.fmt0(totalPMMD) + " · tienda"), chartPMMD);
        VBox.setVgrow(chartPMMD, Priority.ALWAYS);
        return cardPMMD;
    }

    private Node porModeloPMMD() {
        var seg = dashboardPMMD.ventasPorModelo().stream().map(sPMMD -> new DonutChartPMMD.Segmento(sPMMD.nombre(), sPMMD.valor(), sPMMD.color())).toList();
        double totalPMMD = seg.stream().mapToDouble(DonutChartPMMD.Segmento::valor).sum();
        DonutChartPMMD dPMMD = new DonutChartPMMD(seg, 170, MoneyPMMD.corto(totalPMMD), "octubre");
        return UIPMMD.card(UIPMMD.cardHeader("Ventas por modelo", "Participación del mes"), dPMMD);
    }

    private Node porEtapa() {
        Map<EtapaPMMD, Long> mPMMD = produccionPMMD.porEtapa();
        CategoryAxis x = new CategoryAxis();
        x.setTickMarkVisible(false);
        long maxPMMD = mPMMD.values().stream().mapToLong(Long::longValue).max().orElse(1);
        NumberAxis y = new NumberAxis(0, maxPMMD + 1, 1);
        y.setMinorTickVisible(false);
        BarChart<String, Number> chartPMMD = new BarChart<>(x, y);
        chartPMMD.setLegendVisible(false);
        chartPMMD.setAnimated(false);
        chartPMMD.setCategoryGap(10);
        chartPMMD.setVerticalGridLinesVisible(false);
        XYChart.Series<String, Number> sPMMD = new XYChart.Series<>();
        for (EtapaPMMD ePMMD : EtapaPMMD.values()) sPMMD.getData().add(new XYChart.Data<>(corto(ePMMD), mPMMD.get(ePMMD)));
        chartPMMD.getData().add(sPMMD);
        int iPMMD = 0;
        for (XYChart.Data<String, Number> dPMMD : sPMMD.getData()) {
            EtapaPMMD ePMMD = EtapaPMMD.values()[iPMMD++];
            if (dPMMD.getNode() != null) {
                dPMMD.getNode().setStyle("-fx-bar-fill: " + ePMMD.color() + "; -fx-background-color: " + ePMMD.color() + ";");
                Tooltip.install(dPMMD.getNode(), new Tooltip(ePMMD.nombre() + ": " + dPMMD.getYValue() + " sofás"));
                dPMMD.getNode().setOnMouseClicked(evPMMD -> navigatorPMMD.goTo("produccion"));
            }
        }
        chartPMMD.setPrefHeight(240);
        return UIPMMD.card(UIPMMD.cardHeader("Producción por etapa", "Sofás en cada etapa del taller (incluye listos)"), chartPMMD);
    }

    private static String corto(EtapaPMMD ePMMD) {
        return switch (ePMMD) {
            case CORTE_MADERA -> "C. madera";
            case ARMADO -> "Armado";
            case CINCHADO -> "Cinchado";
            case CORTE_TELA -> "C. tela";
            case ACABADO -> "Acabado";
            case CONTROL_CALIDAD -> "Calidad";
            default -> ePMMD.nombre();
        };
    }

    private Node alertas() {
        VBox listaPMMD = new VBox();
        var todasPMMD = notificacionesPMMD.todas();
        for (var nPMMD : todasPMMD) {
            StackPane ic = new StackPane(IcoPMMD.of(nPMMD.icono(), 16));
            ic.getStyleClass().addAll("notif-icon", "notif-" + nPMMD.tipo());
            VBox t = new VBox(1, UIPMMD.wrap(nPMMD.titulo(), "popup-item-title"), UIPMMD.wrap(nPMMD.detalle(), "popup-item-sub"));
            HBox.setHgrow(t, Priority.ALWAYS);
            HBox filaPMMD = new HBox(12, ic, t, IcoPMMD.of("CHEVRON_RIGHT", 16));
            filaPMMD.getStyleClass().add("alert-row");
            filaPMMD.setOnMouseClicked(ePMMD -> navigatorPMMD.goTo(nPMMD.modulo(), nPMMD.dato() == null ? IntentPMMD.NONE : IntentPMMD.abrir(nPMMD.dato())));
            listaPMMD.getChildren().add(filaPMMD);
        }
        if (todasPMMD.isEmpty()) listaPMMD.getChildren().add(UIPMMD.empty("DONE_ALL", "Sin alertas", "Todo en orden en el taller y el almacén.", null));
        return UIPMMD.card(UIPMMD.cardHeader("Alertas", todasPMMD.size() + " por atender"), listaPMMD);
    }

    private Node entregas() {
        VBox listaPMMD = new VBox();
        for (var ePMMD : dashboardPMMD.proximasEntregas(6)) {
            VBox fecha = new VBox(0, UIPMMD.label(AppClockPMMD.diaMes(ePMMD.fecha()).split(" ")[0], "kpi-value"),
                    UIPMMD.label(AppClockPMMD.diaMes(ePMMD.fecha()).split(" ")[1], "cell-sub"));
            fecha.setAlignment(Pos.CENTER);
            fecha.setMinWidth(46);
            ((javafx.scene.control.Label) fecha.getChildren().get(0)).setStyle("-fx-font-size: 17px;");
            VBox t = new VBox(1, UIPMMD.label(ePMMD.titulo(), "cell-title"), UIPMMD.label(ePMMD.cliente() + " · " + ePMMD.codigo(), "cell-sub"));
            HBox.setHgrow(t, Priority.ALWAYS);
            HBox filaPMMD = new HBox(10, fecha, t, new StatusBadgePMMD(ePMMD.estado()));
            filaPMMD.getStyleClass().add("alert-row");
            filaPMMD.setOnMouseClicked(evPMMD -> navigatorPMMD.goTo(ePMMD.modulo(), IntentPMMD.abrir(ePMMD.codigo())));
            listaPMMD.getChildren().add(filaPMMD);
        }
        return UIPMMD.card(UIPMMD.cardHeader("Próximas entregas", "Pedidos con fecha comprometida"), listaPMMD);
    }

    private Node ultimosPedidos() {
        TableView<PedidoPMMD> t = CellsPMMD.tabla("Sin pedidos.");
        t.getColumns().addAll(List.of(
                CellsPMMD.texto("Pedido", PedidoPMMD::getCodigo, 80, "cell-mono"),
                CellsPMMD.texto("Cliente", pPMMD -> pPMMD.getCliente().getNombre(), 150, "cell-strong"),
                CellsPMMD.texto("Modelo", PedidoPMMD::getProductoNombre, 150),
                CellsPMMD.derecha("Total", pPMMD -> MoneyPMMD.fmt0(pPMMD.getPrecio()), 80),
                CellsPMMD.badge("Estado", pPMMD -> pPMMD.isRetrasado() ? "Retrasado" : pPMMD.getEstado().nombre(), 110)));
        t.getItems().setAll(dashboardPMMD.ultimosPedidos(6));
        t.setPrefHeight(46 * 6 + 42);
        t.setRowFactory(tvPMMD -> {
            var rPMMD = new javafx.scene.control.TableRow<PedidoPMMD>();
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (!rPMMD.isEmpty()) navigatorPMMD.goTo("pedidos", IntentPMMD.abrir(rPMMD.getItem().getCodigo()));
            });
            return rPMMD;
        });
        VBox cardPMMD = UIPMMD.card(UIPMMD.cardHeader("Últimos pedidos", null), t);
        cardPMMD.getStyleClass().add("table-card");
        cardPMMD.setStyle("-fx-padding: 16 0 0 0;");
        ((HBox) cardPMMD.getChildren().get(0)).setStyle("-fx-padding: 0 18 0 18;");
        return cardPMMD;
    }

    private Node accesosPMMD() {
        VBox vPMMD = new VBox(10,
                accesoPMMD("POINT_OF_SALE", "Nueva venta", "Abrir el POS", () -> navigatorPMMD.goTo("ventas")),
                accesoPMMD("ASSIGNMENT", "Nuevo pedido a medida", "Registrar cliente y medidas", () -> navigatorPMMD.goTo("pedidos", IntentPMMD.NEW)),
                accesoPMMD("MDOAL_3D_ROTATION", "Configurar sofá", "Diseñar en 3D", () -> navigatorPMMD.goTo("configurador")),
                accesoPMMD("BUILD", "Nueva orden de producción", "Planificar en el taller", () -> navigatorPMMD.goTo("produccion", IntentPMMD.NEW)));
        return UIPMMD.card(UIPMMD.cardHeader("Accesos rápidos", null), vPMMD);
    }

    private Node accesoPMMD(String iconoPMMD, String tituloPMMD, String subPMMD, Runnable rPMMD) {
        StackPane ic = new StackPane(IcoPMMD.of(iconoPMMD, 18));
        ic.getStyleClass().addAll("kpi-icon", "kpi-blue");
        HBox hPMMD = new HBox(10, ic, new VBox(1, UIPMMD.label(tituloPMMD, "quick-action-title"), UIPMMD.label(subPMMD, "cell-sub")));
        hPMMD.getStyleClass().add("quick-action");
        hPMMD.setFocusTraversable(true);
        hPMMD.setOnMouseClicked(ePMMD -> rPMMD.run());
        hPMMD.setOnKeyPressed(ePMMD -> {
            if (ePMMD.getCode() == javafx.scene.input.KeyCode.ENTER) rPMMD.run();
        });
        return hPMMD;
    }
}
