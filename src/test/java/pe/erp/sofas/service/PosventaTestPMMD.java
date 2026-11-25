package pe.erp.sofas.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.calculo.FormulaPMMD;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@IntegracionTestPMMD
class PosventaTestPMMD {

    @Autowired RetapizadoServicePMMD retapizado;

    @Autowired GarantiaServicePMMD garantias;

    @Autowired TelaServicePMMD telas;

    @Autowired ProduccionServicePMMD produccion;

    @Test
    void cotizacionDeRetapizadoPMMD() {
        TelaPMMD linoPMMD = telas.porCodigo("TL-10").orElseThrow();
        RetapizadoServicePMMD.Cotizacion buenaPMMD = retapizado.cotizar(2, 160, 88, 82, "Buena", "Buena", linoPMMD);
        RetapizadoServicePMMD.Cotizacion hundidaPMMD = retapizado.cotizar(2, 160, 88, 82, "Buena", "Hundida", linoPMMD);
        RetapizadoServicePMMD.Cotizacion totalPMMD = retapizado.cotizar(2, 160, 88, 82, "Requiere refuerzo", "Cambio total", linoPMMD);

        assertThat(buenaPMMD.precio()).isEqualTo(FormulaPMMD.redondearDecena(buenaPMMD.costo() * 1.06 * 1.45));
        assertThat(buenaPMMD.ficha().getCodigo()).isEqualTo("FT-002");

        assertThat(buenaPMMD.lineas()).noneMatch(lPMMD -> lPMMD.material().getCategoria().equals("Espuma"));
        double espHundidaPMMD = hundidaPMMD.lineas().stream().filter(lPMMD -> lPMMD.material().getCategoria().equals("Espuma")).mapToDouble(RetapizadoServicePMMD.LineaCot::costo).sum();
        double espTotalPMMD = totalPMMD.lineas().stream().filter(lPMMD -> lPMMD.material().getCategoria().equals("Espuma")).mapToDouble(RetapizadoServicePMMD.LineaCot::costo).sum();
        assertThat(espTotalPMMD).isCloseTo(espHundidaPMMD * 2, within(0.5));
        assertThat(totalPMMD.lineas()).anyMatch(lPMMD -> lPMMD.material().getCategoria().equals("Madera"));

        assertThat(buenaPMMD.costoMO()).isCloseTo(buenaPMMD.horas() * 9, within(1e-6));
        assertThat(totalPMMD.precio()).isGreaterThan(hundidaPMMD.precio());
        assertThat(hundidaPMMD.precio()).isGreaterThan(buenaPMMD.precio());

        assertThat(retapizado.cotizar(3, 280, 95, 85, "Buena", "Buena", linoPMMD).aviso()).contains("no alcanza");
    }

    @Test
    void enTallerCreaLaOPDeRetapizadoEnCorteDeTelaPMMD() {
        RetapizadoPMMD rPMMD = retapizado.listar().stream().filter(xPMMD -> xPMMD.getEstado() == EstadoRetapizadoPMMD.RECOJO_PROGRAMADO).findFirst().orElseThrow();
        retapizado.avanzar(rPMMD.getId());
        RetapizadoPMMD despuesPMMD = retapizado.retapizado(rPMMD.getId());
        assertThat(despuesPMMD.getEstado()).isEqualTo(EstadoRetapizadoPMMD.EN_TALLER);
        OrdenProduccionPMMD opPMMD = produccion.porCodigo(despuesPMMD.getOpCodigo()).orElseThrow();
        assertThat(opPMMD.getEtapa()).isEqualTo(EtapaPMMD.CORTE_TELA);
        assertThat(opPMMD.getProductoNombre()).startsWith("Retapizado · ");
        assertThat(opPMMD.getTela().getId()).isEqualTo(rPMMD.getTela().getId());
    }

    @Test
    void reclamosConCoberturaYSlaPMMD() {
        ReclamoGarantiaPMMD gr013PMMD = garantias.listar().stream().filter(xPMMD -> xPMMD.getCodigo().equals("GR-013")).findFirst().orElseThrow();
        assertThat(garantias.cobertura(gr013PMMD).cubierto()).isFalse();
        ReclamoGarantiaPMMD gr015PMMD = garantias.listar().stream().filter(xPMMD -> xPMMD.getCodigo().equals("GR-015")).findFirst().orElseThrow();
        assertThat(garantias.cobertura(gr015PMMD).cubierto()).isTrue();
        assertThat(garantias.slaVencido(gr015PMMD)).isTrue();
        garantias.actualizar(gr015PMMD.getId(), "Cambio de mecanismo", "Reemplazo del mecanismo", java.math.BigDecimal.valueOf(180), EstadoReclamoPMMD.RESUELTO);
        assertThat(garantias.reclamo(gr015PMMD.getId()).getFechaCierre()).isEqualTo(pe.erp.sofas.core.AppClockPMMD.hoy());
    }
}
