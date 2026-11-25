package pe.erp.sofas.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.calculo.UnidadCompraPMMD;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@IntegracionTestPMMD
class MrpYComprasTestPMMD {

    @Autowired MrpServicePMMD mrp;

    @Autowired CompraServicePMMD compras;

    @Autowired InventarioServicePMMD inventario;

    @Autowired TelaServicePMMD telas;

    @Test
    void faltanteNetoConsideraLasComprasEnTransitoPMMD() {
        MrpServicePMMD.Resultado rPMMD = mrp.calcular(MrpServicePMMD.Filtro.TODO);
        assertThat(rPMMD.ops()).isGreaterThan(0);
        for (MrpServicePMMD.Fila fPMMD : rPMMD.filas()) {
            assertThat(fPMMD.faltante()).isCloseTo(Math.max(0, fPMMD.requerido() - fPMMD.stock() - fPMMD.transito()), within(1e-6));
        }

        MrpServicePMMD.Fila barnizPMMD = rPMMD.filas().stream().filter(fPMMD -> fPMMD.material().getCodigo().equals("MP-029")).findFirst().orElseThrow();
        assertThat(barnizPMMD.transito()).isEqualTo(3);
        assertThat(barnizPMMD.faltante()).isZero();
        MrpServicePMMD.Fila fibraPMMD = rPMMD.filas().stream().filter(fPMMD -> fPMMD.material().getCodigo().equals("MP-013")).findFirst().orElseThrow();
        assertThat(fibraPMMD.transito()).isZero();
        assertThat(fibraPMMD.faltante()).isGreaterThan(0);

        assertThat(rPMMD.pedidos()).isEqualTo(1);
    }

    @Test
    void generarOrdenesRedondeaALaUnidadDeCompraYRecibirSumaStockPMMD() {
        MaterialPMMD madera = inventario.material("MP-001");
        MrpServicePMMD.Fila filaPMMD = new MrpServicePMMD.Fila(madera, 120, 0, 37, null, madera.getProveedor(), java.util.Set.of("OP-T"));
        List<String> codigosPMMD = mrp.generarOrdenes(List.of(filaPMMD), "MRP");
        assertThat(codigosPMMD).hasSize(1);
        OrdenCompraPMMD ocPMMD = compras.listar().stream().filter(oPMMD -> oPMMD.getCodigo().equals(codigosPMMD.get(0))).findFirst().orElseThrow();
        assertThat(ocPMMD.getEstado()).isEqualTo(EstadoCompraPMMD.BORRADOR);
        assertThat(ocPMMD.getOrigen()).isEqualTo("MRP");
        assertThat(ocPMMD.getItems().get(0).getCantidad()).isEqualTo(UnidadCompraPMMD.redondear("pt", 37)).isEqualTo(50);

        double antesPMMD = inventario.material("MP-001").getStock();
        compras.solicitar(ocPMMD.getId());
        compras.aprobar(ocPMMD.getId());
        compras.recibir(ocPMMD.getId());
        assertThat(inventario.material("MP-001").getStock()).isEqualTo(antesPMMD + 50);
        assertThat(inventario.kardex(madera.getId())).anyMatch(mPMMD -> mPMMD.getTipo() == TipoMovimientoPMMD.ENTRADA && mPMMD.getReferencia().equals(ocPMMD.getCodigo()));
    }

    @Test
    void recibirTelaEntraAlColorYMantieneLaSumaPMMD() {
        MaterialPMMD linoPMMD = inventario.material("MP-006");
        TelaPMMD salviaPMMD = telas.porCodigo("TL-11").orElseThrow();
        OrdenCompraPMMD ocPMMD = compras.crear(linoPMMD.getProveedor().getId(), List.of(new CompraServicePMMD.LineaCompra(linoPMMD.getId(), salviaPMMD.getId(), 20,
                java.math.BigDecimal.valueOf(34))), true, null, "Manual", false);
        compras.aprobar(ocPMMD.getId());
        compras.recibir(ocPMMD.getId());
        assertThat(telas.porCodigo("TL-11").orElseThrow().getStock()).isEqualTo(20);
        double sumaPMMD = telas.porTipo(TipoTelaPMMD.LINO).stream().filter(tPMMD -> !tPMMD.getDescontinuada()).mapToDouble(TelaPMMD::getStock).sum();
        assertThat(inventario.material("MP-006").getStock()).isCloseTo(sumaPMMD, within(1e-6));
    }
}
