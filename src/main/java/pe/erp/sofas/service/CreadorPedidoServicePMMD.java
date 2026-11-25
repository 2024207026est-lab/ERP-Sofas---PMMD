package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.CarritoRepositoryPMMD;
import pe.erp.sofas.repository.PedidoRepositoryPMMD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreadorPedidoServicePMMD {

    private final CarritoRepositoryPMMD carritoRepoPMMD;

    private final PedidoRepositoryPMMD pedidoRepoPMMD;

    private final CalculoPrecioServicePMMD precioServicePMMD;

    private final GestorCarritoServicePMMD gestorCarritoPMMD;

    public List<PedidoPMMD> crearPedidosDesdeCarrito(UsuarioPMMD usuarioPMMD, ClientePMMD clientePMMD, boolean vaciarCarritoPMMD) {
        CarritoPMMD carritoPMMD = gestorCarritoPMMD.obtenerOCrearCarrito(usuarioPMMD);

        if (carritoPMMD.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        List<PedidoPMMD> pedidosCreadosPMMD = new ArrayList<>();

        for (CarritoItemPMMD itemPMMD : carritoPMMD.getItems()) {
            PedidoPMMD pedidoPMMD = crearPedidoDesdeItemPMMD(itemPMMD, clientePMMD);
            PedidoPMMD pedidoGuardadoPMMD = pedidoRepoPMMD.save(pedidoPMMD);
            pedidosCreadosPMMD.add(pedidoGuardadoPMMD);
        }

        if (vaciarCarritoPMMD) {
            gestorCarritoPMMD.vaciarCarrito(usuarioPMMD);
        }

        return pedidosCreadosPMMD;
    }

    private PedidoPMMD crearPedidoDesdeItemPMMD(CarritoItemPMMD itemPMMD, ClientePMMD clientePMMD) {
        ConfiguracionGuardadaPMMD configPMMD = itemPMMD.getConfiguracion();
        ConfigSofaPMMD sofaConfigPMMD = configPMMD.toConfigSofa();


        BigDecimal precioUnitarioPMMD = precioServicePMMD.calcularPrecio(sofaConfigPMMD);
        BigDecimal precioTotalPMMD = precioUnitarioPMMD.multiply(BigDecimal.valueOf(itemPMMD.getCantidad()));


        String codigoPMMD = generarCodigoPedidoPMMD();


        String especificacionPMMD = generarEspecificacionPMMD(sofaConfigPMMD);


        String configSerializedPMMD = generarConfigSerializedPMMD(sofaConfigPMMD);

        PedidoPMMD pedidoPMMD = PedidoPMMD.builder()
            .codigo(codigoPMMD)
            .cliente(clientePMMD)
            .tipo(PedidoPMMD.Tipo.PERSONALIZADO)
            .cantidad(itemPMMD.getCantidad())
            .fecha(LocalDate.now())
            .precio(precioTotalPMMD)
            .estado(EstadoPedidoPMMD.CONFIRMADO)
            .largo(sofaConfigPMMD.largo())
            .fondo(sofaConfigPMMD.profundidad())
            .alto(sofaConfigPMMD.alto())
            .material(sofaConfigPMMD.espuma().nombre())
            .especificacion(especificacionPMMD)
            .configuracion(configSerializedPMMD)
            .observaciones(itemPMMD.getNotas())
            .build();

        return pedidoPMMD;
    }

    private String generarCodigoPedidoPMMD() {

        LocalDate hoyPMMD = LocalDate.now();
        String fechaPartePMMD = String.format("%04d%02d", hoyPMMD.getYear(), hoyPMMD.getMonthValue());
        String aleatorioPartePMMD = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "PED-" + fechaPartePMMD + "-" + aleatorioPartePMMD;
    }

    private String generarEspecificacionPMMD(ConfigSofaPMMD configPMMD) {
        return String.format(
            "%s | %d cuerpos | %s | %s | %s | %s",
            configPMMD.modelo().nombre(),
            configPMMD.cuerpos(),
            configPMMD.espuma().nombre(),
            configPMMD.brazo().nombre(),
            configPMMD.patas().nombre(),
            configPMMD.mecanismo().nombre()
        );
    }

    private String generarConfigSerializedPMMD(ConfigSofaPMMD configPMMD) {
        return String.format(
            "modelo=%s;cuerpos=%d;chaise=%s;profundidad=%d;brazo=%s;patas=%s;" +
            "acabado=%s;espuma=%s;mecanismo=%s",
            configPMMD.modelo().name(),
            configPMMD.cuerpos(),
            configPMMD.chaise().name(),
            configPMMD.profundidad(),
            configPMMD.brazo().name(),
            configPMMD.patas().name(),
            configPMMD.acabado().name(),
            configPMMD.espuma().name(),
            configPMMD.mecanismo().name()
        );
    }

    public PedidoPMMD actualizarPedido(Long pedidoIdPMMD, ClientePMMD clientePMMD, LocalDate fechaEntregaPMMD, String observacionesPMMD) {
        PedidoPMMD pedidoPMMD = pedidoRepoPMMD.findById(pedidoIdPMMD)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (clientePMMD != null) {
            pedidoPMMD.setCliente(clientePMMD);
        }
        if (fechaEntregaPMMD != null) {
            pedidoPMMD.setFechaEntrega(fechaEntregaPMMD);
        }
        if (observacionesPMMD != null) {
            pedidoPMMD.setObservaciones(observacionesPMMD);
        }

        return pedidoRepoPMMD.save(pedidoPMMD);
    }

    public PedidoPMMD registrarAdelanto(Long pedidoIdPMMD, BigDecimal montoPMMD) {
        PedidoPMMD pedidoPMMD = pedidoRepoPMMD.findById(pedidoIdPMMD)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        BigDecimal nuevoAdelantoPMMD = pedidoPMMD.getAdelanto().add(montoPMMD);

        if (nuevoAdelantoPMMD.compareTo(pedidoPMMD.getPrecio()) > 0) {
            throw new IllegalArgumentException("El adelanto no puede exceder el precio total");
        }

        pedidoPMMD.setAdelanto(nuevoAdelantoPMMD);
        return pedidoRepoPMMD.save(pedidoPMMD);
    }

    public PedidoPMMD cambiarEstado(Long pedidoIdPMMD, EstadoPedidoPMMD nuevoEstadoPMMD) {
        PedidoPMMD pedidoPMMD = pedidoRepoPMMD.findById(pedidoIdPMMD)
            .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        pedidoPMMD.setEstado(nuevoEstadoPMMD);
        return pedidoRepoPMMD.save(pedidoPMMD);
    }

    public record ResumenPedidos(
        
        List<PedidoPMMD> pedidos,
        
        Integer totalPedidos,
        
        BigDecimal montoTotal,
        
        Integer cantidadTotal
    ) {

        public static ResumenPedidos desde(List<PedidoPMMD> pedidos) {
            Integer totalPedidos = pedidos.size();
            BigDecimal montoTotal = pedidos.stream()
                .map(PedidoPMMD::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            Integer cantidadTotal = pedidos.stream()
                .mapToInt(PedidoPMMD::getCantidad)
                .sum();

            return new ResumenPedidos(pedidos, totalPedidos, montoTotal, cantidadTotal);
        }
    }
}
