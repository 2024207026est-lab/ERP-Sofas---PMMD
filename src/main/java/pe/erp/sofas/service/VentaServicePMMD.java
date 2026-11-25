package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.MoneyPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.ClienteRepositoryPMMD;
import pe.erp.sofas.repository.ProductoRepositoryPMMD;
import pe.erp.sofas.repository.VentaRepositoryPMMD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VentaServicePMMD {

    public record Linea(
      Long productoId, 
        int cantidad, 
          BigDecimal precioUnitario) {
    }






    public record Cobro(
      Long clienteId, 
        VentaPMMD.Comprobante comprobante, 
          List<Linea> lineas, 
            BigDecimal descuento,
                        
                        String metodo, 
                          BigDecimal recibido, 
                            String metodo2, 
                              BigDecimal monto1, 
                                BigDecimal monto2) {
    }

    private final VentaRepositoryPMMD repositoryPMMD;

    private final ProductoRepositoryPMMD productoRepositoryPMMD;

    private final ClienteRepositoryPMMD clienteRepositoryPMMD;

    private final UsuarioActualPMMD usuarioPMMD;

    public List<VentaPMMD> historial() {
        return repositoryPMMD.findAllByOrderByFechaDesc();
    }

    public List<VentaPMMD> delMes(LocalDate referenciaPMMD) {
        LocalDate inicioPMMD = referenciaPMMD.withDayOfMonth(1);
        LocalDate finPMMD = inicioPMMD.plusMonths(1);
        return historial().stream().filter(vPMMD -> !vPMMD.getFecha().toLocalDate().isBefore(inicioPMMD) && vPMMD.getFecha().toLocalDate().isBefore(finPMMD)).toList();
    }

    public BigDecimal totalMes() {
        return delMes(AppClockPMMD.hoy()).stream().map(VentaPMMD::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int sofasMes() {
        return delMes(AppClockPMMD.hoy()).stream().mapToInt(VentaPMMD::getUnidades).sum();
    }

    public int siguienteNumero(VentaPMMD.Comprobante cPMMD) {
        return repositoryPMMD.findBySerie(cPMMD.serie()).stream().mapToInt(VentaPMMD::getNumero).max().orElse(0) + 1;
    }

    @Transactional
    public VentaPMMD cobrar(Cobro cPMMD) {
        if (cPMMD.lineas() == null || cPMMD.lineas().isEmpty()) throw new NegocioExceptionPMMD("El carrito está vacío.");
        ClientePMMD clientePMMD = cPMMD.clienteId() == null ? null : clienteRepositoryPMMD.findById(cPMMD.clienteId()).orElseThrow();
        if (cPMMD.comprobante() == VentaPMMD.Comprobante.FACTURA && (clientePMMD == null || !clientePMMD.tieneRuc())) {
            throw new NegocioExceptionPMMD("La factura exige un cliente con RUC.");
        }
        VentaPMMD ventaPMMD = VentaPMMD.builder().comprobante(cPMMD.comprobante()).serie(cPMMD.comprobante().serie())
                .numero(siguienteNumero(cPMMD.comprobante())).fecha(LocalDateTime.of(AppClockPMMD.hoy(), AppClockPMMD.HORA))
                .cliente(clientePMMD).vendedor(usuarioPMMD.nombre()).build();
        BigDecimal brutoPMMD = BigDecimal.ZERO;
        for (Linea lPMMD : cPMMD.lineas()) {
            ProductoPMMD pPMMD = productoRepositoryPMMD.findById(lPMMD.productoId()).orElseThrow();
            if (lPMMD.cantidad() <= 0) throw new NegocioExceptionPMMD("Cantidad inválida para " + pPMMD.getNombre() + ".");
            if (pPMMD.getStock() < lPMMD.cantidad()) {
                throw new NegocioExceptionPMMD("Stock insuficiente de " + pPMMD.getNombre() + ": quedan " + pPMMD.getStock() + ".");
            }
            pPMMD.setStock(pPMMD.getStock() - lPMMD.cantidad());
            productoRepositoryPMMD.save(pPMMD);
            BigDecimal precioPMMD = lPMMD.precioUnitario() == null ? pPMMD.getPrecio() : lPMMD.precioUnitario();
            ventaPMMD.agregar(ItemVentaPMMD.builder().producto(pPMMD).cantidad(lPMMD.cantidad()).precioUnitario(precioPMMD).build());
            brutoPMMD = brutoPMMD.add(precioPMMD.multiply(BigDecimal.valueOf(lPMMD.cantidad())));
        }
        BigDecimal descuento = cPMMD.descuento() == null ? BigDecimal.ZERO : cPMMD.descuento().setScale(2, RoundingMode.HALF_UP);
        if (descuento.signum() < 0 || descuento.compareTo(brutoPMMD) >= 0) throw new NegocioExceptionPMMD("El descuento no es válido.");
        BigDecimal totalPMMD = brutoPMMD.subtract(descuento);
        ventaPMMD.setDescuento(descuento);
        ventaPMMD.setTotal(totalPMMD);
        String metodo = cPMMD.metodo() == null ? "Efectivo" : cPMMD.metodo();
        if ("Mixto".equals(metodo)) {
            BigDecimal aPMMD = cPMMD.monto1() == null ? BigDecimal.ZERO : cPMMD.monto1();
            BigDecimal bPMMD = cPMMD.monto2() == null ? BigDecimal.ZERO : cPMMD.monto2();
            if (aPMMD.add(bPMMD).compareTo(totalPMMD) != 0) {
                throw new NegocioExceptionPMMD("En pago mixto los montos deben sumar " + MoneyPMMD.fmt(totalPMMD) + ".");
            }
            metodo = "Mixto (Efectivo " + MoneyPMMD.fmt(aPMMD) + " + " + (cPMMD.metodo2() == null ? "Yape" : cPMMD.metodo2()) + " " + MoneyPMMD.fmt(bPMMD) + ")";
        }
        if ("Efectivo".equals(metodo)) {
            BigDecimal recibido = cPMMD.recibido() == null ? totalPMMD : cPMMD.recibido();
            if (recibido.compareTo(totalPMMD) < 0) throw new NegocioExceptionPMMD("El monto recibido no cubre el total.");
            ventaPMMD.setRecibido(recibido);
            ventaPMMD.setVuelto(recibido.subtract(totalPMMD));
        }
        ventaPMMD.setMetodoPago(metodo);
        return repositoryPMMD.save(ventaPMMD);
    }
}
