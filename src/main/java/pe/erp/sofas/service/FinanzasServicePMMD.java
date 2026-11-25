package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanzasServicePMMD {

    public record Mes(
      int anio, 
        int mes, 
          double ventas, 
            double ingresos, 
              double egresos) {

        public String etiqueta() {
            return AppClockPMMD.mesCorto(mes) + (mes == 1 ? " " + String.valueOf(anio).substring(2) : "");
        }
    }

    public record Kpis(
      double ingresos, 
        double egresos, 
          double utilidad, 
            double porCobrar, 
              double porPagar) {
    }

    public record Rentabilidad(
      String nombre, 
        String detalle, 
          int unidades, 
            double ingresos, 
              double costo) {

        public double margen() {
            return ingresos - costo;
        }

        public double margenPct() {
            return ingresos == 0 ? 0 : margen() * 100 / ingresos;
        }
    }

    private final ResumenMensualRepositoryPMMD resumenRepositoryPMMD;

    private final VentaRepositoryPMMD ventaRepositoryPMMD;

    private final PagoRepositoryPMMD pagoRepositoryPMMD;

    private final OrdenCompraRepositoryPMMD compraRepositoryPMMD;

    private final GastoRepositoryPMMD gastoRepositoryPMMD;

    private final PedidoRepositoryPMMD pedidoRepositoryPMMD;

    private final ConsumoRealRepositoryPMMD consumoRepositoryPMMD;

    private final ProductoServicePMMD productoServicePMMD;

    private final CompraServicePMMD compraServicePMMD;



    public List<Mes> ultimos12() {
        List<Mes> lPMMD = new ArrayList<>();
        LocalDate hoyPMMD = AppClockPMMD.hoy();
        Map<String, ResumenMensualPMMD> histPMMD = new HashMap<>();
        resumenRepositoryPMMD.findAll().forEach(rPMMD -> histPMMD.put(rPMMD.getAnio() + "-" + rPMMD.getMes(), rPMMD));
        for (int iPMMD = 11; iPMMD >= 1; iPMMD--) {
            LocalDate dPMMD = hoyPMMD.minusMonths(iPMMD);
            ResumenMensualPMMD rPMMD = histPMMD.get(dPMMD.getYear() + "-" + dPMMD.getMonthValue());
            double v = rPMMD == null ? 0 : rPMMD.getVentas().doubleValue();
            lPMMD.add(new Mes(dPMMD.getYear(), dPMMD.getMonthValue(), v, v * 1.18, rPMMD == null ? 0 : rPMMD.getEgresos().doubleValue()));
        }
        lPMMD.add(new Mes(hoyPMMD.getYear(), hoyPMMD.getMonthValue(), ventasMes(), ingresosMes(), egresosMes()));
        return lPMMD;
    }

    public double ventasMes() {
        LocalDate inicioPMMD = AppClockPMMD.hoy().withDayOfMonth(1);
        return ventaRepositoryPMMD.findAll().stream().filter(v -> !v.getFecha().toLocalDate().isBefore(inicioPMMD))
                .mapToDouble(v -> v.getTotal().doubleValue()).sum();
    }



    public double ingresosMes() {
        LocalDate inicioPMMD = AppClockPMMD.hoy().withDayOfMonth(1);
        double pagosPMMD = pagoRepositoryPMMD.findAll().stream().filter(pPMMD -> pPMMD.getFecha() != null && !pPMMD.getFecha().isBefore(inicioPMMD))
                .mapToDouble(pPMMD -> pPMMD.getMonto().doubleValue()).sum();
        return ventasMes() + pagosPMMD;
    }



    public double egresosMes() {
        LocalDate inicioPMMD = AppClockPMMD.hoy().withDayOfMonth(1);
        double comprasPMMD = compraRepositoryPMMD.findAll().stream().filter(oPMMD -> oPMMD.getEstado() == EstadoCompraPMMD.RECIBIDA
                && oPMMD.getFechaRecepcion() != null && !oPMMD.getFechaRecepcion().isBefore(inicioPMMD)).mapToDouble(oPMMD -> oPMMD.getTotal().doubleValue()).sum();
        double gastosPMMD = gastoRepositoryPMMD.findAll().stream().filter(gPMMD -> !gPMMD.getFecha().isBefore(inicioPMMD)).mapToDouble(gPMMD -> gPMMD.getMonto().doubleValue()).sum();
        return comprasPMMD + gastosPMMD;
    }

    public List<PedidoPMMD> porCobrar() {
        return pedidoRepositoryPMMD.findAllByOrderByCodigoDesc().stream().filter(pPMMD -> !pPMMD.getEstado().cerrado() && pPMMD.getSaldo().signum() > 0).toList();
    }

    public List<OrdenCompraPMMD> porPagar() {
        return compraServicePMMD.porPagar();
    }

    public List<GastoPMMD> gastosMes() {
        LocalDate inicioPMMD = AppClockPMMD.hoy().withDayOfMonth(1);
        return gastoRepositoryPMMD.findAll().stream().filter(gPMMD -> !gPMMD.getFecha().isBefore(inicioPMMD))
                .sorted(Comparator.comparing(GastoPMMD::getFecha).reversed()).toList();
    }

    public Kpis kpis() {
        double ing = ingresosMes(), egr = egresosMes();
        double cxc = porCobrar().stream().mapToDouble(pPMMD -> pPMMD.getSaldo().doubleValue()).sum();
        double cxp = porPagar().stream().mapToDouble(oPMMD -> oPMMD.getTotal().doubleValue()).sum();
        return new Kpis(ing, egr, ing - egr, cxc, cxp);
    }



    public List<Rentabilidad> porModelo() {
        Map<Long, int[]> unidades = new LinkedHashMap<>();
        Map<Long, double[]> ingresos = new HashMap<>();
        Map<Long, ProductoPMMD> productosPMMD = new HashMap<>();
        for (VentaPMMD v : ventaRepositoryPMMD.findAll()) {
            for (ItemVentaPMMD iPMMD : v.getItems()) {
                ProductoPMMD pPMMD = iPMMD.getProducto();
                productosPMMD.put(pPMMD.getId(), pPMMD);
                unidades.computeIfAbsent(pPMMD.getId(), kPMMD -> new int[1])[0] += iPMMD.getCantidad();
                ingresos.computeIfAbsent(pPMMD.getId(), kPMMD -> new double[1])[0] += iPMMD.getSubtotal().doubleValue();
            }
        }
        for (PedidoPMMD pPMMD : pedidoRepositoryPMMD.findAll()) {
            if (pPMMD.getEstado() == EstadoPedidoPMMD.CANCELADO || pPMMD.getProducto() == null) continue;
            productosPMMD.put(pPMMD.getProducto().getId(), pPMMD.getProducto());
            unidades.computeIfAbsent(pPMMD.getProducto().getId(), kPMMD -> new int[1])[0] += pPMMD.getCantidad();
            ingresos.computeIfAbsent(pPMMD.getProducto().getId(), kPMMD -> new double[1])[0] += pPMMD.getPrecio().doubleValue();
        }
        List<Rentabilidad> rPMMD = new ArrayList<>();
        for (var ePMMD : unidades.entrySet()) {
            ProductoPMMD pPMMD = productosPMMD.get(ePMMD.getKey());
            var cePMMD = productoServicePMMD.costoEstandar(pPMMD);
            double costoUPMMD = cePMMD != null ? cePMMD.costo() : pPMMD.getCosto() == null ? 0 : pPMMD.getCosto().doubleValue();
            rPMMD.add(new Rentabilidad(pPMMD.getNombre(), pPMMD.getCodigo(), ePMMD.getValue()[0], ingresos.get(ePMMD.getKey())[0], costoUPMMD * ePMMD.getValue()[0]));
        }
        rPMMD.sort(Comparator.comparingDouble(Rentabilidad::margen).reversed());
        return rPMMD;
    }



    public List<Rentabilidad> porTela() {
        List<Rentabilidad> rPMMD = new ArrayList<>();
        for (TipoTelaPMMD tPMMD : TipoTelaPMMD.values()) {
            List<PedidoPMMD> pedPMMD = pedidoRepositoryPMMD.findAll().stream().filter(pPMMD -> pPMMD.getTela() != null && pPMMD.getTela().getTipo() == tPMMD
                    && pPMMD.getEstado() != EstadoPedidoPMMD.CANCELADO).toList();
            double metrosPMMD = consumoRepositoryPMMD.findAll().stream().filter(cPMMD -> cPMMD.getMaterial().getCodigo().equals(tPMMD.materialCodigo()))
                    .mapToDouble(ConsumoRealPMMD::getRealConsumido).sum();
            double ingresoTela = pedPMMD.stream().mapToDouble(pPMMD -> pPMMD.getPrecio().doubleValue() * 0.22).sum() + metrosPMMD * tPMMD.costoMetro() * 1.55;
            double costo = metrosPMMD * tPMMD.costoMetro() + pedPMMD.stream().mapToDouble(pPMMD -> pPMMD.getCantidad() * 10.5 * tPMMD.costoMetro()).sum();
            rPMMD.add(new Rentabilidad(tPMMD.nombre(), "S/ " + (int) tPMMD.costoMetro() + "/m · recargo " + (int) tPMMD.recargoPct() + " %",
                    pedPMMD.size(), ingresoTela, costo));
        }
        rPMMD.sort(Comparator.comparingDouble(Rentabilidad::margen).reversed());
        return rPMMD;
    }
}
