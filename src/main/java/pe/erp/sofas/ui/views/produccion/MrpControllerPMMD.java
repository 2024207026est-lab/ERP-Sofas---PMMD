package pe.erp.sofas.ui.views.produccion;

import javafx.concurrent.Task;
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
import pe.erp.sofas.model.MaterialPMMD;
import pe.erp.sofas.model.ProveedorPMMD;
import pe.erp.sofas.service.MrpServicePMMD;
import pe.erp.sofas.service.ProveedorServicePMMD;
import pe.erp.sofas.ui.components.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class MrpControllerPMMD implements VistaPMMD {

    private final MrpServicePMMD mrpPMMD;

    private final ProveedorServicePMMD proveedoresPMMD;

    private final CalculadoraMaterialesPMMD calculadoraPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private final VBox kpisPMMD = new VBox();

    private DatePicker desdePMMD, hastaPMMD;

    private ComboBox<String> categoriaPMMD;

    private ComboBox<ProveedorPMMD> proveedorPMMD;

    private CheckBox soloFaltantesPMMD;

    private final Label notaPMMD = UIPMMD.label("", "cell-sub");

    private final HBarChartPMMD graficoPMMD = new HBarChartPMMD().colores("#14213D", "#16A34A").leyenda("Requerido", "Stock + tránsito").porFila(true);

    private final TableView<MrpServicePMMD.Fila> tablaPMMD = CellsPMMD.tabla("Sin materiales requeridos con estos filtros.");

    private final StackPane tablaHostPMMD = new StackPane();

    private MrpServicePMMD.Resultado ultimoPMMD;

    private int consultaPMMD;

    @FXML
    public void initialize() {
        desdePMMD = FormsPMMD.fecha(null);
        desdePMMD.setPromptText("Entrega desde");
        hastaPMMD = FormsPMMD.fecha(null);
        hastaPMMD.setPromptText("Entrega hasta");
        desdePMMD.setPrefWidth(150);
        hastaPMMD.setPrefWidth(150);
        List<String> catsPMMD = new ArrayList<>();
        catsPMMD.add(null);
        catsPMMD.addAll(MaterialPMMD.CATEGORIAS);
        categoriaPMMD = FormsPMMD.combo(catsPMMD, null, cPMMD -> cPMMD == null ? "Todas las categorías" : cPMMD);
        categoriaPMMD.setPrefWidth(190);
        List<ProveedorPMMD> psPMMD = new ArrayList<>();
        psPMMD.add(null);
        psPMMD.addAll(proveedoresPMMD.listar());
        proveedorPMMD = FormsPMMD.combo(psPMMD, null, pPMMD -> pPMMD == null ? "Todos los proveedores" : pPMMD.getRazonSocial());
        proveedorPMMD.setPrefWidth(220);
        soloFaltantesPMMD = new CheckBox("Solo faltantes");
        for (var pPMMD : List.of(desdePMMD.valueProperty(), hastaPMMD.valueProperty(), categoriaPMMD.valueProperty(), proveedorPMMD.valueProperty())) pPMMD.addListener((oPMMD, aPMMD, bPMMD) -> calcular());
        soloFaltantesPMMD.selectedProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcular());
        Button generar = UIPMMD.primary("Generar órdenes de compra", "SHOPPING_CART");
        generar.setOnAction(ePMMD -> generar());
        HBox barraPMMD = UIPMMD.hbox(10, desdePMMD, hastaPMMD, categoriaPMMD, proveedorPMMD, soloFaltantesPMMD, UIPMMD.spacer(), generar);
        construirTablaPMMD();
        VBox tablaCard = new VBox(tablaHostPMMD);
        tablaCard.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(tablaHostPMMD, Priority.ALWAYS);
        VBox graf = UIPMMD.card(UIPMMD.cardHeader("Materiales críticos", "Los 10 de mayor faltante valorizado"), graficoPMMD);
        HBox cuerpoPMMD = new HBox(14, graf, tablaCard);
        graf.setPrefWidth(350);
        graf.setMinWidth(340);
        graf.setMaxWidth(350);
        HBox.setHgrow(tablaCard, Priority.ALWAYS);
        VBox.setVgrow(cuerpoPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(new ProduccionTabsPMMD(ModuloPMMD.MRP, navigatorPMMD, calculadoraPMMD::abrir), kpisPMMD, barraPMMD, notaPMMD, cuerpoPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        calcular();
    }



    private void calcular() {
        int tokenPMMD = ++consultaPMMD;
        MrpServicePMMD.Filtro f = new MrpServicePMMD.Filtro(desdePMMD.getValue(), hastaPMMD.getValue(), categoriaPMMD.getValue(),
                proveedorPMMD.getValue() == null ? null : proveedorPMMD.getValue().getId(), soloFaltantesPMMD.isSelected());
        ProgressIndicator piPMMD = new ProgressIndicator();
        piPMMD.setMaxSize(40, 40);
        tablaHostPMMD.getChildren().setAll(tablaPMMD, cubiertaPMMD(piPMMD));
        Task<Object[]> t = new Task<>() {

            @Override
            protected Object[] call() {
                return new Object[]{mrpPMMD.calcular(f), mrpPMMD.comprasEnTransito()};
            }
        };
        t.setOnSucceeded(ePMMD -> {
            if (tokenPMMD != consultaPMMD) return;
            mostrar((MrpServicePMMD.Resultado) t.getValue()[0], (Long) t.getValue()[1]);
        });
        t.setOnFailed(ePMMD -> toastPMMD.error("No se pudo calcular el requerimiento: " + t.getException().getMessage()));
        Thread hPMMD = new Thread(t, "mrp");
        hPMMD.setDaemon(true);
        hPMMD.start();
    }

    private Node cubiertaPMMD(Node n) {
        StackPane sPMMD = new StackPane(n);
        sPMMD.getStyleClass().add("overlay-busy");
        return sPMMD;
    }

    private void mostrar(MrpServicePMMD.Resultado rPMMD, long transito) {
        ultimoPMMD = rPMMD;
        kpisPMMD.getChildren().setAll(UIPMMD.fila(
                new KpiCardPMMD("FACT_CHECK", "blue", "Materiales requeridos", String.valueOf(rPMMD.filas().size()), "Por las órdenes y pedidos"),
                new KpiCardPMMD("REPORT_PROBLEM", "red", "Con faltante", String.valueOf(rPMMD.conFaltante()), "Faltante neto mayor a cero"),
                new KpiCardPMMD("ACCOUNT_BALANCE_WALLET", "amber", "Valor del faltante", MoneyPMMD.fmt0(rPMMD.valorFaltante()), "A costo unitario"),
                new KpiCardPMMD("LOCAL_SHIPPING", "teal", "Compras en tránsito", String.valueOf(transito), "OC solicitadas y aprobadas")));
        notaPMMD.setText(rPMMD.ops() + " OP activas + " + rPMMD.pedidos() + (rPMMD.pedidos() == 1 ? " pedido confirmado" : " pedidos confirmados")
                + " sin orden · faltante = máx(0, requerido − stock − tránsito)");
        List<HBarChartPMMD.Fila> topPMMD = rPMMD.filas().stream().sorted(Comparator.comparingDouble(MrpServicePMMD.Fila::valorFaltante).reversed()
                        .thenComparing(f -> -f.requerido() / Math.max(0.01, f.stock() + f.transito()))).limit(10)
                .map(f -> new HBarChartPMMD.Fila(f.material().getNombre(), f.requerido(), f.stock() + f.transito(),
                        f.faltante() > 1e-6 ? "falta " + MoneyPMMD.numero(f.faltante()) + " " + f.material().getUnidad() : "cubierto", f.faltante() > 1e-6))
                .toList();
        graficoPMMD.setFilas(topPMMD);
        tablaPMMD.getItems().setAll(rPMMD.filas());
        tablaHostPMMD.getChildren().setAll(tablaPMMD);
    }

    private void construirTablaPMMD() {
        TableColumn<MrpServicePMMD.Fila, String> faltPMMD = CellsPMMD.derecha("Faltante neto", f -> MoneyPMMD.numero(f.faltante()), 100);
        faltPMMD.setCellFactory(cPMMD -> new TableCell<>() {

            @Override
            protected void updateItem(String itemPMMD, boolean emptyPMMD) {
                super.updateItem(itemPMMD, emptyPMMD);
                setText(null);
                if (emptyPMMD || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                MrpServicePMMD.Fila f = getTableRow().getItem();
                setGraphic(f.faltante() > 1e-6 ? new StatusBadgePMMD(MoneyPMMD.numero(f.faltante()) + " " + f.material().getUnidad(), "red")
                        : new StatusBadgePMMD("Cubierto", "green"));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        faltPMMD.setComparator(Comparator.comparingDouble(sPMMD -> Double.parseDouble(sPMMD.replace(",", ""))));
        tablaPMMD.getColumns().addAll(List.of(
                CellsPMMD.texto("Código", f -> f.material().getCodigo(), 66, "cell-mono"),
                CellsPMMD.texto("Material", f -> f.material().getNombre(), 200, "cell-strong"),
                numPMMD("Requerido", MrpServicePMMD.Fila::requerido),
                numPMMD("Stock", MrpServicePMMD.Fila::stock),
                numPMMD("Reservado", MrpServicePMMD.Fila::reservado),
                numPMMD("Disponible", MrpServicePMMD.Fila::disponible),
                numPMMD("Tránsito", MrpServicePMMD.Fila::transito),
                faltPMMD,
                CellsPMMD.texto("Se necesita", f -> AppClockPMMD.fmt(f.necesitaEl()), 90),
                CellsPMMD.texto("Proveedor sugerido", f -> f.proveedor() == null ? "—" : f.proveedor().getRazonSocial(), 170, "cell-muted")));
        tablaPMMD.setRowFactory(tvPMMD -> {
            TableRow<MrpServicePMMD.Fila> rPMMD = new TableRow<>();
            Tooltip tipPMMD = new Tooltip();
            rPMMD.itemProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
                if (bPMMD != null) {
                    tipPMMD.setText("Origen: " + String.join(", ", bPMMD.origenes()));
                    rPMMD.setTooltip(tipPMMD);
                }
            });
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (ePMMD.getClickCount() == 2 && !rPMMD.isEmpty()) navigatorPMMD.goTo("inventario", IntentPMMD.abrir(rPMMD.getItem().material().getCodigo()));
            });
            return rPMMD;
        });
    }

    private TableColumn<MrpServicePMMD.Fila, String> numPMMD(String tituloPMMD, java.util.function.ToDoubleFunction<MrpServicePMMD.Fila> f) {
        TableColumn<MrpServicePMMD.Fila, String> cPMMD = CellsPMMD.derecha(tituloPMMD, xPMMD -> MoneyPMMD.numero(f.applyAsDouble(xPMMD)), 74);
        cPMMD.setComparator(Comparator.comparingDouble(sPMMD -> Double.parseDouble(sPMMD.replace(",", ""))));
        return cPMMD;
    }

    private void generar() {
        if (ultimoPMMD == null) return;
        List<MrpServicePMMD.Fila> conFaltantePMMD = ultimoPMMD.filas().stream().filter(f -> f.faltante() > 1e-6).toList();
        if (conFaltantePMMD.isEmpty()) {
            toastPMMD.info("No hay faltantes: el requerimiento está cubierto por el stock y las compras en tránsito.");
            return;
        }
        long proveedoresNPMMD = conFaltantePMMD.stream().map(MrpServicePMMD.Fila::proveedor).distinct().count();
        confirmPMMD.mostrar(ConfirmDialogPMMD.Tipo.INFO, "¿Generar " + proveedoresNPMMD + " órdenes de compra?",
                "Se creará una OC en Borrador por proveedor con " + conFaltantePMMD.size() + " materiales, redondeando a la unidad de compra "
                        + "(madera de 50 en 50 pt, metros de 10 en 10, unidades de 10 en 10, kg de 5 en 5).", "Generar", () -> {
                    List<String>[] cPMMD = new List[1];
                    if (toastPMMD.intentar(() -> cPMMD[0] = mrpPMMD.generarOrdenes(conFaltantePMMD, "MRP"))) {
                        toastPMMD.exito("Órdenes de compra creadas en Borrador: " + String.join(", ", cPMMD[0]) + ".");
                        navigatorPMMD.datosCambiaron();
                        calcular();
                    }
                });
    }
}
