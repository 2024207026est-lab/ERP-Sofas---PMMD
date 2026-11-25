package pe.erp.sofas.ui.views.modelos;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.FormaPMMD;
import pe.erp.sofas.model.ProductoPMMD;
import pe.erp.sofas.service.BusquedaServicePMMD;
import pe.erp.sofas.service.ImagenServicePMMD;
import pe.erp.sofas.service.ProductoServicePMMD;
import pe.erp.sofas.ui.components.*;
import pe.erp.sofas.ui.three.Render3DPMMD;

import java.math.BigDecimal;
import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ModelosControllerPMMD implements VistaPMMD {

    private static final List<String> ESTADOSPMMD = List.of("Todos los estados", "Disponible", "Stock bajo", "A pedido");

    private static final List<String> ORDENESPMMD = List.of("Código", "Precio: menor a mayor", "Precio: mayor a menor", "Nombre", "Stock");

    private final ProductoServicePMMD productoServicePMMD;

    private final ImagenServicePMMD imagenServicePMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private SearchFieldPMMD buscadorPMMD;

    private final ChipFilterBarPMMD chipsPMMD = new ChipFilterBarPMMD();

    private ComboBox<String> cmbEstadoPMMD;

    private ComboBox<String> cmbOrdenPMMD;

    private SegmentedControlPMMD vistaPMMD;

    private final VBox resultadosPMMD = new VBox(12);

    private final Label conteoPMMD = UIPMMD.label("", "muted");

    private OverlaysPMMD.Handle panelPMMD;

    @FXML
    public void initialize() {
        buscadorPMMD = new SearchFieldPMMD("Buscar por nombre, código o color");
        buscadorPMMD.setPrefWidth(300);
        buscadorPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> render());
        cmbEstadoPMMD = FormsPMMD.combo(ESTADOSPMMD, ESTADOSPMMD.get(0), sPMMD -> sPMMD);
        cmbEstadoPMMD.setPrefWidth(170);
        cmbEstadoPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> render());
        cmbOrdenPMMD = FormsPMMD.combo(ORDENESPMMD, ORDENESPMMD.get(0), sPMMD -> "Ordenar: " + sPMMD);
        cmbOrdenPMMD.setPrefWidth(220);
        cmbOrdenPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> render());
        vistaPMMD = SegmentedControlPMMD.conIconos(new String[]{"Tarjetas", "Tabla"}, new String[]{"VIEW_MODULE", "TABLE_VIEW"});
        vistaPMMD.setValor(PrefsPMMD.str(PrefsPMMD.VISTA_MODELOS, "Tarjetas"));
        vistaPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            PrefsPMMD.setStr(PrefsPMMD.VISTA_MODELOS, bPMMD);
            render();
        });
        Button nuevoPMMD = UIPMMD.primary("Registrar modelo", "ADD");
        nuevoPMMD.setOnAction(ePMMD -> formularioPMMD(null));
        HBox barraPMMD = UIPMMD.hbox(10, buscadorPMMD, cmbEstadoPMMD, cmbOrdenPMMD, UIPMMD.spacer(), vistaPMMD, nuevoPMMD);
        chipsPMMD.seleccionProperty().addListener((oPMMD, aPMMD, bPMMD) -> render());
        ScrollPane spPMMD = UIPMMD.scroll(resultadosPMMD);
        VBox.setVgrow(spPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(barraPMMD, UIPMMD.hbox(10, chipsPMMD, UIPMMD.spacer(), conteoPMMD), spPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        List<ProductoPMMD> todosPMMD = productoServicePMMD.listar();
        List<ChipFilterBarPMMD.Chip> csPMMD = new ArrayList<>();
        csPMMD.add(new ChipFilterBarPMMD.Chip("*", "Todos", todosPMMD.size()));
        for (String c : ProductoPMMD.CATEGORIAS) {
            long n = todosPMMD.stream().filter(pPMMD -> pPMMD.getCategoria().equals(c)).count();
            if (n > 0) csPMMD.add(new ChipFilterBarPMMD.Chip(c, c, n));
        }
        chipsPMMD.setChips(csPMMD);
        render();
        if (intentPMMD.es(IntentPMMD.ACCION_NUEVO)) formularioPMMD(null);
        else if (intentPMMD.es(IntentPMMD.ACCION_ABRIR) && intentPMMD.dato() != null) {
            productoServicePMMD.porCodigo(intentPMMD.dato().toString()).ifPresent(this::detallePMMD);
        }
    }

    private List<ProductoPMMD> filtradosPMMD() {
        String qPMMD = BusquedaServicePMMD.normalizar(buscadorPMMD.getText());
        String catPMMD = chipsPMMD.getSeleccion();
        String estadoPMMD = cmbEstadoPMMD.getValue();
        Comparator<ProductoPMMD> ordenPMMD = switch (cmbOrdenPMMD.getValue()) {
            case "Precio: menor a mayor" -> Comparator.comparing(ProductoPMMD::getPrecio);
            case "Precio: mayor a menor" -> Comparator.comparing(ProductoPMMD::getPrecio).reversed();
            case "Nombre" -> Comparator.comparing(ProductoPMMD::getNombre);
            case "Stock" -> Comparator.comparing(ProductoPMMD::getStock).reversed();
            default -> Comparator.comparing(ProductoPMMD::getCodigo);
        };
        return productoServicePMMD.listar().stream()
                .filter(pPMMD -> catPMMD == null || catPMMD.equals("*") || pPMMD.getCategoria().equals(catPMMD))
                .filter(pPMMD -> estadoPMMD.equals(ESTADOSPMMD.get(0)) || pPMMD.getEstado().equals(estadoPMMD))
                .filter(pPMMD -> qPMMD.isEmpty() || BusquedaServicePMMD.normalizar(pPMMD.getNombre() + " " + pPMMD.getCodigo() + " " + pPMMD.getColor() + " " + pPMMD.getMaterial()).contains(qPMMD))
                .sorted(ordenPMMD).toList();
    }

    private void render() {
        if (vistaPMMD == null) return;
        List<ProductoPMMD> lPMMD = filtradosPMMD();
        conteoPMMD.setText(lPMMD.size() + (lPMMD.size() == 1 ? " modelo" : " modelos"));
        if (lPMMD.isEmpty()) {
            Button limpiarPMMD = UIPMMD.secondary("Limpiar filtros", "FILTER_LIST");
            limpiarPMMD.setOnAction(ePMMD -> {
                buscadorPMMD.setText("");
                cmbEstadoPMMD.setValue(ESTADOSPMMD.get(0));
                chipsPMMD.seleccionar("*");
            });
            resultadosPMMD.getChildren().setAll(UIPMMD.empty("WEEKEND", "Ningún modelo coincide", "Prueba con otra categoría o borra la búsqueda.", limpiarPMMD));
            return;
        }
        if ("Tabla".equals(vistaPMMD.getValor())) {
            resultadosPMMD.getChildren().setAll(tabla(lPMMD));
        } else {
            TilePane gridPMMD = new TilePane(16, 16);
            gridPMMD.setPrefTileWidth(258);
            gridPMMD.setTileAlignment(Pos.TOP_LEFT);
            for (ProductoPMMD pPMMD : lPMMD) gridPMMD.getChildren().add(tarjetaPMMD(pPMMD));
            resultadosPMMD.getChildren().setAll(gridPMMD);
        }
    }

    private ImageSlotPMMD fotoPMMD(ProductoPMMD pPMMD, int indicePMMD, double w, double h, boolean editablePMMD) {
        ImageSlotPMMD sPMMD = new ImageSlotPMMD("Foto: " + pPMMD.getNombre(), w, h, 12);
        if (indicePMMD == 0 && pPMMD.getImagenPrincipal() == null) sPMMD.respaldo(() -> Render3DPMMD.modelo(pPMMD));
        if (!editablePMMD) sPMMD.soloLectura();
        else {
            sPMMD.alElegir(fPMMD -> toastPMMD.intentar(() -> {
                String rutaPMMD = imagenServicePMMD.guardar(fPMMD, pPMMD.getCodigo() + "-" + indicePMMD, 1200, 800);
                ImagenesPMMD.olvidar(rutaPMMD);
                productoServicePMMD.fijarImagen(pPMMD.getId(), indicePMMD, rutaPMMD);
                toastPMMD.exito("Foto " + (indicePMMD == 0 ? "principal" : indicePMMD + 1) + " de " + pPMMD.getNombre() + " guardada.");
                refrescarDetallePMMD(pPMMD.getId());
            }));
            sPMMD.alQuitar(() -> toastPMMD.intentar(() -> {
                productoServicePMMD.fijarImagen(pPMMD.getId(), indicePMMD, null);
                toastPMMD.info("Foto quitada.");
                refrescarDetallePMMD(pPMMD.getId());
            }));
        }
        sPMMD.mostrar(pPMMD.getListaImagenes().get(indicePMMD));
        return sPMMD;
    }

    private void refrescarDetallePMMD(Long idPMMD) {
        render();
        if (panelPMMD != null) {
            panelPMMD.cerrar();
            detallePMMD(productoServicePMMD.producto(idPMMD));
        }
    }

    private Node tarjetaPMMD(ProductoPMMD pPMMD) {
        ImageSlotPMMD img = fotoPMMD(pPMMD, 0, 258, 168, false);
        img.setMouseTransparent(true);
        VBox cuerpo = new VBox(5,
                UIPMMD.hbox(6, UIPMMD.codigo(pPMMD.getCodigo()), UIPMMD.spacer(), new StatusBadgePMMD(pPMMD.getEstado())),
                UIPMMD.label(pPMMD.getNombre(), "product-name"),
                UIPMMD.label(pPMMD.getCategoria() + " · " + pPMMD.getMedidas(), "cell-sub"),
                UIPMMD.hbox(6, UIPMMD.label(MoneyPMMD.fmt(pPMMD.getPrecio()), "product-price"), UIPMMD.spacer(),
                        UIPMMD.label("Stock " + pPMMD.getStock(), "cell-sub")));
        cuerpo.getStyleClass().add("product-card-body");
        VBox cardPMMD = new VBox(img, cuerpo);
        cardPMMD.getStyleClass().addAll("card", "product-card", "clickable");
        cardPMMD.setFocusTraversable(true);
        cardPMMD.setOnMouseClicked(ePMMD -> detallePMMD(pPMMD));
        cardPMMD.setOnKeyPressed(ePMMD -> {
            if (ePMMD.getCode() == javafx.scene.input.KeyCode.ENTER) detallePMMD(pPMMD);
        });
        return cardPMMD;
    }

    private Node tabla(List<ProductoPMMD> lPMMD) {
        TableView<ProductoPMMD> t = CellsPMMD.tabla("Sin modelos.");
        TableColumn<ProductoPMMD, String> precioPMMD = CellsPMMD.derecha("Precio", pPMMD -> MoneyPMMD.fmt(pPMMD.getPrecio()), 100, "cell-strong");
        precioPMMD.setComparator(Comparator.comparing(MoneyPMMD::parse));
        TableColumn<ProductoPMMD, String> stockPMMD = CellsPMMD.derecha("Stock", pPMMD -> String.valueOf(pPMMD.getStock()), 60);
        stockPMMD.setComparator(Comparator.comparingInt(Integer::parseInt));
        t.getColumns().addAll(List.of(
                CellsPMMD.nodo("", pPMMD -> {
                    ImageSlotPMMD sPMMD = fotoPMMD(pPMMD, 0, 54, 38, false);
                    sPMMD.setMouseTransparent(true);
                    return sPMMD;
                }, 70),
                CellsPMMD.texto("Código", ProductoPMMD::getCodigo, 70, "cell-mono"),
                CellsPMMD.texto("Modelo", ProductoPMMD::getNombre, 200, "cell-strong"),
                CellsPMMD.texto("Categoría", ProductoPMMD::getCategoria, 110, "cell-muted"),
                CellsPMMD.texto("Medidas", ProductoPMMD::getMedidas, 140),
                CellsPMMD.texto("Tela", pPMMD -> pPMMD.getMaterial() + " " + pPMMD.getColor(), 150, "cell-muted"),
                CellsPMMD.derecha("Costo", pPMMD -> MoneyPMMD.fmt(pPMMD.getCosto()), 90, "cell-muted"), precioPMMD,
                CellsPMMD.derecha("Margen", pPMMD -> pPMMD.getMargen() + " %", 70), stockPMMD,
                CellsPMMD.badge("Estado", ProductoPMMD::getEstado, 100)));
        t.getItems().setAll(lPMMD);
        t.setFixedCellSize(54);
        t.setPrefHeight(54 * lPMMD.size() + 44);
        t.setRowFactory(tvPMMD -> {
            TableRow<ProductoPMMD> rPMMD = new TableRow<>();
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (!rPMMD.isEmpty() && ePMMD.getClickCount() >= 1) detallePMMD(rPMMD.getItem());
            });
            return rPMMD;
        });
        VBox cardPMMD = new VBox(t);
        cardPMMD.getStyleClass().addAll("card", "table-card");
        return cardPMMD;
    }



    private void detallePMMD(ProductoPMMD pPMMD) {
        VBox cuerpo = new VBox(16);
        ImageSlotPMMD principalPMMD = fotoPMMD(pPMMD, 0, 452, 270, true);
        HBox miniaturasPMMD = new HBox(10);
        for (int iPMMD = 1; iPMMD <= 4; iPMMD++) miniaturasPMMD.getChildren().add(fotoPMMD(pPMMD, iPMMD, 105, 72, true));
        cuerpo.getChildren().addAll(principalPMMD, miniaturasPMMD,
                UIPMMD.wrap("Arrastra fotos a los recuadros o haz clic para elegirlas. Clic derecho: reemplazar o quitar.", "cell-sub"));

        GridPane datosPMMD = UIPMMD.grid(3);
        datosPMMD.add(UIPMMD.dato("Categoría", pPMMD.getCategoria()), 0, 0);
        datosPMMD.add(UIPMMD.dato("Material", pPMMD.getMaterial()), 1, 0);
        datosPMMD.add(UIPMMD.dato("Color", pPMMD.getColor()), 2, 0);
        datosPMMD.add(UIPMMD.dato("Medidas", pPMMD.getMedidas()), 0, 1);
        datosPMMD.add(UIPMMD.dato("Cuerpos", String.valueOf(pPMMD.getCuerpos())), 1, 1);
        datosPMMD.add(UIPMMD.dato("Stock", pPMMD.getStock() + " · " + pPMMD.getEstado()), 2, 1);
        datosPMMD.add(UIPMMD.dato("Costo", MoneyPMMD.fmt(pPMMD.getCosto())), 0, 2);
        datosPMMD.add(UIPMMD.dato("Precio", MoneyPMMD.fmt(pPMMD.getPrecio())), 1, 2);
        datosPMMD.add(UIPMMD.dato("Margen", pPMMD.getMargen() + " %"), 2, 2);
        cuerpo.getChildren().addAll(UIPMMD.label("Ficha del modelo", "card-title"), datosPMMD);


        var cePMMD = productoServicePMMD.costoEstandar(pPMMD);
        VBox estandarPMMD = new VBox(8);
        estandarPMMD.getStyleClass().add("card-flat");
        if (cePMMD == null) {
            Button crearPMMD = UIPMMD.button("Ir a Fichas técnicas", "DESCRIPTION", "btn-sm", "btn-secondary");
            crearPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("fichas", IntentPMMD.preseleccionar(pPMMD.getCodigo())));
            estandarPMMD.getChildren().addAll(UIPMMD.hbox(8, UIPMMD.label("Costo estándar", "card-title"), UIPMMD.spacer(), new StatusBadgePMMD("Sin ficha aprobada")),
                    UIPMMD.wrap("El modelo no tiene una ficha técnica aprobada; el costo es estimado.", "muted"), crearPMMD);
        } else {
            double margenPMMD = cePMMD.margenPct();
            Hyperlink linkPMMD = new Hyperlink(cePMMD.ficha().getEtiqueta());
            linkPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("fichas", IntentPMMD.abrir(cePMMD.ficha().getUid())));
            estandarPMMD.getChildren().addAll(
                    UIPMMD.hbox(8, UIPMMD.label("Costo estándar", "card-title"), UIPMMD.spacer(), linkPMMD, new StatusBadgePMMD("Aprobada")),
                    UIPMMD.hbox(14, UIPMMD.miniStat("Costo estándar", MoneyPMMD.fmt(cePMMD.costo())), UIPMMD.miniStat("Precio", MoneyPMMD.fmt(pPMMD.getPrecio())),
                            UIPMMD.miniStat("Margen real", String.format("%.1f %%", margenPMMD))));
            if (margenPMMD < 35) {
                estandarPMMD.getChildren().add(UIPMMD.banner("danger-banner", "WARNING", "Margen por debajo del 35 %. Precio sugerido: "
                        + MoneyPMMD.fmt0(cePMMD.costo() / 0.65) + "."));
            }
        }
        cuerpo.getChildren().add(estandarPMMD);
        if (pPMMD.getDescripcion() != null) cuerpo.getChildren().addAll(UIPMMD.label("Descripción", "card-title"), UIPMMD.wrap(pPMMD.getDescripcion(), "muted"));

        Button personalizarPMMD = UIPMMD.primary("Personalizar en 3D", "MDOAL_3D_ROTATION");
        personalizarPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("configurador", IntentPMMD.preseleccionar(pPMMD.getCodigo())));
        Button venderPMMD = UIPMMD.button("Vender", "POINT_OF_SALE", "btn-wood");
        venderPMMD.setDisable(pPMMD.getStock() <= 0);
        UIPMMD.tooltip(venderPMMD, pPMMD.getStock() <= 0 ? "Sin stock: créalo como pedido a medida" : "Agregar al carrito del POS");
        venderPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("ventas", new IntentPMMD("AGREGAR", pPMMD.getCodigo())));
        Button editarPMMD = UIPMMD.secondary("Editar", "EDIT");
        editarPMMD.setOnAction(ePMMD -> {
            panelPMMD.cerrar();
            formularioPMMD(pPMMD);
        });
        Button eliminarPMMD = UIPMMD.iconButton("DELETE_OUTLINE", "Eliminar modelo", "btn-danger-soft");
        eliminarPMMD.setOnAction(ePMMD -> confirmPMMD.mostrar("¿Eliminar " + pPMMD.getNombre() + "?",
                "El modelo dejará de aparecer en el catálogo y en el POS. Las ventas y fichas anteriores se conservan.", "Eliminar",
                () -> {
                    if (toastPMMD.intentar(() -> productoServicePMMD.eliminar(pPMMD.getId()))) {
                        panelPMMD.cerrar();
                        toastPMMD.exito(pPMMD.getCodigo() + " eliminado del catálogo.");
                        onShow(IntentPMMD.NONE);
                    }
                }));
        HBox piePMMD = UIPMMD.hbox(8, eliminarPMMD, UIPMMD.spacer(), editarPMMD, venderPMMD, personalizarPMMD);
        panelPMMD = overlaysPMMD.panel(pPMMD.getNombre(), pPMMD.getCodigo() + " · " + pPMMD.getCategoria(), cuerpo, piePMMD, 500);
    }



    private void formularioPMMD(ProductoPMMD existentePMMD) {
        ProductoPMMD pPMMD = existentePMMD == null ? ProductoPMMD.builder().cuerpos(3).stock(0).forma(FormaPMMD.MODERNO).categoria("Sofá").build() : existentePMMD;
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        TextField nombre = UIPMMD.text(pPMMD.getNombre(), "Sofá Moderno 3 cuerpos");
        ComboBox<String> categoriaPMMD = FormsPMMD.combo(ProductoPMMD.CATEGORIAS, pPMMD.getCategoria(), sPMMD -> sPMMD);
        ComboBox<FormaPMMD> formaPMMD = FormsPMMD.combo(List.of(FormaPMMD.values()), pPMMD.getForma(), FormaPMMD::nombre);
        TextField materialPMMD = UIPMMD.text(pPMMD.getMaterial(), "Chenille");
        TextField colorPMMD = UIPMMD.text(pPMMD.getColor(), "Gris perla");
        ColorPicker hexPMMD = new ColorPicker(ColorSwatchPMMD.parse(pPMMD.getColorHex()));
        hexPMMD.setMaxWidth(Double.MAX_VALUE);
        TextField largoPMMD = FormsPMMD.entero(pPMMD.getLargo());
        TextField fondoPMMD = FormsPMMD.entero(pPMMD.getFondo());
        TextField altoPMMD = FormsPMMD.entero(pPMMD.getAlto());
        TextField cuerposPMMD = FormsPMMD.entero(pPMMD.getCuerpos());
        TextField costoPMMD = FormsPMMD.decimal(pPMMD.getCosto());
        TextField precioPMMD = FormsPMMD.decimal(pPMMD.getPrecio());
        TextField stockPMMD = FormsPMMD.entero(pPMMD.getStock());
        TextArea descPMMD = FormsPMMD.area(pPMMD.getDescripcion(), "Descripción para la ficha y la cotización", 3);

        FormGridPMMD fPMMD = new FormGridPMMD(3);
        fPMMD.seccion("1", "Modelo");
        fPMMD.nodo(vPMMD.campo("Nombre", nombre, () -> ValidacionPMMD.requerido(nombre, "el nombre")), 2);
        fPMMD.nodo(vPMMD.campo("Categoría", categoriaPMMD, () -> ValidacionPMMD.requerido(categoriaPMMD, "la categoría")), 1);
        fPMMD.nodo(vPMMD.campo("Forma (3D y corte)", formaPMMD, () -> ValidacionPMMD.requerido(formaPMMD, "la forma")), 1);
        fPMMD.nodo(vPMMD.campo("Material", materialPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Color", colorPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Color (render)", hexPMMD, null), 1);
        fPMMD.seccion("2", "Medidas");
        fPMMD.nodo(vPMMD.campo("Largo (cm)", largoPMMD, () -> ValidacionPMMD.numero(largoPMMD, 30, "El largo")), 1);
        fPMMD.nodo(vPMMD.campo("Fondo (cm)", fondoPMMD, () -> ValidacionPMMD.numero(fondoPMMD, 30, "El fondo")), 1);
        fPMMD.nodo(vPMMD.campo("Alto (cm)", altoPMMD, () -> ValidacionPMMD.numero(altoPMMD, 30, "El alto")), 1);
        fPMMD.nodo(vPMMD.campo("Cuerpos", cuerposPMMD, () -> ValidacionPMMD.numero(cuerposPMMD, 1, "Los cuerpos")), 1);
        fPMMD.seccion("3", "Precio y stock");
        fPMMD.nodo(vPMMD.campo("Costo (S/)", costoPMMD, () -> ValidacionPMMD.numero(costoPMMD, 0, "El costo")), 1);
        fPMMD.nodo(vPMMD.campo("Precio de venta (S/)", precioPMMD, () -> {
            String ePMMD = ValidacionPMMD.numero(precioPMMD, 1, "El precio");
            if (ePMMD != null) return ePMMD;
            return ValidacionPMMD.valor(precioPMMD) <= ValidacionPMMD.valor(costoPMMD) ? "El precio debe ser mayor que el costo." : null;
        }), 1);
        fPMMD.nodo(vPMMD.campo("Stock", stockPMMD, () -> ValidacionPMMD.numero(stockPMMD, 0, "El stock")), 1);
        fPMMD.nodo(vPMMD.campo("Descripción", descPMMD, null), 3);
        vPMMD.escuchar(costoPMMD.textProperty());

        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button guardarPMMD = UIPMMD.primary(existentePMMD == null ? "Registrar modelo" : "Guardar cambios", "SAVE");
        vPMMD.enlazar(guardarPMMD);
        var h = overlaysPMMD.modal(existentePMMD == null ? "Registrar modelo" : "Editar " + pPMMD.getCodigo(),
                "Los campos marcados se validan mientras escribes.", fPMMD, 640, cancelarPMMD, guardarPMMD);
        cancelarPMMD.setOnAction(ePMMD -> h.cerrar());
        guardarPMMD.setOnAction(ePMMD -> {
            pPMMD.setNombre(nombre.getText().trim());
            pPMMD.setCategoria(categoriaPMMD.getValue());
            pPMMD.setForma(formaPMMD.getValue());
            pPMMD.setMaterial(materialPMMD.getText());
            pPMMD.setColor(colorPMMD.getText());
            pPMMD.setColorHex(String.format("#%02X%02X%02X", (int) (hexPMMD.getValue().getRed() * 255), (int) (hexPMMD.getValue().getGreen() * 255), (int) (hexPMMD.getValue().getBlue() * 255)));
            pPMMD.setLargo(FormsPMMD.entero(largoPMMD));
            pPMMD.setFondo(FormsPMMD.entero(fondoPMMD));
            pPMMD.setAlto(FormsPMMD.entero(altoPMMD));
            pPMMD.setCuerpos(FormsPMMD.entero(cuerposPMMD));
            pPMMD.setCosto(FormsPMMD.dinero(costoPMMD));
            pPMMD.setPrecio(FormsPMMD.dinero(precioPMMD));
            pPMMD.setStock(FormsPMMD.entero(stockPMMD));
            pPMMD.setDescripcion(descPMMD.getText());
            if (toastPMMD.intentar(() -> {
                ProductoPMMD gPMMD = productoServicePMMD.guardar(pPMMD);
                toastPMMD.exito(gPMMD.getCodigo() + " · " + gPMMD.getNombre() + (existentePMMD == null ? " registrado." : " actualizado."));
            })) {
                h.cerrar();
                onShow(IntentPMMD.NONE);
            }
        });
    }
}
