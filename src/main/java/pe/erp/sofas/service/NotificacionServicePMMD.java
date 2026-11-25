package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.NotificacionEstadoRepositoryPMMD;

import java.util.*;
import java.util.stream.Collectors;






@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificacionServicePMMD {
    


    public record Notificacion(
      String clave, 
        String tipo, 
          String icono, 
            String titulo, 
              String detalle, 
                String hace,
                               
                               String modulo, 
                                 Object dato) {
    }

    private final InventarioServicePMMD inventarioServicePMMD;

    private final TelaServicePMMD telaServicePMMD;

    private final ProduccionServicePMMD produccionServicePMMD;

    private final PedidoServicePMMD pedidoServicePMMD;

    private final NotificacionEstadoRepositoryPMMD estadoRepositoryPMMD;

    public List<Notificacion> todas() {
        List<Notificacion> lPMMD = new ArrayList<>();
        List<String> criticosPMMD = new ArrayList<>();
        inventarioServicePMMD.materiales().stream().filter(mPMMD -> "Crítico".equals(mPMMD.getEstado()))
                .forEach(mPMMD -> criticosPMMD.add(mPMMD.getNombre().toLowerCase(Locale.ROOT)));
        telaServicePMMD.activas().stream().filter(tPMMD -> "Stock bajo".equals(tPMMD.getEstado()))
                .forEach(tPMMD -> criticosPMMD.add("tela " + tPMMD.getColor()));
        if (!criticosPMMD.isEmpty()) {
            lPMMD.add(new Notificacion("stock:" + String.join(",", criticosPMMD), "danger", "ALL_INBOX",
                    "Stock crítico: " + unirPMMD(criticosPMMD), "Por debajo del mínimo; revisa el requerimiento de materiales.",
                    "Hace 20 min", "mrp", null));
        }
        for (OrdenProduccionPMMD opPMMD : produccionServicePMMD.activas()) {
            if (!opPMMD.isRetrasada()) continue;
            String clientePMMD = opPMMD.getCliente() == null ? "stock" : primerosNombresPMMD(opPMMD.getCliente().getNombre());
            lPMMD.add(new Notificacion("retraso:" + opPMMD.getCodigo() + ":" + opPMMD.getFechaEntrega(), "danger", "ALARM",
                    "Orden retrasada: " + modeloCortoPMMD(opPMMD.getProductoNombre()) + " de " + clientePMMD + ", vencida el " + AppClockPMMD.corta(opPMMD.getFechaEntrega()),
                    opPMMD.getCodigo() + " · sigue en " + opPMMD.getEtapa().nombre(),
                    "Hace 1 h", "produccion", opPMMD.getCodigo()));
        }
        var mananaPMMD = AppClockPMMD.hoy().plusDays(1);
        for (PedidoPMMD pPMMD : pedidoServicePMMD.listar()) {
            if (pPMMD.getEstado().cerrado() || !mananaPMMD.equals(pPMMD.getFechaEntrega())) continue;
            lPMMD.add(new Notificacion("entrega:" + pPMMD.getCodigo(), "info", "LOCAL_SHIPPING",
                    "Entrega mañana: " + pPMMD.getCantidad() + (pPMMD.getCantidad() == 1 ? " sofá" : " sofás") + " para " + nombreCortoPMMD(pPMMD.getCliente().getNombre()),
                    pPMMD.getCodigo() + " · " + pPMMD.getProductoNombre() + " · " + pPMMD.getEstado().nombre(), "Hace 2 h", "pedidos", pPMMD.getCodigo()));
        }
        List<MaterialPMMD> agotadosPMMD = inventarioServicePMMD.materiales().stream()
                .filter(mPMMD -> "Agotado".equals(mPMMD.getEstado()) && !mPMMD.getBajoPedido()).toList();
        if (!agotadosPMMD.isEmpty()) {
            lPMMD.add(new Notificacion("agotado:" + agotadosPMMD.stream().map(MaterialPMMD::getCodigo).collect(Collectors.joining(",")), "warn", "REMOVE_SHOPPING_CART",
                    "Material agotado: " + unirPMMD(agotadosPMMD.stream().map(mPMMD -> mPMMD.getNombre().toLowerCase(Locale.ROOT)).toList()),
                    "Sin disponible para nuevas órdenes. Genera la orden de compra.", "Hace 3 h", "inventario",
                    agotadosPMMD.get(0).getCodigo()));
        }
        List<OrdenProduccionPMMD> bloqueadas = bloqueadas();
        if (!bloqueadas.isEmpty()) {
            lPMMD.add(new Notificacion("bloqueadas:" + bloqueadas.stream().map(OrdenProduccionPMMD::getCodigo).collect(Collectors.joining(",")), "warn", "BLOCK",
                    bloqueadas.size() + (bloqueadas.size() == 1 ? " orden bloqueada" : " órdenes bloqueadas") + " por falta de material",
                    bloqueadas.stream().map(OrdenProduccionPMMD::getCodigo).collect(Collectors.joining(", ")), "Hoy", "produccion", null));
        }
        Set<String> descartadasPMMD = new HashSet<>();
        estadoRepositoryPMMD.findAll().forEach(ePMMD -> {
            if (Boolean.TRUE.equals(ePMMD.getDescartada())) descartadasPMMD.add(ePMMD.getClave());
        });
        lPMMD.removeIf(nPMMD -> descartadasPMMD.contains(nPMMD.clave()));
        return lPMMD;
    }



    public List<OrdenProduccionPMMD> bloqueadas() {
        return produccionServicePMMD.activas().stream().filter(oPMMD -> oPMMD.getEtapa().productiva() && !oPMMD.isSinFicha())
                .filter(oPMMD -> produccionServicePMMD.estadoMateriales(oPMMD).faltantes() > 0).toList();
    }

    public boolean leida(Notificacion nPMMD) {
        return estadoRepositoryPMMD.findById(nPMMD.clave()).map(ePMMD -> Boolean.TRUE.equals(ePMMD.getLeida())).orElse(false);
    }

    public long noLeidas() {
        return todas().stream().filter(nPMMD -> !leida(nPMMD)).count();
    }

    @Transactional
    public void marcarLeida(Notificacion nPMMD) {
        var ePMMD = estadoRepositoryPMMD.findById(nPMMD.clave()).orElse(new NotificacionEstadoPMMD(nPMMD.clave(), false, false));
        ePMMD.setLeida(true);
        estadoRepositoryPMMD.save(ePMMD);
    }

    @Transactional
    public void marcarTodasLeidas() {
        todas().forEach(this::marcarLeida);
    }

    @Transactional
    public void descartar(Notificacion nPMMD) {
        var ePMMD = estadoRepositoryPMMD.findById(nPMMD.clave()).orElse(new NotificacionEstadoPMMD(nPMMD.clave(), true, false));
        ePMMD.setDescartada(true);
        estadoRepositoryPMMD.save(ePMMD);
    }

    private static String unirPMMD(List<String> lPMMD) {
        if (lPMMD.size() == 1) return lPMMD.get(0);
        return String.join(", ", lPMMD.subList(0, lPMMD.size() - 1)) + " y " + lPMMD.get(lPMMD.size() - 1);
    }



    private static String modeloCortoPMMD(String nPMMD) {
        String sPMMD = nPMMD.split(" · ")[0].replaceAll("\\s+\\d+ cuerpos?$", "");
        return sPMMD.toLowerCase(Locale.ROOT);
    }



    private static String primerosNombresPMMD(String nPMMD) {
        String[] pPMMD = nPMMD.split(" ");
        return pPMMD.length >= 2 ? pPMMD[0] + " " + pPMMD[1] : nPMMD;
    }



    private static String nombreCortoPMMD(String nPMMD) {
        if (nPMMD.startsWith("Hotel")) return "el " + nPMMD.replace(" Juliaca", "").replace(" S.A.C.", "");
        return primerosNombresPMMD(nPMMD);
    }
}
