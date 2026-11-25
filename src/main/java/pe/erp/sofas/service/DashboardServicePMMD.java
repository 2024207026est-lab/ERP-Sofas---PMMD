package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.ResumenMensualRepositoryPMMD;

import java.time.LocalDate;
import java.util.*;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServicePMMD {

    public record Kpis(
      double ventasMes, 
        double variacionPct, 
          int sofasVendidos, 
            long sofasFabricacion, 
              long ordenesFabricacion,
                       
                       long pedidosPendientesPMMD, 
                         double metrosTelaPMMD, 
                           long colores) {
    }

    public record Entrega(
      LocalDate fecha, 
        String titulo, 
          String cliente, 
            String codigo, 
              String modulo, 
                String estado) {
    }

    public record Segmento(
      String nombre, 
        double valor, 
          String color) {
    }

    private final VentaServicePMMD ventaServicePMMD;

    private final ProduccionServicePMMD produccionServicePMMD;

    private final PedidoServicePMMD pedidoServicePMMD;

    private final TelaServicePMMD telaServicePMMD;

    private final FinanzasServicePMMD finanzasServicePMMD;

    private final ResumenMensualRepositoryPMMD resumenRepositoryPMMD;

    public Kpis kpis() {
        LocalDate anteriorPMMD = AppClockPMMD.hoy().minusMonths(1);
        double previoPMMD = resumenRepositoryPMMD.findAll().stream()
                .filter(rPMMD -> rPMMD.getAnio() == anteriorPMMD.getYear() && rPMMD.getMes() == anteriorPMMD.getMonthValue())
                .mapToDouble(rPMMD -> rPMMD.getVentas().doubleValue()).findFirst().orElse(0);
        double ventas = ventaServicePMMD.totalMes().doubleValue();
        var kPMMD = produccionServicePMMD.kpis();
        return new Kpis(ventas, previoPMMD == 0 ? 0 : (ventas - previoPMMD) * 100 / previoPMMD, ventaServicePMMD.sofasMes(),
                kPMMD.sofasEnProceso(), kPMMD.ordenesEnProceso(), pedidoServicePMMD.pendientesAMedida(), telaServicePMMD.metrosEnStock(),
                telaServicePMMD.activas().size());
    }

    public List<FinanzasServicePMMD.Mes> ventas12() {
        return finanzasServicePMMD.ultimos12();
    }



    public List<Segmento> ventasPorModelo() {
        Map<String, Double> mPMMD = new LinkedHashMap<>();
        for (String gPMMD : List.of("Sofá moderno", "Seccional", "Juego de sala", "Sofá cama", "Chesterfield", "Otros")) mPMMD.put(gPMMD, 0d);
        for (VentaPMMD vPMMD : ventaServicePMMD.delMes(AppClockPMMD.hoy())) {
            double factorPMMD = vPMMD.getItems().stream().mapToDouble(iPMMD -> iPMMD.getSubtotal().doubleValue()).sum();
            factorPMMD = factorPMMD == 0 ? 0 : vPMMD.getTotal().doubleValue() / factorPMMD;
            for (ItemVentaPMMD iPMMD : vPMMD.getItems()) mPMMD.merge(grupo(iPMMD.getProducto().getForma()), iPMMD.getSubtotal().doubleValue() * factorPMMD, Double::sum);
        }
        String[] colores = {"#2563EB", "#A47148", "#14B8A6", "#8B5CF6", "#C69A6D", "#94A3B8"};
        List<Segmento> lPMMD = new ArrayList<>();
        int iPMMD = 0;
        for (var ePMMD : mPMMD.entrySet()) lPMMD.add(new Segmento(ePMMD.getKey(), ePMMD.getValue(), colores[iPMMD++]));
        return lPMMD;
    }

    public static String grupo(FormaPMMD fPMMD) {
        return switch (fPMMD) {
            case MODERNO -> "Sofá moderno";
            case SECCIONAL_L, SECCIONAL_U -> "Seccional";
            case JUEGO -> "Juego de sala";
            case SOFA_CAMA -> "Sofá cama";
            case CHESTERFIELD -> "Chesterfield";
            default -> "Otros";
        };
    }

    public List<Entrega> proximasEntregas(int maxPMMD) {
        List<Entrega> lPMMD = new ArrayList<>();
        for (PedidoPMMD pPMMD : pedidoServicePMMD.listar()) {
            if (pPMMD.getEstado().cerrado() || pPMMD.getFechaEntrega() == null || pPMMD.getFechaEntrega().isBefore(AppClockPMMD.hoy().minusDays(3))) continue;
            lPMMD.add(new Entrega(pPMMD.getFechaEntrega(), pPMMD.getCantidad() + " × " + pPMMD.getProductoNombre(), pPMMD.getCliente().getNombre(),
                    pPMMD.getCodigo(), "pedidos", pPMMD.isRetrasado() ? "Retrasado" : pPMMD.getEstado().nombre()));
        }
        lPMMD.sort(Comparator.comparing(Entrega::fecha));
        return lPMMD.subList(0, Math.min(maxPMMD, lPMMD.size()));
    }

    public List<PedidoPMMD> ultimosPedidos(int maxPMMD) {
        List<PedidoPMMD> lPMMD = pedidoServicePMMD.listar();
        return lPMMD.subList(0, Math.min(maxPMMD, lPMMD.size()));
    }
}
