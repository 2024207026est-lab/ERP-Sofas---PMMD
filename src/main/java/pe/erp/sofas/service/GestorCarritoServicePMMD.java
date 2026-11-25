package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.CarritoRepositoryPMMD;
import pe.erp.sofas.repository.ConfiguracionGuardadaRepositoryPMMD;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GestorCarritoServicePMMD {

    private final CarritoRepositoryPMMD carritoRepoPMMD;

    private final ConfiguracionGuardadaRepositoryPMMD configRepoPMMD;

    private final CalculoPrecioServicePMMD precioServicePMMD;

    public CarritoPMMD obtenerOCrearCarrito(UsuarioPMMD usuarioPMMD) {
        Optional<CarritoPMMD> existentePMMD = carritoRepoPMMD.findByUsuario(usuarioPMMD);
        if (existentePMMD.isPresent()) {
            return existentePMMD.get();
        }

        CarritoPMMD nuevoPMMD = CarritoPMMD.builder()
            .usuario(usuarioPMMD)
            .build();
        return carritoRepoPMMD.save(nuevoPMMD);
    }

    public CarritoPMMD agregarAlCarrito(UsuarioPMMD usuarioPMMD, Long configuracionIdPMMD, Integer cantidadPMMD) {
        CarritoPMMD carritoPMMD = obtenerOCrearCarrito(usuarioPMMD);

        ConfiguracionGuardadaPMMD configPMMD = configRepoPMMD.findByIdAndUsuario(configuracionIdPMMD, usuarioPMMD)
            .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));


        Optional<CarritoItemPMMD> existentePMMD = carritoPMMD.getItems().stream()
            .filter(itemPMMD -> itemPMMD.getConfiguracion().getId().equals(configuracionIdPMMD))
            .findFirst();

        if (existentePMMD.isPresent()) {
            existentePMMD.get().setCantidad(existentePMMD.get().getCantidad() + cantidadPMMD);
        } else {
            carritoPMMD.agregarItem(configPMMD, cantidadPMMD);
        }

        return carritoRepoPMMD.save(carritoPMMD);
    }

    public CarritoPMMD removerDelCarrito(UsuarioPMMD usuarioPMMD, Long itemIdPMMD) {
        CarritoPMMD carritoPMMD = obtenerOCrearCarrito(usuarioPMMD);
        carritoPMMD.removerItem(itemIdPMMD);
        return carritoRepoPMMD.save(carritoPMMD);
    }

    public CarritoPMMD actualizarCantidad(UsuarioPMMD usuarioPMMD, Long itemIdPMMD, Integer nuevaCantidadPMMD) {
        CarritoPMMD carritoPMMD = obtenerOCrearCarrito(usuarioPMMD);

        if (nuevaCantidadPMMD <= 0) {
            carritoPMMD.removerItem(itemIdPMMD);
        } else {
            carritoPMMD.actualizarCantidad(itemIdPMMD, nuevaCantidadPMMD);
        }

        return carritoRepoPMMD.save(carritoPMMD);
    }

    public CarritoPMMD vaciarCarrito(UsuarioPMMD usuarioPMMD) {
        CarritoPMMD carritoPMMD = obtenerOCrearCarrito(usuarioPMMD);
        carritoPMMD.vaciar();
        return carritoRepoPMMD.save(carritoPMMD);
    }

    public List<CarritoItemPMMD> listarItems(UsuarioPMMD usuarioPMMD) {
        CarritoPMMD carritoPMMD = obtenerOCrearCarrito(usuarioPMMD);
        return carritoPMMD.getItems();
    }

    public BigDecimal calcularTotal(UsuarioPMMD usuarioPMMD) {
        CarritoPMMD carritoPMMD = obtenerOCrearCarrito(usuarioPMMD);
        BigDecimal totalPMMD = BigDecimal.ZERO;

        for (CarritoItemPMMD itemPMMD : carritoPMMD.getItems()) {
            ConfigSofaPMMD configPMMD = itemPMMD.getConfiguracion().toConfigSofa();
            BigDecimal precioUnitarioPMMD = precioServicePMMD.calcularPrecio(configPMMD);
            totalPMMD = totalPMMD.add(precioUnitarioPMMD.multiply(BigDecimal.valueOf(itemPMMD.getCantidad())));
        }

        return totalPMMD.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public DetalleCarrito obtenerDetalleCarrito(UsuarioPMMD usuarioPMMD) {
        CarritoPMMD carritoPMMD = obtenerOCrearCarrito(usuarioPMMD);
        Integer cantidadItems = carritoPMMD.obtenerCantidadItems();
        Integer cantidadTotal = carritoPMMD.obtenerCantidadTotal();
        BigDecimal precioTotal = calcularTotal(usuarioPMMD);

        return new DetalleCarrito(
            cantidadItems,
            cantidadTotal,
            precioTotal,
            carritoPMMD.getItems()
        );
    }

    public void eliminarCarrito(UsuarioPMMD usuarioPMMD) {
        carritoRepoPMMD.deleteByUsuario(usuarioPMMD);
    }

    public record DetalleCarrito(
        
        Integer cantidadItems,
        
        Integer cantidadTotal,
        
        BigDecimal precioTotal,
        
        List<CarritoItemPMMD> items
    ) {}
}
