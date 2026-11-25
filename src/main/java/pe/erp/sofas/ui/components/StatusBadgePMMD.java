package pe.erp.sofas.ui.components;

import javafx.scene.control.Label;

import java.util.HashMap;
import java.util.Map;



public class StatusBadgePMMD extends Label {

    private static final Map<String, String> VARIANTESPMMD = new HashMap<>();

    static {
        for (String sPMMD : new String[]{"Disponible", "Normal", "Aprobada", "Entregado", "Pagado", "Resuelto", "En garantía", "Listo",
                "Reservado", "Suficiente", "Recibida", "Activo", "Consumido", "Listo para entrega", "Cubierto", "Materiales reservados",
                "Materiales consumidos"}) VARIANTESPMMD.put(sPMMD, "green");
        for (String sPMMD : new String[]{"Stock bajo", "Borrador", "Pendiente", "Por vencer", "Abierto", "Parcial", "Faltará", "Por cotizar",
                "Media", "Vacaciones", "Sin ficha aprobada", "Sin ficha · autorizado", "Registrado", "Nueva", "En reparación"}) VARIANTESPMMD.put(sPMMD, "amber");
        for (String sPMMD : new String[]{"Agotado", "Crítico", "Retrasado", "Cancelado", "Rechazado", "Fuera de garantía", "Faltante", "Sin stock",
                "Alta", "Cancelada", "Vencido", "SLA vencido"}) VARIANTESPMMD.put(sPMMD, "red");
        for (String sPMMD : new String[]{"En producción", "En diagnóstico", "Aprobado", "Confirmado", "Solicitada", "Cotizado", "En taller",
                "Factura", "Aprobada OC", "Materiales disponibles"}) VARIANTESPMMD.put(sPMMD, "blue");
        for (String sPMMD : new String[]{"Personalizado", "Exhibición", "Recojo programado", "A pedido", "Bajo pedido", "Compuesta", "Retazo",
                "MRP", "Calculadora"}) VARIANTESPMMD.put(sPMMD, "wood");
        for (String sPMMD : new String[]{"Obsoleta", "Descontinuada", "Inactivo", "Estándar", "Boleta", "Baja", "Stock de tienda"}) VARIANTESPMMD.put(sPMMD, "gray");
    }

    public StatusBadgePMMD(String estadoPMMD) {
        this(estadoPMMD, variante(estadoPMMD));
    }

    public StatusBadgePMMD(String textoPMMD, String variante) {
        super(textoPMMD);
        getStyleClass().addAll("badge", "badge-" + variante);
        setMinWidth(USE_PREF_SIZE);
    }

    public static String variante(String estadoPMMD) {
        if (estadoPMMD == null) return "gray";
        if (estadoPMMD.startsWith("Faltan") || estadoPMMD.startsWith("Faltarán")) return "red";
        if (estadoPMMD.startsWith("En garantía")) return "green";
        if (estadoPMMD.startsWith("OP-")) return "wood";
        return VARIANTESPMMD.getOrDefault(estadoPMMD, "gray");
    }

    public StatusBadgePMMD conIcono(String iconoPMMD) {
        setGraphic(IcoPMMD.of(iconoPMMD, 13));
        return this;
    }
}
