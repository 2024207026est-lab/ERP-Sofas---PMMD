package pe.erp.sofas.ui.views.produccion;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.service.calculo.ExplosionPMMD;
import pe.erp.sofas.service.calculo.MedidasPMMD;
import pe.erp.sofas.service.calculo.Opciones;
import pe.erp.sofas.ui.components.*;

import java.util.*;







@Component
@Lazy
@RequiredArgsConstructor
public class CalculadoraMaterialesPMMD {

    private record Linea(
      MaterialPMMD material, 
        String componentes, 
          double necesario, 
            double disponible, 
              double faltante, 
                double costo, 
                  String estado) {
    }

    private final ProductoServicePMMD productosPMMD;

    private final FichaServicePMMD fichasPMMD;

    private final TelaServicePMMD telasPMMD;

    private final ProduccionServicePMMD produccionPMMD;

    private final MrpServicePMMD mrpPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final NavigatorPMMD navigatorPMMD;

    public void abrir() {
        List<ProductoPMMD> listaPMMD = new ArrayList<>(productosPMMD.listar().stream().filter(pPMMD -> fichasPMMD.aprobadaDe(pPMMD) != null).toList());
        listaPMMD.sort(Comparator.comparing((ProductoPMMD pPMMD) -> !Boolean.TRUE.equals(fichasPMMD.aprobadaDe(pPMMD).getCompuesta())).thenComparing(ProductoPMMD::getCodigo));
        ComboBox<ProductoPMMD> productoPMMD = FormsPMMD.combo(listaPMMD, listaPMMD.stream().filter(pPMMD -> pPMMD.getCodigo().equals("SF-001")).findFirst().orElse(listaPMMD.get(0)),
                pPMMD -> (Boolean.TRUE.equals(fichasPMMD.aprobadaDe(pPMMD).getCompuesta()) ? "Juego · " : "") + pPMMD.getCodigo() + " · " + pPMMD.getNombre());
        Spinner<Integer> cantidadPMMD = new Spinner<>(1, 50, 1);
        cantidadPMMD.setEditable(true);
        cantidadPMMD.setMaxWidth(Double.MAX_VALUE);
        CheckBox aMedidaPMMD = new CheckBox("Medidas a pedido");
        TextField largoPMMD = FormsPMMD.entero(210), fondoPMMD = FormsPMMD.entero(92), altoPMMD = FormsPMMD.entero(85);
        List<TelaPMMD> tsPMMD = new ArrayList<>();
        tsPMMD.add(null);
        tsPMMD.addAll(telasPMMD.activas());
        ComboBox<TelaPMMD> telaPMMD = FormsPMMD.combo(tsPMMD, null, t -> t == null ? "Según ficha" : t.getNombre() + " · " + MoneyPMMD.numero(t.getStock()) + " m");
        ComboBox<String> espumaPMMD = FormsPMMD.combo(List.of("D23 estándar", "D30 alta densidad", "Viscoelástica"), "D23 estándar", sPMMD -> sPMMD);

        VBox resultado = new VBox(12);
        Object[] estado = new Object[2];
        Runnable calcularPMMD = () -> {
            ProductoPMMD pPMMD = productoPMMD.getValue();
            if (pPMMD == null) return;
            FichaTecnicaPMMD f = fichasPMMD.aprobadaDe(pPMMD);
            boolean juegoPMMD = Boolean.TRUE.equals(f.getCompuesta());
            aMedidaPMMD.setDisable(juegoPMMD);
            if (juegoPMMD) aMedidaPMMD.setSelected(false);
            largoPMMD.setDisable(!aMedidaPMMD.isSelected());
            fondoPMMD.setDisable(!aMedidaPMMD.isSelected());
            altoPMMD.setDisable(!aMedidaPMMD.isSelected());
            Map<String, String> sust = new HashMap<>();
            if (telaPMMD.getValue() != null) for (TipoTelaPMMD t : TipoTelaPMMD.values()) sust.put(t.materialCodigo(), telaPMMD.getValue().getTipo().materialCodigo());
            if (espumaPMMD.getValue().startsWith("D30")) sust.put("MP-019", "MP-020");
            if (espumaPMMD.getValue().startsWith("Visco")) sust.put("MP-019", "MP-034");
            MedidasPMMD m = aMedidaPMMD.isSelected() ? new MedidasPMMD(FormsPMMD.entero(largoPMMD), FormsPMMD.entero(fondoPMMD), FormsPMMD.entero(altoPMMD), f.getCuerpos()) : null;
            ExplosionPMMD.Resultado rPMMD = fichasPMMD.explotar(f, cantidadPMMD.getValue(), new Opciones(m, sust, List.of(), false, 0, 0));
            Map<String, Set<String>> compPMMD = new LinkedHashMap<>();
            rPMMD.lineas().forEach(lPMMD -> compPMMD.computeIfAbsent(lPMMD.material().getCodigo(), kPMMD -> new LinkedHashSet<>()).add(lPMMD.componente().nombre()));
            List<Linea> lineasPMMD = new ArrayList<>();
            int okPMMD = 0, parcialPMMD = 0, sinPMMD = 0;
            for (ExplosionPMMD.PorMaterial pmPMMD : rPMMD.porMaterial()) {
                MaterialPMMD mat = pmPMMD.material();
                double disp = mat.getDisponible();
                double falta = Math.max(0, pmPMMD.cantidad() - disp);
                String est;
                if (Boolean.TRUE.equals(mat.getBajoPedido()) && mat.getStock() <= 0) {
                    est = "Bajo pedido";
                    sinPMMD++;
                } else if (falta <= 1e-6) {
                    est = "Suficiente";
                    okPMMD++;
                } else if (disp > 0) {
                    est = "Parcial";
                    parcialPMMD++;
                } else {
                    est = "Sin stock";
                    sinPMMD++;
                }
                lineasPMMD.add(new Linea(mat, String.join(", ", compPMMD.get(mat.getCodigo())), pmPMMD.cantidad(), disp, falta, pmPMMD.costo(), est));
            }
            estado[0] = rPMMD;
            estado[1] = lineasPMMD;
            int diasPMMD = (int) Math.ceil(rPMMD.horasMO() / 16 + 1) + (parcialPMMD + sinPMMD > 0 ? 5 : 0);
            resultado.getChildren().setAll(
                    UIPMMD.hbox(10, UIPMMD.miniStat("Materiales", MoneyPMMD.fmt(rPMMD.costoMateriales())), UIPMMD.miniStat("Mano de obra (" + MoneyPMMD.numero(rPMMD.horasMO()) + " h)", MoneyPMMD.fmt(rPMMD.costoMO())),
                            UIPMMD.miniStat("Costo total (con indirectos)", MoneyPMMD.fmt(rPMMD.costoEstandar())), UIPMMD.miniStat("Fabricación estimada", diasPMMD + " días")),
                    UIPMMD.hbox(8, new StatusBadgePMMD(okPMMD + " suficientes", "green"), new StatusBadgePMMD(parcialPMMD + " parciales", "amber"),
                            new StatusBadgePMMD(sinPMMD + " sin stock o a pedido", "red"), UIPMMD.spacer(), UIPMMD.label("Ficha " + f.getEtiqueta(), "code")),
                    tabla(lineasPMMD));
        };
        for (var cPMMD : List.of(productoPMMD.valueProperty(), telaPMMD.valueProperty(), espumaPMMD.valueProperty())) cPMMD.addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        cantidadPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        aMedidaPMMD.selectedProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        for (TextField t : List.of(largoPMMD, fondoPMMD, altoPMMD)) t.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            if (FormsPMMD.entero(t) >= 30) calcularPMMD.run();
        });
        productoPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            if (bPMMD != null && bPMMD.getLargo() != null) {
                largoPMMD.setText(String.valueOf(bPMMD.getLargo()));
                fondoPMMD.setText(String.valueOf(bPMMD.getFondo()));
                altoPMMD.setText(String.valueOf(bPMMD.getAlto()));
            }
        });
        calcularPMMD.run();

        GridPane f = UIPMMD.grid(4);
        f.add(UIPMMD.field("Producto o juego", productoPMMD), 0, 0, 3, 1);
        f.add(UIPMMD.field("Cantidad", cantidadPMMD), 3, 0);
        f.add(UIPMMD.field("", aMedidaPMMD), 0, 1);
        f.add(UIPMMD.field("Largo (cm)", largoPMMD), 1, 1);
        f.add(UIPMMD.field("Fondo (cm)", fondoPMMD), 2, 1);
        f.add(UIPMMD.field("Alto (cm)", altoPMMD), 3, 1);
        f.add(UIPMMD.field("Tela", telaPMMD), 0, 2, 2, 1);
        f.add(UIPMMD.field("Espuma de asiento", espumaPMMD), 2, 2, 2, 1);
        VBox cuerpoPMMD = new VBox(16, f, resultado);

        Button copiarPMMD = UIPMMD.secondary("Copiar a cotización", "CONTENT_COPY");
        Button ocPMMD = UIPMMD.button("Generar OC de faltantes", "SHOPPING_CART", "btn-wood");
        Button reservarPMMD = UIPMMD.secondary("Reservar materiales", "LOCK");
        Button crearPMMD = UIPMMD.primary("Crear OP", "ADD");
        var hPMMD = overlaysPMMD.modal("Calculadora de materiales", "Explosión de la ficha aprobada con medidas, tela y espuma", cuerpoPMMD, 900, copiarPMMD, ocPMMD, UIPMMD.spacer(), reservarPMMD, crearPMMD);
        copiarPMMD.setOnAction(ePMMD -> {
            @SuppressWarnings("unchecked") List<Linea> lPMMD = (List<Linea>) estado[1];
            ExplosionPMMD.Resultado rPMMD = (ExplosionPMMD.Resultado) estado[0];
            StringBuilder sbPMMD = new StringBuilder("SISTEMA ERP SOFAS · Cotización de materiales\n");
            sbPMMD.append(productoPMMD.getValue().getNombre()).append(" × ").append(cantidadPMMD.getValue());
            if (telaPMMD.getValue() != null) sbPMMD.append(" · ").append(telaPMMD.getValue().getNombre());
            sbPMMD.append("\n");
            for (Linea xPMMD : lPMMD) sbPMMD.append("- ").append(xPMMD.material().getNombre()).append(": ").append(MoneyPMMD.numero(xPMMD.necesario())).append(" ").append(xPMMD.material().getUnidad()).append("\n");
            sbPMMD.append("Materiales ").append(MoneyPMMD.fmt(rPMMD.costoMateriales())).append(" · Mano de obra ").append(MoneyPMMD.fmt(rPMMD.costoMO()))
                    .append(" · Costo total ").append(MoneyPMMD.fmt(rPMMD.costoEstandar())).append("\n");
            ClipboardContent ccPMMD = new ClipboardContent();
            ccPMMD.putString(sbPMMD.toString());
            Clipboard.getSystemClipboard().setContent(ccPMMD);
            toastPMMD.exito("Resumen copiado al portapapeles.");
        });
        ocPMMD.setOnAction(ePMMD -> {
            @SuppressWarnings("unchecked") List<Linea> lPMMD = (List<Linea>) estado[1];
            Map<Long, Double> trPMMD = mrpPMMD.transito();
            List<MrpServicePMMD.Fila> filasPMMD = new ArrayList<>();
            for (Linea xPMMD : lPMMD) {
                double falta = Math.max(0, xPMMD.faltante() - trPMMD.getOrDefault(xPMMD.material().getId(), 0d));
                if (falta > 0) filasPMMD.add(new MrpServicePMMD.Fila(xPMMD.material(), xPMMD.necesario(), trPMMD.getOrDefault(xPMMD.material().getId(), 0d), falta, AppClockPMMD.hoy(), xPMMD.material().getProveedor(), Set.of("Calculadora")));
            }
            if (filasPMMD.isEmpty()) {
                toastPMMD.info("No hay faltantes por comprar.");
                return;
            }
            List<String>[] cPMMD = new List[1];
            if (toastPMMD.intentar(() -> cPMMD[0] = mrpPMMD.generarOrdenes(filasPMMD, "Calculadora"))) {
                toastPMMD.exito("OC en borrador: " + String.join(", ", cPMMD[0]) + ".");
                navigatorPMMD.datosCambiaron();
            }
        });
        reservarPMMD.setOnAction(ePMMD -> crearOPPMMD(hPMMD, productoPMMD, cantidadPMMD, telaPMMD, espumaPMMD, aMedidaPMMD, largoPMMD, fondoPMMD, altoPMMD, true));
        crearPMMD.setOnAction(ePMMD -> crearOPPMMD(hPMMD, productoPMMD, cantidadPMMD, telaPMMD, espumaPMMD, aMedidaPMMD, largoPMMD, fondoPMMD, altoPMMD, false));
    }

    private void crearOPPMMD(OverlaysPMMD.Handle hPMMD, ComboBox<ProductoPMMD> productoPMMD, Spinner<Integer> cantidadPMMD, ComboBox<TelaPMMD> telaPMMD, ComboBox<String> espumaPMMD,
                         CheckBox aMedidaPMMD, TextField largoPMMD, TextField fondoPMMD, TextField altoPMMD, boolean reservarPMMD) {
        Map<String, String> sust = new HashMap<>();
        if (espumaPMMD.getValue().startsWith("D30")) sust.put("MP-019", "MP-020");
        if (espumaPMMD.getValue().startsWith("Visco")) sust.put("MP-019", "MP-034");
        FichaTecnicaPMMD f = fichasPMMD.aprobadaDe(productoPMMD.getValue());
        MedidasPMMD m = aMedidaPMMD.isSelected() ? new MedidasPMMD(FormsPMMD.entero(largoPMMD), FormsPMMD.entero(fondoPMMD), FormsPMMD.entero(altoPMMD), f.getCuerpos()) : null;
        String[] msgPMMD = new String[1];
        if (toastPMMD.intentar(() -> {
            OrdenProduccionPMMD opPMMD = produccionPMMD.crear(new ProduccionServicePMMD.NuevaOP(productoPMMD.getValue().getId(), null, cantidadPMMD.getValue(), null, null,
                    PrioridadPMMD.MEDIA, AppClockPMMD.hoy(), AppClockPMMD.hoy().plusDays(15), telaPMMD.getValue() == null ? null : telaPMMD.getValue().getId(), m, sust,
                    List.of(), null, null, "Creada desde la calculadora de materiales", EtapaPMMD.PENDIENTE, null, null));
            if (reservarPMMD) {
                var rPMMD = produccionPMMD.reservar(opPMMD.getId());
                msgPMMD[0] = opPMMD.getCodigo() + " creada y " + rPMMD.reservados() + " materiales reservados" + (rPMMD.faltantes().isEmpty() ? "" : " · faltan " + rPMMD.faltantes().size());
            } else {
                msgPMMD[0] = opPMMD.getCodigo() + " creada en Pendiente con la ficha " + f.getEtiqueta();
            }
        })) {
            hPMMD.cerrar();
            toastPMMD.exito(msgPMMD[0] + ".");
            navigatorPMMD.datosCambiaron();
            navigatorPMMD.goTo("produccion", IntentPMMD.NONE);
        }
    }

    private Node tabla(List<Linea> lPMMD) {
        TableView<Linea> t = CellsPMMD.tabla("Sin materiales.");
        t.getColumns().addAll(List.of(
                CellsPMMD.nodo("Material y componente", xPMMD -> new VBox(0, UIPMMD.label(xPMMD.material().getNombre(), "cell-title"),
                        UIPMMD.label(xPMMD.material().getCodigo() + " · " + xPMMD.componentes(), "cell-sub")), 260),
                CellsPMMD.derecha("Necesario", xPMMD -> MoneyPMMD.numero(xPMMD.necesario()) + " " + xPMMD.material().getUnidad(), 100),
                CellsPMMD.derecha("Disponible", xPMMD -> MoneyPMMD.numero(xPMMD.disponible()), 90, "cell-muted"),
                CellsPMMD.nodo("Faltante", xPMMD -> {
                    Label lbPMMD = UIPMMD.label(xPMMD.faltante() > 0 ? MoneyPMMD.numero(xPMMD.faltante()) : "—", "cell-title");
                    if (xPMMD.faltante() > 0) lbPMMD.setStyle("-fx-text-fill: -is-danger;");
                    return lbPMMD;
                }, 80),
                CellsPMMD.derecha("Costo", xPMMD -> MoneyPMMD.fmt(xPMMD.costo()), 90),
                CellsPMMD.badge("Estado", Linea::estado, 100)));
        t.getItems().setAll(lPMMD);
        t.setFixedCellSize(46);
        t.setPrefHeight(Math.min(10, lPMMD.size()) * 46 + 44);
        VBox cardPMMD = new VBox(t);
        cardPMMD.getStyleClass().addAll("card", "table-card");
        return cardPMMD;
    }
}
