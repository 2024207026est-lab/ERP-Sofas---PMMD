package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.MoneyPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;
import pe.erp.sofas.service.calculo.FormulaPMMD;
import pe.erp.sofas.service.calculo.MedidasPMMD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;






@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoServicePMMD {

    public static final BigDecimal MIN_ADELANTO = new BigDecimal("0.50");

    public record NuevoPedido(
      Long clienteId, 
        Long productoId, 
          PedidoPMMD.Tipo tipo, 
            int cantidad, 
              Integer largo,
                              
                              Integer fondo, 
                                Integer alto, 
                                  Long telaId, 
                                    LocalDate fechaEntrega, 
                                      BigDecimal precio,
                              
                              BigDecimal adelanto, 
                                String metodo, 
                                  String observaciones, 
                                    String configuracion,
                              
                              String especificacion) {
    }

    private final PedidoRepositoryPMMD repositoryPMMD;

    private final ClienteRepositoryPMMD clienteRepositoryPMMD;

    private final ProductoRepositoryPMMD productoRepositoryPMMD;

    private final TelaRepositoryPMMD telaRepositoryPMMD;

    private final PagoRepositoryPMMD pagoRepositoryPMMD;

    private final FichaServicePMMD fichaServicePMMD;

    private final ProduccionServicePMMD produccionServicePMMD;

    private final ObjectProvider<ConfiguradorServicePMMD> configuradorPMMD;

    public List<PedidoPMMD> listar() {
        return repositoryPMMD.findAllByOrderByCodigoDesc();
    }

    public PedidoPMMD pedido(Long idPMMD) {
        return repositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("El pedido ya no existe."));
    }

    public Optional<PedidoPMMD> porCodigo(String codigoPMMD) {
        return repositoryPMMD.findByCodigo(codigoPMMD);
    }

    public String siguienteCodigo() {
        return CodigosPMMD.siguiente(repositoryPMMD.findAll(), PedidoPMMD::getCodigo, "PE-", 4);
    }



    public long pendientesAMedida() {
        return listar().stream().filter(pPMMD -> pPMMD.getTipo() == PedidoPMMD.Tipo.PERSONALIZADO && !pPMMD.getEstado().cerrado()).count();
    }

    public long abiertos() {
        return listar().stream().filter(pPMMD -> !pPMMD.getEstado().cerrado()).count();
    }

    public List<PagoPMMD> pagos(PedidoPMMD pPMMD) {
        return pagoRepositoryPMMD.findAllByOrderByFechaDesc().stream().filter(xPMMD -> pPMMD.getCodigo().equals(xPMMD.getReferencia())).toList();
    }



    public BigDecimal precioSugerido(ProductoPMMD pPMMD, Integer largo, TelaPMMD telaPMMD, int cantidad) {
        if (pPMMD == null) return BigDecimal.ZERO;
        double basePMMD = pPMMD.getPrecio().doubleValue();
        if (largo != null && largo > 0 && pPMMD.getLargo() != null && pPMMD.getLargo() > 0 && !largo.equals(pPMMD.getLargo())) {
            basePMMD = basePMMD * Math.pow(largo / (double) pPMMD.getLargo(), 0.85);
        }
        if (telaPMMD != null) basePMMD = basePMMD * (1 + telaPMMD.getTipo().recargoPct() / 100.0);
        return BigDecimal.valueOf(FormulaPMMD.redondearDecena(basePMMD) * Math.max(1, cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public PedidoPMMD crear(NuevoPedido dPMMD) {
        if (dPMMD.clienteId() == null) throw new NegocioExceptionPMMD("Elige el cliente.");
        if (dPMMD.productoId() == null) throw new NegocioExceptionPMMD("Elige el modelo de sofá.");
        if (dPMMD.cantidad() <= 0) throw new NegocioExceptionPMMD("La cantidad debe ser mayor a cero.");
        if (dPMMD.precio() == null || dPMMD.precio().signum() <= 0) throw new NegocioExceptionPMMD("Ingresa el precio del pedido.");
        if (dPMMD.fechaEntrega() != null && dPMMD.fechaEntrega().isBefore(AppClockPMMD.hoy())) throw new NegocioExceptionPMMD("La entrega no puede ser en el pasado.");
        BigDecimal adelanto = dPMMD.adelanto() == null ? BigDecimal.ZERO : dPMMD.adelanto();
        if (adelanto.signum() < 0 || adelanto.compareTo(dPMMD.precio()) > 0) throw new NegocioExceptionPMMD("El adelanto no puede superar el precio.");
        ClientePMMD cPMMD = clienteRepositoryPMMD.findById(dPMMD.clienteId()).orElseThrow();
        PedidoPMMD pPMMD = PedidoPMMD.builder().codigo(siguienteCodigo()).cliente(cPMMD)
                .producto(productoRepositoryPMMD.findById(dPMMD.productoId()).orElseThrow())
                .tipo(dPMMD.tipo() == null ? PedidoPMMD.Tipo.ESTANDAR : dPMMD.tipo()).cantidad(dPMMD.cantidad())
                .fecha(AppClockPMMD.hoy()).fechaEntrega(dPMMD.fechaEntrega()).precio(dPMMD.precio().setScale(2, RoundingMode.HALF_UP))
                .adelanto(BigDecimal.ZERO).estado(EstadoPedidoPMMD.REGISTRADO)
                .largo(dPMMD.largo()).fondo(dPMMD.fondo()).alto(dPMMD.alto())
                .tela(dPMMD.telaId() == null ? null : telaRepositoryPMMD.findById(dPMMD.telaId()).orElse(null))
                .observaciones(dPMMD.observaciones()).configuracion(dPMMD.configuracion()).especificacion(dPMMD.especificacion())
                .build();
        if (pPMMD.getTela() != null) pPMMD.setMaterial(pPMMD.getTela().getTipo().nombre());
        pPMMD = repositoryPMMD.save(pPMMD);
        if (adelanto.signum() > 0) {
            pPMMD = registrarAdelanto(pPMMD.getId(), adelanto, dPMMD.metodo() == null ? "Efectivo" : dPMMD.metodo());
        }
        return pPMMD;
    }



    @Transactional
    public PedidoPMMD registrarAdelanto(Long idPMMD, BigDecimal montoPMMD, String metodo) {
        PedidoPMMD pPMMD = pedido(idPMMD);
        if (pPMMD.getEstado() == EstadoPedidoPMMD.CANCELADO) throw new NegocioExceptionPMMD("El pedido está cancelado.");
        if (montoPMMD == null || montoPMMD.signum() <= 0) throw new NegocioExceptionPMMD("Ingresa un monto válido.");
        if (montoPMMD.compareTo(pPMMD.getSaldo()) > 0) throw new NegocioExceptionPMMD("El monto supera el saldo de " + MoneyPMMD.fmt(pPMMD.getSaldo()) + ".");
        pPMMD.setAdelanto(pPMMD.getAdelanto().add(montoPMMD));
        pagoRepositoryPMMD.save(PagoPMMD.builder().cliente(pPMMD.getCliente()).referencia(pPMMD.getCodigo()).fecha(AppClockPMMD.hoy())
                .monto(montoPMMD).metodo(metodo).concepto(pPMMD.getAdelanto().compareTo(pPMMD.getPrecio()) >= 0 ? "Cancelación" : "Adelanto").build());
        if ((pPMMD.getEstado() == EstadoPedidoPMMD.COTIZADO || pPMMD.getEstado() == EstadoPedidoPMMD.REGISTRADO) && cubreMinimo(pPMMD)) {
            pPMMD.setEstado(EstadoPedidoPMMD.CONFIRMADO);
        }
        return repositoryPMMD.save(pPMMD);
    }

    public static boolean cubreMinimo(PedidoPMMD pPMMD) {
        return pPMMD.getAdelanto().compareTo(pPMMD.getPrecio().multiply(MIN_ADELANTO)) >= 0;
    }



    @Transactional
    public PedidoPMMD avanzar(Long idPMMD) {
        PedidoPMMD pPMMD = pedido(idPMMD);
        return avanzarA(idPMMD, pPMMD.getEstado().siguiente());
    }

    @Transactional
    public PedidoPMMD avanzarA(Long idPMMD, EstadoPedidoPMMD destinoPMMD) {
        PedidoPMMD pPMMD = pedido(idPMMD);
        if (pPMMD.getEstado().cerrado()) throw new NegocioExceptionPMMD("El pedido ya está " + pPMMD.getEstado().nombre().toLowerCase() + ".");
        switch (destinoPMMD) {
            case CONFIRMADO -> {
                if (!cubreMinimo(pPMMD)) {
                    throw new NegocioExceptionPMMD("Para confirmar se necesita un adelanto mínimo del 50 % ("
                            + MoneyPMMD.fmt(pPMMD.getPrecio().multiply(MIN_ADELANTO)) + ").");
                }
            }
            case EN_PRODUCCION -> {
                if (pPMMD.getOpCodigo() == null) throw new NegocioExceptionPMMD("Genera la orden de producción para pasar a producción.");
            }
            case LISTO -> {
                if (pPMMD.getOpCodigo() != null) {
                    var opPMMD = produccionServicePMMD.porCodigo(pPMMD.getOpCodigo()).orElse(null);
                    if (opPMMD != null && opPMMD.getEtapa() != EtapaPMMD.LISTO) {
                        throw new NegocioExceptionPMMD("La orden " + opPMMD.getCodigo() + " aún está en " + opPMMD.getEtapa().nombre() + ".");
                    }
                }
            }
            case ENTREGADO -> {
                if (pPMMD.getSaldo().signum() > 0) throw new NegocioExceptionPMMD("Registra el saldo de " + MoneyPMMD.fmt(pPMMD.getSaldo()) + " antes de entregar.");
            }
            default -> {
            }
        }
        pPMMD.setEstado(destinoPMMD);
        return repositoryPMMD.save(pPMMD);
    }

    @Transactional
    public PedidoPMMD vincularOP(Long idPMMD, String opCodigoPMMD) {
        PedidoPMMD pPMMD = pedido(idPMMD);
        pPMMD.setOpCodigo(opCodigoPMMD);
        if (pPMMD.getEstado() == EstadoPedidoPMMD.CONFIRMADO) pPMMD.setEstado(EstadoPedidoPMMD.EN_PRODUCCION);
        return repositoryPMMD.save(pPMMD);
    }



    @Transactional
    public OrdenProduccionPMMD generarOP(Long idPMMD, Long responsableId) {
        PedidoPMMD pPMMD = pedido(idPMMD);
        if (pPMMD.getOpCodigo() != null) throw new NegocioExceptionPMMD("El pedido ya tiene la orden " + pPMMD.getOpCodigo() + ".");
        if (pPMMD.getEstado().cerrado()) throw new NegocioExceptionPMMD("El pedido está cerrado.");
        if (!cubreMinimo(pPMMD)) throw new NegocioExceptionPMMD("Registra un adelanto mínimo del 50 % antes de producir.");
        if (pPMMD.getEstado() == EstadoPedidoPMMD.REGISTRADO || pPMMD.getEstado() == EstadoPedidoPMMD.COTIZADO) pPMMD.setEstado(EstadoPedidoPMMD.CONFIRMADO);
        repositoryPMMD.save(pPMMD);
        OrdenProduccionPMMD opPMMD;
        ConfigSofaPMMD cfgPMMD = ConfigSofaPMMD.leer(pPMMD.getConfiguracion());
        if (cfgPMMD != null) {
            ConfiguradorServicePMMD confPMMD = configuradorPMMD.getObject();
            opPMMD = produccionServicePMMD.crear(confPMMD.nuevaOP(cfgPMMD, confPMMD.cotizar(cfgPMMD), pPMMD.getCliente().getId(), responsableId,
                    pPMMD.getFechaEntrega(), pPMMD.getCodigo()));
        } else {
            FichaTecnicaPMMD fPMMD = fichaServicePMMD.aprobadaDe(pPMMD.getProducto());
            MedidasPMMD medidas = null;
            if (fPMMD != null && pPMMD.getLargo() != null && pPMMD.getLargo() > 0 && !Boolean.TRUE.equals(fPMMD.getCompuesta())
                    && !pPMMD.getLargo().equals(fPMMD.getLargo())) {
                medidas = new MedidasPMMD(pPMMD.getLargo(), pPMMD.getFondo() == null ? fPMMD.getFondo() : pPMMD.getFondo(),
                        pPMMD.getAlto() == null ? fPMMD.getAlto() : pPMMD.getAlto(), fPMMD.getCuerpos());
            }
            opPMMD = produccionServicePMMD.crear(new ProduccionServicePMMD.NuevaOP(pPMMD.getProducto().getId(), null, pPMMD.getCantidad(),
                    pPMMD.getCliente().getId(), responsableId, PrioridadPMMD.MEDIA, AppClockPMMD.hoy(), pPMMD.getFechaEntrega(),
                    pPMMD.getTela() == null ? null : pPMMD.getTela().getId(), medidas, Map.of(), List.of(), pPMMD.getCodigo(), null,
                    pPMMD.getEspecificacion(), EtapaPMMD.PENDIENTE, null, null));
        }
        vincularOP(idPMMD, opPMMD.getCodigo());
        return opPMMD;
    }

    @Transactional
    public PedidoPMMD cancelar(Long idPMMD) {
        PedidoPMMD pPMMD = pedido(idPMMD);
        if (pPMMD.getEstado() == EstadoPedidoPMMD.ENTREGADO) throw new NegocioExceptionPMMD("No se puede cancelar un pedido entregado.");
        if (pPMMD.getOpCodigo() != null) {
            produccionServicePMMD.porCodigo(pPMMD.getOpCodigo()).filter(OrdenProduccionPMMD::isActiva)
                    .ifPresent(opPMMD -> produccionServicePMMD.anular(opPMMD.getId()));
            pPMMD = pedido(idPMMD);
        }
        pPMMD.setEstado(EstadoPedidoPMMD.CANCELADO);
        return repositoryPMMD.save(pPMMD);
    }



    public String mensajeWhatsapp(PedidoPMMD pPMMD) {
        return "Hola " + pPMMD.getCliente().getNombre().split(" ")[0] + ", te saluda SISTEMA ERP SOFAS Sofás. "
                + "Tu pedido " + pPMMD.getCodigo() + " (" + pPMMD.getProductoNombre() + (pPMMD.getTela() == null ? "" : " en " + pPMMD.getTela().getNombre())
                + ") está en estado: " + pPMMD.getEstado().nombre() + ". Precio " + MoneyPMMD.fmt(pPMMD.getPrecio()) + ", a cuenta "
                + MoneyPMMD.fmt(pPMMD.getAdelanto()) + ", saldo " + MoneyPMMD.fmt(pPMMD.getSaldo())
                + (pPMMD.getFechaEntrega() == null ? "" : ". Entrega estimada: " + AppClockPMMD.fmt(pPMMD.getFechaEntrega())) + ". ¡Gracias!";
    }
}
