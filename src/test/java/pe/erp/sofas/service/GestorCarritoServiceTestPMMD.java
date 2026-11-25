package pe.erp.sofas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.CarritoRepositoryPMMD;
import pe.erp.sofas.repository.ConfiguracionGuardadaRepositoryPMMD;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GestorCarritoServiceTestPMMD {

    private GestorCarritoServicePMMD servicePMMD;

    @Mock
    private CarritoRepositoryPMMD carritoRepoPMMD;

    @Mock
    private ConfiguracionGuardadaRepositoryPMMD configRepoPMMD;

    @Mock
    private CalculoPrecioServicePMMD precioServicePMMD;

    private UsuarioPMMD usuarioTestPMMD;

    private ConfiguracionGuardadaPMMD configTestPMMD;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servicePMMD = new GestorCarritoServicePMMD(carritoRepoPMMD, configRepoPMMD, precioServicePMMD);

        usuarioTestPMMD = new UsuarioPMMD();
        usuarioTestPMMD.setId(1L);
        usuarioTestPMMD.setNombre("Test User");

        configTestPMMD = ConfiguracionGuardadaPMMD.builder()
            .id(1L)
            .usuario(usuarioTestPMMD)
            .nombre("Mi Moderno")
            .modelo(ConfigSofaPMMD.Modelo.MODERNO)
            .cuerpos(3)
            .build();
    }

    @Test
    void testObtenerOCrearCarritoNuevoPMMD() {
        when(carritoRepoPMMD.findByUsuario(usuarioTestPMMD)).thenReturn(Optional.empty());
        when(carritoRepoPMMD.save(any(CarritoPMMD.class))).thenAnswer(invPMMD -> {
            CarritoPMMD cPMMD = invPMMD.getArgument(0);
            cPMMD.setId(1L);
            return cPMMD;
        });

        CarritoPMMD carritoPMMD = servicePMMD.obtenerOCrearCarrito(usuarioTestPMMD);

        assertNotNull(carritoPMMD);
        assertEquals(usuarioTestPMMD, carritoPMMD.getUsuario());
    }

    @Test
    void testAgregarAlCarritoPMMD() {
        CarritoPMMD carritoPMMD = CarritoPMMD.builder().usuario(usuarioTestPMMD).build();

        when(carritoRepoPMMD.findByUsuario(usuarioTestPMMD)).thenReturn(Optional.of(carritoPMMD));
        when(configRepoPMMD.findByIdAndUsuario(1L, usuarioTestPMMD)).thenReturn(Optional.of(configTestPMMD));
        when(carritoRepoPMMD.save(any(CarritoPMMD.class))).thenReturn(carritoPMMD);

        servicePMMD.agregarAlCarrito(usuarioTestPMMD, 1L, 2);

        assertEquals(1, carritoPMMD.getItems().size());
        assertEquals(2, carritoPMMD.getItems().get(0).getCantidad());
    }

    @Test
    void testRemoverDelCarritoPMMD() {
        CarritoItemPMMD itemPMMD = CarritoItemPMMD.builder().id(1L).configuracion(configTestPMMD).cantidad(2).build();
        List<CarritoItemPMMD> itemsPMMD = new ArrayList<>();
        itemsPMMD.add(itemPMMD);
        CarritoPMMD carritoPMMD = CarritoPMMD.builder().usuario(usuarioTestPMMD).items(itemsPMMD).build();

        when(carritoRepoPMMD.findByUsuario(usuarioTestPMMD)).thenReturn(Optional.of(carritoPMMD));
        when(carritoRepoPMMD.save(any(CarritoPMMD.class))).thenReturn(carritoPMMD);

        servicePMMD.removerDelCarrito(usuarioTestPMMD, 1L);

        assertTrue(carritoPMMD.getItems().isEmpty());
    }

    @Test
    void testActualizarCantidadPMMD() {
        CarritoItemPMMD itemPMMD = CarritoItemPMMD.builder().id(1L).configuracion(configTestPMMD).cantidad(2).build();
        List<CarritoItemPMMD> itemsPMMD = new ArrayList<>();
        itemsPMMD.add(itemPMMD);
        CarritoPMMD carritoPMMD = CarritoPMMD.builder().usuario(usuarioTestPMMD).items(itemsPMMD).build();

        when(carritoRepoPMMD.findByUsuario(usuarioTestPMMD)).thenReturn(Optional.of(carritoPMMD));
        when(carritoRepoPMMD.save(any(CarritoPMMD.class))).thenReturn(carritoPMMD);

        servicePMMD.actualizarCantidad(usuarioTestPMMD, 1L, 5);

        assertEquals(5, carritoPMMD.getItems().get(0).getCantidad());
    }

    @Test
    void testCalcularTotalPMMD() {
        CarritoItemPMMD itemPMMD = CarritoItemPMMD.builder().id(1L).configuracion(configTestPMMD).cantidad(2).build();
        List<CarritoItemPMMD> itemsPMMD = new ArrayList<>();
        itemsPMMD.add(itemPMMD);
        CarritoPMMD carritoPMMD = CarritoPMMD.builder().usuario(usuarioTestPMMD).items(itemsPMMD).build();

        when(carritoRepoPMMD.findByUsuario(usuarioTestPMMD)).thenReturn(Optional.of(carritoPMMD));
        when(precioServicePMMD.calcularPrecio(any(ConfigSofaPMMD.class))).thenReturn(BigDecimal.valueOf(1500));

        BigDecimal totalPMMD = servicePMMD.calcularTotal(usuarioTestPMMD);

        assertEquals(BigDecimal.valueOf(3000).setScale(2, java.math.RoundingMode.HALF_UP), totalPMMD);
    }

    @Test
    void testObtenerDetalleCarritoPMMD() {
        CarritoItemPMMD itemPMMD = CarritoItemPMMD.builder().id(1L).configuracion(configTestPMMD).cantidad(2).build();
        List<CarritoItemPMMD> itemsPMMD = new ArrayList<>();
        itemsPMMD.add(itemPMMD);
        CarritoPMMD carritoPMMD = CarritoPMMD.builder().usuario(usuarioTestPMMD).items(itemsPMMD).build();

        when(carritoRepoPMMD.findByUsuario(usuarioTestPMMD)).thenReturn(Optional.of(carritoPMMD));
        when(precioServicePMMD.calcularPrecio(any(ConfigSofaPMMD.class))).thenReturn(BigDecimal.valueOf(1500));

        GestorCarritoServicePMMD.DetalleCarrito detallePMMD = servicePMMD.obtenerDetalleCarrito(usuarioTestPMMD);

        assertEquals(1, detallePMMD.cantidadItems());
        assertEquals(2, detallePMMD.cantidadTotal());
    }
}
