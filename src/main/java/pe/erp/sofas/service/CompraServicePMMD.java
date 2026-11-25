package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompraServicePMMD {

    public record LineaCompra(
      Long materialId, 
        Long telaId, 
          double cantidad, 
            BigDecimal precioUnitario) {
    }

    private final OrdenCompraRepositoryPMMD repositoryPMMD;

    private final ProveedorRepositoryPMMD proveedorRepositoryPMMD;

    private final MaterialRepositoryPMMD materialRepositoryPMMD;

    private final TelaRepositoryPMMD telaRepositoryPMMD;

    private final InventarioServicePMMD inventarioServicePMMD;

    private final TelaServicePMMD telaServicePMMD;

    public List<OrdenCompraPMMD> listar() {
        return repositoryPMMD.findAllByOrderByCodigoDesc();
    }

    public OrdenCompraPMMD orden(Long idPMMD) {
        return repositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("La orden de compra ya no existe."));
    }

    public String siguienteCodigo() {
        return CodigosPMMD.siguiente(repositoryPMMD.findAll(), OrdenCompraPMMD::getCodigo, "OC-", 4);
    }



    public List<OrdenCompraPMMD> porPagar() {
        return listar().stream()
                .filter(oPMMD -> (oPMMD.getEstado() == EstadoCompraPMMD.APROBADA || oPMMD.getEstado() == EstadoCompraPMMD.RECIBIDA) && oPMMD.getFechaPago() == null)
                .toList();
    }

    @Transactional
    public OrdenCompraPMMD crear(Long proveedorIdPMMD, List<LineaCompra> lineasPMMD, boolean enviarSolicitudPMMD, String observacionesPMMD,
                             String origenPMMD, boolean nuevaPMMD) {
        if (proveedorIdPMMD == null) throw new NegocioExceptionPMMD("Selecciona un proveedor.");
        ProveedorPMMD proveedorPMMD = proveedorRepositoryPMMD.findById(proveedorIdPMMD).orElseThrow();
        if (!proveedorPMMD.getActivo()) throw new NegocioExceptionPMMD("El proveedor está inactivo.");
        List<LineaCompra> validasPMMD = lineasPMMD.stream().filter(lPMMD -> lPMMD.materialId() != null && lPMMD.cantidad() > 0).toList();
        if (validasPMMD.isEmpty()) throw new NegocioExceptionPMMD("Agrega al menos un material con cantidad.");
        OrdenCompraPMMD orden = OrdenCompraPMMD.builder()
                .codigo(siguienteCodigo()).proveedor(proveedorPMMD).fecha(AppClockPMMD.hoy()).fechaEntrega(AppClockPMMD.hoy().plusDays(5))
                .estado(enviarSolicitudPMMD ? EstadoCompraPMMD.SOLICITADA : EstadoCompraPMMD.BORRADOR)
                .origen(origenPMMD == null ? "Manual" : origenPMMD).nueva(nuevaPMMD)
                .observaciones(observacionesPMMD).build();
        for (LineaCompra lPMMD : validasPMMD) {
            if (lPMMD.precioUnitario() == null || lPMMD.precioUnitario().signum() < 0) throw new NegocioExceptionPMMD("Revisa los precios unitarios.");
            MaterialPMMD mPMMD = materialRepositoryPMMD.findById(lPMMD.materialId()).orElseThrow();
            TelaPMMD telaPMMD = lPMMD.telaId() == null ? null : telaRepositoryPMMD.findById(lPMMD.telaId()).orElse(null);
            if (telaPMMD == null && mPMMD.isTela()) telaPMMD = colorParaReponerPMMD(mPMMD);
            orden.agregar(ItemCompraPMMD.builder().material(mPMMD).tela(telaPMMD).cantidad(lPMMD.cantidad()).precioUnitario(lPMMD.precioUnitario()).build());
        }
        return repositoryPMMD.save(orden);
    }



    private TelaPMMD colorParaReponerPMMD(MaterialPMMD mPMMD) {
        TipoTelaPMMD tipoPMMD = TipoTelaPMMD.porMaterial(mPMMD.getCodigo());
        return telaServicePMMD.porTipo(tipoPMMD).stream().filter(tPMMD -> !tPMMD.getDescontinuada())
                .min(Comparator.comparingDouble(tPMMD -> tPMMD.getStock() / Math.max(1, tPMMD.getMinimo()))).orElse(null);
    }

    @Transactional
    public OrdenCompraPMMD solicitar(Long idPMMD) {
        return transicionPMMD(idPMMD, EstadoCompraPMMD.BORRADOR, EstadoCompraPMMD.SOLICITADA);
    }

    @Transactional
    public OrdenCompraPMMD aprobar(Long idPMMD) {
        return transicionPMMD(idPMMD, EstadoCompraPMMD.SOLICITADA, EstadoCompraPMMD.APROBADA);
    }

    @Transactional
    public void marcarVista(Long idPMMD) {
        OrdenCompraPMMD oPMMD = orden(idPMMD);
        if (Boolean.TRUE.equals(oPMMD.getNueva())) {
            oPMMD.setNueva(false);
            repositoryPMMD.save(oPMMD);
        }
    }



    @Transactional
    public OrdenCompraPMMD recibir(Long idPMMD) {
        OrdenCompraPMMD orden = orden(idPMMD);
        if (orden.getEstado() != EstadoCompraPMMD.APROBADA) throw new NegocioExceptionPMMD("Solo se pueden recibir órdenes aprobadas.");
        for (ItemCompraPMMD itemPMMD : orden.getItems()) {
            MaterialPMMD mPMMD = materialRepositoryPMMD.findById(itemPMMD.getMaterial().getId()).orElseThrow();
            if (mPMMD.isTela() && itemPMMD.getTela() != null) {
                telaServicePMMD.entradaRollo(itemPMMD.getTela().getId(), itemPMMD.getCantidad(), orden.getCodigo());
            } else {
                inventarioServicePMMD.registrar(mPMMD, TipoMovimientoPMMD.ENTRADA, itemPMMD.getCantidad(), orden.getCodigo(),
                        "Recepción " + orden.getProveedor().getRazonSocial());
            }
        }
        orden.setEstado(EstadoCompraPMMD.RECIBIDA);
        orden.setFechaRecepcion(AppClockPMMD.hoy());
        orden.setNueva(false);
        ProveedorPMMD pPMMD = orden.getProveedor();
        pPMMD.setTotalComprado(pPMMD.getTotalComprado().add(orden.getTotal()));
        proveedorRepositoryPMMD.save(pPMMD);
        return repositoryPMMD.save(orden);
    }

    @Transactional
    public OrdenCompraPMMD cancelar(Long idPMMD) {
        OrdenCompraPMMD orden = orden(idPMMD);
        if (orden.getEstado() == EstadoCompraPMMD.RECIBIDA) throw new NegocioExceptionPMMD("No se puede cancelar una orden ya recibida.");
        orden.setEstado(EstadoCompraPMMD.CANCELADA);
        orden.setNueva(false);
        return repositoryPMMD.save(orden);
    }

    @Transactional
    public OrdenCompraPMMD registrarPago(Long idPMMD) {
        OrdenCompraPMMD orden = orden(idPMMD);
        orden.setFechaPago(AppClockPMMD.hoy());
        return repositoryPMMD.save(orden);
    }

    private OrdenCompraPMMD transicionPMMD(Long idPMMD, EstadoCompraPMMD desdePMMD, EstadoCompraPMMD haciaPMMD) {
        OrdenCompraPMMD orden = orden(idPMMD);
        if (orden.getEstado() != desdePMMD) throw new NegocioExceptionPMMD("La orden está en estado " + orden.getEstado() + ".");
        orden.setEstado(haciaPMMD);
        orden.setNueva(false);
        return repositoryPMMD.save(orden);
    }
}
