package pe.erp.sofas.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.calculo.ExplosionPMMD;
import pe.erp.sofas.service.calculo.FormulaPMMD;
import pe.erp.sofas.service.calculo.Opciones;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@IntegracionTestPMMD
class FichasYConfiguradorTestPMMD {

    @Autowired FichaServicePMMD fichaService;

    @Autowired ConfiguradorServicePMMD configurador;

    @Autowired TelaServicePMMD telaService;

    @Test
    void juegoCompuestoSumaLasHijasAprobadasPMMD() {
        FichaTecnicaPMMD juegoPMMD = fichaService.aprobada("FT-004");
        assertThat(juegoPMMD.getCompuesta()).isTrue();
        ExplosionPMMD.Resultado rPMMD = fichaService.explotar(juegoPMMD, 1, Opciones.NINGUNA);
        double esperadoPMMD = 0, moPMMD = 0;
        Map<String, Double> porMaterialPMMD = new HashMap<>();
        for (String hijoPMMD : new String[]{"FT-001", "FT-002", "FT-003"}) {
            ExplosionPMMD.Resultado hPMMD = fichaService.explotar(fichaService.aprobada(hijoPMMD));
            esperadoPMMD += hPMMD.costoMateriales();
            moPMMD += hPMMD.costoMO();
            for (ExplosionPMMD.Linea lPMMD : hPMMD.lineas()) porMaterialPMMD.merge(lPMMD.material().getCodigo(), lPMMD.bruta(), Double::sum);
        }
        assertThat(rPMMD.costoMateriales()).isCloseTo(esperadoPMMD, within(1e-6));
        assertThat(rPMMD.costoMO()).isCloseTo(moPMMD, within(1e-6));

        for (ExplosionPMMD.PorMaterial pmPMMD : rPMMD.porMaterial()) {
            double sumaPMMD = porMaterialPMMD.get(pmPMMD.material().getCodigo());
            assertThat(pmPMMD.cantidad()).isCloseTo(pmPMMD.material().isEntera() ? Math.ceil(sumaPMMD - 1e-9) : sumaPMMD, within(1e-6));
        }
        assertThat(rPMMD.porMaterial().stream().map(pPMMD -> pPMMD.material().getCodigo()).distinct().count()).isEqualTo(rPMMD.porMaterial().size());
    }

    @Test
    void misma_tela_para_todo_el_juego_por_sustitucionPMMD() {
        FichaTecnicaPMMD juegoPMMD = fichaService.aprobada("FT-004");
        ExplosionPMMD.Resultado rPMMD = fichaService.explotar(juegoPMMD, 1, Opciones.sustituir(Map.of("MP-003", "MP-007")));
        assertThat(rPMMD.porMaterial()).noneMatch(pPMMD -> pPMMD.material().getCodigo().equals("MP-003"));
        assertThat(rPMMD.porMaterial()).anyMatch(pPMMD -> pPMMD.material().getCodigo().equals("MP-007"));
    }

    @Test
    void precioDelConfiguradorSofa3CuerposEnChenillePMMD() {
        ConfigSofaPMMD cPMMD = configurador.defecto(ConfigSofaPMMD.Modelo.MODERNO);
        assertThat(cPMMD.cuerpos()).isEqualTo(3);
        assertThat(telaService.tela(cPMMD.telaId()).getTipo()).isEqualTo(TipoTelaPMMD.CHENILLE);
        ConfiguradorServicePMMD.Cotizacion cotPMMD = configurador.cotizar(cPMMD);
        assertThat(cotPMMD.referencia()).isEqualTo("ficha FT-001 v3");

        assertThat(cotPMMD.precio()).isEqualTo(FormulaPMMD.redondearDecena(cotPMMD.costo() * 1.55));
        assertThat(cotPMMD.precio() % 10).isZero();

        assertThat(cotPMMD.costo()).isCloseTo((cotPMMD.costoMateriales() + cotPMMD.costoMO()) * 1.06, within(1e-6));

        double sinCojinesPMMD = configurador.cotizar(cPMMD.conCojines(0, null)).precio();
        double conChaisePMMD = configurador.cotizar(cPMMD.conChaise(ConfigSofaPMMD.Chaise.DERECHA)).costo();
        assertThat(sinCojinesPMMD).isLessThan(cotPMMD.precio());
        assertThat(conChaisePMMD).isGreaterThan(cotPMMD.costo());

        assertThat(cotPMMD.dias()).isEqualTo((int) Math.ceil(cotPMMD.horasMO() / 8 + 3) + (cotPMMD.critico() ? 5 : 0) + (cotPMMD.bajoPedido() ? 4 : 0));

        ConfiguradorServicePMMD.Cotizacion viscoPMMD = configurador.cotizar(cPMMD.conEspuma(ConfigSofaPMMD.Espuma.VISCO));
        assertThat(viscoPMMD.bajoPedido()).isTrue();
    }

    @Test
    void editarUnaFichaAprobadaCreaLaSiguienteVersionEnBorradorPMMD() {
        FichaTecnicaPMMD v3PMMD = fichaService.aprobada("FT-001");
        double originalPMMD = v3PMMD.getLineas().get(0).getCantidad();
        FichaServicePMMD.Edicion ePMMD = fichaService.editarLinea(v3PMMD.getId(), 0, lPMMD -> lPMMD.setCantidad(40d));
        assertThat(ePMMD.nuevaVersion()).isTrue();
        assertThat(ePMMD.ficha().getVersion()).isEqualTo(4);
        assertThat(ePMMD.ficha().getEstado()).isEqualTo(EstadoFichaPMMD.BORRADOR);
        assertThat(fichaService.ficha(v3PMMD.getId()).getLineas().get(0).getCantidad()).isEqualTo(originalPMMD);

        fichaService.aprobar(ePMMD.ficha().getId());
        assertThat(fichaService.ficha(v3PMMD.getId()).getEstado()).isEqualTo(EstadoFichaPMMD.OBSOLETA);
        assertThat(fichaService.aprobada("FT-001").getVersion()).isEqualTo(4);
    }
}
