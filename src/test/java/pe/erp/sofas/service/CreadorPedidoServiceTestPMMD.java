package pe.erp.sofas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.CarritoRepositoryPMMD;
import pe.erp.sofas.repository.PedidoRepositoryPMMD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreadorPedidoServiceTestPMMD {

    private CreadorPedidoServicePMMD servicePMMD;

    @Mock
    private CarritoRepositoryPMMD carritoRepoPMMD;

    @Mock
    private PedidoRepositoryPMMD pedidoRepoPMMD;

    @Mock
    private CalculoPrecioServicePMMD precioServicePMMD;

    @Mock
    private GestorCarritoServicePMMD gestorCarritoPMMD;

    private UsuarioPMMD usuarioTestPMMD;

    private ClientePMMD clienteTestPMMD;

    private ConfiguracionGuardadaPMMD configTestPMMD;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servicePMMD = new CreadorPedidoServicePMMD(carritoRepoPMMD, pedidoRepoPMMD, precioServicePMMD, gestorCarritoPMMD);

        usuarioTestPMMD = new UsuarioPMMD();
        usuarioTestPMMD.setId(1L);

        clienteTestPMMD = new ClientePMMD();
        clienteTestPMMD.setId(1L);
        clienteTestPMMD.setNombre("Cliente Test");

        configTestPMMD = ConfiguracionGuardadaPMMD.builder()
            .id(1L)
            .usuario(usuarioTestPMMD)
            .nombre("Mi Moderno")
            .modelo(ConfigSofaPMMD.Modelo.MODERNO)
            .cuerpos(3)
            .build();
    }

    @Test
    void testCrearPedidoDesdeCarritoVacioPMMD() {
        CarritoPMMD carritoVacioPMMD = CarritoPMMD.builder().usuario(usuarioTestPMMD).items(new ArrayList<>()).build();
        when(gestorCarritoPMMD.obtenerOCrearCarrito(usuarioTestPMMD)).thenReturn(carritoVacioPMMD);

        assertThrows(IllegalArgumentException.class, () ->
            servicePMMD.crearPedidosDesdeCarrito(usuarioTestPMMD, clienteTestPMMD, false)
        );
    }

    @Test
    void testCrearPedidoDesdeCarritoConItemsPMMD() {
        CarritoItemPMMD itemPMMD = CarritoItemPMMD.builder()
            .id(1L)
            .configuracion(configTestPMMD)
            .cantidad(2)
            .build();

        List<CarritoItemPMMD> itemsPMMD = new ArrayList<>();
        itemsPMMD.add(itemPMMD);

        CarritoPMMD carritoPMMD = CarritoPMMD.builder()
            .usuario(usuarioTestPMMD)
            .items(itemsPMMD)
            .build();

        when(gestorCarritoPMMD.obtenerOCrearCarrito(usuarioTestPMMD)).thenReturn(carritoPMMD);
        when(precioServicePMMD.calcularPrecio(any(ConfigSofaPMMD.class))).thenReturn(BigDecimal.valueOf(1500));
        when(pedidoRepoPMMD.save(any(PedidoPMMD.class))).thenAnswer(invPMMD -> {
            PedidoPMMD pPMMD = invPMMD.getArgument(0);
            pPMMD.setId(1L);
            return pPMMD;
        });

        List<PedidoPMMD> pedidosPMMD = servicePMMD.crearPedidosDesdeCarrito(usuarioTestPMMD, clienteTestPMMD, false);

        assertEquals(1, pedidosPMMD.size());
        PedidoPMMD pedidoPMMD = pedidosPMMD.get(0);
        assertEquals(clienteTestPMMD, pedidoPMMD.getCliente());
        assertEquals(2, pedidoPMMD.getCantidad());
        assertEquals(BigDecimal.valueOf(3000), pedidoPMMD.getPrecio());
    }

    @Test
    void testCrearPedidoVaciaCarritoPMMD() {
        CarritoItemPMMD itemPMMD = CarritoItemPMMD.builder()
            .id(1L)
            .configuracion(configTestPMMD)
            .cantidad(1)
            .build();

        List<CarritoItemPMMD> itemsPMMD = new ArrayList<>();
        itemsPMMD.add(itemPMMD);

        CarritoPMMD carritoPMMD = CarritoPMMD.builder()
            .usuario(usuarioTestPMMD)
            .items(itemsPMMD)
            .build();

        when(gestorCarritoPMMD.obtenerOCrearCarrito(usuarioTestPMMD)).thenReturn(carritoPMMD);
        when(precioServicePMMD.calcularPrecio(any(ConfigSofaPMMD.class))).thenReturn(BigDecimal.valueOf(1500));
        when(pedidoRepoPMMD.save(any(PedidoPMMD.class))).thenAnswer(invPMMD -> {
            PedidoPMMD pPMMD = invPMMD.getArgument(0);
            pPMMD.setId(1L);
            return pPMMD;
        });

        servicePMMD.crearPedidosDesdeCarrito(usuarioTestPMMD, clienteTestPMMD, true);

        verify(gestorCarritoPMMD).vaciarCarrito(usuarioTestPMMD);
    }

    @Test
    void testRegistrarAdelantoPMMD() {
        PedidoPMMD pedidoPMMD = PedidoPMMD.builder()
            .id(1L)
            .precio(BigDecimal.valueOf(3000))
            .adelanto(BigDecimal.valueOf(500))
            .build();

        when(pedidoRepoPMMD.findById(1L)).thenReturn(Optional.of(pedidoPMMD));
        when(pedidoRepoPMMD.save(any(PedidoPMMD.class))).thenReturn(pedidoPMMD);

        servicePMMD.registrarAdelanto(1L, BigDecimal.valueOf(1000));

        assertEquals(BigDecimal.valueOf(1500), pedidoPMMD.getAdelanto());
    }

    @Test
    void testRegistrarAdelantoExcedentePMMD() {
        PedidoPMMD pedidoPMMD = PedidoPMMD.builder()
            .id(1L)
            .precio(BigDecimal.valueOf(1000))
            .adelanto(BigDecimal.valueOf(500))
            .build();

        when(pedidoRepoPMMD.findById(1L)).thenReturn(Optional.of(pedidoPMMD));

        assertThrows(IllegalArgumentException.class, () ->
            servicePMMD.registrarAdelanto(1L, BigDecimal.valueOf(700))
        );
    }

    @Test
    void testCambiarEstadoPMMD() {
        PedidoPMMD pedidoPMMD = PedidoPMMD.builder()
            .id(1L)
            .estado(EstadoPedidoPMMD.CONFIRMADO)
            .build();

        when(pedidoRepoPMMD.findById(1L)).thenReturn(Optional.of(pedidoPMMD));
        when(pedidoRepoPMMD.save(any(PedidoPMMD.class))).thenReturn(pedidoPMMD);

        servicePMMD.cambiarEstado(1L, EstadoPedidoPMMD.EN_PRODUCCION);

        assertEquals(EstadoPedidoPMMD.EN_PRODUCCION, pedidoPMMD.getEstado());
    }

    @Test
    void testResumenPedidosPMMD() {
        PedidoPMMD p1PMMD = PedidoPMMD.builder()
            .id(1L)
            .precio(BigDecimal.valueOf(1500))
            .cantidad(1)
            .build();

        PedidoPMMD p2PMMD = PedidoPMMD.builder()
            .id(2L)
            .precio(BigDecimal.valueOf(3000))
            .cantidad(2)
            .build();

        List<PedidoPMMD> pedidosPMMD = new ArrayList<>();
        pedidosPMMD.add(p1PMMD);
        pedidosPMMD.add(p2PMMD);

        CreadorPedidoServicePMMD.ResumenPedidos resumenPMMD = CreadorPedidoServicePMMD.ResumenPedidos.desde(pedidosPMMD);

        assertEquals(2, resumenPMMD.totalPedidos());
        assertEquals(BigDecimal.valueOf(4500), resumenPMMD.montoTotal());
        assertEquals(3, resumenPMMD.cantidadTotal());
    }
}
