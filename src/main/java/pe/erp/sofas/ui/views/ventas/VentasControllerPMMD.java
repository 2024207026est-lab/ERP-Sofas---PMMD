package pe.erp.sofas.ui.views.ventas;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.service.calculo.IgvPMMD;
import pe.erp.sofas.ui.components.*;
import pe.erp.sofas.ui.three.Render3DPMMD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class VentasControllerPMMD implements VistaPMMD {

    private static final class Item {
        
        final ProductoPMMD producto;
        
        int cantidad;

        Item(ProductoPMMD p) {
            this.producto = p;
            this.cantidad = 1;
        }

        BigDecimal subtotal() {
            return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        }
    }

    private static final List<String> METODOSPMMD = List.of("Efectivo", "Tarjeta", "Transferencia", "Yape", "Plin", "Mixto");

    private static final Map<String, String> ICONOSPMMD = Map.of("Efectivo", "PAYMENTS", "Tarjeta", "CREDIT_CARD", "Transferencia", "ACCOUNT_BALANCE",
            "Yape", "QR_CODE_2", "Plin", "SMARTPHONE", "Mixto", "CALL_SPLIT");

    private final ProductoServicePMMD productosPMMD;

    private final ClienteServicePMMD clientesPMMD;

    private final VentaServicePMMD ventasPMMD;

    private final ConfiguracionServicePMMD configuracionPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private SegmentedControlPMMD modoPMMD;

    private SearchFieldPMMD buscadorPMMD;

    private final ChipFilterBarPMMD chipsPMMD = new ChipFilterBarPMMD();

    private final TilePane gridPMMD = new TilePane(12, 12);

    private final StackPane contenidoPMMD = new StackPane();

    private final VBox historialPMMD = new VBox(10);

    private final List<Item> carritoPMMD = new ArrayList<>();

    private final VBox carritoBoxPMMD = new VBox();

    private final VBox totalesPMMD = new VBox(6);

    private ComboBox<ClientePMMD> clientePMMD;

    private SegmentedControlPMMD comprobantePMMD;

    private SegmentedControlPMMD tipoDescuentoPMMD;

    private TextField descuentoPMMD;

    private final ToggleGroup metodosPMMD = new ToggleGroup();

    private final VBox detallePagoPMMD = new VBox(8);

    private TextField recibidoPMMD, monto1PMMD, monto2PMMD;

    private ComboBox<String> metodo2PMMD;

    private Button cobrar;

    private final Label errorCobroPMMD = UIPMMD.label("", "field-error");

    @FXML
    public void initialize() {
        modoPMMD = SegmentedControlPMMD.conIconos(new String[]{"Vender", "Historial de ventas"}, new String[]{"POINT_OF_SALE", "RECEIPT_LONG"});
        modoPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> mostrarModoPMMD());
        buscadorPMMD = new SearchFieldPMMD("Buscar modelo o código");
        buscadorPMMD.setPrefWidth(260);
        buscadorPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderGridPMMD());
        chipsPMMD.seleccionProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderGridPMMD());
        gridPMMD.setPrefTileWidth(196);
        gridPMMD.setTileAlignment(Pos.TOP_LEFT);
        ScrollPane spGrid = UIPMMD.scroll(gridPMMD);
        VBox venderPMMD = new VBox(10, UIPMMD.hbox(10, buscadorPMMD, UIPMMD.spacer()), chipsPMMD, spGrid);
        VBox.setVgrow(spGrid, Priority.ALWAYS);
        contenidoPMMD.getChildren().setAll(venderPMMD, historialPMMD);
        historialPMMD.setVisible(false);
        VBox.setVgrow(contenidoPMMD, Priority.ALWAYS);
        VBox izq = new VBox(12, modoPMMD, contenidoPMMD);
        HBox.setHgrow(izq, Priority.ALWAYS);
        VBox der = construirCarritoPMMD();
        HBox cuerpoPMMD = new HBox(16, izq, der);
        VBox.setVgrow(cuerpoPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(cuerpoPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        List<ProductoPMMD> todosPMMD = productosPMMD.listar();
        List<ChipFilterBarPMMD.Chip> csPMMD = new ArrayList<>();
        csPMMD.add(new ChipFilterBarPMMD.Chip("*", "Todos", todosPMMD.size()));
        for (String c : ProductoPMMD.CATEGORIAS) {
            long n = todosPMMD.stream().filter(p -> p.getCategoria().equals(c)).count();
            if (n > 0) csPMMD.add(new ChipFilterBarPMMD.Chip(c, c, n));
        }
        chipsPMMD.setChips(csPMMD);
        ClientePMMD selPMMD = clientePMMD.getValue();
        List<ClientePMMD> listaPMMD = new ArrayList<>();
        listaPMMD.add(null);
        listaPMMD.addAll(clientesPMMD.listar());
        clientePMMD.getItems().setAll(listaPMMD);
        clientePMMD.setValue(selPMMD);
        if (intentPMMD.es("AGREGAR") && intentPMMD.dato() != null) {
            modoPMMD.setValor("Vender");
            productosPMMD.porCodigo(intentPMMD.dato().toString()).ifPresent(this::agregarPMMD);
        }
        if (intentPMMD.es(IntentPMMD.ACCION_NUEVO) && intentPMMD.dato() instanceof ClientePMMD c) {
            modoPMMD.setValor("Vender");
            clientePMMD.setValue(clientePMMD.getItems().stream().filter(xPMMD -> xPMMD != null && xPMMD.getId().equals(c.getId())).findFirst().orElse(null));
        }
        renderGridPMMD();
        renderCarritoPMMD();
        if ("Historial de ventas".equals(modoPMMD.getValor())) renderHistorialPMMD();
    }

    private void mostrarModoPMMD() {
        boolean histPMMD = "Historial de ventas".equals(modoPMMD.getValor());
        contenidoPMMD.getChildren().get(0).setVisible(!histPMMD);
        historialPMMD.setVisible(histPMMD);
        if (histPMMD) renderHistorialPMMD();
    }



    private void renderGridPMMD() {
        String qPMMD = BusquedaServicePMMD.normalizar(buscadorPMMD.getText());
        String catPMMD = chipsPMMD.getSeleccion();
        gridPMMD.getChildren().clear();
        for (ProductoPMMD p : productosPMMD.listar()) {
            if (catPMMD != null && !catPMMD.equals("*") && !p.getCategoria().equals(catPMMD)) continue;
            if (!qPMMD.isEmpty() && !BusquedaServicePMMD.normalizar(p.getNombre() + " " + p.getCodigo()).contains(qPMMD)) continue;
            gridPMMD.getChildren().add(tarjetaPMMD(p));
        }
    }

    private int enCarritoPMMD(ProductoPMMD p) {
        return carritoPMMD.stream().filter(iPMMD -> iPMMD.producto.getId().equals(p.getId())).mapToInt(iPMMD -> iPMMD.cantidad).sum();
    }

    private Node tarjetaPMMD(ProductoPMMD p) {
        ImageSlotPMMD img = new ImageSlotPMMD("Foto: " + p.getNombre(), 180, 116, 10).soloLectura();
        if (p.getImagenPrincipal() == null) img.respaldo(() -> Render3DPMMD.modelo(p));
        img.mostrar(p.getImagenPrincipal());
        img.setMouseTransparent(true);
        int disponiblePMMD = p.getStock() - enCarritoPMMD(p);
        Label stockPMMD = UIPMMD.label(p.getStock() <= 0 ? "Sin stock" : "Stock " + p.getStock(), "cell-sub");
        if (p.getStock() <= 0) stockPMMD.setStyle("-fx-text-fill: -is-danger-ink;");
        VBox v = new VBox(6, img, UIPMMD.label(p.getNombre(), "cell-title"), UIPMMD.hbox(6, UIPMMD.label(MoneyPMMD.fmt0(p.getPrecio()), "product-price"), UIPMMD.spacer(), stockPMMD));
        StackPane cardPMMD = new StackPane(v);
        cardPMMD.getStyleClass().add("pos-product");
        int enPMMD = enCarritoPMMD(p);
        if (enPMMD > 0) {
            Label qPMMD = UIPMMD.label(String.valueOf(enPMMD), "cart-qty");
            StackPane.setAlignment(qPMMD, Pos.TOP_RIGHT);
            cardPMMD.getChildren().add(qPMMD);
        }
        if (p.getStock() <= 0) {
            cardPMMD.getStyleClass().add("pos-product-disabled");
            Tooltip.install(cardPMMD, new Tooltip("Sin stock: créalo como pedido a medida."));
        } else {
            cardPMMD.setOnMouseClicked(ePMMD -> {
                if (disponiblePMMD <= 0) toastPMMD.advertencia("No hay más stock de " + p.getNombre() + ".");
                else agregarPMMD(p);
            });
        }
        return cardPMMD;
    }

    private void agregarPMMD(ProductoPMMD p) {
        if (p.getStock() - enCarritoPMMD(p) <= 0) {
            toastPMMD.advertencia(p.getNombre() + " no tiene stock disponible.");
            return;
        }
        carritoPMMD.stream().filter(iPMMD -> iPMMD.producto.getId().equals(p.getId())).findFirst().ifPresentOrElse(iPMMD -> iPMMD.cantidad++, () -> carritoPMMD.add(new Item(p)));
        renderCarritoPMMD();
        renderGridPMMD();
    }



    private VBox construirCarritoPMMD() {
        clientePMMD = FormsPMMD.combo(List.of(), null, c -> c == null ? "Cliente varios" : c.getNombre() + " · " + c.getTipoDocumento() + " " + c.getDocumento());
        clientePMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderCarritoPMMD());
        comprobantePMMD = new SegmentedControlPMMD("Boleta", "Factura");
        comprobantePMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderCarritoPMMD());
        tipoDescuentoPMMD = new SegmentedControlPMMD("%", "S/");
        tipoDescuentoPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderCarritoPMMD());
        descuentoPMMD = FormsPMMD.decimal(0);
        descuentoPMMD.setPrefWidth(90);
        descuentoPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderCarritoPMMD());
        FlowPane met = new FlowPane(6, 6);
        for (String m : METODOSPMMD) {
            ToggleButton t = new ToggleButton(m);
            t.setGraphic(IcoPMMD.of(ICONOSPMMD.get(m), 18));
            t.getStyleClass().add("pay-method");
            t.setPrefWidth(104);
            t.setUserData(m);
            t.setToggleGroup(metodosPMMD);
            met.getChildren().add(t);
        }
        metodosPMMD.selectToggle(metodosPMMD.getToggles().get(0));
        metodosPMMD.selectedToggleProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            if (bPMMD == null) metodosPMMD.selectToggle(aPMMD);
            renderPagoPMMD();
            renderCarritoPMMD();
        });
        recibidoPMMD = FormsPMMD.decimal(BigDecimal.ZERO);
        recibidoPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderCarritoPMMD());
        monto1PMMD = FormsPMMD.decimal(BigDecimal.ZERO);
        monto2PMMD = FormsPMMD.decimal(BigDecimal.ZERO);
        monto1PMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderCarritoPMMD());
        monto2PMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderCarritoPMMD());
        metodo2PMMD = FormsPMMD.combo(List.of("Yape", "Plin", "Tarjeta", "Transferencia"), "Yape", sPMMD -> sPMMD);
        cobrar = UIPMMD.button("Cobrar", "POINT_OF_SALE", "btn-primary", "btn-charge");
        cobrar.setMaxWidth(Double.MAX_VALUE);
        cobrar.setOnAction(ePMMD -> cobrar());
        Button vaciarPMMD = UIPMMD.button("Vaciar", "DELETE_OUTLINE", "btn-ghost", "btn-sm");
        vaciarPMMD.setOnAction(ePMMD -> {
            carritoPMMD.clear();
            renderCarritoPMMD();
            renderGridPMMD();
        });
        ScrollPane spItems = UIPMMD.scroll(carritoBoxPMMD);
        spItems.setPrefHeight(200);
        VBox.setVgrow(spItems, Priority.ALWAYS);
        renderPagoPMMD();
        VBox v = new VBox(12, UIPMMD.hbox(8, UIPMMD.label("Carrito", "card-title"), UIPMMD.spacer(), vaciarPMMD), UIPMMD.field("Cliente", clientePMMD), spItems,
                UIPMMD.hbox(8, UIPMMD.label("Descuento", "form-label"), UIPMMD.spacer(), tipoDescuentoPMMD, descuentoPMMD), totalesPMMD,
                UIPMMD.hbox(8, UIPMMD.label("Comprobante", "form-label"), UIPMMD.spacer(), comprobantePMMD), met, detallePagoPMMD, errorCobroPMMD, cobrar);
        v.getStyleClass().add("cart-panel");
        v.setPadding(new Insets(16));
        v.setPrefWidth(380);
        v.setMinWidth(360);
        v.setMaxWidth(390);
        return v;
    }

    private String metodoPMMD() {
        return metodosPMMD.getSelectedToggle() == null ? "Efectivo" : (String) metodosPMMD.getSelectedToggle().getUserData();
    }

    private void renderPagoPMMD() {
        detallePagoPMMD.getChildren().clear();
        switch (metodoPMMD()) {
            case "Efectivo" -> detallePagoPMMD.getChildren().add(UIPMMD.field("Monto recibido (S/)", recibidoPMMD));
            case "Mixto" -> {
                GridPane gPMMD = UIPMMD.grid(2);
                gPMMD.add(UIPMMD.field("Efectivo (S/)", monto1PMMD), 0, 0);
                gPMMD.add(UIPMMD.field("Monto (S/)", monto2PMMD), 1, 0);
                gPMMD.add(UIPMMD.field("Segundo método", metodo2PMMD), 0, 1, 2, 1);
                detallePagoPMMD.getChildren().add(gPMMD);
            }
            default -> detallePagoPMMD.getChildren().add(UIPMMD.label("Se registra el pago con " + metodoPMMD().toLowerCase() + " por el total.", "cell-sub"));
        }
    }

    private BigDecimal brutoPMMD() {
        return carritoPMMD.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal descuentoSolesPMMD() {
        BigDecimal v = FormsPMMD.dinero(descuentoPMMD);
        if (v == null) return BigDecimal.ZERO;
        if ("%".equals(tipoDescuentoPMMD.getValor())) return brutoPMMD().multiply(v).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return v;
    }

    private BigDecimal total() {
        return brutoPMMD().subtract(descuentoSolesPMMD()).max(BigDecimal.ZERO);
    }

    private void renderCarritoPMMD() {
        if (carritoBoxPMMD == null || clientePMMD == null) return;
        carritoBoxPMMD.getChildren().clear();
        if (carritoPMMD.isEmpty()) {
            carritoBoxPMMD.getChildren().add(UIPMMD.empty("SHOPPING_CART", "Carrito vacío", "Haz clic en un modelo para agregarlo.", null));
        }
        for (Item iPMMD : new ArrayList<>(carritoPMMD)) {
            Button menosPMMD = UIPMMD.iconButton("REMOVE", "Quitar uno", "btn-ghost", "btn-xs");
            Button masPMMD = UIPMMD.iconButton("ADD", "Agregar uno", "btn-ghost", "btn-xs");
            Button quitarPMMD = UIPMMD.iconButton("DELETE_OUTLINE", "Eliminar del carrito", "btn-ghost", "btn-xs");
            masPMMD.setDisable(iPMMD.cantidad >= iPMMD.producto.getStock());
            menosPMMD.setOnAction(ePMMD -> {
                iPMMD.cantidad--;
                if (iPMMD.cantidad <= 0) carritoPMMD.remove(iPMMD);
                renderCarritoPMMD();
                renderGridPMMD();
            });
            masPMMD.setOnAction(ePMMD -> {
                iPMMD.cantidad++;
                renderCarritoPMMD();
                renderGridPMMD();
            });
            quitarPMMD.setOnAction(ePMMD -> {
                carritoPMMD.remove(iPMMD);
                renderCarritoPMMD();
                renderGridPMMD();
            });
            HBox qtyPMMD = UIPMMD.hbox(2, menosPMMD, UIPMMD.label(String.valueOf(iPMMD.cantidad), "qty-value"), masPMMD);
            qtyPMMD.getStyleClass().add("qty-box");
            HBox fila = UIPMMD.hbox(8, new VBox(1, UIPMMD.label(iPMMD.producto.getNombre(), "cell-title"), UIPMMD.label(MoneyPMMD.fmt(iPMMD.producto.getPrecio()) + " c/u", "cell-sub")),
                    UIPMMD.spacer(), qtyPMMD, UIPMMD.label(MoneyPMMD.fmt0(iPMMD.subtotal()), "cell-title"), quitarPMMD);
            fila.getStyleClass().add("cart-item");
            carritoBoxPMMD.getChildren().add(fila);
        }
        BigDecimal total = total();
        BigDecimal subPMMD = IgvPMMD.subtotal(total);
        totalesPMMD.getChildren().setAll(
                fila("Subtotal (sin IGV)", MoneyPMMD.fmt(subPMMD)),
                fila("IGV 18 % (incluido)", MoneyPMMD.fmt(total.subtract(subPMMD))),
                fila("Descuento", "− " + MoneyPMMD.fmt(descuentoSolesPMMD())),
                UIPMMD.hbox(8, UIPMMD.label("Total", "card-title"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(total), "grand-total")));
        String errorPMMD = validarPMMD();
        errorCobroPMMD.setText(errorPMMD == null ? "" : errorPMMD);
        errorCobroPMMD.setVisible(errorPMMD != null && !carritoPMMD.isEmpty());
        errorCobroPMMD.setManaged(errorCobroPMMD.isVisible());
        cobrar.setDisable(errorPMMD != null);
        cobrar.setText(carritoPMMD.isEmpty() ? "Cobrar" : "Cobrar " + MoneyPMMD.fmt(total));
        if ("Efectivo".equals(metodoPMMD()) && FormsPMMD.dinero(recibidoPMMD) != null && FormsPMMD.dinero(recibidoPMMD).compareTo(total) >= 0 && total.signum() > 0) {
            totalesPMMD.getChildren().add(UIPMMD.hbox(8, UIPMMD.label("Vuelto", "text-success"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(FormsPMMD.dinero(recibidoPMMD).subtract(total)), "text-success", "semibold")));
        }
    }

    private HBox fila(String aPMMD, String bPMMD) {
        HBox hPMMD = UIPMMD.hbox(8, UIPMMD.label(aPMMD, "muted"), UIPMMD.spacer(), UIPMMD.label(bPMMD, "cell-title"));
        hPMMD.getStyleClass().add("total-row");
        return hPMMD;
    }

    private String validarPMMD() {
        if (carritoPMMD.isEmpty()) return "Agrega productos al carrito.";
        BigDecimal dPMMD = descuentoSolesPMMD();
        if (dPMMD.signum() < 0 || dPMMD.compareTo(brutoPMMD()) >= 0) return "El descuento no es válido.";
        if ("Factura".equals(comprobantePMMD.getValor()) && (clientePMMD.getValue() == null || !clientePMMD.getValue().tieneRuc())) {
            return "La factura exige un cliente con RUC.";
        }
        BigDecimal total = total();
        if ("Efectivo".equals(metodoPMMD())) {
            BigDecimal rPMMD = FormsPMMD.dinero(recibidoPMMD);
            if (rPMMD == null || rPMMD.compareTo(total) < 0) return "El monto recibido no cubre el total.";
        }
        if ("Mixto".equals(metodoPMMD())) {
            BigDecimal aPMMD = FormsPMMD.dinero(monto1PMMD), bPMMD = FormsPMMD.dinero(monto2PMMD);
            if (aPMMD == null || bPMMD == null || aPMMD.add(bPMMD).compareTo(total) != 0) return "Los dos montos deben sumar " + MoneyPMMD.fmt(total) + ".";
        }
        return null;
    }

    private void cobrar() {
        if (validarPMMD() != null) return;
        VentaPMMD[] v = new VentaPMMD[1];
        List<VentaServicePMMD.Linea> lineas = carritoPMMD.stream().map(iPMMD -> new VentaServicePMMD.Linea(iPMMD.producto.getId(), iPMMD.cantidad, null)).toList();
        if (toastPMMD.intentar(() -> v[0] = ventasPMMD.cobrar(new VentaServicePMMD.Cobro(clientePMMD.getValue() == null ? null : clientePMMD.getValue().getId(),
                "Factura".equals(comprobantePMMD.getValor()) ? VentaPMMD.Comprobante.FACTURA : VentaPMMD.Comprobante.BOLETA, lineas, descuentoSolesPMMD(), metodoPMMD(),
                FormsPMMD.dinero(recibidoPMMD), metodo2PMMD.getValue(), FormsPMMD.dinero(monto1PMMD), FormsPMMD.dinero(monto2PMMD))))) {
            toastPMMD.exito(v[0].getNumeroCompleto() + " emitida por " + MoneyPMMD.fmt(v[0].getTotal()) + " · stock actualizado.");
            navigatorPMMD.datosCambiaron();
            ticket(v[0]);
        }
    }

    private void nuevaVentaPMMD() {
        carritoPMMD.clear();
        descuentoPMMD.setText("0");
        recibidoPMMD.setText("0");
        monto1PMMD.setText("0");
        monto2PMMD.setText("0");
        clientePMMD.setValue(null);
        comprobantePMMD.setValor("Boleta");
        renderGridPMMD();
        renderCarritoPMMD();
    }



    private void ticket(VentaPMMD v) {
        EmpresaPMMD ePMMD = configuracionPMMD.empresa();
        VBox t = new VBox(4);
        t.getStyleClass().add("ticket");
        t.setPrefWidth(320);
        t.setMaxWidth(320);
        Label marcaPMMD = UIPMMD.label("SISTEMA ERP SOFAS · SOFÁS", "ticket-strong");
        t.getChildren().addAll(marcaPMMD, UIPMMD.label(ePMMD.getRazonSocial() + " · RUC " + ePMMD.getRuc()), UIPMMD.label(ePMMD.getDireccion()),
                reglaPMMD(), UIPMMD.label((v.getComprobante() == VentaPMMD.Comprobante.BOLETA ? "BOLETA DE VENTA" : "FACTURA") + " " + v.getNumeroCompleto(), "ticket-strong"),
                UIPMMD.label(AppClockPMMD.fmt(v.getFecha().toLocalDate()) + " " + v.getFecha().toLocalTime().toString().substring(0, 5) + " · " + v.getVendedor()),
                UIPMMD.label("Cliente: " + v.getClienteNombre()));
        if (v.getCliente() != null) t.getChildren().add(UIPMMD.label(v.getCliente().getTipoDocumento() + ": " + v.getCliente().getDocumento()));
        t.getChildren().add(reglaPMMD());
        for (ItemVentaPMMD iPMMD : v.getItems()) {
            t.getChildren().add(UIPMMD.label(iPMMD.getCantidad() + " × " + iPMMD.getProducto().getNombre()));
            t.getChildren().add(UIPMMD.hbox(4, UIPMMD.label("   " + MoneyPMMD.fmt(iPMMD.getPrecioUnitario()) + " c/u"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(iPMMD.getSubtotal()))));
        }
        t.getChildren().add(reglaPMMD());
        BigDecimal subPMMD = IgvPMMD.subtotal(v.getTotal());
        if (v.getDescuento() != null && v.getDescuento().signum() > 0) t.getChildren().add(UIPMMD.hbox(4, UIPMMD.label("Descuento"), UIPMMD.spacer(), UIPMMD.label("− " + MoneyPMMD.fmt(v.getDescuento()))));
        t.getChildren().addAll(UIPMMD.hbox(4, UIPMMD.label("Op. gravada"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(subPMMD))),
                UIPMMD.hbox(4, UIPMMD.label("IGV 18 %"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(v.getTotal().subtract(subPMMD)))),
                UIPMMD.hbox(4, UIPMMD.label("TOTAL", "ticket-total"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(v.getTotal()), "ticket-total")),
                UIPMMD.label("Pago: " + v.getMetodoPago()));
        if (v.getVuelto() != null) t.getChildren().add(UIPMMD.label("Recibido " + MoneyPMMD.fmt(v.getRecibido()) + " · vuelto " + MoneyPMMD.fmt(v.getVuelto())));
        t.getChildren().addAll(reglaPMMD(), UIPMMD.wrap("Ticket de prueba · sin validez tributaria", "ticket-muted"), UIPMMD.wrap("Garantía: 12 meses estructura · 6 meses tapiz y espuma", "ticket-muted"));
        StackPane fondoPMMD = new StackPane(t);
        fondoPMMD.getStyleClass().add("doc-backdrop");
        Button imprimirPMMD = UIPMMD.secondary("Imprimir", "PRINT");
        Button nuevaPMMD = UIPMMD.primary("Nueva venta", "ADD");
        var hPMMD = overlaysPMMD.modal("Venta registrada", v.getNumeroCompleto(), fondoPMMD, 440, imprimirPMMD, nuevaPMMD);
        imprimirPMMD.setOnAction(e2PMMD -> {
            PrinterJob jobPMMD = PrinterJob.createPrinterJob();
            if (jobPMMD != null && jobPMMD.showPrintDialog(raiz.getScene().getWindow())) {
                if (jobPMMD.printPage(t)) {
                    jobPMMD.endJob();
                    toastPMMD.exito("Ticket enviado a la impresora.");
                }
            } else if (jobPMMD == null) {
                toastPMMD.info("No hay impresora configurada en este equipo.");
            }
        });
        nuevaPMMD.setOnAction(e2PMMD -> {
            hPMMD.cerrar();
            nuevaVentaPMMD();
        });
        hPMMD.alCerrar(this::nuevaVentaPMMD);
    }

    private Region reglaPMMD() {
        Region rPMMD = new Region();
        rPMMD.getStyleClass().add("ticket-rule");
        VBox.setMargin(rPMMD, new Insets(4, 0, 4, 0));
        return rPMMD;
    }



    private void renderHistorialPMMD() {
        SearchFieldPMMD bPMMD = new SearchFieldPMMD("Buscar comprobante, cliente o modelo");
        bPMMD.setPrefWidth(300);
        ComboBox<String> tipoPMMD = FormsPMMD.combo(Arrays.asList(null, "Boleta", "Factura"), null, sPMMD -> sPMMD == null ? "Boletas y facturas" : sPMMD);
        TableView<VentaPMMD> t = CellsPMMD.tabla("Sin ventas.");
        TableColumn<VentaPMMD, String> total = CellsPMMD.derecha("Total", v -> MoneyPMMD.fmt(v.getTotal()), 100, "cell-strong");
        total.setComparator(Comparator.comparing(MoneyPMMD::parse));
        t.getColumns().addAll(List.of(
                CellsPMMD.texto("Comprobante", VentaPMMD::getNumeroCompleto, 120, "cell-mono"),
                CellsPMMD.texto("Fecha", v -> AppClockPMMD.fmt(v.getFecha().toLocalDate()) + " " + v.getFecha().toLocalTime().toString().substring(0, 5), 120),
                CellsPMMD.texto("Cliente", VentaPMMD::getClienteNombre, 200, "cell-strong"),
                CellsPMMD.texto("Productos", v -> v.getItems().stream().map(iPMMD -> iPMMD.getCantidad() + "× " + iPMMD.getProducto().getNombre()).reduce((aPMMD, c) -> aPMMD + ", " + c).orElse(""), 260, "cell-muted"),
                CellsPMMD.texto("Pago", VentaPMMD::getMetodoPago, 160),
                CellsPMMD.texto("Vendedor", VentaPMMD::getVendedor, 150, "cell-muted"),
                total, CellsPMMD.badge("Tipo", v -> v.getComprobante().nombre(), 80)));
        Runnable filtrarPMMD = () -> {
            String qPMMD = BusquedaServicePMMD.normalizar(bPMMD.getText());
            t.getItems().setAll(ventasPMMD.historial().stream()
                    .filter(v -> tipoPMMD.getValue() == null || v.getComprobante().nombre().equals(tipoPMMD.getValue()))
                    .filter(v -> qPMMD.isEmpty() || BusquedaServicePMMD.normalizar(v.getNumeroCompleto() + " " + v.getClienteNombre() + " "
                            + v.getItems().stream().map(iPMMD -> iPMMD.getProducto().getNombre()).reduce("", String::concat)).contains(qPMMD)).toList());
        };
        bPMMD.textProperty().addListener((oPMMD, aPMMD, c) -> filtrarPMMD.run());
        tipoPMMD.valueProperty().addListener((oPMMD, aPMMD, c) -> filtrarPMMD.run());
        filtrarPMMD.run();
        VBox cardPMMD = new VBox(t);
        VBox.setVgrow(t, Priority.ALWAYS);
        cardPMMD.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(cardPMMD, Priority.ALWAYS);
        historialPMMD.getChildren().setAll(UIPMMD.hbox(10, bPMMD, tipoPMMD, UIPMMD.spacer(), UIPMMD.label("Octubre: " + MoneyPMMD.fmt0(ventasPMMD.totalMes()) + " · " + ventasPMMD.sofasMes() + " unidades", "cell-title")), cardPMMD);
    }
}
