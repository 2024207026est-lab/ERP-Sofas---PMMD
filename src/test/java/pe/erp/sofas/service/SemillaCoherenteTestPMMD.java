package pe.erp.sofas.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.MovimientoRepositoryPMMD;
import pe.erp.sofas.service.calculo.ExplosionPMMD;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;



@IntegracionTestPMMD
class SemillaCoherenteTestPMMD {

    @Autowired DashboardServicePMMD dashboard;

    @Autowired TelaServicePMMD telas;

    @Autowired InventarioServicePMMD inventario;

    @Autowired ProduccionServicePMMD produccion;

    @Autowired NotificacionServicePMMD notificaciones;

    @Autowired ProductoServicePMMD productos;

    @Autowired MovimientoRepositoryPMMD movimientos;

    @Test
    void indicadoresDelDashboardPMMD() {
        DashboardServicePMMD.Kpis kPMMD = dashboard.kpis();
        assertThat(kPMMD.ventasMes()).isEqualTo(86420);
        assertThat(kPMMD.variacionPct()).isCloseTo(14, within(0.1));
        assertThat(kPMMD.sofasVendidos()).isEqualTo(34);
        assertThat(kPMMD.sofasFabricacion()).isEqualTo(17);
        assertThat(kPMMD.ordenesFabricacion()).isEqualTo(10);
        assertThat(kPMMD.pedidosPendientesPMMD()).isEqualTo(9);
        assertThat(kPMMD.metrosTelaPMMD()).isEqualTo(277);
        assertThat(kPMMD.colores()).isEqualTo(21);
    }

    @Test
    void telasYMaterialesDeTelaPMMD() {
        assertThat(telas.porCodigo("TL-07").orElseThrow().getColor()).isEqualTo("Mostaza");
        assertThat(telas.porCodigo("TL-07").orElseThrow().getEstado()).isEqualTo("Stock bajo");
        assertThat(telas.porCodigo("TL-11").orElseThrow().getEstado()).isEqualTo("Agotado");
        for (TipoTelaPMMD tPMMD : TipoTelaPMMD.values()) {
            double sumaPMMD = telas.porTipo(tPMMD).stream().filter(xPMMD -> !xPMMD.getDescontinuada()).mapToDouble(TelaPMMD::getStock).sum();
            assertThat(inventario.material(tPMMD.materialCodigo()).getStock()).as(tPMMD.nombre()).isCloseTo(sumaPMMD, within(1e-6));
        }
    }

    @Test
    void ordenesConReservasYTresParcialesPMMD() {
        List<OrdenProduccionPMMD> productivasPMMD = produccion.activas().stream().filter(oPMMD -> oPMMD.getEtapa().productiva() && !oPMMD.isSinFicha()).toList();
        long parcialesPMMD = productivasPMMD.stream().filter(oPMMD -> produccion.estadoMateriales(oPMMD).faltantes() > 0).count();
        assertThat(parcialesPMMD).isEqualTo(3);

        Map<String, Double> porColorPMMD = new HashMap<>();
        for (OrdenProduccionPMMD oPMMD : productivasPMMD) {
            ExplosionPMMD.Resultado rPMMD = produccion.requerimiento(oPMMD);
            rPMMD.porMaterial().stream().filter(pPMMD -> pPMMD.material().isTela()).forEach(pPMMD -> porColorPMMD.merge(oPMMD.getTela().getCodigo(), pPMMD.cantidad(), Double::sum));
        }
        var softPMMD = new org.assertj.core.api.SoftAssertions();
        porColorPMMD.forEach((codigoPMMD, metrosPMMD) -> softPMMD.assertThat(telas.porCodigo(codigoPMMD).orElseThrow().getStock()).as(codigoPMMD).isGreaterThanOrEqualTo(metrosPMMD));
        softPMMD.assertAll();

        assertThat(inventario.materiales()).allMatch(mPMMD -> mPMMD.getStock() - mPMMD.getReservado() >= -1e-6);
    }

    @Test
    void kardexCuadraConElStockPMMD() {
        for (MaterialPMMD mPMMD : inventario.materiales()) {
            double saldoPMMD = movimientos.findByMaterialIdOrderByFechaAscIdAsc(mPMMD.getId()).stream().mapToDouble(MovimientoPMMD::getEfecto).sum();
            assertThat(saldoPMMD).as(mPMMD.getCodigo()).isCloseTo(mPMMD.getStock(), within(1e-6));
        }
    }

    @Test
    void alertasDeLaBarraSuperiorPMMD() {
        List<String> titulosPMMD = notificaciones.todas().stream().map(NotificacionServicePMMD.Notificacion::titulo).toList();
        assertThat(titulosPMMD).anyMatch(tPMMD -> tPMMD.startsWith("Stock crítico") && tPMMD.contains("fibra siliconada") && tPMMD.contains("Mostaza"));
        assertThat(titulosPMMD).anyMatch(tPMMD -> tPMMD.contains("reclinable de Carmen Flores") && tPMMD.contains("14/10"));
        assertThat(titulosPMMD).anyMatch(tPMMD -> tPMMD.startsWith("Entrega mañana: 4 sofás para el Hotel Royal Inn"));
        assertThat(titulosPMMD).anyMatch(tPMMD -> tPMMD.startsWith("Material agotado") && tPMMD.contains("barniz para patas"));
        assertThat(titulosPMMD).anyMatch(tPMMD -> tPMMD.contains("bloqueadas por falta de material"));
    }

    @Test
    void estadoDelModeloSaleDelStockPMMD() {
        assertThat(productos.porCodigo("SF-007").orElseThrow().getEstado()).isEqualTo("A pedido");
        assertThat(productos.porCodigo("SF-003").orElseThrow().getEstado()).isEqualTo("Stock bajo");
        assertThat(productos.porCodigo("SF-001").orElseThrow().getEstado()).isEqualTo("Disponible");
        assertThat(productos.listar()).hasSize(11);
        assertThat(productos.costoEstandar(productos.porCodigo("SF-011").orElseThrow())).isNull();
        assertThat(productos.costoEstandar(productos.porCodigo("SF-001").orElseThrow()).ficha().getEtiqueta()).isEqualTo("FT-001 v3");
    }
}
