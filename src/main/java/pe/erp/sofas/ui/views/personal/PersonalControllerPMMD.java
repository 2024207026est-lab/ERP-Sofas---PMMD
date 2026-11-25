package pe.erp.sofas.ui.views.personal;

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
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.components.*;

import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class PersonalControllerPMMD implements VistaPMMD {

    private final EmpleadoServicePMMD empleadosPMMD;

    private final ParametroServicePMMD parametrosPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    @FXML private VBox raiz;

    private SegmentedControlPMMD tabsPMMD;

    private final VBox contenidoPMMD = new VBox(14);

    @FXML
    public void initialize() {
        tabsPMMD = new SegmentedControlPMMD("Equipo", "Productividad", "Pago a destajo");
        tabsPMMD.getStyleClass().add("tabs");
        tabsPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        Button nuevoPMMD = UIPMMD.primary("Nuevo trabajador", "PERSON_ADD");
        nuevoPMMD.setOnAction(ePMMD -> formularioPMMD(null));
        ScrollPane spPMMD = UIPMMD.scroll(contenidoPMMD);
        VBox.setVgrow(spPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(UIPMMD.hbox(10, tabsPMMD, UIPMMD.spacer(), nuevoPMMD), spPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        renderPMMD();
    }

    private void renderPMMD() {
        List<EmpleadoPMMD> todosPMMD = empleadosPMMD.listar();
        Node kPMMD = UIPMMD.fila(
                new KpiCardPMMD("ENGINEERING", "blue", "Trabajadores", String.valueOf(todosPMMD.size()), todosPMMD.stream().filter(EmpleadoPMMD::isTaller).count() + " en el taller"),
                new KpiCardPMMD("CARPENTER", "wood", "Carpinteros", String.valueOf(todosPMMD.stream().filter(ePMMD -> "Taller · Carpintería".equals(ePMMD.getArea())).count()), "Estructuras"),
                new KpiCardPMMD("GESTURE", "amber", "Costureras", String.valueOf(todosPMMD.stream().filter(ePMMD -> "Taller · Costura".equals(ePMMD.getArea())).count()), "Corte y costura"),
                new KpiCardPMMD("WEEKEND", "violet", "Tapiceros", String.valueOf(todosPMMD.stream().filter(ePMMD -> ePMMD.getRol() == RolPMMD.TAPICERO).count()), "Tapizado y espumado"));
        switch (tabsPMMD.getValor()) {
            case "Productividad" -> contenidoPMMD.getChildren().setAll(kPMMD, productividad());
            case "Pago a destajo" -> contenidoPMMD.getChildren().setAll(kPMMD, destajo());
            default -> contenidoPMMD.getChildren().setAll(kPMMD, equipoPMMD(todosPMMD));
        }
    }

    private Node equipoPMMD(List<EmpleadoPMMD> todosPMMD) {
        TableView<EmpleadoPMMD> t = CellsPMMD.tabla("Sin trabajadores.");
        t.getColumns().addAll(List.of(
                CellsPMMD.nodo("Trabajador", ePMMD -> UIPMMD.hbox(10, AvatarInitialsPMMD.conColor(ePMMD.getNombre(), 30, ePMMD.getColor() == null ? "#64748B" : ePMMD.getColor()),
                        new VBox(0, UIPMMD.label(ePMMD.getNombre(), "cell-title"), UIPMMD.label("DNI " + ePMMD.getDni(), "cell-sub"))), 240),
                CellsPMMD.nodo("Cargo", ePMMD -> new VBox(0, UIPMMD.label(ePMMD.getCargo(), "cell-title"),
                        UIPMMD.label(ePMMD.getEtapa() == null ? "—" : "Etapa: " + ePMMD.getEtapa().nombre(), "cell-sub")), 180),
                CellsPMMD.texto("Área", EmpleadoPMMD::getArea, 160, "cell-muted"),
                CellsPMMD.texto("Rol del sistema", ePMMD -> ePMMD.getRol() == null ? "—" : ePMMD.getRol().nombre(), 140),
                CellsPMMD.texto("Teléfono", EmpleadoPMMD::getTelefono, 110),
                CellsPMMD.texto("Ingreso", ePMMD -> AppClockPMMD.fmt(ePMMD.getIngreso()), 90),
                CellsPMMD.badge("Estado", EmpleadoPMMD::getEstado, 90)));
        t.getItems().setAll(todosPMMD);
        t.setFixedCellSize(52);
        t.setPrefHeight(52 * todosPMMD.size() + 44);
        t.setRowFactory(tvPMMD -> {
            TableRow<EmpleadoPMMD> rPMMD = new TableRow<>();
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (!rPMMD.isEmpty() && ePMMD.getClickCount() == 2) formularioPMMD(rPMMD.getItem());
            });
            return rPMMD;
        });
        VBox card = new VBox(t);
        card.getStyleClass().addAll("card", "table-card");
        return new VBox(8, UIPMMD.label("Doble clic en una fila para editar.", "cell-sub"), card);
    }

    private Node productividad() {
        List<EmpleadoServicePMMD.Productividad> pPMMD = empleadosPMMD.productividad();
        double maxPMMD = pPMMD.stream().mapToInt(EmpleadoServicePMMD.Productividad::sofasTerminados).max().orElse(1);
        VBox barrasPMMD = new VBox(10);
        pPMMD.stream().sorted(Comparator.comparingInt(EmpleadoServicePMMD.Productividad::sofasTerminados).reversed()).forEach(xPMMD ->
                barrasPMMD.getChildren().add(ChartsPMMD.barraHorizontal(xPMMD.empleado().getNombre(), xPMMD.sofasTerminados(), Math.max(1, maxPMMD),
                        xPMMD.empleado().getColor() == null ? "#2563EB" : xPMMD.empleado().getColor(), xPMMD.sofasTerminados() + " sofás")));
        return UIPMMD.card(UIPMMD.cardHeader("Sofás terminados en octubre", "Órdenes en Listo de las que fue responsable (incluye las cerradas este mes)"), barrasPMMD);
    }

    private Node destajo() {
        List<EmpleadoServicePMMD.Productividad> pPMMD = empleadosPMMD.productividad();
        GridPane tarifasPMMD = new GridPane();
        tarifasPMMD.setHgap(12);
        tarifasPMMD.setVgap(6);
        tarifasPMMD.addRow(0, UIPMMD.label("ETAPA", "dato-label"), UIPMMD.label("TARIFA POR SOFÁ (S/)", "dato-label"));
        int iPMMD = 1;
        for (EtapaPMMD ePMMD : ParametroServicePMMD.DESTAJO.keySet()) {
            TextField t = FormsPMMD.decimal(parametrosPMMD.tarifaDestajo(ePMMD));
            t.setPrefWidth(90);
            t.getStyleClass().add("cell-field");
            Runnable guardarPMMD = () -> {
                double vPMMD = ValidacionPMMD.valor(t);
                if (Math.abs(vPMMD - parametrosPMMD.tarifaDestajo(ePMMD)) > 1e-9 && vPMMD >= 0) {
                    parametrosPMMD.fijar("destajo." + ePMMD.name(), vPMMD);
                    toastPMMD.exito("Tarifa de " + ePMMD.nombre() + ": S/ " + MoneyPMMD.numero(vPMMD) + " por sofá.");
                    renderPMMD();
                }
            };
            t.setOnAction(xPMMD -> guardarPMMD.run());
            t.focusedProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
                if (!bPMMD) guardarPMMD.run();
            });
            Region puntoPMMD = new Region();
            puntoPMMD.getStyleClass().add("legend-dot");
            puntoPMMD.setStyle("-fx-background-color: " + ePMMD.color() + ";");
            tarifasPMMD.addRow(iPMMD++, UIPMMD.hbox(8, puntoPMMD, UIPMMD.label(ePMMD.nombre(), "cell-title")), t);
        }
        TableView<EmpleadoServicePMMD.Productividad> tabla = CellsPMMD.tabla("Sin trabajadores de taller.");
        tabla.getColumns().addAll(List.of(
                CellsPMMD.texto("Trabajador", xPMMD -> xPMMD.empleado().getNombre(), 200, "cell-strong"),
                CellsPMMD.texto("Etapa", xPMMD -> xPMMD.empleado().getEtapa() == null ? "—" : xPMMD.empleado().getEtapa().nombre(), 170),
                CellsPMMD.derecha("Sofás del mes", xPMMD -> String.valueOf(xPMMD.sofasEnEtapa()), 110),
                CellsPMMD.derecha("Tarifa", xPMMD -> MoneyPMMD.fmt(xPMMD.tarifa()), 90, "cell-muted"),
                CellsPMMD.derecha("Monto a destajo", xPMMD -> MoneyPMMD.fmt(xPMMD.destajo()), 120, "cell-strong")));
        tabla.getItems().setAll(pPMMD);
        tabla.setFixedCellSize(44);
        tabla.setPrefHeight(44 * pPMMD.size() + 44);
        double totalPMMD = pPMMD.stream().mapToDouble(EmpleadoServicePMMD.Productividad::destajo).sum();
        VBox card = new VBox(tabla);
        card.getStyleClass().addAll("card", "table-card");
        return UIPMMD.columnas(new double[]{1, 2.2}, UIPMMD.card(UIPMMD.cardHeader("Tarifas por etapa", "Configurables · se guardan al salir del campo"), tarifasPMMD),
                new VBox(10, UIPMMD.hbox(8, UIPMMD.label("Pago a destajo de octubre", "card-title"), UIPMMD.spacer(), UIPMMD.label("Total " + MoneyPMMD.fmt(totalPMMD), "price-value")), card));
    }

    private void formularioPMMD(EmpleadoPMMD existentePMMD) {
        EmpleadoPMMD ePMMD = existentePMMD == null ? EmpleadoPMMD.builder().estado("Activo").ingreso(AppClockPMMD.hoy()).area("Taller · Tapizado").rol(RolPMMD.TAPICERO).build() : existentePMMD;
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        TextField nombre = UIPMMD.text(ePMMD.getNombre(), "Nombres y apellidos");
        TextField dniPMMD = FormsPMMD.digitos(ePMMD.getDni(), 8);
        TextField cargoPMMD = UIPMMD.text(ePMMD.getCargo(), "Tapicero");
        ComboBox<String> areaPMMD = FormsPMMD.combo(EmpleadoPMMD.AREAS, ePMMD.getArea(), sPMMD -> sPMMD);
        ComboBox<RolPMMD> rolPMMD = FormsPMMD.combo(List.of(RolPMMD.values()), ePMMD.getRol(), RolPMMD::nombre);
        List<EtapaPMMD> etapasPMMD = new ArrayList<>();
        etapasPMMD.add(null);
        etapasPMMD.addAll(List.of(EtapaPMMD.values()).subList(1, 10));
        ComboBox<EtapaPMMD> etapaPMMD = FormsPMMD.combo(etapasPMMD, ePMMD.getEtapa(), xPMMD -> xPMMD == null ? "No aplica" : xPMMD.nombre());
        TextField telPMMD = UIPMMD.text(ePMMD.getTelefono(), "951 000 000");
        DatePicker ingresoPMMD = FormsPMMD.fecha(ePMMD.getIngreso());
        ComboBox<String> estadoPMMD = FormsPMMD.combo(List.of("Activo", "Vacaciones", "Inactivo"), ePMMD.getEstado(), sPMMD -> sPMMD);
        FormGridPMMD fPMMD = new FormGridPMMD(2);
        fPMMD.nodo(vPMMD.campo("Nombre", nombre, () -> ValidacionPMMD.requerido(nombre, "el nombre")), 2);
        fPMMD.nodo(vPMMD.campo("DNI", dniPMMD, () -> dniPMMD.getText().matches("\\d{8}") ? null : "El DNI debe tener 8 dígitos."), 1);
        fPMMD.nodo(vPMMD.campo("Cargo", cargoPMMD, () -> ValidacionPMMD.requerido(cargoPMMD, "el cargo")), 1);
        fPMMD.nodo(vPMMD.campo("Área", areaPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Rol del sistema", rolPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Etapa (destajo)", etapaPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Teléfono", telPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Ingreso", ingresoPMMD, () -> ingresoPMMD.getValue() == null ? "Elige la fecha." : null), 1);
        fPMMD.nodo(vPMMD.campo("Estado", estadoPMMD, null), 1);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary(existentePMMD == null ? "Registrar" : "Guardar cambios", "SAVE");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal(existentePMMD == null ? "Nuevo trabajador" : ePMMD.getNombre(), null, fPMMD, 580, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(xPMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(xPMMD -> {
            ePMMD.setNombre(nombre.getText().trim());
            ePMMD.setDni(dniPMMD.getText());
            ePMMD.setCargo(cargoPMMD.getText());
            ePMMD.setArea(areaPMMD.getValue());
            ePMMD.setRol(rolPMMD.getValue());
            ePMMD.setEtapa(etapaPMMD.getValue());
            ePMMD.setTelefono(telPMMD.getText());
            ePMMD.setIngreso(ingresoPMMD.getValue());
            ePMMD.setEstado(estadoPMMD.getValue());
            if (ePMMD.getColor() == null) ePMMD.setColor("#64748B");
            EmpleadoPMMD[] gPMMD = new EmpleadoPMMD[1];
            if (toastPMMD.intentar(() -> gPMMD[0] = empleadosPMMD.guardar(ePMMD))) {
                hPMMD.cerrar();
                toastPMMD.exito(gPMMD[0].getNombre() + " guardado.");
                renderPMMD();
            }
        });
    }
}
