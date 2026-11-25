package pe.erp.sofas.ui.views.finanzas;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.GastoPMMD;
import pe.erp.sofas.model.OrdenCompraPMMD;
import pe.erp.sofas.model.PedidoPMMD;
import pe.erp.sofas.service.FinanzasServicePMMD;
import pe.erp.sofas.ui.components.*;

import java.util.List;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class FinanzasControllerPMMD implements VistaPMMD {

    private final FinanzasServicePMMD finanzasPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private final VBox contenidoPMMD = new VBox(16);

    @FXML
    public void initialize() {
        ScrollPane spPMMD = UIPMMD.scroll(contenidoPMMD);
        VBox.setVgrow(spPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(spPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        FinanzasServicePMMD.Kpis kPMMD = finanzasPMMD.kpis();
        Node kpisPMMD = UIPMMD.fila(
                new KpiCardPMMD("TRENDING_UP", "green", "Ingresos del mes", MoneyPMMD.fmt0(kPMMD.ingresos()), "Ventas + cobros de pedidos").bordeSuperior("#16A34A"),
                new KpiCardPMMD("TRENDING_DOWN", "red", "Egresos del mes", MoneyPMMD.fmt0(kPMMD.egresos()), "Compras recibidas + gastos").bordeSuperior("#DC2626"),
                new KpiCardPMMD("ACCOUNT_BALANCE_WALLET", "blue", "Utilidad", MoneyPMMD.fmt0(kPMMD.utilidad()), String.format("Margen %.0f %%", kPMMD.ingresos() == 0 ? 0 : kPMMD.utilidad() * 100 / kPMMD.ingresos())).bordeSuperior("#2563EB"),
                new KpiCardPMMD("PENDING_ACTIONS", "amber", "Cuentas por cobrar", MoneyPMMD.fmt0(kPMMD.porCobrar()), finanzasPMMD.porCobrar().size() + " pedidos con saldo").bordeSuperior("#F59E0B")
                        .alClic(() -> navigatorPMMD.goTo("pedidos")),
                new KpiCardPMMD("RECEIPT_LONG", "wood", "Cuentas por pagar", MoneyPMMD.fmt0(kPMMD.porPagar()), finanzasPMMD.porPagar().size() + " órdenes de compra").bordeSuperior("#A47148")
                        .alClic(() -> navigatorPMMD.goTo("compras")));
        var mesesPMMD = finanzasPMMD.ultimos12();
        var chartPMMD = ChartsPMMD.barras(List.of(
                new ChartsPMMD.Serie("Ingresos", mesesPMMD.stream().map(FinanzasServicePMMD.Mes::etiqueta).toList(), mesesPMMD.stream().map(FinanzasServicePMMD.Mes::ingresos).toList()),
                new ChartsPMMD.Serie("Egresos", mesesPMMD.stream().map(FinanzasServicePMMD.Mes::etiqueta).toList(), mesesPMMD.stream().map(FinanzasServicePMMD.Mes::egresos).toList())),
                MoneyPMMD::corto, null);
        chartPMMD.setPrefHeight(280);
        VBox graficoPMMD = UIPMMD.card(UIPMMD.cardHeader("Ingresos frente a egresos", "Últimos 12 meses (octubre: en curso)"), chartPMMD);

        TableView<PedidoPMMD> cxcPMMD = CellsPMMD.tabla("Sin saldos por cobrar.");
        cxcPMMD.getColumns().addAll(List.of(
                CellsPMMD.texto("Pedido", PedidoPMMD::getCodigo, 80, "cell-mono"),
                CellsPMMD.texto("Cliente", pPMMD -> pPMMD.getCliente().getNombre(), 190, "cell-strong"),
                CellsPMMD.texto("Entrega", pPMMD -> AppClockPMMD.fmt(pPMMD.getFechaEntrega()), 90),
                CellsPMMD.derecha("Saldo", pPMMD -> MoneyPMMD.fmt(pPMMD.getSaldo()), 100, "cell-strong")));
        cxcPMMD.getItems().setAll(finanzasPMMD.porCobrar());
        altoPMMD(cxcPMMD, 6);
        cxcPMMD.setRowFactory(tvPMMD -> {
            TableRow<PedidoPMMD> rPMMD = new TableRow<>();
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (!rPMMD.isEmpty()) navigatorPMMD.goTo("pedidos", IntentPMMD.abrir(rPMMD.getItem().getCodigo()));
            });
            return rPMMD;
        });
        TableView<OrdenCompraPMMD> cxpPMMD = CellsPMMD.tabla("Sin cuentas por pagar.");
        cxpPMMD.getColumns().addAll(List.of(
                CellsPMMD.texto("OC", OrdenCompraPMMD::getCodigo, 80, "cell-mono"),
                CellsPMMD.texto("Proveedor", oPMMD -> oPMMD.getProveedor().getRazonSocial(), 190, "cell-strong"),
                CellsPMMD.texto("Condición", oPMMD -> oPMMD.getProveedor().getCondicionPago(), 110, "cell-muted"),
                CellsPMMD.derecha("Total", oPMMD -> MoneyPMMD.fmt(oPMMD.getTotal()), 100, "cell-strong")));
        cxpPMMD.getItems().setAll(finanzasPMMD.porPagar());
        altoPMMD(cxpPMMD, 6);
        Node cuentasPMMD = UIPMMD.columnas(new double[]{1, 1}, tarjetaPMMD("Cuentas por cobrar", "Saldos de pedidos abiertos", cxcPMMD),
                tarjetaPMMD("Cuentas por pagar", "OC aprobadas o recibidas sin pago", cxpPMMD));

        TableView<FinanzasServicePMMD.Rentabilidad> modeloPMMD = tablaRentPMMD("Modelo");
        modeloPMMD.getItems().setAll(finanzasPMMD.porModelo());
        altoPMMD(modeloPMMD, 8);
        TableView<FinanzasServicePMMD.Rentabilidad> telaPMMD = tablaRentPMMD("Tela");
        telaPMMD.getItems().setAll(finanzasPMMD.porTela());
        altoPMMD(telaPMMD, 6);
        Node rentPMMD = UIPMMD.columnas(new double[]{1.2, 1}, tarjetaPMMD("Rentabilidad por modelo", "Ventas y pedidos frente al costo estándar", modeloPMMD),
                tarjetaPMMD("Rentabilidad por tela", "Pedidos y metros consumidos por tipo", telaPMMD));

        TableView<GastoPMMD> gastosPMMD = CellsPMMD.tabla("Sin gastos.");
        gastosPMMD.getColumns().addAll(List.of(
                CellsPMMD.texto("Fecha", gPMMD -> AppClockPMMD.fmt(gPMMD.getFecha()), 90),
                CellsPMMD.texto("Categoría", GastoPMMD::getCategoria, 120),
                CellsPMMD.texto("Concepto", GastoPMMD::getConcepto, 300, "cell-muted"),
                CellsPMMD.derecha("Monto", gPMMD -> MoneyPMMD.fmt(gPMMD.getMonto()), 100, "cell-strong")));
        gastosPMMD.getItems().setAll(finanzasPMMD.gastosMes());
        altoPMMD(gastosPMMD, 5);
        contenidoPMMD.getChildren().setAll(kpisPMMD, graficoPMMD, cuentasPMMD, rentPMMD, tarjetaPMMD("Gastos del mes", "Egresos operativos de octubre", gastosPMMD));
    }

    private TableView<FinanzasServicePMMD.Rentabilidad> tablaRentPMMD(String tituloPMMD) {
        TableView<FinanzasServicePMMD.Rentabilidad> t = CellsPMMD.tabla("Sin datos.");
        t.getColumns().addAll(List.of(
                CellsPMMD.nodo(tituloPMMD, rPMMD -> new VBox(0, UIPMMD.label(rPMMD.nombre(), "cell-title"), UIPMMD.label(rPMMD.detalle(), "cell-sub")), 200),
                CellsPMMD.derecha("Unid.", rPMMD -> String.valueOf(rPMMD.unidades()), 60),
                CellsPMMD.derecha("Ingresos", rPMMD -> MoneyPMMD.fmt0(rPMMD.ingresos()), 100),
                CellsPMMD.derecha("Costo", rPMMD -> MoneyPMMD.fmt0(rPMMD.costo()), 100, "cell-muted"),
                CellsPMMD.derecha("Margen", rPMMD -> MoneyPMMD.fmt0(rPMMD.margen()), 100, "cell-strong"),
                CellsPMMD.nodo("%", rPMMD -> new StatusBadgePMMD(String.format("%.0f %%", rPMMD.margenPct()), rPMMD.margenPct() >= 35 ? "green" : rPMMD.margenPct() >= 20 ? "amber" : "red"), 70)));
        return t;
    }

    private static void altoPMMD(TableView<?> t, int filasPMMD) {
        t.setFixedCellSize(46);
        t.setPrefHeight(46 * Math.max(3, Math.min(filasPMMD, Math.max(t.getItems().size(), 3))) + 44);
    }

    private Node tarjetaPMMD(String tituloPMMD, String subPMMD, TableView<?> t) {
        VBox vPMMD = new VBox(10, UIPMMD.cardHeader(tituloPMMD, subPMMD), t);
        vPMMD.getStyleClass().add("card");
        vPMMD.setStyle("-fx-padding: 16 0 0 0;");
        ((HBox) vPMMD.getChildren().get(0)).setStyle("-fx-padding: 0 18;");
        return vPMMD;
    }
}
