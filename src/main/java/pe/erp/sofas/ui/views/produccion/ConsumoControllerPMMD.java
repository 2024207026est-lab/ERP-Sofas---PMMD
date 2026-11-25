package pe.erp.sofas.ui.views.produccion;

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
import pe.erp.sofas.model.ConsumoRealPMMD;
import pe.erp.sofas.model.FichaTecnicaPMMD;
import pe.erp.sofas.service.ConsumoServicePMMD;
import pe.erp.sofas.ui.components.*;

import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ConsumoControllerPMMD implements VistaPMMD {

    private final ConsumoServicePMMD consumoPMMD;

    private final CalculadoraMaterialesPMMD calculadoraPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private final VBox kpisPMMD = new VBox();

    private ComboBox<String> cmbOpPMMD;

    private ComboBox<String> cmbProductoPMMD;

    private Button ajustarPMMD;

    private final HBarChartPMMD trabajadoresPMMD = new HBarChartPMMD().centrado(true);

    private final HBarChartPMMD productosPMMD = new HBarChartPMMD().centrado(true);

    private final TableView<ConsumoRealPMMD> tablaPMMD = CellsPMMD.tabla("Sin consumos registrados con estos filtros.");

    @FXML
    public void initialize() {
        cmbOpPMMD = FormsPMMD.combo(List.of(), null, sPMMD -> sPMMD == null ? "Todas las OP" : sPMMD);
        cmbOpPMMD.setPrefWidth(180);
        cmbProductoPMMD = FormsPMMD.combo(List.of(), null, sPMMD -> sPMMD == null ? "Todos los productos" : sPMMD);
        cmbProductoPMMD.setPrefWidth(240);
        cmbOpPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        cmbProductoPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        ajustarPMMD = UIPMMD.primary("Ajustar ficha con el promedio real", "TUNE");
        ajustarPMMD.setOnAction(ePMMD -> ajustarFicha());
        HBox barra = UIPMMD.hbox(10, cmbOpPMMD, cmbProductoPMMD, UIPMMD.label("Edita la columna «Real» para corregir un registro.", "cell-sub"), UIPMMD.spacer(), ajustarPMMD);
        construirTablaPMMD();
        VBox tablaCard = new VBox(tablaPMMD);
        tablaCard.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(tablaPMMD, Priority.ALWAYS);
        tablaCard.setMinHeight(300);
        VBox ranking1PMMD = UIPMMD.card(UIPMMD.cardHeader("Eficiencia por trabajador", "Desvío del consumo real · verde si consume menos, rojo si supera +10 %"), trabajadoresPMMD);
        VBox ranking2PMMD = UIPMMD.card(UIPMMD.cardHeader("Productos con mayor desvío", "Desvío valorizado por modelo"), productosPMMD);
        VBox contenidoPMMD = new VBox(14, kpisPMMD, UIPMMD.columnas(new double[]{1, 1}, ranking1PMMD, ranking2PMMD), barra, tablaCard);
        ScrollPane spPMMD = UIPMMD.scroll(contenidoPMMD);
        VBox.setVgrow(spPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(new ProduccionTabsPMMD(ModuloPMMD.CONSUMO, navigatorPMMD, calculadoraPMMD::abrir), spPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        List<ConsumoRealPMMD> todosPMMD = consumoPMMD.registros();
        List<String> opsPMMD = new ArrayList<>();
        opsPMMD.add(null);
        todosPMMD.stream().map(ConsumoRealPMMD::getOpCodigo).distinct().sorted(Comparator.reverseOrder()).forEach(opsPMMD::add);
        List<String> prodsPMMD = new ArrayList<>();
        prodsPMMD.add(null);
        todosPMMD.stream().map(ConsumoRealPMMD::getProducto).distinct().sorted().forEach(prodsPMMD::add);
        String opPMMD = cmbOpPMMD.getValue(), prPMMD = cmbProductoPMMD.getValue();
        cmbOpPMMD.getItems().setAll(opsPMMD);
        cmbProductoPMMD.getItems().setAll(prodsPMMD);
        cmbOpPMMD.setValue(opPMMD);
        cmbProductoPMMD.setValue(prPMMD);
        renderPMMD();
    }

    private List<ConsumoRealPMMD> filtradosPMMD() {
        return consumoPMMD.filtrar(cmbOpPMMD.getValue(), cmbProductoPMMD.getValue());
    }

    private void renderPMMD() {
        List<ConsumoRealPMMD> lPMMD = filtradosPMMD();
        ConsumoServicePMMD.Kpis kPMMD = consumoPMMD.kpis(lPMMD);
        kpisPMMD.getChildren().setAll(UIPMMD.fila(
                new KpiCardPMMD("COMPARE_ARROWS", kPMMD.desvioPromedioPct() > 10 ? "red" : "amber", "Desvío promedio valorizado", String.format("%+.1f %%", kPMMD.desvioPromedioPct()),
                        "Real frente a la ficha"),
                new KpiCardPMMD("CONTENT_CUT", "red", "Costo de sobremerma", MoneyPMMD.fmt0(kPMMD.costoSobremerma()), "Consumo por encima del estimado"),
                new KpiCardPMMD("BUILD", "blue", "Órdenes registradas", String.valueOf(kPMMD.ordenes()), "Con consumo real cerrado"),
                new KpiCardPMMD("WARNING", "wood", "Material con mayor desvío", kPMMD.materialMayorDesvio(), String.format("%+.1f %% sobre la ficha", kPMMD.desvioMaterialPct()))));
        trabajadoresPMMD.setFilas(consumoPMMD.porTrabajador(consumoPMMD.registros()).stream()
                .map(rPMMD -> new HBarChartPMMD.Fila(rPMMD.nombre(), rPMMD.desvioPct(), 0, String.format("%+.1f %%", rPMMD.desvioPct()), rPMMD.desvioPct() > 10)).toList());
        productosPMMD.setFilas(consumoPMMD.porProducto(consumoPMMD.registros()).stream().limit(8)
                .map(rPMMD -> new HBarChartPMMD.Fila(rPMMD.nombre(), rPMMD.desvioPct(), 0, String.format("%+.1f %%", rPMMD.desvioPct()), rPMMD.desvioPct() > 10)).toList());
        tablaPMMD.getItems().setAll(lPMMD);
        Optional<String> fichaPMMD = consumoPMMD.fichaComun(lPMMD);
        ajustarPMMD.setDisable(fichaPMMD.isEmpty());
        UIPMMD.tooltip(ajustarPMMD, fichaPMMD.map(fPMMD -> "Crea una nueva versión en Borrador de " + fPMMD + " con las cantidades × (real / estimado).")
                .orElse("Filtra por un producto u OP para ajustar su ficha."));
    }

    private void construirTablaPMMD() {
        TableColumn<ConsumoRealPMMD, ConsumoRealPMMD> realPMMD = CellsPMMD.nodo("Real", cPMMD -> {
            TextField tPMMD = FormsPMMD.decimal(cPMMD.getRealConsumido());
            tPMMD.getStyleClass().add("cell-field");
            tPMMD.setPrefWidth(84);
            Runnable commitPMMD = () -> {
                double vPMMD = ValidacionPMMD.valor(tPMMD);
                if (Math.abs(vPMMD - cPMMD.getRealConsumido()) > 1e-9) {
                    if (toastPMMD.intentar(() -> consumoPMMD.editarReal(cPMMD.getId(), vPMMD))) {
                        toastPMMD.exito("Consumo real de " + cPMMD.getMaterial().getNombre() + " en " + cPMMD.getOpCodigo() + " actualizado.");
                        renderPMMD();
                    }
                }
            };
            tPMMD.setOnAction(ePMMD -> commitPMMD.run());
            tPMMD.focusedProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
                if (!bPMMD) commitPMMD.run();
            });
            return tPMMD;
        }, 100);
        tablaPMMD.getColumns().addAll(List.of(
                CellsPMMD.texto("OP", ConsumoRealPMMD::getOpCodigo, 76, "cell-mono"),
                CellsPMMD.texto("Producto", ConsumoRealPMMD::getProducto, 170, "cell-strong"),
                CellsPMMD.texto("Responsable", ConsumoRealPMMD::getResponsable, 150),
                CellsPMMD.nodo("Material y fecha", cPMMD -> new VBox(0, UIPMMD.label(cPMMD.getMaterial().getNombre(), "cell-title"),
                        UIPMMD.label(cPMMD.getMaterial().getCodigo() + " · " + AppClockPMMD.fmt(cPMMD.getFecha()), "cell-sub")), 190),
                CellsPMMD.derecha("Estimado", cPMMD -> MoneyPMMD.numero(cPMMD.getEstimado()) + " " + cPMMD.getMaterial().getUnidad(), 100, "cell-muted"),
                realPMMD,
                CellsPMMD.derecha("Diferencia", cPMMD -> String.format("%+.2f", cPMMD.getDiferencia()), 80),
                CellsPMMD.nodo("Dif. %", cPMMD -> {
                    double pPMMD = cPMMD.getDiferenciaPct();
                    return new StatusBadgePMMD(String.format("%+.1f %%", pPMMD), pPMMD > 10 ? "red" : pPMMD < 0 ? "green" : pPMMD > 5 ? "amber" : "gray");
                }, 80)));
        tablaPMMD.setFixedCellSize(48);
        tablaPMMD.setPrefHeight(48 * 12 + 44);
    }

    private void ajustarFicha() {
        List<ConsumoRealPMMD> lPMMD = filtradosPMMD();
        Optional<String> fichaPMMD = consumoPMMD.fichaComun(lPMMD);
        if (fichaPMMD.isEmpty()) return;
        confirmPMMD.mostrar(ConfirmDialogPMMD.Tipo.INFO, "¿Ajustar " + fichaPMMD.get() + " con el promedio real?",
                "Se creará una nueva versión en Borrador con cada cantidad × (real / estimado) de " + lPMMD.stream().map(ConsumoRealPMMD::getOpCodigo).distinct().count()
                        + " órdenes. La versión aprobada no cambia hasta que apruebes la nueva.", "Crear versión", () -> {
                    FichaTecnicaPMMD[] fPMMD = new FichaTecnicaPMMD[1];
                    if (toastPMMD.intentar(() -> fPMMD[0] = consumoPMMD.ajustarFicha(fichaPMMD.get(), consumoPMMD.registros()))) {
                        toastPMMD.exito(fPMMD[0].getEtiqueta() + " creada en Borrador con el consumo real promedio.");
                        navigatorPMMD.goTo("fichas", IntentPMMD.abrir(fPMMD[0].getUid()));
                    }
                });
    }
}
