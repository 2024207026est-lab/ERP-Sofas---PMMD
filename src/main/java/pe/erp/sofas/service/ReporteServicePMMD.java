package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.MoneyPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServicePMMD {

    public enum Tipo {
        
        VENTAS_MODELO("Ventas por modelo", "Unidades e importe vendido por modelo de sofá.", "INSERT_CHART"),
        
        TELAS_VENDIDAS("Telas y colores más vendidos", "Colores con más pedidos y ventas.", "PALETTE"),
        
        METROS_TELA("Metros de tela consumidos", "Metros estimados y reales por tipo de tela.", "STRAIGHTEN"),
        
        PRODUCCION_TAPICERO("Producción por tapicero", "Órdenes y sofás por responsable.", "ENGINEERING"),
        
        COSTO_ESTANDAR_REAL("Costo estándar frente al real", "Desvío valorizado por orden de producción.", "COMPARE_ARROWS"),
        
        MERMAS("Mermas por material", "Sobremerma y su costo por material.", "CONTENT_CUT"),
        
        PEDIDOS_PENDIENTES("Pedidos pendientes", "Pedidos abiertos con su saldo y entrega.", "PENDING_ACTIONS"),
        
        GARANTIAS("Garantías y reclamos", "Reclamos, cobertura y costo.", "VERIFIED_USER"),
        
        KARDEX("Kardex", "Movimientos de almacén del periodo.", "RECEIPT_LONG");

        private final String nombre;

        private final String descripcion;

        private final String icono;

        Tipo(String nombre, String descripcion, String icono) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.icono = icono;
        }

        public String nombre() {
            return nombre;
        }

        public String descripcion() {
            return descripcion;
        }

        public String icono() {
            return icono;
        }
    }

    private final VentaRepositoryPMMD ventaRepositoryPMMD;

    private final PedidoRepositoryPMMD pedidoRepositoryPMMD;

    private final ConsumoRealRepositoryPMMD consumoRepositoryPMMD;

    private final OrdenProduccionRepositoryPMMD opRepositoryPMMD;

    private final MovimientoRepositoryPMMD movimientoRepositoryPMMD;

    private final TelaRepositoryPMMD telaRepositoryPMMD;

    private final GarantiaServicePMMD garantiaServicePMMD;

    public Tabla generar(Tipo tipoPMMD, LocalDate desdePMMD, LocalDate hastaPMMD) {
        String rangoPMMD = "Del " + AppClockPMMD.fmt(desdePMMD) + " al " + AppClockPMMD.fmt(hastaPMMD);
        return switch (tipoPMMD) {
            case VENTAS_MODELO -> ventasModeloPMMD(rangoPMMD, desdePMMD, hastaPMMD);
            case TELAS_VENDIDAS -> telasVendidasPMMD(rangoPMMD, desdePMMD, hastaPMMD);
            case METROS_TELA -> metrosTelaPMMD(rangoPMMD, desdePMMD, hastaPMMD);
            case PRODUCCION_TAPICERO -> produccionTapiceroPMMD(rangoPMMD, desdePMMD, hastaPMMD);
            case COSTO_ESTANDAR_REAL -> costoRealPMMD(rangoPMMD, desdePMMD, hastaPMMD);
            case MERMAS -> mermasPMMD(rangoPMMD, desdePMMD, hastaPMMD);
            case PEDIDOS_PENDIENTES -> pedidosPendientesPMMD(rangoPMMD, desdePMMD, hastaPMMD);
            case GARANTIAS -> garantiasPMMD(rangoPMMD, desdePMMD, hastaPMMD);
            case KARDEX -> kardexPMMD(rangoPMMD, desdePMMD, hastaPMMD);
        };
    }

    private static boolean en(LocalDate fPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        return fPMMD != null && !fPMMD.isBefore(dPMMD) && !fPMMD.isAfter(hPMMD);
    }

    private Tabla ventasModeloPMMD(String rangoPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        Map<String, double[]> accPMMD = new LinkedHashMap<>();
        double totalPMMD = 0;
        for (VentaPMMD vPMMD : ventaRepositoryPMMD.findAll()) {
            if (!en(vPMMD.getFecha().toLocalDate(), dPMMD, hPMMD)) continue;
            for (ItemVentaPMMD iPMMD : vPMMD.getItems()) {
                double[] aPMMD = accPMMD.computeIfAbsent(iPMMD.getProducto().getCodigo() + "|" + iPMMD.getProducto().getNombre(), kPMMD -> new double[2]);
                aPMMD[0] += iPMMD.getCantidad();
                aPMMD[1] += iPMMD.getSubtotal().doubleValue();
                totalPMMD += iPMMD.getSubtotal().doubleValue();
            }
        }
        Tabla tPMMD = Tabla.de(Tipo.VENTAS_MODELO.nombre(), rangoPMMD, List.of("Código", "Modelo", "Unidades", "Importe", "% del total"),
                new boolean[]{false, false, true, true, true});
        double totPMMD = totalPMMD;
        int unidadesPMMD = 0;
        for (var ePMMD : accPMMD.entrySet().stream().sorted((aPMMD, bPMMD) -> Double.compare(bPMMD.getValue()[1], aPMMD.getValue()[1])).toList()) {
            String[] kPMMD = ePMMD.getKey().split("\\|");
            unidadesPMMD += (int) ePMMD.getValue()[0];
            tPMMD.fila(kPMMD[0], kPMMD[1], (int) ePMMD.getValue()[0], MoneyPMMD.fmt(ePMMD.getValue()[1]), String.format("%.1f %%", totPMMD == 0 ? 0 : ePMMD.getValue()[1] * 100 / totPMMD));
        }
        return tPMMD.conTotales("", "Total", String.valueOf(unidadesPMMD), MoneyPMMD.fmt(totalPMMD), "100 %");
    }

    private Tabla telasVendidasPMMD(String rangoPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        Map<String, int[]> accPMMD = new LinkedHashMap<>();
        Map<String, TelaPMMD> telasPMMD = new HashMap<>();
        telaRepositoryPMMD.findAll().forEach(tPMMD -> telasPMMD.put(tPMMD.getCodigo(), tPMMD));
        for (VentaPMMD vPMMD : ventaRepositoryPMMD.findAll()) {
            if (!en(vPMMD.getFecha().toLocalDate(), dPMMD, hPMMD)) continue;
            for (ItemVentaPMMD iPMMD : vPMMD.getItems()) if (iPMMD.getTelaCodigo() != null) accPMMD.computeIfAbsent(iPMMD.getTelaCodigo(), kPMMD -> new int[2])[0] += iPMMD.getCantidad();
        }
        for (PedidoPMMD pPMMD : pedidoRepositoryPMMD.findAll()) {
            if (pPMMD.getTela() == null || pPMMD.getEstado() == EstadoPedidoPMMD.CANCELADO || pPMMD.getFecha() == null || pPMMD.getFecha().isAfter(hPMMD)) continue;
            accPMMD.computeIfAbsent(pPMMD.getTela().getCodigo(), kPMMD -> new int[2])[1] += pPMMD.getCantidad();
        }
        Tabla tPMMD = Tabla.de(Tipo.TELAS_VENDIDAS.nombre(), rangoPMMD, List.of("Código", "Tela", "Tipo", "Ventas tienda", "Pedidos", "Total"),
                new boolean[]{false, false, false, true, true, true});
        accPMMD.entrySet().stream().sorted((aPMMD, bPMMD) -> (bPMMD.getValue()[0] + bPMMD.getValue()[1]) - (aPMMD.getValue()[0] + aPMMD.getValue()[1]))
                .forEach(ePMMD -> {
                    TelaPMMD telaPMMD = telasPMMD.get(ePMMD.getKey());
                    tPMMD.fila(ePMMD.getKey(), telaPMMD == null ? "—" : telaPMMD.getColor(), telaPMMD == null ? "—" : telaPMMD.getTipo().nombre(),
                            ePMMD.getValue()[0], ePMMD.getValue()[1], ePMMD.getValue()[0] + ePMMD.getValue()[1]);
                });
        return tPMMD;
    }

    private List<ConsumoRealPMMD> consumosPMMD(LocalDate dPMMD, LocalDate hPMMD) {
        return consumoRepositoryPMMD.findAll().stream().filter(cPMMD -> en(cPMMD.getFecha(), dPMMD, hPMMD)).toList();
    }

    private Tabla metrosTelaPMMD(String rangoPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        Tabla tPMMD = Tabla.de(Tipo.METROS_TELA.nombre(), rangoPMMD, List.of("Material", "Órdenes", "Estimado (m)", "Real (m)", "Diferencia", "Costo real"),
                new boolean[]{false, true, true, true, true, true});
        Map<MaterialPMMD, List<ConsumoRealPMMD>> gPMMD = consumosPMMD(dPMMD, hPMMD).stream().filter(cPMMD -> cPMMD.getMaterial().isTela())
                .collect(Collectors.groupingBy(ConsumoRealPMMD::getMaterial, LinkedHashMap::new, Collectors.toList()));
        double tePMMD = 0, trPMMD = 0;
        for (var ePMMD : gPMMD.entrySet()) {
            double estPMMD = ePMMD.getValue().stream().mapToDouble(ConsumoRealPMMD::getEstimado).sum();
            double realPMMD = ePMMD.getValue().stream().mapToDouble(ConsumoRealPMMD::getRealConsumido).sum();
            tePMMD += estPMMD;
            trPMMD += realPMMD;
            tPMMD.fila(ePMMD.getKey().getNombre(), ePMMD.getValue().stream().map(ConsumoRealPMMD::getOpCodigo).distinct().count(),
                    MoneyPMMD.numero(estPMMD), MoneyPMMD.numero(realPMMD), String.format("%+.1f %%", estPMMD == 0 ? 0 : (realPMMD - estPMMD) * 100 / estPMMD),
                    MoneyPMMD.fmt(realPMMD * ePMMD.getKey().getCosto()));
        }
        return tPMMD.conTotales("Total", "", MoneyPMMD.numero(tePMMD), MoneyPMMD.numero(trPMMD), String.format("%+.1f %%", tePMMD == 0 ? 0 : (trPMMD - tePMMD) * 100 / tePMMD), "");
    }

    private Tabla produccionTapiceroPMMD(String rangoPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        Tabla tPMMD = Tabla.de(Tipo.PRODUCCION_TAPICERO.nombre(), rangoPMMD, List.of("Responsable", "Cargo", "OP terminadas", "OP en proceso", "Sofás"),
                new boolean[]{false, false, true, true, true});
        Map<String, int[]> accPMMD = new LinkedHashMap<>();
        Map<String, String> cargosPMMD = new HashMap<>();
        for (OrdenProduccionPMMD oPMMD : opRepositoryPMMD.findAll()) {
            if (oPMMD.getAnulada() || oPMMD.getResponsable() == null) continue;
            String nPMMD = oPMMD.getResponsable().getNombre();
            cargosPMMD.put(nPMMD, oPMMD.getResponsable().getCargo());
            int[] aPMMD = accPMMD.computeIfAbsent(nPMMD, kPMMD -> new int[3]);
            if (oPMMD.getEtapa() == EtapaPMMD.LISTO && en(oPMMD.getFechaFin(), dPMMD, hPMMD)) {
                aPMMD[0]++;
                aPMMD[2] += oPMMD.getCantidad();
            } else if (oPMMD.isActiva()) {
                aPMMD[1]++;
                aPMMD[2] += oPMMD.getCantidad();
            }
        }
        for (ConsumoRealPMMD cPMMD : consumosPMMD(dPMMD, hPMMD)) {
            accPMMD.computeIfAbsent(cPMMD.getResponsable(), kPMMD -> new int[3]);
            cargosPMMD.putIfAbsent(cPMMD.getResponsable(), "Taller");
        }
        Map<String, Set<String>> histPMMD = consumosPMMD(dPMMD, hPMMD).stream().collect(Collectors.groupingBy(ConsumoRealPMMD::getResponsable,
                Collectors.mapping(ConsumoRealPMMD::getOpCodigo, Collectors.toSet())));
        histPMMD.forEach((nPMMD, opsPMMD) -> {
            int[] aPMMD = accPMMD.get(nPMMD);
            long nuevasPMMD = opsPMMD.stream().filter(codePMMD -> opRepositoryPMMD.findByCodigo(codePMMD).isEmpty()).count();
            aPMMD[0] += nuevasPMMD;
            aPMMD[2] += nuevasPMMD;
        });
        accPMMD.forEach((nPMMD, aPMMD) -> tPMMD.fila(nPMMD, cargosPMMD.get(nPMMD), aPMMD[0], aPMMD[1], aPMMD[2]));
        return tPMMD;
    }

    private Tabla costoRealPMMD(String rangoPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        Tabla tPMMD = Tabla.de(Tipo.COSTO_ESTANDAR_REAL.nombre(), rangoPMMD, List.of("OP", "Producto", "Responsable", "Estándar materiales", "Real", "Desvío", "Desvío %"),
                new boolean[]{false, false, false, true, true, true, true});
        Map<String, List<ConsumoRealPMMD>> gPMMD = consumosPMMD(dPMMD, hPMMD).stream().collect(Collectors.groupingBy(ConsumoRealPMMD::getOpCodigo, TreeMap::new, Collectors.toList()));
        double tePMMD = 0, trPMMD = 0;
        for (var ePMMD : gPMMD.entrySet()) {
            double estPMMD = ePMMD.getValue().stream().mapToDouble(cPMMD -> cPMMD.getEstimado() * cPMMD.getMaterial().getCosto()).sum();
            double realPMMD = ePMMD.getValue().stream().mapToDouble(cPMMD -> cPMMD.getRealConsumido() * cPMMD.getMaterial().getCosto()).sum();
            tePMMD += estPMMD;
            trPMMD += realPMMD;
            ConsumoRealPMMD c0PMMD = ePMMD.getValue().get(0);
            tPMMD.fila(ePMMD.getKey(), c0PMMD.getProducto(), c0PMMD.getResponsable(), MoneyPMMD.fmt(estPMMD), MoneyPMMD.fmt(realPMMD), MoneyPMMD.fmt(realPMMD - estPMMD),
                    String.format("%+.1f %%", estPMMD == 0 ? 0 : (realPMMD - estPMMD) * 100 / estPMMD));
        }
        return tPMMD.conTotales("Total", "", "", MoneyPMMD.fmt(tePMMD), MoneyPMMD.fmt(trPMMD), MoneyPMMD.fmt(trPMMD - tePMMD), String.format("%+.1f %%", tePMMD == 0 ? 0 : (trPMMD - tePMMD) * 100 / tePMMD));
    }

    private Tabla mermasPMMD(String rangoPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        Tabla tPMMD = Tabla.de(Tipo.MERMAS.nombre(), rangoPMMD, List.of("Código", "Material", "Estimado", "Real", "Sobremerma", "Costo sobremerma"),
                new boolean[]{false, false, true, true, true, true});
        Map<MaterialPMMD, List<ConsumoRealPMMD>> gPMMD = consumosPMMD(dPMMD, hPMMD).stream()
                .collect(Collectors.groupingBy(ConsumoRealPMMD::getMaterial, LinkedHashMap::new, Collectors.toList()));
        double totalPMMD = 0;
        List<Object[]> filasPMMD = new ArrayList<>();
        for (var ePMMD : gPMMD.entrySet()) {
            double estPMMD = ePMMD.getValue().stream().mapToDouble(ConsumoRealPMMD::getEstimado).sum();
            double realPMMD = ePMMD.getValue().stream().mapToDouble(ConsumoRealPMMD::getRealConsumido).sum();
            double sobrePMMD = Math.max(0, realPMMD - estPMMD);
            totalPMMD += sobrePMMD * ePMMD.getKey().getCosto();
            filasPMMD.add(new Object[]{ePMMD.getKey().getCodigo(), ePMMD.getKey().getNombre(), MoneyPMMD.numero(estPMMD) + " " + ePMMD.getKey().getUnidad(),
                    MoneyPMMD.numero(realPMMD) + " " + ePMMD.getKey().getUnidad(), MoneyPMMD.numero(sobrePMMD), sobrePMMD * ePMMD.getKey().getCosto()});
        }
        filasPMMD.sort((aPMMD, bPMMD) -> Double.compare((double) bPMMD[5], (double) aPMMD[5]));
        for (Object[] fPMMD : filasPMMD) tPMMD.fila(fPMMD[0], fPMMD[1], fPMMD[2], fPMMD[3], fPMMD[4], MoneyPMMD.fmt((double) fPMMD[5]));
        return tPMMD.conTotales("", "Total", "", "", "", MoneyPMMD.fmt(totalPMMD));
    }

    private Tabla pedidosPendientesPMMD(String rangoPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        Tabla tPMMD = Tabla.de(Tipo.PEDIDOS_PENDIENTES.nombre(), rangoPMMD, List.of("Pedido", "Cliente", "Modelo", "Tipo", "Entrega", "Estado", "Precio", "Saldo"),
                new boolean[]{false, false, false, false, false, false, true, true});
        double saldoPMMD = 0;
        for (PedidoPMMD pPMMD : pedidoRepositoryPMMD.findAllByOrderByCodigoDesc()) {
            if (pPMMD.getEstado().cerrado()) continue;
            saldoPMMD += pPMMD.getSaldo().doubleValue();
            tPMMD.fila(pPMMD.getCodigo(), pPMMD.getCliente().getNombre(), pPMMD.getProductoNombre(), pPMMD.getTipo().nombre(), AppClockPMMD.fmt(pPMMD.getFechaEntrega()),
                    pPMMD.getEstado().nombre(), MoneyPMMD.fmt(pPMMD.getPrecio()), MoneyPMMD.fmt(pPMMD.getSaldo()));
        }
        return tPMMD.conTotales("", "", "", "", "", "", "Total", MoneyPMMD.fmt(saldoPMMD));
    }

    private Tabla garantiasPMMD(String rangoPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        Tabla tPMMD = Tabla.de(Tipo.GARANTIAS.nombre(), rangoPMMD, List.of("Reclamo", "Cliente", "Sofá", "Falla", "Cobertura", "SLA (días)", "Estado", "Costo"),
                new boolean[]{false, false, false, false, false, true, false, true});
        double costoPMMD = 0;
        for (ReclamoGarantiaPMMD rPMMD : garantiaServicePMMD.listar()) {
            costoPMMD += rPMMD.getCosto() == null ? 0 : rPMMD.getCosto().doubleValue();
            tPMMD.fila(rPMMD.getCodigo(), rPMMD.getCliente().getNombre(), rPMMD.getProductoNombre(), rPMMD.getFalla(), garantiaServicePMMD.cobertura(rPMMD).texto(),
                    garantiaServicePMMD.sla(rPMMD), rPMMD.getEstado().nombre(), MoneyPMMD.fmt(rPMMD.getCosto()));
        }
        return tPMMD.conTotales("", "", "", "", "", "", "Total", MoneyPMMD.fmt(costoPMMD));
    }

    private Tabla kardexPMMD(String rangoPMMD, LocalDate dPMMD, LocalDate hPMMD) {
        Tabla tPMMD = Tabla.de(Tipo.KARDEX.nombre(), rangoPMMD, List.of("Fecha", "Material", "Tipo", "Cantidad", "Referencia", "Concepto", "Saldo", "Usuario"),
                new boolean[]{false, false, false, true, false, false, true, false});
        movimientoRepositoryPMMD.findAllByOrderByFechaDescIdDesc().stream().filter(mPMMD -> en(mPMMD.getFecha().toLocalDate(), dPMMD, hPMMD))
                .forEach(mPMMD -> tPMMD.fila(mPMMD.getFecha().toLocalDate().format(AppClockPMMD.FECHA) + " " + mPMMD.getFecha().toLocalTime().toString().substring(0, 5),
                        mPMMD.getMaterial().getCodigo() + " " + mPMMD.getMaterial().getNombre(), mPMMD.getTipo().nombre(),
                        MoneyPMMD.numero(mPMMD.getCantidad()) + " " + mPMMD.getMaterial().getUnidad(), mPMMD.getReferencia(), mPMMD.getConcepto(),
                        MoneyPMMD.numero(mPMMD.getSaldo() == null ? 0 : mPMMD.getSaldo()), mPMMD.getUsuario()));
        return tPMMD;
    }
}
