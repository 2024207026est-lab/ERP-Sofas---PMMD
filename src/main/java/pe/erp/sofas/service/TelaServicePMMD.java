package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;

import java.util.*;






@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TelaServicePMMD {

    private final TelaRepositoryPMMD telaRepositoryPMMD;

    private final MaterialRepositoryPMMD materialRepositoryPMMD;

    private final MovimientoRepositoryPMMD movimientoRepositoryPMMD;

    private final ProductoRepositoryPMMD productoRepositoryPMMD;

    private final PedidoRepositoryPMMD pedidoRepositoryPMMD;

    private final OrdenProduccionRepositoryPMMD opRepositoryPMMD;

    private final InventarioServicePMMD inventarioServicePMMD;



    public record Usos(
      List<ProductoPMMD> productos, 
        List<PedidoPMMD> pedidos, 
          List<OrdenProduccionPMMD> ordenes) {

        public int total() {
            return productos.size() + pedidos.size() + ordenes.size();
        }
    }

    public List<TelaPMMD> telas() {
        return telaRepositoryPMMD.findAllByOrderByCodigoAsc();
    }

    public List<TelaPMMD> activas() {
        return telas().stream().filter(tPMMD -> !tPMMD.getDescontinuada()).toList();
    }

    public List<TelaPMMD> porTipo(TipoTelaPMMD tipoPMMD) {
        return telas().stream().filter(tPMMD -> tPMMD.getTipo() == tipoPMMD).toList();
    }

    public TelaPMMD tela(Long idPMMD) {
        return telaRepositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("La tela ya no existe."));
    }

    public Optional<TelaPMMD> porCodigo(String codigoPMMD) {
        return telaRepositoryPMMD.findByCodigo(codigoPMMD);
    }

    public double metrosEnStock() {
        return activas().stream().mapToDouble(TelaPMMD::getStock).sum();
    }

    public double valorMuestrario() {
        return activas().stream().mapToDouble(tPMMD -> tPMMD.getStock() * tPMMD.getTipo().costoMetro()).sum();
    }

    public long conAlerta() {
        return activas().stream().filter(tPMMD -> !"Disponible".equals(tPMMD.getEstado())).count();
    }

    public String siguienteCodigo() {
        return CodigosPMMD.siguiente(telaRepositoryPMMD.findAll(), TelaPMMD::getCodigo, "TL-", 2);
    }



    @Transactional
    public void sincronizar(TipoTelaPMMD tipoPMMD) {
        materialRepositoryPMMD.findByCodigo(tipoPMMD.materialCodigo()).ifPresent(mPMMD -> {
            double sumaPMMD = telaRepositoryPMMD.findByTipo(tipoPMMD).stream().filter(tPMMD -> !tPMMD.getDescontinuada()).mapToDouble(TelaPMMD::getStock).sum();
            mPMMD.setStock(sumaPMMD);
            materialRepositoryPMMD.save(mPMMD);
        });
    }



    @Transactional
    public TelaPMMD entradaRollo(Long telaIdPMMD, double metrosPMMD, String referenciaPMMD) {
        if (metrosPMMD <= 0) throw new NegocioExceptionPMMD("Ingresa los metros recibidos.");
        TelaPMMD tPMMD = tela(telaIdPMMD);
        if (tPMMD.getDescontinuada()) throw new NegocioExceptionPMMD("El color está descontinuado. Reactívalo para recibir rollos.");
        tPMMD.setStock(tPMMD.getStock() + metrosPMMD);
        telaRepositoryPMMD.save(tPMMD);
        MaterialPMMD mPMMD = inventarioServicePMMD.material(tPMMD.getTipo().materialCodigo());
        inventarioServicePMMD.registrar(mPMMD, TipoMovimientoPMMD.ENTRADA, metrosPMMD, referenciaPMMD == null || referenciaPMMD.isBlank() ? "ROLLO" : referenciaPMMD,
                "Rollo " + tPMMD.getColor() + " " + tPMMD.getCodigo());
        return tPMMD;
    }

    @Transactional
    public TelaPMMD registrar(TipoTelaPMMD tipoPMMD, String colorPMMD, String hexPMMD, String codigoProveedorPMMD, double stockPMMD, double minimoPMMD) {
        if (colorPMMD == null || colorPMMD.isBlank()) throw new NegocioExceptionPMMD("Escribe el nombre del color.");
        if (stockPMMD < 0 || minimoPMMD < 0) throw new NegocioExceptionPMMD("Los metros no pueden ser negativos.");
        boolean existePMMD = porTipo(tipoPMMD).stream().anyMatch(tPMMD -> tPMMD.getColor().equalsIgnoreCase(colorPMMD.trim()));
        if (existePMMD) throw new NegocioExceptionPMMD("Ya existe el color " + colorPMMD.trim() + " en " + tipoPMMD.nombre() + ".");
        TelaPMMD tPMMD = telaRepositoryPMMD.save(TelaPMMD.builder().codigo(siguienteCodigo()).tipo(tipoPMMD).color(colorPMMD.trim())
                .hex(hexPMMD).codigoProveedor(codigoProveedorPMMD).stock(0d).minimo(minimoPMMD).build());
        if (stockPMMD > 0) entradaRollo(tPMMD.getId(), stockPMMD, "INI-" + tPMMD.getCodigo());
        return tPMMD;
    }

    @Transactional
    public TelaPMMD alternarDescontinuada(Long telaIdPMMD) {
        TelaPMMD tPMMD = tela(telaIdPMMD);
        MaterialPMMD mPMMD = inventarioServicePMMD.material(tPMMD.getTipo().materialCodigo());
        boolean descontinuarPMMD = !tPMMD.getDescontinuada();
        if (descontinuarPMMD && tPMMD.getStock() > 0 && mPMMD.getStock() - tPMMD.getStock() < mPMMD.getReservado() - 1e-9) {
            throw new NegocioExceptionPMMD("No se puede descontinuar: sus metros están reservados para producción.");
        }
        tPMMD.setDescontinuada(descontinuarPMMD);
        telaRepositoryPMMD.save(tPMMD);
        if (tPMMD.getStock() > 0) {

            movimientoRepositoryPMMD.save(MovimientoPMMD.builder().material(mPMMD)
                    .fecha(java.time.LocalDateTime.of(pe.erp.sofas.core.AppClockPMMD.hoy(), pe.erp.sofas.core.AppClockPMMD.HORA))
                    .tipo(TipoMovimientoPMMD.AJUSTE).cantidad(descontinuarPMMD ? -tPMMD.getStock() : tPMMD.getStock())
                    .referencia(tPMMD.getCodigo()).concepto((descontinuarPMMD ? "Descontinuado " : "Reactivado ") + tPMMD.getColor())
                    .saldo(mPMMD.getStock() + (descontinuarPMMD ? -tPMMD.getStock() : tPMMD.getStock())).usuario("sistema").build());
        }
        sincronizar(tPMMD.getTipo());
        return tPMMD;
    }



    @Transactional
    public void descontar(TelaPMMD tPMMD, double metrosPMMD) {
        TelaPMMD frescaPMMD = tela(tPMMD.getId());
        frescaPMMD.setStock(Math.max(0, frescaPMMD.getStock() - metrosPMMD));
        telaRepositoryPMMD.save(frescaPMMD);
    }

    public Usos usos(TelaPMMD tPMMD) {
        String nombrePMMD = tPMMD.getColor().toLowerCase(Locale.ROOT);
        List<ProductoPMMD> productos = productoRepositoryPMMD.findAllByOrderByCodigoAsc().stream()
                .filter(pPMMD -> pPMMD.getColor() != null && pPMMD.getColor().toLowerCase(Locale.ROOT).equals(nombrePMMD)
                        && pPMMD.getMaterial() != null && tPMMD.getTipo().nombre().toLowerCase(Locale.ROOT).startsWith(pPMMD.getMaterial().toLowerCase(Locale.ROOT).split(" ")[0]))
                .toList();
        List<PedidoPMMD> pedidos = pedidoRepositoryPMMD.findAllByOrderByCodigoDesc().stream()
                .filter(pPMMD -> pPMMD.getTela() != null && pPMMD.getTela().getId().equals(tPMMD.getId())).toList();
        List<OrdenProduccionPMMD> ops = opRepositoryPMMD.findAllByOrderByCodigoAsc().stream()
                .filter(oPMMD -> oPMMD.getTela() != null && oPMMD.getTela().getId().equals(tPMMD.getId()) && !oPMMD.getAnulada()).toList();
        return new Usos(productos, pedidos, ops);
    }



    public String notaDisponibilidad(TelaPMMD tPMMD, double metrosNecesariosPMMD) {
        if (tPMMD.getDescontinuada()) return "Color descontinuado";
        if (tPMMD.getStock() <= 0) return "Agotado · reposición en 7 días";
        if (tPMMD.getStock() < metrosNecesariosPMMD) return "Solo " + InventarioServicePMMD.fmt(tPMMD.getStock()) + " m · faltan " + InventarioServicePMMD.fmt(metrosNecesariosPMMD - tPMMD.getStock()) + " m";
        return InventarioServicePMMD.fmt(tPMMD.getStock()) + " m disponibles";
    }
}
