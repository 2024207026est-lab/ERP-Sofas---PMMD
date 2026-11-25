package pe.erp.sofas.ui.views.proveedores;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.ProveedorPMMD;
import pe.erp.sofas.service.BusquedaServicePMMD;
import pe.erp.sofas.service.ProveedorServicePMMD;
import pe.erp.sofas.ui.components.*;

import java.util.Comparator;
import java.util.List;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ProveedoresControllerPMMD implements VistaPMMD {

    private final ProveedorServicePMMD proveedoresPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private SearchFieldPMMD buscadorPMMD;

    private final TableView<ProveedorPMMD> tablaPMMD = CellsPMMD.tabla("Ningún proveedor coincide.");

    private OverlaysPMMD.Handle panelPMMD;

    @FXML
    public void initialize() {
        buscadorPMMD = new SearchFieldPMMD("Buscar proveedor, RUC o material");
        buscadorPMMD.setPrefWidth(320);
        buscadorPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        Button nuevoPMMD = UIPMMD.primary("Nuevo proveedor", "ADD");
        nuevoPMMD.setOnAction(ePMMD -> formularioPMMD(null));
        TableColumn<ProveedorPMMD, String> totalPMMD = CellsPMMD.derecha("Total comprado", pPMMD -> MoneyPMMD.fmt0(pPMMD.getTotalComprado()), 120, "cell-strong");
        totalPMMD.setComparator(Comparator.comparing(MoneyPMMD::parse));
        tablaPMMD.getColumns().addAll(List.of(
                CellsPMMD.texto("Código", ProveedorPMMD::getCodigo, 66, "cell-mono"),
                CellsPMMD.texto("Proveedor", ProveedorPMMD::getRazonSocial, 220, "cell-strong"),
                CellsPMMD.texto("RUC", ProveedorPMMD::getRuc, 110, "cell-mono"),
                CellsPMMD.nodo("Contacto", pPMMD -> new VBox(0, UIPMMD.label(pPMMD.getContacto(), "cell-title"), UIPMMD.label(pPMMD.getTelefono(), "cell-sub")), 150),
                CellsPMMD.texto("Materiales", ProveedorPMMD::getRubro, 260, "cell-muted"),
                CellsPMMD.texto("Condición", ProveedorPMMD::getCondicionPago, 120),
                CellsPMMD.badge("Estado", pPMMD -> pPMMD.getActivo() ? "Activo" : "Inactivo", 80), totalPMMD));
        tablaPMMD.setRowFactory(tvPMMD -> {
            TableRow<ProveedorPMMD> rPMMD = new TableRow<>();
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (!rPMMD.isEmpty()) fichaPMMD(rPMMD.getItem());
            });
            return rPMMD;
        });
        VBox cardPMMD = new VBox(tablaPMMD);
        VBox.setVgrow(tablaPMMD, Priority.ALWAYS);
        cardPMMD.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(cardPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(UIPMMD.hbox(10, buscadorPMMD, UIPMMD.spacer(), nuevoPMMD), cardPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        renderPMMD();
        if (intentPMMD.es(IntentPMMD.ACCION_NUEVO)) formularioPMMD(null);
    }

    private void renderPMMD() {
        String qPMMD = BusquedaServicePMMD.normalizar(buscadorPMMD.getText());
        tablaPMMD.getItems().setAll(proveedoresPMMD.listar().stream()
                .filter(pPMMD -> qPMMD.isEmpty() || BusquedaServicePMMD.normalizar(pPMMD.getRazonSocial() + " " + pPMMD.getRuc() + " " + pPMMD.getRubro() + " " + pPMMD.getCodigo()).contains(qPMMD)).toList());
    }

    private void fichaPMMD(ProveedorPMMD pPMMD) {
        GridPane g = UIPMMD.grid(2);
        g.add(UIPMMD.dato("RUC", pPMMD.getRuc()), 0, 0);
        g.add(UIPMMD.dato("Condición de pago", pPMMD.getCondicionPago()), 1, 0);
        g.add(UIPMMD.dato("Contacto", pPMMD.getContacto() + " · " + pPMMD.getTelefono()), 0, 1);
        g.add(UIPMMD.dato("Correo", pPMMD.getEmail()), 1, 1);
        g.add(UIPMMD.dato("Dirección", pPMMD.getDireccion()), 0, 2, 2, 1);
        VBox mats = new VBox(4, UIPMMD.label("Materiales que abastece", "card-title"));
        proveedoresPMMD.materiales(pPMMD).forEach(mPMMD -> mats.getChildren().add(UIPMMD.hbox(8, UIPMMD.codigo(mPMMD.getCodigo()), UIPMMD.label(mPMMD.getNombre(), "cell-title"), UIPMMD.spacer(),
                new StatusBadgePMMD(mPMMD.getEstado()))));
        VBox ocs = new VBox(4, UIPMMD.label("Órdenes de compra", "card-title"));
        proveedoresPMMD.compras(pPMMD).forEach(oPMMD -> {
            Hyperlink lPMMD = new Hyperlink(oPMMD.getCodigo());
            lPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("compras", IntentPMMD.abrir(oPMMD.getCodigo())));
            ocs.getChildren().add(UIPMMD.hbox(8, lPMMD, UIPMMD.label(AppClockPMMD.fmt(oPMMD.getFecha()), "cell-sub"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(oPMMD.getTotal()), "cell-title"),
                    new StatusBadgePMMD(oPMMD.getEstado().nombre())));
        });
        if (ocs.getChildren().size() == 1) ocs.getChildren().add(UIPMMD.label("Sin órdenes.", "muted"));
        VBox cuerpoPMMD = new VBox(16, UIPMMD.hbox(8, new StatusBadgePMMD(pPMMD.getActivo() ? "Activo" : "Inactivo"), UIPMMD.label(pPMMD.getRubro(), "cell-sub")), g,
                UIPMMD.hbox(8, UIPMMD.label("Total comprado", "muted"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(pPMMD.getTotalComprado()), "price-value")), mats, ocs);
        Button editarPMMD = UIPMMD.secondary("Editar", "EDIT");
        editarPMMD.setOnAction(ePMMD -> {
            panelPMMD.cerrar();
            formularioPMMD(pPMMD);
        });
        Button estadoPMMD = UIPMMD.button(pPMMD.getActivo() ? "Desactivar" : "Activar", pPMMD.getActivo() ? "BLOCK" : "DONE", pPMMD.getActivo() ? "btn-danger-soft" : "btn-success");
        estadoPMMD.setOnAction(ePMMD -> confirmPMMD.mostrar(pPMMD.getActivo() ? ConfirmDialogPMMD.Tipo.PELIGRO : ConfirmDialogPMMD.Tipo.INFO,
                (pPMMD.getActivo() ? "¿Desactivar " : "¿Activar ") + pPMMD.getRazonSocial() + "?",
                pPMMD.getActivo() ? "No se le podrán emitir nuevas órdenes de compra." : "Volverá a estar disponible para compras.", pPMMD.getActivo() ? "Desactivar" : "Activar", () -> {
                    pPMMD.setActivo(!pPMMD.getActivo());
                    if (toastPMMD.intentar(() -> proveedoresPMMD.guardar(pPMMD))) {
                        panelPMMD.cerrar();
                        toastPMMD.exito(pPMMD.getRazonSocial() + (pPMMD.getActivo() ? " activado." : " desactivado."));
                        renderPMMD();
                    }
                }));
        panelPMMD = overlaysPMMD.panel(pPMMD.getRazonSocial(), pPMMD.getCodigo(), cuerpoPMMD, UIPMMD.hbox(8, estadoPMMD, UIPMMD.spacer(), editarPMMD), 500);
    }

    private void formularioPMMD(ProveedorPMMD existentePMMD) {
        ProveedorPMMD pPMMD = existentePMMD == null ? ProveedorPMMD.builder().activo(true).totalComprado(java.math.BigDecimal.ZERO).condicionPago("Contado").build() : existentePMMD;
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        TextField rsPMMD = UIPMMD.text(pPMMD.getRazonSocial(), "Razón social");
        TextField rucPMMD = FormsPMMD.digitos(pPMMD.getRuc(), 11);
        TextField contactoPMMD = UIPMMD.text(pPMMD.getContacto(), "Nombre del contacto");
        TextField telPMMD = UIPMMD.text(pPMMD.getTelefono(), "951 000 000");
        TextField emailPMMD = UIPMMD.text(pPMMD.getEmail(), "ventas@proveedor.pe");
        TextField dirPMMD = UIPMMD.text(pPMMD.getDireccion(), "Dirección");
        TextField rubroPMMD = UIPMMD.text(pPMMD.getRubro(), "Materiales que abastece");
        ComboBox<String> condPMMD = FormsPMMD.combo(List.of("Contado", "Crédito 15 días", "Crédito 30 días", "Crédito 45 días"), pPMMD.getCondicionPago(), sPMMD -> sPMMD);
        FormGridPMMD fPMMD = new FormGridPMMD(2);
        fPMMD.nodo(vPMMD.campo("Razón social", rsPMMD, () -> ValidacionPMMD.requerido(rsPMMD, "la razón social")), 2);
        fPMMD.nodo(vPMMD.campo("RUC", rucPMMD, () -> rucPMMD.getText().matches("\\d{11}") ? null : "El RUC debe tener 11 dígitos."), 1);
        fPMMD.nodo(vPMMD.campo("Condición de pago", condPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Contacto", contactoPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Teléfono", telPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Correo", emailPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Dirección", dirPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Materiales que abastece", rubroPMMD, () -> ValidacionPMMD.requerido(rubroPMMD, "los materiales")), 2);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary(existentePMMD == null ? "Registrar proveedor" : "Guardar cambios", "SAVE");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal(existentePMMD == null ? "Nuevo proveedor" : "Editar " + pPMMD.getCodigo(), null, fPMMD, 600, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            pPMMD.setRazonSocial(rsPMMD.getText().trim());
            pPMMD.setRuc(rucPMMD.getText().trim());
            pPMMD.setContacto(contactoPMMD.getText());
            pPMMD.setTelefono(telPMMD.getText());
            pPMMD.setEmail(emailPMMD.getText());
            pPMMD.setDireccion(dirPMMD.getText());
            pPMMD.setRubro(rubroPMMD.getText());
            pPMMD.setCondicionPago(condPMMD.getValue());
            ProveedorPMMD[] g = new ProveedorPMMD[1];
            if (toastPMMD.intentar(() -> g[0] = proveedoresPMMD.guardar(pPMMD))) {
                hPMMD.cerrar();
                toastPMMD.exito(g[0].getCodigo() + " · " + g[0].getRazonSocial() + " guardado.");
                renderPMMD();
            }
        });
    }
}
