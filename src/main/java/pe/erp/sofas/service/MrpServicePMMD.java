package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.MaterialRepositoryPMMD;
import pe.erp.sofas.repository.OrdenCompraRepositoryPMMD;
import pe.erp.sofas.repository.PedidoRepositoryPMMD;
import pe.erp.sofas.service.calculo.ExplosionPMMD;
import pe.erp.sofas.service.calculo.MedidasPMMD;
import pe.erp.sofas.service.calculo.Opciones;
import pe.erp.sofas.service.calculo.UnidadCompraPMMD;

import java.time.LocalDate;
import java.util.*;






@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MrpServicePMMD {

    public record Fila(
      MaterialPMMD material, 
        double requerido, 
          double transito, 
            double faltante, 
              LocalDate necesitaEl,
                       
                       ProveedorPMMD proveedor, 
                         Set<String> origenes) {

        public double stock() {
            return material.getStock();
        }

        public double reservado() {
            return material.getReservado();
        }

        public double disponible() {
            return material.getDisponible();
        }

        public double valorFaltante() {
            return faltante * material.getCosto();
        }
    }

    public record Filtro(
      LocalDate desde, 
        LocalDate hasta, 
          String categoria, 
            Long proveedorId, 
              boolean soloFaltantes) {

        public static final Filtro TODO = new Filtro(null, null, null, null, false);
    }

    public record Resultado(
      List<Fila> filas, 
        int ops, 
          int pedidos) {

        public long conFaltante() {
            return filas.stream().filter(fPMMD -> fPMMD.faltante() > 1e-6).count();
        }

        public double valorFaltante() {
            return filas.stream().mapToDouble(Fila::valorFaltante).sum();
        }
    }

    private final ProduccionServicePMMD produccionServicePMMD;

    private final FichaServicePMMD fichaServicePMMD;

    private final PedidoRepositoryPMMD pedidoRepositoryPMMD;

    private final OrdenCompraRepositoryPMMD compraRepositoryPMMD;

    private final MaterialRepositoryPMMD materialRepositoryPMMD;

    private final CompraServicePMMD compraServicePMMD;

    public Resultado calcular(Filtro filtroPMMD) {
        if (filtroPMMD == null) filtroPMMD = Filtro.TODO;
        Map<Long, double[]> reqPMMD = new LinkedHashMap<>();
        Map<Long, LocalDate> fechasPMMD = new HashMap<>();
        Map<Long, Set<String>> origenes = new HashMap<>();
        int ops = 0, pedidos = 0;
        for (OrdenProduccionPMMD opPMMD : produccionServicePMMD.activas()) {
            if (!enRangoPMMD(opPMMD.getFechaEntrega(), filtroPMMD)) continue;
            ExplosionPMMD.Resultado rPMMD = produccionServicePMMD.requerimiento(opPMMD);
            if (rPMMD == null) continue;
            ops++;
            LocalDate necesitaPMMD = opPMMD.getEtapa() == EtapaPMMD.PENDIENTE ? fechaPMMD(opPMMD.getFechaInicio()) : AppClockPMMD.hoy();
            acumularPMMD(rPMMD, opPMMD.getCodigo(), necesitaPMMD, reqPMMD, fechasPMMD, origenes);
        }
        for (PedidoPMMD pPMMD : pedidoRepositoryPMMD.findAllByOrderByCodigoDesc()) {
            if (pPMMD.getEstado() != EstadoPedidoPMMD.CONFIRMADO || pPMMD.getOpCodigo() != null || pPMMD.getProducto() == null) continue;
            if (!enRangoPMMD(pPMMD.getFechaEntrega(), filtroPMMD)) continue;
            FichaTecnicaPMMD fPMMD = fichaServicePMMD.aprobadaDe(pPMMD.getProducto());
            if (fPMMD == null) continue;
            pedidos++;
            ExplosionPMMD.Resultado rPMMD = fichaServicePMMD.explotar(fPMMD, pPMMD.getCantidad(), opcionesPedido(pPMMD, fPMMD));
            LocalDate necesitaPMMD = pPMMD.getFechaEntrega() == null ? AppClockPMMD.hoy().plusDays(7) : pPMMD.getFechaEntrega().minusDays(10);
            acumularPMMD(rPMMD, pPMMD.getCodigo(), necesitaPMMD.isBefore(AppClockPMMD.hoy()) ? AppClockPMMD.hoy() : necesitaPMMD, reqPMMD, fechasPMMD, origenes);
        }
        Map<Long, Double> transito = transito();
        List<Fila> filas = new ArrayList<>();
        for (var ePMMD : reqPMMD.entrySet()) {
            MaterialPMMD m = materialRepositoryPMMD.findById(ePMMD.getKey()).orElseThrow();
            if (filtroPMMD.categoria() != null && !filtroPMMD.categoria().equals(m.getCategoria())) continue;
            if (filtroPMMD.proveedorId() != null && (m.getProveedor() == null || !m.getProveedor().getId().equals(filtroPMMD.proveedorId()))) continue;
            double requerido = m.isEntera() ? Math.ceil(ePMMD.getValue()[0] - 1e-9) : ePMMD.getValue()[0];
            double tr = transito.getOrDefault(m.getId(), 0d);
            double faltante = Math.max(0, requerido - m.getStock() - tr);
            if (filtroPMMD.soloFaltantes() && faltante <= 1e-6) continue;
            filas.add(new Fila(m, requerido, tr, faltante, fechasPMMD.get(m.getId()), m.getProveedor(), origenes.get(m.getId())));
        }
        filas.sort(Comparator.comparingDouble(Fila::valorFaltante).reversed().thenComparing(fPMMD -> fPMMD.material().getCodigo()));
        return new Resultado(filas, ops, pedidos);
    }

    public static Opciones opcionesPedido(PedidoPMMD pPMMD, FichaTecnicaPMMD fPMMD) {
        Map<String, String> sust = new HashMap<>();
        if (pPMMD.getTela() != null) {
            for (LineaBOMPMMD lPMMD : fPMMD.getLineas()) if (lPMMD.getMaterial().isTela()) sust.put(lPMMD.getMaterial().getCodigo(), pPMMD.getTela().getTipo().materialCodigo());
            if (Boolean.TRUE.equals(fPMMD.getCompuesta())) {
                for (TipoTelaPMMD tPMMD : TipoTelaPMMD.values()) sust.put(tPMMD.materialCodigo(), pPMMD.getTela().getTipo().materialCodigo());
            }
        }
        MedidasPMMD m = null;
        if (pPMMD.getLargo() != null && pPMMD.getLargo() > 0 && !Boolean.TRUE.equals(fPMMD.getCompuesta())) {
            m = new MedidasPMMD(pPMMD.getLargo(), pPMMD.getFondo() == null ? fPMMD.getFondo() : pPMMD.getFondo(),
                    pPMMD.getAlto() == null ? fPMMD.getAlto() : pPMMD.getAlto(), fPMMD.getCuerpos());
        }
        return new Opciones(m, sust, List.of(), false, 0, 0);
    }



    public Map<Long, Double> transito() {
        Map<Long, Double> tPMMD = new HashMap<>();
        for (OrdenCompraPMMD ocPMMD : compraRepositoryPMMD.findAll()) {
            if (!ocPMMD.getEstado().enTransito()) continue;
            for (ItemCompraPMMD iPMMD : ocPMMD.getItems()) tPMMD.merge(iPMMD.getMaterial().getId(), iPMMD.getCantidad(), Double::sum);
        }
        return tPMMD;
    }

    public long comprasEnTransito() {
        return compraRepositoryPMMD.findAll().stream().filter(oPMMD -> oPMMD.getEstado().enTransito()).count();
    }



    @Transactional
    public List<String> generarOrdenes(List<Fila> filas, String origenPMMD) {
        Map<ProveedorPMMD, List<CompraServicePMMD.LineaCompra>> porProveedorPMMD = new LinkedHashMap<>();
        for (Fila fPMMD : filas) {
            if (fPMMD.faltante() <= 1e-6 || fPMMD.proveedor() == null) continue;
            double cant = UnidadCompraPMMD.redondear(fPMMD.material().getUnidad(), fPMMD.faltante());
            porProveedorPMMD.computeIfAbsent(fPMMD.proveedor(), kPMMD -> new ArrayList<>())
                    .add(new CompraServicePMMD.LineaCompra(fPMMD.material().getId(), null, cant,
                            java.math.BigDecimal.valueOf(fPMMD.material().getCosto())));
        }
        List<String> codigosPMMD = new ArrayList<>();
        porProveedorPMMD.forEach((pPMMD, lineasPMMD) -> codigosPMMD.add(compraServicePMMD.crear(pPMMD.getId(), lineasPMMD, false,
                "Generada desde el requerimiento de materiales.", origenPMMD, true).getCodigo()));
        return codigosPMMD;
    }

    private void acumularPMMD(ExplosionPMMD.Resultado rPMMD, String origenPMMD, LocalDate necesitaPMMD, Map<Long, double[]> reqPMMD,
                          Map<Long, LocalDate> fechasPMMD, Map<Long, Set<String>> origenes) {
        for (ExplosionPMMD.PorMaterial pmPMMD : rPMMD.porMaterial()) {
            Long idPMMD = pmPMMD.material().getId();
            reqPMMD.computeIfAbsent(idPMMD, kPMMD -> new double[1])[0] += pmPMMD.cantidad();
            fechasPMMD.merge(idPMMD, necesitaPMMD, (aPMMD, bPMMD) -> aPMMD.isBefore(bPMMD) ? aPMMD : bPMMD);
            origenes.computeIfAbsent(idPMMD, kPMMD -> new LinkedHashSet<>()).add(origenPMMD);
        }
    }

    private static boolean enRangoPMMD(LocalDate fPMMD, Filtro filtroPMMD) {
        if (fPMMD == null) return true;
        if (filtroPMMD.desde() != null && fPMMD.isBefore(filtroPMMD.desde())) return false;
        return filtroPMMD.hasta() == null || !fPMMD.isAfter(filtroPMMD.hasta());
    }

    private static LocalDate fechaPMMD(LocalDate fPMMD) {
        return fPMMD == null || fPMMD.isBefore(AppClockPMMD.hoy()) ? AppClockPMMD.hoy() : fPMMD;
    }
}
