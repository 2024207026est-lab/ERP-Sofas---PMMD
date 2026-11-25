package pe.erp.sofas.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@IntegracionTestPMMD
class VentasYPedidosTestPMMD {

    @Autowired VentaServicePMMD ventas;

    @Autowired ProductoServicePMMD productos;

    @Autowired ClienteServicePMMD clientes;

    @Autowired PedidoServicePMMD pedidos;

    @Autowired ConfiguradorServicePMMD configurador;

    @Autowired ProduccionServicePMMD produccion;

    @Test
    void cobrarDescuentaStockYNumeraLaBoletaPMMD() {
        ProductoPMMD pPMMD = productos.porCodigo("SF-001").orElseThrow();
        int stockPMMD = pPMMD.getStock();
        int numeroPMMD = ventas.siguienteNumero(VentaPMMD.Comprobante.BOLETA);
        VentaPMMD vPMMD = ventas.cobrar(new VentaServicePMMD.Cobro(null, VentaPMMD.Comprobante.BOLETA,
                List.of(new VentaServicePMMD.Linea(pPMMD.getId(), 1, null)), BigDecimal.ZERO, "Efectivo", new BigDecimal("2500"), null, null, null));
        assertThat(vPMMD.getSerie()).isEqualTo("B001");
        assertThat(vPMMD.getNumero()).isEqualTo(numeroPMMD);
        assertThat(vPMMD.getVuelto()).isEqualByComparingTo("10.00");
        assertThat(productos.porCodigo("SF-001").orElseThrow().getStock()).isEqualTo(stockPMMD - 1);
    }

    @Test
    void facturaExigeRucYMixtoDebeSumarElTotalPMMD() {
        ProductoPMMD pPMMD = productos.porCodigo("SF-010").orElseThrow();
        ClientePMMD personaPMMD = clientes.listar().stream().filter(cPMMD -> !cPMMD.tieneRuc()).findFirst().orElseThrow();
        assertThatThrownBy(() -> ventas.cobrar(new VentaServicePMMD.Cobro(personaPMMD.getId(), VentaPMMD.Comprobante.FACTURA,
                List.of(new VentaServicePMMD.Linea(pPMMD.getId(), 1, null)), null, "Tarjeta", null, null, null, null)))
                .isInstanceOf(NegocioExceptionPMMD.class).hasMessageContaining("RUC");
        assertThatThrownBy(() -> ventas.cobrar(new VentaServicePMMD.Cobro(null, VentaPMMD.Comprobante.BOLETA,
                List.of(new VentaServicePMMD.Linea(pPMMD.getId(), 1, null)), null, "Mixto", null, "Yape", new BigDecimal("100"), new BigDecimal("100"))))
                .isInstanceOf(NegocioExceptionPMMD.class);
        VentaPMMD okPMMD = ventas.cobrar(new VentaServicePMMD.Cobro(null, VentaPMMD.Comprobante.BOLETA,
                List.of(new VentaServicePMMD.Linea(pPMMD.getId(), 1, null)), null, "Mixto", null, "Yape", new BigDecimal("160"), new BigDecimal("100")));
        assertThat(okPMMD.getMetodoPago()).startsWith("Mixto");
        ProductoPMMD sinStockPMMD = productos.porCodigo("SF-007").orElseThrow();
        assertThatThrownBy(() -> ventas.cobrar(new VentaServicePMMD.Cobro(null, VentaPMMD.Comprobante.BOLETA,
                List.of(new VentaServicePMMD.Linea(sinStockPMMD.getId(), 1, null)), null, "Tarjeta", null, null, null, null)))
                .isInstanceOf(NegocioExceptionPMMD.class).hasMessageContaining("Stock insuficiente");
    }

    @Test
    void pedidoNecesitaElCincuentaPorCientoParaConfirmarYGenerarOPPMMD() {
        PedidoPMMD pPMMD = pedidos.listar().stream().filter(xPMMD -> xPMMD.getCodigo().equals("PE-0249")).findFirst().orElseThrow();
        assertThatThrownBy(() -> pedidos.avanzarA(pPMMD.getId(), EstadoPedidoPMMD.CONFIRMADO)).isInstanceOf(NegocioExceptionPMMD.class);
        assertThatThrownBy(() -> pedidos.generarOP(pPMMD.getId(), null)).isInstanceOf(NegocioExceptionPMMD.class);
        PedidoPMMD conAdelantoPMMD = pedidos.registrarAdelanto(pPMMD.getId(), pPMMD.getPrecio().multiply(new BigDecimal("0.5")), "Yape");
        assertThat(conAdelantoPMMD.getEstado()).isEqualTo(EstadoPedidoPMMD.CONFIRMADO);
        OrdenProduccionPMMD opPMMD = pedidos.generarOP(pPMMD.getId(), null);
        assertThat(opPMMD.getFichaUid()).isEqualTo("FT-009v1");
        assertThat(pedidos.pedido(pPMMD.getId()).getEstado()).isEqualTo(EstadoPedidoPMMD.EN_PRODUCCION);
        assertThat(pedidos.pedido(pPMMD.getId()).getOpCodigo()).isEqualTo(opPMMD.getCodigo());
    }

    @Test
    void crearPedidoDesdeElConfiguradorConSuOPPMMD() {
        ClientePMMD cPMMD = clientes.listar().get(0);
        ConfigSofaPMMD cfgPMMD = configurador.defecto(ConfigSofaPMMD.Modelo.CHESTERFIELD);
        ConfiguradorServicePMMD.Cotizacion cotPMMD = configurador.cotizar(cfgPMMD);
        PedidoPMMD pPMMD = configurador.crearPedido(cfgPMMD, cPMMD.getId(), null, BigDecimal.valueOf(cotPMMD.precio() / 2), "Efectivo", null, true, null);
        assertThat(pPMMD.getTipo()).isEqualTo(PedidoPMMD.Tipo.PERSONALIZADO);
        assertThat(pPMMD.getPrecio()).isEqualByComparingTo(BigDecimal.valueOf(cotPMMD.precio()));
        assertThat(pPMMD.getOpCodigo()).isNotNull();
        OrdenProduccionPMMD opPMMD = produccion.porCodigo(pPMMD.getOpCodigo()).orElseThrow();
        assertThat(opPMMD.getFichaUid()).isEqualTo("FT-001v3");
        assertThat(opPMMD.getExtras()).anyMatch(xPMMD -> xPMMD.getMaterialCodigo().equals("MP-016") && xPMMD.getCantidad() == 48);
        assertThat(opPMMD.getSustituciones()).containsEntry("MP-003", "MP-004");
        assertThat(ConfigSofaPMMD.leer(pPMMD.getConfiguracion())).isEqualTo(cfgPMMD);
    }
}
