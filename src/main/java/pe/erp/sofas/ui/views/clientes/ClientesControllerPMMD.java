package pe.erp.sofas.ui.views.clientes;

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
import pe.erp.sofas.service.BusquedaServicePMMD;
import pe.erp.sofas.service.ClienteServicePMMD;
import pe.erp.sofas.ui.components.*;

import java.math.BigDecimal;
import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ClientesControllerPMMD implements VistaPMMD {

    private final ClienteServicePMMD clientesPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private SearchFieldPMMD buscadorPMMD;

    private SegmentedControlPMMD tipoPMMD;

    private final TableView<ClientePMMD> tablaPMMD = CellsPMMD.tabla("Ningún cliente coincide.");

    private final Map<Long, ClienteServicePMMD.Resumen> resumenesPMMD = new HashMap<>();

    private final VBox kpisPMMD = new VBox();

    private OverlaysPMMD.Handle panelPMMD;

    @FXML
    public void initialize() {
        buscadorPMMD = new SearchFieldPMMD("Buscar por nombre, DNI/RUC o teléfono");
        buscadorPMMD.setPrefWidth(320);
        buscadorPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        tipoPMMD = new SegmentedControlPMMD("Todos", "Persona", "Empresa");
        tipoPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        Button nuevoPMMD = UIPMMD.primary("Nuevo cliente", "PERSON_ADD");
        nuevoPMMD.setOnAction(ePMMD -> formularioPMMD(null));
        TableColumn<ClientePMMD, String> totalPMMD = CellsPMMD.derecha("Total comprado", c -> MoneyPMMD.fmt(resumenesPMMD.get(c.getId()).totalComprado()), 120, "cell-strong");
        totalPMMD.setComparator(Comparator.comparing(MoneyPMMD::parse));
        TableColumn<ClientePMMD, String> saldo = CellsPMMD.derecha("Saldo pendiente", c -> MoneyPMMD.fmt(resumenesPMMD.get(c.getId()).saldo()), 120);
        saldo.setComparator(Comparator.comparing(MoneyPMMD::parse));
        tablaPMMD.getColumns().addAll(List.of(
                CellsPMMD.texto("Código", ClientePMMD::getCodigo, 76, "cell-mono"),
                CellsPMMD.nodo("Cliente", c -> UIPMMD.hbox(10, new AvatarInitialsPMMD(c.getNombre(), 30, c.getTipo() == ClientePMMD.Tipo.EMPRESA ? "navy" : "wood"),
                        UIPMMD.label(c.getNombre(), "cell-title")), 260),
                CellsPMMD.badge("Tipo", c -> c.getTipo() == ClientePMMD.Tipo.EMPRESA ? "Empresa" : "Persona", 90),
                CellsPMMD.texto("Documento", c -> c.getTipoDocumento() + " " + c.getDocumento(), 140, "cell-mono"),
                CellsPMMD.texto("Teléfono", ClientePMMD::getTelefono, 110),
                CellsPMMD.texto("Ciudad", ClientePMMD::getCiudad, 90, "cell-muted"),
                totalPMMD, saldo));
        tablaPMMD.setRowFactory(tvPMMD -> {
            TableRow<ClientePMMD> rPMMD = new TableRow<>();
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (!rPMMD.isEmpty()) fichaPMMD(rPMMD.getItem());
            });
            return rPMMD;
        });
        VBox cardPMMD = new VBox(tablaPMMD);
        VBox.setVgrow(tablaPMMD, Priority.ALWAYS);
        cardPMMD.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(cardPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(kpisPMMD, UIPMMD.hbox(10, buscadorPMMD, tipoPMMD, UIPMMD.spacer(), nuevoPMMD), cardPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        renderPMMD();
        if (intentPMMD.es(IntentPMMD.ACCION_NUEVO)) formularioPMMD(null);
        else if (intentPMMD.es(IntentPMMD.ACCION_ABRIR) && intentPMMD.dato() != null) {
            clientesPMMD.listar().stream().filter(c -> c.getCodigo().equals(intentPMMD.dato().toString())).findFirst().ifPresent(this::fichaPMMD);
        }
    }

    private void renderPMMD() {
        List<ClientePMMD> todosPMMD = clientesPMMD.listar();
        resumenesPMMD.clear();
        todosPMMD.forEach(c -> resumenesPMMD.put(c.getId(), clientesPMMD.resumen(c)));
        BigDecimal saldo = resumenesPMMD.values().stream().map(ClienteServicePMMD.Resumen::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPMMD = resumenesPMMD.values().stream().map(ClienteServicePMMD.Resumen::totalComprado).reduce(BigDecimal.ZERO, BigDecimal::add);
        kpisPMMD.getChildren().setAll(UIPMMD.fila(
                new KpiCardPMMD("PEOPLE", "blue", "Clientes", String.valueOf(todosPMMD.size()), todosPMMD.stream().filter(c -> c.getTipo() == ClientePMMD.Tipo.EMPRESA).count() + " empresas"),
                new KpiCardPMMD("ACCOUNT_BALANCE_WALLET", "green", "Total comprado", MoneyPMMD.fmt0(totalPMMD), "Ventas, pedidos y retapizados"),
                new KpiCardPMMD("PENDING_ACTIONS", "amber", "Saldo por cobrar", MoneyPMMD.fmt0(saldo), "Pedidos abiertos"),
                new KpiCardPMMD("STAR", "wood", "Mejor cliente", todosPMMD.stream().max(Comparator.comparing(c -> resumenesPMMD.get(c.getId()).totalComprado()))
                        .map(ClientePMMD::getNombre).orElse("—"), "Por monto comprado")));
        String qPMMD = BusquedaServicePMMD.normalizar(buscadorPMMD.getText());
        String t = tipoPMMD.getValor();
        tablaPMMD.getItems().setAll(todosPMMD.stream()
                .filter(c -> t == null || t.equals("Todos") || (t.equals("Empresa") == (c.getTipo() == ClientePMMD.Tipo.EMPRESA)))
                .filter(c -> qPMMD.isEmpty() || BusquedaServicePMMD.normalizar(c.getNombre() + " " + c.getDocumento() + " " + c.getTelefono() + " " + c.getCodigo()).contains(qPMMD))
                .toList());
    }

    private void fichaPMMD(ClientePMMD c) {
        if (panelPMMD != null) panelPMMD.cerrar();
        ClienteServicePMMD.Resumen rPMMD = clientesPMMD.resumen(c);
        GridPane datos = UIPMMD.grid(2);
        datos.add(UIPMMD.dato(c.getTipoDocumento(), c.getDocumento()), 0, 0);
        datos.add(UIPMMD.dato("Teléfono", c.getTelefono()), 1, 0);
        datos.add(UIPMMD.dato("Correo", c.getEmail()), 0, 1);
        datos.add(UIPMMD.dato("Dirección", c.getDireccion() + ", " + c.getCiudad()), 1, 1);
        datos.add(UIPMMD.dato("Cliente desde", AppClockPMMD.fmt(c.getFechaRegistro())), 0, 2);
        VBox cuerpoPMMD = new VBox(16, UIPMMD.hbox(12, new AvatarInitialsPMMD(c.getNombre(), 52, c.getTipo() == ClientePMMD.Tipo.EMPRESA ? "navy" : "gradient"),
                new VBox(2, UIPMMD.label(c.getNombre(), "card-title"), UIPMMD.label(c.getCodigo() + " · " + c.getTipo().nombre(), "cell-sub"))), datos,
                UIPMMD.hbox(8, UIPMMD.miniStat("Compras en tienda", String.valueOf(rPMMD.compras())), UIPMMD.miniStat("Pedidos", String.valueOf(rPMMD.pedidos())),
                        UIPMMD.miniStat("Saldo", MoneyPMMD.fmt0(rPMMD.saldo()))),
                UIPMMD.hbox(8, UIPMMD.label("Total comprado", "muted"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(rPMMD.totalComprado()), "price-value")));
        SegmentedControlPMMD tabsPMMD = new SegmentedControlPMMD("Pedidos", "Ventas", "Pagos");
        tabsPMMD.getStyleClass().add("tabs");
        VBox listaPMMD = new VBox(4);
        Runnable mostrarPMMD = () -> {
            listaPMMD.getChildren().clear();
            switch (tabsPMMD.getValor()) {
                case "Ventas" -> clientesPMMD.ventas(c).forEach(vPMMD -> listaPMMD.getChildren().add(fila("RECEIPT_LONG", vPMMD.getNumeroCompleto() + " · " + MoneyPMMD.fmt(vPMMD.getTotal()),
                        AppClockPMMD.fmt(vPMMD.getFecha().toLocalDate()) + " · " + vPMMD.getItems().stream().map(iPMMD -> iPMMD.getProducto().getNombre()).reduce((aPMMD, bPMMD) -> aPMMD + ", " + bPMMD).orElse(""),
                        new StatusBadgePMMD(vPMMD.getComprobante().nombre()), () -> navigatorPMMD.goTo("ventas"))));
                case "Pagos" -> clientesPMMD.pagos(c).forEach(pPMMD -> listaPMMD.getChildren().add(fila("PAYMENTS", MoneyPMMD.fmt(pPMMD.getMonto()) + " · " + pPMMD.getConcepto(),
                        AppClockPMMD.fmt(pPMMD.getFecha()) + " · " + pPMMD.getMetodo() + " · " + pPMMD.getReferencia(), new Region(), () -> {
                        })));
                default -> clientesPMMD.pedidos(c).forEach(pPMMD -> listaPMMD.getChildren().add(fila("ASSIGNMENT", pPMMD.getCodigo() + " · " + pPMMD.getProductoNombre(),
                        "Entrega " + AppClockPMMD.fmt(pPMMD.getFechaEntrega()) + " · " + MoneyPMMD.fmt(pPMMD.getPrecio()) + " · saldo " + MoneyPMMD.fmt(pPMMD.getSaldo()),
                        new StatusBadgePMMD(pPMMD.getEstado().nombre()), () -> navigatorPMMD.goTo("pedidos", IntentPMMD.abrir(pPMMD.getCodigo())))));
            }
            if (listaPMMD.getChildren().isEmpty()) listaPMMD.getChildren().add(UIPMMD.label("Sin registros.", "muted"));
        };
        tabsPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> mostrarPMMD.run());
        mostrarPMMD.run();
        cuerpoPMMD.getChildren().addAll(tabsPMMD, listaPMMD);
        Button pedidoPMMD = UIPMMD.primary("Nuevo pedido", "ASSIGNMENT");
        pedidoPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("pedidos", new IntentPMMD(IntentPMMD.ACCION_NUEVO, c)));
        Button ventaPMMD = UIPMMD.button("Nueva venta", "POINT_OF_SALE", "btn-wood");
        ventaPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("ventas", new IntentPMMD(IntentPMMD.ACCION_NUEVO, c)));
        Button editarPMMD = UIPMMD.secondary("Editar", "EDIT");
        editarPMMD.setOnAction(ePMMD -> {
            panelPMMD.cerrar();
            formularioPMMD(c);
        });
        Button eliminarPMMD = UIPMMD.iconButton("DELETE_OUTLINE", "Eliminar cliente", "btn-danger-soft");
        eliminarPMMD.setOnAction(ePMMD -> confirmPMMD.mostrar("¿Eliminar a " + c.getNombre() + "?", "Solo se pueden eliminar clientes sin ventas ni pedidos.", "Eliminar", () -> {
            if (toastPMMD.intentar(() -> clientesPMMD.eliminar(c.getId()))) {
                panelPMMD.cerrar();
                toastPMMD.exito(c.getNombre() + " eliminado.");
                renderPMMD();
            }
        }));
        panelPMMD = overlaysPMMD.panel("Ficha del cliente", c.getNombre(), cuerpoPMMD, UIPMMD.hbox(8, eliminarPMMD, UIPMMD.spacer(), editarPMMD, ventaPMMD, pedidoPMMD), 520);
    }

    private Node fila(String iconoPMMD, String tituloPMMD, String subPMMD, Node derecha, Runnable rPMMD) {
        StackPane ic = new StackPane(IcoPMMD.of(iconoPMMD, 16));
        ic.getStyleClass().add("popup-item-icon");
        VBox t = new VBox(1, UIPMMD.label(tituloPMMD, "cell-title"), UIPMMD.wrap(subPMMD, "cell-sub"));
        HBox.setHgrow(t, Priority.ALWAYS);
        HBox hPMMD = new HBox(10, ic, t, derecha);
        hPMMD.getStyleClass().add("alert-row");
        hPMMD.setOnMouseClicked(ePMMD -> rPMMD.run());
        return hPMMD;
    }

    private void formularioPMMD(ClientePMMD existentePMMD) {
        ClientePMMD c = existentePMMD == null ? ClientePMMD.builder().tipo(ClientePMMD.Tipo.PERSONA).ciudad("Juliaca").build() : existentePMMD;
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        ComboBox<ClientePMMD.Tipo> tipoCPMMD = FormsPMMD.combo(List.of(ClientePMMD.Tipo.values()), c.getTipo(), ClientePMMD.Tipo::nombre);
        TextField nombre = UIPMMD.text(c.getNombre(), "Nombre completo o razón social");
        TextField docPMMD = FormsPMMD.digitos(c.getDocumento(), 11);
        TextField telPMMD = UIPMMD.text(c.getTelefono(), "951 234 567");
        TextField emailPMMD = UIPMMD.text(c.getEmail(), "correo@ejemplo.com");
        TextField dirPMMD = UIPMMD.text(c.getDireccion(), "Jr. …");
        TextField ciudadPMMD = UIPMMD.text(c.getCiudad(), "Juliaca");
        Label docLabelPMMD = UIPMMD.label("", "form-label");
        FormGridPMMD fPMMD = new FormGridPMMD(2);
        fPMMD.nodo(vPMMD.campo("Tipo", tipoCPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("DNI / RUC", docPMMD, () -> {
            String dPMMD = docPMMD.getText().trim();
            if (tipoCPMMD.getValue() == ClientePMMD.Tipo.PERSONA && !dPMMD.matches("\\d{8}")) return "El DNI debe tener 8 dígitos.";
            if (tipoCPMMD.getValue() == ClientePMMD.Tipo.EMPRESA && !dPMMD.matches("\\d{11}")) return "El RUC debe tener 11 dígitos.";
            return null;
        }), 1);
        fPMMD.nodo(vPMMD.campo("Nombre o razón social", nombre, () -> ValidacionPMMD.requerido(nombre, "el nombre")), 2);
        fPMMD.nodo(vPMMD.campo("Teléfono", telPMMD, () -> telPMMD.getText() == null || telPMMD.getText().isBlank() || telPMMD.getText().replace(" ", "").matches("\\+?\\d{6,12}") ? null : "Revisa el teléfono."), 1);
        fPMMD.nodo(vPMMD.campo("Correo", emailPMMD, () -> emailPMMD.getText() == null || emailPMMD.getText().isBlank() || emailPMMD.getText().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+") ? null : "Correo no válido."), 1);
        fPMMD.nodo(vPMMD.campo("Dirección", dirPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Ciudad", ciudadPMMD, null), 1);
        vPMMD.escuchar(tipoCPMMD.valueProperty());
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary(existentePMMD == null ? "Registrar cliente" : "Guardar cambios", "SAVE");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal(existentePMMD == null ? "Nuevo cliente" : "Editar " + c.getCodigo(), "Persona con DNI de 8 dígitos o empresa con RUC de 11", fPMMD, 580, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            c.setTipo(tipoCPMMD.getValue());
            c.setNombre(nombre.getText().trim());
            c.setDocumento(docPMMD.getText().trim());
            c.setTelefono(telPMMD.getText());
            c.setEmail(emailPMMD.getText());
            c.setDireccion(dirPMMD.getText());
            c.setCiudad(ciudadPMMD.getText());
            ClientePMMD[] gPMMD = new ClientePMMD[1];
            if (toastPMMD.intentar(() -> gPMMD[0] = clientesPMMD.guardar(c))) {
                hPMMD.cerrar();
                toastPMMD.exito(gPMMD[0].getCodigo() + " · " + gPMMD[0].getNombre() + (existentePMMD == null ? " registrado." : " actualizado."));
                renderPMMD();
            }
        });
    }
}
