package pe.erp.sofas.ui.views.produccion;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.service.calculo.ExplosionPMMD;
import pe.erp.sofas.ui.AppWindowPMMD;
import pe.erp.sofas.ui.components.*;

import java.io.File;
import java.util.*;






@Component
@Lazy
@RequiredArgsConstructor
public class OrdenesUIPMMD {

    private final ProduccionServicePMMD produccionPMMD;

    private final ProductoServicePMMD productosPMMD;

    private final FichaServicePMMD fichasPMMD;

    private final ClienteServicePMMD clientesPMMD;

    private final EmpleadoServicePMMD empleadosPMMD;

    private final TelaServicePMMD telasPMMD;

    private final MrpServicePMMD mrpPMMD;

    private final AuthServicePMMD authPMMD;

    private final ExportServicePMMD exportPMMD;

    private final SessionPMMD sessionPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmPMMD;

    private final NavigatorPMMD navigatorPMMD;

    private final AppWindowPMMD appWindowPMMD;

    private OverlaysPMMD.Handle panelPMMD;





    public void mover(OrdenProduccionPMMD opPMMD, EtapaPMMD destinoPMMD, Runnable despuesPMMD) {
        if (opPMMD.getEtapa() == destinoPMMD) return;
        ProduccionServicePMMD.Evaluacion evPMMD;
        try {
            evPMMD = produccionPMMD.evaluar(opPMMD, destinoPMMD);
        } catch (NegocioExceptionPMMD ePMMD) {
            toastPMMD.error(ePMMD.getMessage());
            return;
        }
        Runnable finPMMD = () -> {
            navigatorPMMD.datosCambiaron();
            despuesPMMD.run();
        };
        switch (evPMMD.requisito()) {
            case NINGUNO -> ejecutar(() -> produccionPMMD.mover(opPMMD.getId(), destinoPMMD, null), finPMMD);
            case LIBERAR -> confirmPMMD.mostrar(ConfirmDialogPMMD.Tipo.AVISO, "¿Volver " + opPMMD.getCodigo() + " a Pendiente?",
                    "Se liberarán las reservas de material de la orden para que las usen otras órdenes.", "Volver y liberar",
                    () -> ejecutar(() -> produccionPMMD.mover(opPMMD.getId(), destinoPMMD, null), finPMMD));
            case AUTORIZACION -> autorizar(opPMMD, destinoPMMD, finPMMD);
            case CONFIRMAR_FALTANTES -> {
                StringBuilder sbPMMD = new StringBuilder("Al reservar faltarán:\n");
                evPMMD.faltantes().stream().limit(6).forEach(f -> sbPMMD.append("• ").append(f.material().getNombre()).append(": ")
                        .append(MoneyPMMD.numero(f.falta())).append(" ").append(f.material().getUnidad()).append("\n"));
                if (evPMMD.faltantes().size() > 6) sbPMMD.append("… y ").append(evPMMD.faltantes().size() - 6).append(" más.\n");
                sbPMMD.append("Se reservará lo disponible y la orden quedará con reserva parcial.");
                confirmPMMD.mostrar(ConfirmDialogPMMD.Tipo.AVISO, "Faltan " + evPMMD.faltantes().size() + " materiales para " + opPMMD.getCodigo(), sbPMMD.toString(),
                        "Avanzar de todos modos", () -> ejecutar(() -> produccionPMMD.mover(opPMMD.getId(), destinoPMMD, null), finPMMD));
            }
            case CONFIRMAR_CONSUMO -> confirmarConsumoPMMD(opPMMD, finPMMD);
        }
    }

    private void ejecutar(java.util.function.Supplier<String> accionPMMD, Runnable finPMMD) {
        String[] msgPMMD = new String[1];
        if (toastPMMD.intentar(() -> msgPMMD[0] = accionPMMD.get())) {
            if (msgPMMD[0] != null) toastPMMD.exito(msgPMMD[0]);
            finPMMD.run();
        }
    }

    private void autorizar(OrdenProduccionPMMD opPMMD, EtapaPMMD destinoPMMD, Runnable finPMMD) {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        List<UsuarioPMMD> supPMMD = authPMMD.autorizadores();
        UsuarioPMMD propioPMMD = supPMMD.stream().filter(uPMMD -> uPMMD.getUsername().equals(sessionPMMD.username())).findFirst().orElse(null);
        ComboBox<UsuarioPMMD> quienPMMD = FormsPMMD.combo(supPMMD, propioPMMD != null ? propioPMMD : supPMMD.stream().filter(uPMMD -> uPMMD.getRol() == RolPMMD.SUPERVISOR).findFirst().orElse(supPMMD.get(0)),
                uPMMD -> uPMMD.getNombre() + " · " + uPMMD.getRol().nombre());
        PasswordField clavePMMD = new PasswordField();
        clavePMMD.setPromptText("Contraseña del supervisor");
        TextArea motivoPMMD = FormsPMMD.area("", "Motivo (p. ej. ficha en revisión, pedido urgente)", 2);
        VBox cuerpoPMMD = new VBox(12, UIPMMD.banner("lock-banner", "WARNING", opPMMD.getCodigo() + " no tiene ficha técnica aprobada: avanzará sin reservar material."),
                vPMMD.campo("Autoriza", quienPMMD, () -> ValidacionPMMD.requerido(quienPMMD, "quién autoriza")),
                vPMMD.campo("Contraseña", clavePMMD, () -> ValidacionPMMD.requerido(clavePMMD, "la contraseña")),
                vPMMD.campo("Motivo", motivoPMMD, () -> ValidacionPMMD.requerido(motivoPMMD, "el motivo")));
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Autorizar y avanzar", "VERIFIED_USER");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal("Autorización de Supervisor", opPMMD.getCodigo() + " → " + destinoPMMD.nombre(), cuerpoPMMD, 480, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            if (toastPMMD.intentar(() -> {
                UsuarioPMMD uPMMD = authPMMD.autorizar(quienPMMD.getValue().getUsername(), clavePMMD.getText());
                String textoPMMD = "Autorizado por " + uPMMD.getNombre() + " (" + uPMMD.getRol().nombre() + ") el " + AppClockPMMD.fmt(AppClockPMMD.hoy()) + ": " + motivoPMMD.getText().trim();
                toastPMMD.exito(produccionPMMD.mover(opPMMD.getId(), destinoPMMD, textoPMMD));
            })) {
                hPMMD.cerrar();
                finPMMD.run();
            }
        });
    }

    private void confirmarConsumoPMMD(OrdenProduccionPMMD opPMMD, Runnable finPMMD) {
        ProduccionServicePMMD.EstadoMateriales emPMMD = produccionPMMD.estadoMateriales(opPMMD);
        if (emPMMD.explosion() == null) {
            confirmPMMD.mostrar(ConfirmDialogPMMD.Tipo.INFO, "¿Pasar " + opPMMD.getCodigo() + " a Listo?", "La orden no tiene ficha: no hay consumo de materiales que registrar.",
                    "Marcar como lista", () -> ejecutar(() -> produccionPMMD.completar(opPMMD.getId(), Map.of()), finPMMD));
            return;
        }
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(6);
        g.addRow(0, UIPMMD.label("MATERIAL", "dato-label"), UIPMMD.label("ESTIMADO", "dato-label"), UIPMMD.label("RESERVADO", "dato-label"), UIPMMD.label("REAL", "dato-label"));
        Map<Long, TextField> realesPMMD = new LinkedHashMap<>();
        int filaPMMD = 1;
        for (var lPMMD : emPMMD.lineas()) {
            TextField realPMMD = FormsPMMD.decimal(round(lPMMD.reservado()));
            realPMMD.setPrefWidth(90);
            realPMMD.getStyleClass().add("cell-field");
            realesPMMD.put(lPMMD.material().getId(), realPMMD);
            g.addRow(filaPMMD++, new VBox(0, UIPMMD.label(lPMMD.material().getNombre(), "cell-title"), UIPMMD.label(lPMMD.material().getCodigo() + " · " + lPMMD.material().getUnidad(), "cell-sub")),
                    UIPMMD.label(MoneyPMMD.numero(lPMMD.necesario()), "cell-sub"), UIPMMD.label(MoneyPMMD.numero(lPMMD.reservado()), "cell-sub"), realPMMD);
        }
        VBox cuerpoPMMD = new VBox(12, UIPMMD.wrap("Se descuenta del stock el consumo real (por defecto, lo reservado), se registra «Consumo OP» en el kardex "
                + "y se guarda el consumo real frente al estimado para Consumo y mermas.", "muted"), g);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.success("Confirmar consumo y pasar a Listo", "DONE_ALL");
        var hPMMD = overlaysPMMD.modal("Confirmar consumo de " + opPMMD.getCodigo(), opPMMD.getProductoNombre() + " ×" + opPMMD.getCantidad(), cuerpoPMMD, 620, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            Map<Long, Double> mPMMD = new HashMap<>();
            realesPMMD.forEach((idPMMD, tfPMMD) -> mPMMD.put(idPMMD, ValidacionPMMD.valor(tfPMMD)));
            hPMMD.cerrar();
            ejecutar(() -> produccionPMMD.completar(opPMMD.getId(), mPMMD), finPMMD);
        });
    }

    private static double round(double vPMMD) {
        return Math.round(vPMMD * 100) / 100.0;
    }



    public void detalle(OrdenProduccionPMMD oPMMD, Runnable alCambiarPMMD) {
        if (panelPMMD != null) panelPMMD.cerrar();
        OrdenProduccionPMMD opPMMD = produccionPMMD.op(oPMMD.getId());
        Runnable refrescarPMMD = () -> {
            alCambiarPMMD.run();
            detalle(opPMMD, alCambiarPMMD);
        };
        VBox cuerpoPMMD = new VBox(16);
        StepperPMMD stepperPMMD = new StepperPMMD(opPMMD.getEtapa(), ePMMD -> mover(opPMMD, ePMMD, refrescarPMMD));
        Button antPMMD = UIPMMD.secondary("Etapa anterior", "CHEVRON_LEFT");
        antPMMD.setDisable(opPMMD.getEtapa() == EtapaPMMD.PENDIENTE || opPMMD.getEtapa() == EtapaPMMD.LISTO || opPMMD.getAnulada());
        antPMMD.setOnAction(ePMMD -> mover(opPMMD, opPMMD.getEtapa().anterior(), refrescarPMMD));
        Button sigPMMD = UIPMMD.primary("Siguiente etapa", "CHEVRON_RIGHT");
        sigPMMD.setContentDisplay(ContentDisplay.RIGHT);
        sigPMMD.setDisable(opPMMD.getEtapa() == EtapaPMMD.LISTO || opPMMD.getAnulada());
        sigPMMD.setOnAction(ePMMD -> mover(opPMMD, opPMMD.getEtapa().siguiente(), refrescarPMMD));
        HBox navPMMD = UIPMMD.hbox(8, new StatusBadgePMMD(opPMMD.getEstado()), UIPMMD.label("Etapa " + opPMMD.getEtapa().paso() + " de 10 · " + opPMMD.getEtapa().nombre(), "cell-title"),
                UIPMMD.spacer(), antPMMD, sigPMMD);
        cuerpoPMMD.getChildren().addAll(stepperPMMD, navPMMD);

        GridPane datosPMMD = UIPMMD.grid(3);
        datosPMMD.add(UIPMMD.dato("Producto", opPMMD.getProductoNombre() + " ×" + opPMMD.getCantidad()), 0, 0, 2, 1);
        HBox telaPMMD = new HBox(8);
        telaPMMD.setAlignment(Pos.CENTER_LEFT);
        if (opPMMD.getTela() != null) telaPMMD.getChildren().add(new ColorSwatchPMMD(opPMMD.getTela().getHex(), opPMMD.getTela().getTipo(), 20));
        telaPMMD.getChildren().add(UIPMMD.label(opPMMD.getTelaNombre(), "dato-valor"));
        datosPMMD.add(UIPMMD.dato("Tela y color", telaPMMD), 2, 0);
        datosPMMD.add(UIPMMD.dato("Cliente", opPMMD.getClienteNombre()), 0, 1);
        datosPMMD.add(UIPMMD.dato("Responsable", opPMMD.getResponsable() == null ? "—" : opPMMD.getResponsable().getNombre()), 1, 1);
        datosPMMD.add(UIPMMD.dato("Prioridad", new StatusBadgePMMD(opPMMD.getPrioridad().nombre())), 2, 1);
        datosPMMD.add(UIPMMD.dato("Inicio", AppClockPMMD.fmt(opPMMD.getFechaInicio())), 0, 2);
        Label entregaPMMD = UIPMMD.label(AppClockPMMD.fmt(opPMMD.getFechaEntrega()) + (opPMMD.isRetrasada() ? " · vencida" : ""), "dato-valor");
        if (opPMMD.isRetrasada()) entregaPMMD.setStyle("-fx-text-fill: -is-danger-ink;");
        datosPMMD.add(UIPMMD.dato("Entrega", entregaPMMD), 1, 2);
        datosPMMD.add(UIPMMD.dato("Costo", MoneyPMMD.fmt(opPMMD.getCosto())), 2, 2);
        Node pedidoPMMD;
        if (opPMMD.getPedidoCodigo() != null) {
            Hyperlink lPMMD = new Hyperlink(opPMMD.getPedidoCodigo());
            lPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("pedidos", IntentPMMD.abrir(opPMMD.getPedidoCodigo())));
            pedidoPMMD = lPMMD;
        } else if (opPMMD.getRetapizadoCodigo() != null) {
            Hyperlink lPMMD = new Hyperlink(opPMMD.getRetapizadoCodigo());
            lPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("posventa", IntentPMMD.abrir(opPMMD.getRetapizadoCodigo())));
            pedidoPMMD = lPMMD;
        } else pedidoPMMD = UIPMMD.label("Stock de tienda", "dato-valor");
        datosPMMD.add(UIPMMD.dato("Pedido vinculado", pedidoPMMD), 0, 3);
        if (opPMMD.getLargo() != null) datosPMMD.add(UIPMMD.dato("Medidas a pedido", opPMMD.getLargo() + " × " + opPMMD.getFondo() + " × " + opPMMD.getAlto() + " cm"), 1, 3, 2, 1);
        cuerpoPMMD.getChildren().addAll(UIPMMD.label("Datos de la orden", "card-title"), datosPMMD);
        if (opPMMD.getEspecificacion() != null) cuerpoPMMD.getChildren().add(UIPMMD.wrap(opPMMD.getEspecificacion(), "muted"));
        if (opPMMD.getAutorizacion() != null) cuerpoPMMD.getChildren().add(UIPMMD.banner("lock-banner", "VERIFIED_USER", opPMMD.getAutorizacion()));
        cuerpoPMMD.getChildren().add(materiales(opPMMD, refrescarPMMD));

        Button hojaPMMD = UIPMMD.secondary("Hoja de trabajo (PDF)", "PICTURE_AS_PDF");
        hojaPMMD.setOnAction(ePMMD -> hojaTrabajo(opPMMD));
        Button anularPMMD = UIPMMD.button("Anular orden", "BLOCK", "btn-danger-soft");
        anularPMMD.setDisable(opPMMD.getEtapa() == EtapaPMMD.LISTO || opPMMD.getAnulada());
        anularPMMD.setOnAction(ePMMD -> confirmPMMD.mostrar("¿Anular " + opPMMD.getCodigo() + "?",
                "La orden se cancela y se liberan sus reservas de material." + (opPMMD.getPedidoCodigo() != null ? " El pedido " + opPMMD.getPedidoCodigo() + " vuelve a Confirmado." : ""),
                "Anular orden", () -> {
                    String[] mPMMD = new String[1];
                    if (toastPMMD.intentar(() -> mPMMD[0] = produccionPMMD.anular(opPMMD.getId()))) {
                        toastPMMD.exito(mPMMD[0]);
                        panelPMMD.cerrar();
                        navigatorPMMD.datosCambiaron();
                        alCambiarPMMD.run();
                    }
                }));
        HBox piePMMD = UIPMMD.hbox(8, anularPMMD, UIPMMD.spacer(), hojaPMMD);
        panelPMMD = overlaysPMMD.panel(opPMMD.getCodigo() + " · " + opPMMD.getProductoNombre(), opPMMD.getClienteNombre() + " · " + opPMMD.getEtapa().nombre(), cuerpoPMMD, piePMMD, 680);
    }

    private Node materiales(OrdenProduccionPMMD opPMMD, Runnable refrescarPMMD) {
        VBox boxPMMD = new VBox(10);
        boxPMMD.getChildren().add(UIPMMD.label("Materiales · explosión de la ficha", "card-title"));
        if (opPMMD.isSinFicha()) {
            boxPMMD.getChildren().add(UIPMMD.banner("lock-banner", "WARNING", opPMMD.getAutorizacion() != null
                    ? "Sin ficha · autorizado: la orden avanza sin reservar material."
                    : "Sin ficha aprobada: para pasar a producción se necesita autorización de Supervisor."));
            return boxPMMD;
        }
        ProduccionServicePMMD.EstadoMateriales emPMMD = produccionPMMD.estadoMateriales(opPMMD);
        ExplosionPMMD.Resultado rPMMD = emPMMD.explosion();
        long faltPMMD = emPMMD.faltantes();
        String estadoReservaPMMD = opPMMD.getEtapa() == EtapaPMMD.LISTO ? "Consumido" : emPMMD.completo() ? "Reservado"
                : emPMMD.lineas().stream().anyMatch(lPMMD -> lPMMD.reservado() > 0) ? "Parcial" : opPMMD.getEtapa() == EtapaPMMD.PENDIENTE ? "Pendiente" : "Faltante";
        boxPMMD.getChildren().add(UIPMMD.hbox(10, UIPMMD.miniStat("Ficha", opPMMD.getFichaUid().replace("v", " v")), UIPMMD.miniStat("Materiales", String.valueOf(emPMMD.lineas().size())),
                UIPMMD.miniStat("Costo de materiales", MoneyPMMD.fmt(rPMMD.costoMateriales())), UIPMMD.miniStat("Reserva", estadoReservaPMMD + (faltPMMD > 0 ? " · " + faltPMMD + " faltan" : ""))));

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(2);
        ColumnConstraints c0PMMD = new ColumnConstraints();
        c0PMMD.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c0PMMD, new ColumnConstraints(82), new ColumnConstraints(82), new ColumnConstraints(100), new ColumnConstraints(90));
        g.addRow(0, UIPMMD.label("MATERIAL", "dato-label"), UIPMMD.label("NECESARIO", "dato-label"), UIPMMD.label("RESERVADO", "dato-label"),
                UIPMMD.label("DISPONIBILIDAD", "dato-label"), UIPMMD.label("ESTADO", "dato-label"));
        int iPMMD = 1;
        for (var lPMMD : emPMMD.lineas()) {
            double cubre = lPMMD.necesario() <= 0 ? 1 : Math.min(1, Math.max(lPMMD.reservado(), "Disponible".equals(lPMMD.estado()) ? lPMMD.necesario() : Math.min(lPMMD.disponible(), lPMMD.necesario())) / lPMMD.necesario());
            String color = switch (lPMMD.estado()) {
                case "Reservado", "Disponible", "Consumido" -> "#16A34A";
                case "Parcial", "Faltará" -> "#F59E0B";
                default -> "#DC2626";
            };
            ProgressBarMiniPMMD barraPMMD = new ProgressBarMiniPMMD(cubre, color, 6);
            barraPMMD.setPrefWidth(90);
            Tooltip.install(barraPMMD, new Tooltip("Disponible para la OP: " + MoneyPMMD.numero(lPMMD.disponible()) + " " + lPMMD.material().getUnidad()));
            g.addRow(iPMMD++, new VBox(0, UIPMMD.label(lPMMD.material().getNombre(), "cell-title"), UIPMMD.label(lPMMD.material().getCodigo() + " · " + lPMMD.componentes(), "cell-sub")),
                    UIPMMD.label(MoneyPMMD.numero(lPMMD.necesario()) + " " + lPMMD.material().getUnidad(), "cell-sub"),
                    UIPMMD.label(MoneyPMMD.numero(lPMMD.reservado()), "cell-sub"), barraPMMD, new StatusBadgePMMD(lPMMD.estado()));
        }
        boxPMMD.getChildren().add(g);

        Button reservarPMMD = UIPMMD.button("Reservar ahora", "LOCK", "btn-sm", "btn-secondary");
        reservarPMMD.setDisable(opPMMD.getEtapa() == EtapaPMMD.LISTO || emPMMD.completo());
        reservarPMMD.setOnAction(ePMMD -> {
            String[] mPMMD = new String[1];
            if (toastPMMD.intentar(() -> mPMMD[0] = opPMMD.getCodigo() + ": " + produccionPMMD.reservar(opPMMD.getId()).texto())) {
                toastPMMD.exito(mPMMD[0]);
                navigatorPMMD.datosCambiaron();
                refrescarPMMD.run();
            }
        });
        Button liberarPMMD = UIPMMD.button("Liberar reserva", "LOCK_OPEN", "btn-sm", "btn-secondary");
        liberarPMMD.setDisable(emPMMD.lineas().stream().noneMatch(lPMMD -> lPMMD.reservado() > 0) || opPMMD.getEtapa() == EtapaPMMD.LISTO);
        liberarPMMD.setOnAction(ePMMD -> confirmPMMD.mostrar(ConfirmDialogPMMD.Tipo.AVISO, "¿Liberar la reserva de " + opPMMD.getCodigo() + "?",
                "El material vuelve a estar disponible para otras órdenes.", "Liberar", () -> {
                    int[] nPMMD = new int[1];
                    if (toastPMMD.intentar(() -> nPMMD[0] = produccionPMMD.liberar(opPMMD.getId()))) {
                        toastPMMD.exito(nPMMD[0] + " reservas liberadas de " + opPMMD.getCodigo() + ".");
                        navigatorPMMD.datosCambiaron();
                        refrescarPMMD.run();
                    }
                }));
        Button ocPMMD = UIPMMD.button("Generar OC de faltantes", "SHOPPING_CART", "btn-sm", "btn-wood");
        ocPMMD.setDisable(faltPMMD == 0 || opPMMD.getEtapa() == EtapaPMMD.LISTO);
        ocPMMD.setOnAction(ePMMD -> generarOC(opPMMD));
        Button cortePMMD = UIPMMD.button("Hoja de corte", "CONTENT_CUT", "btn-sm", "btn-ghost");
        cortePMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("corte", IntentPMMD.abrir(opPMMD.getCodigo())));
        boxPMMD.getChildren().add(UIPMMD.hbox(8, reservarPMMD, liberarPMMD, ocPMMD, UIPMMD.spacer(), cortePMMD));
        return boxPMMD;
    }

    public void generarOC(OrdenProduccionPMMD opPMMD) {
        List<MrpServicePMMD.Fila> filasPMMD = new ArrayList<>();
        Map<Long, Double> transitoPMMD = mrpPMMD.transito();
        for (var f : produccionPMMD.faltantesSiReserva(opPMMD)) {
            double tr = transitoPMMD.getOrDefault(f.material().getId(), 0d);
            double falta = Math.max(0, f.falta() - tr);
            if (falta > 0) filasPMMD.add(new MrpServicePMMD.Fila(f.material(), f.necesario(), tr, falta, AppClockPMMD.hoy(), f.material().getProveedor(), Set.of(opPMMD.getCodigo())));
        }
        if (filasPMMD.isEmpty()) {
            toastPMMD.info("Los faltantes de " + opPMMD.getCodigo() + " ya están cubiertos por compras en tránsito.");
            return;
        }
        List<String>[] codPMMD = new List[1];
        if (toastPMMD.intentar(() -> codPMMD[0] = mrpPMMD.generarOrdenes(filasPMMD, opPMMD.getCodigo()))) {
            toastPMMD.exito("Órdenes de compra en borrador: " + String.join(", ", codPMMD[0]) + ".");
            navigatorPMMD.datosCambiaron();
        }
    }

    public void hojaTrabajo(OrdenProduccionPMMD opPMMD) {
        File f = ArchivosPMMD.guardar(null, "Hoja-de-trabajo-" + opPMMD.getCodigo(), "PDF", "pdf");
        if (f == null) return;
        TareasPMMD.ejecutar(overlaysPMMD, toastPMMD, "Generando hoja de trabajo…", () -> {
            exportPMMD.hojaTrabajo(opPMMD, produccionPMMD.estadoMateriales(opPMMD), f);
            return f;
        }, archivoPMMD -> {
            toastPMMD.exito("Hoja de trabajo guardada: " + archivoPMMD.getName());
            appWindowPMMD.hostServices().showDocument(archivoPMMD.toURI().toString());
        });
    }



    public void nueva(Runnable alCrearPMMD) {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        ComboBox<ProductoPMMD> productoPMMD = FormsPMMD.combo(productosPMMD.listar(), null, pPMMD -> pPMMD.getCodigo() + " · " + pPMMD.getNombre());
        Spinner<Integer> cantidadPMMD = new Spinner<>(1, 50, 1);
        cantidadPMMD.setEditable(true);
        cantidadPMMD.setMaxWidth(Double.MAX_VALUE);
        List<ClientePMMD> csPMMD = new ArrayList<>();
        csPMMD.add(null);
        csPMMD.addAll(clientesPMMD.listar());
        ComboBox<ClientePMMD> clientePMMD = FormsPMMD.combo(csPMMD, null, cPMMD -> cPMMD == null ? "Stock de tienda (sin cliente)" : cPMMD.getNombre());
        ComboBox<EmpleadoPMMD> responsablePMMD = FormsPMMD.combo(empleadosPMMD.taller(), null, ePMMD -> ePMMD.getNombre() + " · " + ePMMD.getCargo());
        ComboBox<PrioridadPMMD> prioridadPMMD = FormsPMMD.combo(List.of(PrioridadPMMD.values()), PrioridadPMMD.MEDIA, PrioridadPMMD::nombre);
        DatePicker inicioPMMD = FormsPMMD.fecha(AppClockPMMD.hoy());
        DatePicker entregaPMMD = FormsPMMD.fecha(AppClockPMMD.hoy().plusDays(15));
        List<TelaPMMD> tsPMMD = new ArrayList<>();
        tsPMMD.add(null);
        tsPMMD.addAll(telasPMMD.activas());
        ComboBox<TelaPMMD> telaPMMD = FormsPMMD.combo(tsPMMD, null, tPMMD -> tPMMD == null ? "Según ficha" : tPMMD.getNombre() + " · " + MoneyPMMD.numero(tPMMD.getStock()) + " m");
        Label costo = UIPMMD.label("Elige un modelo para ver el costo.", "muted");
        Runnable calcularPMMD = () -> {
            ProductoPMMD pPMMD = productoPMMD.getValue();
            if (pPMMD == null) return;
            FichaTecnicaPMMD f = fichasPMMD.aprobadaDe(pPMMD);
            if (f == null) {
                costo.setText("Costo estimado (sin ficha aprobada): " + MoneyPMMD.fmt(pPMMD.getCosto().multiply(java.math.BigDecimal.valueOf(cantidadPMMD.getValue())))
                        + " · necesitará autorización para producir.");
                costo.getStyleClass().setAll("label", "text-warn");
            } else {
                double cPMMD = fichasPMMD.explotar(f, cantidadPMMD.getValue(), pe.erp.sofas.service.calculo.Opciones.NINGUNA).costoEstandar();
                costo.setText("Costo estándar · ficha " + f.getEtiqueta() + ": " + MoneyPMMD.fmt(cPMMD));
                costo.getStyleClass().setAll("label", "text-primary", "semibold");
            }
        };
        productoPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        cantidadPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        FormGridPMMD f = new FormGridPMMD(2);
        f.nodo(vPMMD.campo("Modelo", productoPMMD, () -> ValidacionPMMD.requerido(productoPMMD, "el modelo")), 2);
        f.nodo(vPMMD.campo("Cantidad", cantidadPMMD, null), 1);
        f.nodo(vPMMD.campo("Tela y color", telaPMMD, null), 1);
        f.nodo(vPMMD.campo("Cliente (opcional)", clientePMMD, null), 1);
        f.nodo(vPMMD.campo("Responsable", responsablePMMD, () -> ValidacionPMMD.requerido(responsablePMMD, "el responsable")), 1);
        f.nodo(vPMMD.campo("Prioridad", prioridadPMMD, null), 1);
        f.nodo(new Region(), 1);
        f.nodo(vPMMD.campo("Inicio", inicioPMMD, () -> inicioPMMD.getValue() == null ? "Elige la fecha de inicio." : null), 1);
        f.nodo(vPMMD.campo("Entrega", entregaPMMD, () -> entregaPMMD.getValue() == null ? "Elige la fecha de entrega."
                : inicioPMMD.getValue() != null && entregaPMMD.getValue().isBefore(inicioPMMD.getValue()) ? "La entrega debe ser después del inicio." : null), 1);
        vPMMD.escuchar(inicioPMMD.valueProperty());
        VBox cuerpoPMMD = new VBox(14, f, costo);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Crear orden", "ADD");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal("Nueva orden de producción", "Se crea en Pendiente; al pasar a producción se reservan los materiales.", cuerpoPMMD, 600, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            OrdenProduccionPMMD[] creadaPMMD = new OrdenProduccionPMMD[1];
            if (toastPMMD.intentar(() -> creadaPMMD[0] = produccionPMMD.crear(new ProduccionServicePMMD.NuevaOP(productoPMMD.getValue().getId(), null, cantidadPMMD.getValue(),
                    clientePMMD.getValue() == null ? null : clientePMMD.getValue().getId(), responsablePMMD.getValue().getId(), prioridadPMMD.getValue(),
                    inicioPMMD.getValue(), entregaPMMD.getValue(), telaPMMD.getValue() == null ? null : telaPMMD.getValue().getId(), null, Map.of(), List.of(),
                    null, null, null, EtapaPMMD.PENDIENTE, null, null)))) {
                hPMMD.cerrar();
                toastPMMD.exito(creadaPMMD[0].getCodigo() + " creada en Pendiente" + (creadaPMMD[0].isSinFicha() ? " (sin ficha aprobada)." : " con la ficha " + creadaPMMD[0].getFichaUid().replace("v", " v") + "."));
                navigatorPMMD.datosCambiaron();
                alCrearPMMD.run();
            }
        });
    }
}
