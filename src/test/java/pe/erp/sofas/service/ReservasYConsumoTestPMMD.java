package pe.erp.sofas.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.ConsumoRealRepositoryPMMD;
import pe.erp.sofas.repository.MovimientoRepositoryPMMD;
import pe.erp.sofas.service.calculo.ExplosionPMMD;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@IntegracionTestPMMD
class ReservasYConsumoTestPMMD {

    @Autowired ProduccionServicePMMD produccion;

    @Autowired ProductoServicePMMD productos;

    @Autowired InventarioServicePMMD inventario;

    @Autowired TelaServicePMMD telas;

    @Autowired MovimientoRepositoryPMMD movimientos;

    @Autowired ConsumoRealRepositoryPMMD consumos;

    private OrdenProduccionPMMD nuevaPMMD(String productoPMMD, int cant, String telaPMMD) {
        ProductoPMMD pPMMD = productos.porCodigo(productoPMMD).orElseThrow();
        TelaPMMD tPMMD = telas.porCodigo(telaPMMD).orElseThrow();
        return produccion.crear(new ProduccionServicePMMD.NuevaOP(pPMMD.getId(), null, cant, null, null, PrioridadPMMD.MEDIA, null, null,
                tPMMD.getId(), null, Map.of(), List.of(), null, null, null, EtapaPMMD.PENDIENTE, null, null));
    }

    @Test
    void reservaLiberacionYConsumoPMMD() {
        OrdenProduccionPMMD opPMMD = nuevaPMMD("SF-002", 1, "TL-08");
        assertThat(opPMMD.getFichaUid()).isEqualTo("FT-002v1");
        assertThat(opPMMD.getSustituciones()).containsEntry("MP-003", "MP-003");
        ExplosionPMMD.Resultado reqPMMD = produccion.requerimiento(opPMMD);
        MaterialPMMD maderaPMMD = inventario.material("MP-001");
        double reservadoAntesPMMD = maderaPMMD.getReservado();
        double stockAntesPMMD = maderaPMMD.getStock();
        double necesarioPMMD = reqPMMD.porMaterial().stream().filter(xPMMD -> xPMMD.material().getCodigo().equals("MP-001")).findFirst().orElseThrow().cantidad();


        assertThat(produccion.evaluar(opPMMD, EtapaPMMD.CORTE_MADERA).requisito()).isIn(ProduccionServicePMMD.Requisito.NINGUNO, ProduccionServicePMMD.Requisito.CONFIRMAR_FALTANTES);
        produccion.mover(opPMMD.getId(), EtapaPMMD.CORTE_MADERA, null);
        assertThat(inventario.material("MP-001").getReservado()).isCloseTo(reservadoAntesPMMD + necesarioPMMD, within(1e-6));
        assertThat(inventario.material("MP-001").getStock()).isEqualTo(stockAntesPMMD);
        assertThat(produccion.reservadoPor(produccion.op(opPMMD.getId()), maderaPMMD)).isCloseTo(necesarioPMMD, within(1e-6));


        produccion.mover(opPMMD.getId(), EtapaPMMD.PENDIENTE, null);
        assertThat(inventario.material("MP-001").getReservado()).isCloseTo(reservadoAntesPMMD, within(1e-6));


        produccion.mover(opPMMD.getId(), EtapaPMMD.TAPIZADO, null);
        double telaAntesPMMD = telas.porCodigo("TL-08").orElseThrow().getStock();
        long consumosAntesPMMD = consumos.count();
        produccion.completar(opPMMD.getId(), Map.of(maderaPMMD.getId(), necesarioPMMD + 1));
        MaterialPMMD despuesPMMD = inventario.material("MP-001");
        assertThat(despuesPMMD.getReservado()).isCloseTo(reservadoAntesPMMD, within(1e-6));
        assertThat(despuesPMMD.getStock()).isCloseTo(stockAntesPMMD - necesarioPMMD - 1, within(1e-6));
        assertThat(produccion.op(opPMMD.getId()).getEtapa()).isEqualTo(EtapaPMMD.LISTO);
        List<MovimientoPMMD> kardexPMMD = movimientos.findByMaterialIdOrderByFechaAscIdAsc(maderaPMMD.getId());
        assertThat(kardexPMMD).anyMatch(mPMMD -> mPMMD.getTipo() == TipoMovimientoPMMD.CONSUMO_OP && mPMMD.getReferencia().equals(opPMMD.getCodigo()));
        assertThat(kardexPMMD).anyMatch(mPMMD -> mPMMD.getTipo() == TipoMovimientoPMMD.RESERVA && mPMMD.getReferencia().equals(opPMMD.getCodigo()));
        assertThat(kardexPMMD).anyMatch(mPMMD -> mPMMD.getTipo() == TipoMovimientoPMMD.LIBERACION && mPMMD.getReferencia().equals(opPMMD.getCodigo()));
        assertThat(consumos.count()).isEqualTo(consumosAntesPMMD + reqPMMD.porMaterial().size());

        assertThat(telas.porCodigo("TL-08").orElseThrow().getStock()).isLessThan(telaAntesPMMD);
        double sumaPMMD = telas.porTipo(TipoTelaPMMD.CHENILLE).stream().filter(tPMMD -> !tPMMD.getDescontinuada()).mapToDouble(TelaPMMD::getStock).sum();
        assertThat(inventario.material("MP-003").getStock()).isCloseTo(sumaPMMD, within(1e-6));
        assertThatThrownBy(() -> produccion.mover(opPMMD.getId(), EtapaPMMD.COSTURA, null)).isInstanceOf(NegocioExceptionPMMD.class);
    }

    @Test
    void unaReservaNuncaDejaElDisponibleNegativoPMMD() {

        MaterialPMMD barnizPMMD = inventario.material("MP-029");
        assertThat(barnizPMMD.getDisponible()).isZero();
        OrdenProduccionPMMD opPMMD = nuevaPMMD("SF-001", 1, "TL-01");
        ProduccionServicePMMD.ResultadoReserva rPMMD = produccion.reservar(opPMMD.getId());
        assertThat(rPMMD.faltantes()).anyMatch(fPMMD -> fPMMD.material().getCodigo().equals("MP-029"));
        MaterialPMMD despuesPMMD = inventario.material("MP-029");
        assertThat(despuesPMMD.getStock() - despuesPMMD.getReservado()).isGreaterThanOrEqualTo(-1e-9);
        assertThat(produccion.estadoMateriales(produccion.op(opPMMD.getId())).lineas())
                .filteredOn(lPMMD -> lPMMD.material().getCodigo().equals("MP-029")).first().extracting(ProduccionServicePMMD.LineaMaterial::estado).isEqualTo("Faltará");
        produccion.anular(opPMMD.getId());
        assertThat(produccion.reservadoPor(produccion.op(opPMMD.getId()), inventario.material("MP-001"))).isZero();
    }

    @Test
    void sinFichaAprobadaPideAutorizacionPMMD() {
        OrdenProduccionPMMD opPMMD = nuevaPMMD("SF-011", 1, "TL-16");
        assertThat(opPMMD.isSinFicha()).isTrue();
        assertThat(produccion.evaluar(opPMMD, EtapaPMMD.CORTE_MADERA).requisito()).isEqualTo(ProduccionServicePMMD.Requisito.AUTORIZACION);
        assertThatThrownBy(() -> produccion.mover(opPMMD.getId(), EtapaPMMD.CORTE_MADERA, null)).isInstanceOf(NegocioExceptionPMMD.class);
        produccion.mover(opPMMD.getId(), EtapaPMMD.CORTE_MADERA, "Autorizado por Supervisor");
        OrdenProduccionPMMD despuesPMMD = produccion.op(opPMMD.getId());
        assertThat(despuesPMMD.getEtapa()).isEqualTo(EtapaPMMD.CORTE_MADERA);
        assertThat(produccion.indicador(despuesPMMD).texto()).isEqualTo("Sin ficha · autorizado");
    }
}
