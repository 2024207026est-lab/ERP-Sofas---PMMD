package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteServicePMMD {

    public record Resumen(
      BigDecimal totalComprado, 
        BigDecimal saldo, 
          long compras, 
            long pedidos) {
    }

    private final ClienteRepositoryPMMD repositoryPMMD;

    private final VentaRepositoryPMMD ventaRepositoryPMMD;

    private final PedidoRepositoryPMMD pedidoRepositoryPMMD;

    private final PagoRepositoryPMMD pagoRepositoryPMMD;

    private final RetapizadoRepositoryPMMD retapizadoRepositoryPMMD;

    public List<ClientePMMD> listar() {
        return repositoryPMMD.findAllByOrderByCodigoAsc();
    }

    public ClientePMMD cliente(Long idPMMD) {
        return repositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("El cliente ya no existe."));
    }

    public List<VentaPMMD> ventas(ClientePMMD cPMMD) {
        return ventaRepositoryPMMD.findAllByOrderByFechaDesc().stream().filter(vPMMD -> vPMMD.getCliente() != null && vPMMD.getCliente().getId().equals(cPMMD.getId())).toList();
    }

    public List<PedidoPMMD> pedidos(ClientePMMD cPMMD) {
        return pedidoRepositoryPMMD.findAllByOrderByCodigoDesc().stream().filter(pPMMD -> pPMMD.getCliente().getId().equals(cPMMD.getId())).toList();
    }

    public List<PagoPMMD> pagos(ClientePMMD cPMMD) {
        return pagoRepositoryPMMD.findByClienteIdOrderByFechaDesc(cPMMD.getId());
    }



    public Resumen resumen(ClientePMMD cPMMD) {
        List<VentaPMMD> vPMMD = ventas(cPMMD);
        List<PedidoPMMD> pPMMD = pedidos(cPMMD);
        BigDecimal ventas = vPMMD.stream().map(VentaPMMD::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pedidos = pPMMD.stream().filter(xPMMD -> xPMMD.getEstado() != EstadoPedidoPMMD.CANCELADO).map(PedidoPMMD::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retapPMMD = retapizadoRepositoryPMMD.findAll().stream().filter(rPMMD -> rPMMD.getCliente().getId().equals(cPMMD.getId())
                && rPMMD.getEstado() == EstadoRetapizadoPMMD.ENTREGADO && rPMMD.getTotal() != null).map(RetapizadoPMMD::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = pPMMD.stream().filter(xPMMD -> !xPMMD.getEstado().cerrado()).map(PedidoPMMD::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Resumen(ventas.add(pedidos).add(retapPMMD), saldo, vPMMD.size(), pPMMD.size());
    }

    public String siguienteCodigo() {
        return CodigosPMMD.siguiente(repositoryPMMD.findAll(), ClientePMMD::getCodigo, "C-", 4);
    }

    @Transactional
    public ClientePMMD guardar(ClientePMMD cPMMD) {
        validar(cPMMD);
        boolean duplicadoPMMD = listar().stream().anyMatch(xPMMD -> xPMMD.getDocumento() != null && xPMMD.getDocumento().equals(cPMMD.getDocumento())
                && !xPMMD.getId().equals(cPMMD.getId()));
        if (duplicadoPMMD) throw new NegocioExceptionPMMD("Ya existe un cliente con el documento " + cPMMD.getDocumento() + ".");
        if (cPMMD.getId() == null) {
            cPMMD.setCodigo(siguienteCodigo());
            cPMMD.setFechaRegistro(AppClockPMMD.hoy());
        }
        return repositoryPMMD.save(cPMMD);
    }



    public static void validar(ClientePMMD cPMMD) {
        if (cPMMD.getNombre() == null || cPMMD.getNombre().isBlank()) throw new NegocioExceptionPMMD("El nombre es obligatorio.");
        String docPMMD = cPMMD.getDocumento() == null ? "" : cPMMD.getDocumento().trim();
        if (cPMMD.getTipo() == ClientePMMD.Tipo.PERSONA && !docPMMD.matches("\\d{8}")) throw new NegocioExceptionPMMD("El DNI debe tener 8 dígitos.");
        if (cPMMD.getTipo() == ClientePMMD.Tipo.EMPRESA && !docPMMD.matches("\\d{11}")) throw new NegocioExceptionPMMD("El RUC debe tener 11 dígitos.");
        if (cPMMD.getTelefono() != null && !cPMMD.getTelefono().isBlank() && !cPMMD.getTelefono().replace(" ", "").matches("\\+?\\d{6,12}")) {
            throw new NegocioExceptionPMMD("Revisa el teléfono.");
        }
    }

    @Transactional
    public void eliminar(Long idPMMD) {
        ClientePMMD cPMMD = cliente(idPMMD);
        if (!ventas(cPMMD).isEmpty() || !pedidos(cPMMD).isEmpty()) throw new NegocioExceptionPMMD("El cliente tiene ventas o pedidos; no se puede eliminar.");
        repositoryPMMD.delete(cPMMD);
    }
}
