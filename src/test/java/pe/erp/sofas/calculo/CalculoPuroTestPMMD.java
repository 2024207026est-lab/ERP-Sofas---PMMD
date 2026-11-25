package pe.erp.sofas.calculo;

import org.junit.jupiter.api.Test;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.calculo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;



class CalculoPuroTestPMMD {

    private static MaterialPMMD matPMMD(String codigoPMMD, String unidadPMMD, double costoPMMD) {
        return MaterialPMMD.builder().id((long) codigoPMMD.hashCode()).codigo(codigoPMMD).nombre(codigoPMMD).categoria("Insumos").unidad(unidadPMMD)
                .costo(costoPMMD).stock(100d).reservado(0d).minimo(0d).build();
    }

    private static FichaTecnicaPMMD fichaPMMD(int lPMMD, int fPMMD, int aPMMD, int nPMMD) {
        return FichaTecnicaPMMD.builder().codigo("FT-T").version(1).uid("FT-Tv1").estado(EstadoFichaPMMD.APROBADA).nombre("Prueba")
                .largo(lPMMD).fondo(fPMMD).alto(aPMMD).cuerpos(nPMMD).indirectosPct(6d).build();
    }

    private static CatalogoPMMD catalogoPMMD(Map<String, MaterialPMMD> mats) {
        return new CatalogoPMMD() {

            @Override
            public FichaTecnicaPMMD fichaAprobada(String codigoPMMD) {
                return null;
            }

            @Override
            public MaterialPMMD material(String codigoPMMD) {
                return mats.get(codigoPMMD);
            }
        };
    }

    @Test
    void escaladoYMermaPMMD() {
        MaterialPMMD maderaPMMD = matPMMD("MP-001", "pt", 4.2);
        MaterialPMMD telaPMMD = matPMMD("MP-003", "m", 28);
        MaterialPMMD patasPMMD = matPMMD("MP-021", "unidad", 9);
        FichaTecnicaPMMD fPMMD = fichaPMMD(210, 92, 85, 3);
        fPMMD.agregarLinea(LineaBOMPMMD.builder().material(maderaPMMD).componente(ComponentePMMD.ESTRUCTURA).cantidad(38d).mermaPct(18d).dependencia(DependenciaPMMD.L).build());
        fPMMD.agregarLinea(LineaBOMPMMD.builder().material(telaPMMD).componente(ComponentePMMD.TAPIZADO).cantidad(9.5).mermaPct(10d).dependencia(DependenciaPMMD.T).build());
        fPMMD.agregarLinea(LineaBOMPMMD.builder().material(patasPMMD).componente(ComponentePMMD.PATAS).cantidad(6d).mermaPct(0d).dependencia(DependenciaPMMD.P).build());
        fPMMD.agregarOperacion(OperacionMOPMMD.builder().etapa(EtapaMOPMMD.TAPIZADO).horas(10d).costoHora(9d).build());


        ExplosionPMMD.Resultado basePMMD = ExplosionPMMD.explotar(fPMMD, 2, Opciones.NINGUNA, catalogoPMMD(Map.of()));
        assertThat(basePMMD.lineas().get(0).neta()).isCloseTo(76, within(1e-9));
        assertThat(basePMMD.lineas().get(0).bruta()).isCloseTo(76 * 1.18, within(1e-9));
        assertThat(basePMMD.lineas().get(2).bruta()).isEqualTo(12);


        ExplosionPMMD.Resultado chicoPMMD = ExplosionPMMD.explotar(fPMMD, 1, Opciones.medidas(new MedidasPMMD(160, 92, 85, 2)), catalogoPMMD(Map.of()));
        assertThat(chicoPMMD.lineas().get(0).factor()).isCloseTo(160.0 / 210, within(1e-9));
        assertThat(chicoPMMD.lineas().get(1).bruta()).isCloseTo(9.5 * 160 / 210 * 1.10, within(1e-9));
        assertThat(chicoPMMD.lineas().get(2).bruta()).isEqualTo(4);

        assertThat(chicoPMMD.costoMO()).isCloseTo(90 * Math.pow(160.0 / 210, 0.7), within(1e-9));

        assertThat(basePMMD.costoEstandar()).isCloseTo((basePMMD.costoMateriales() + basePMMD.costoMO()) * 1.06, within(1e-6));


        FichaTecnicaPMMD gPMMD = fichaPMMD(210, 92, 85, 3);
        gPMMD.agregarLinea(LineaBOMPMMD.builder().material(matPMMD("MP-014", "unidad", 1.8)).componente(ComponentePMMD.COJINES).cantidad(6d)
                .mermaPct(5d).dependencia(DependenciaPMMD.N).build());
        assertThat(ExplosionPMMD.explotar(gPMMD, 1, Opciones.medidas(new MedidasPMMD(160, 92, 85, 2)), catalogoPMMD(Map.of())).lineas().get(0).bruta())
                .isEqualTo(5);
    }

    @Test
    void factoresDeLasFormulasPMMD() {
        MedidasPMMD basePMMD = new MedidasPMMD(210, 92, 85, 3);
        MedidasPMMD mPMMD = new MedidasPMMD(250, 100, 90, 4);
        assertThat(FormulaPMMD.factor(DependenciaPMMD.F, mPMMD, basePMMD)).isEqualTo(1);
        assertThat(FormulaPMMD.factor(DependenciaPMMD.LF, mPMMD, basePMMD)).isCloseTo(250.0 * 100 / (210 * 92), within(1e-9));
        assertThat(FormulaPMMD.factor(DependenciaPMMD.LA, mPMMD, basePMMD)).isCloseTo(250.0 * 90 / (210 * 85), within(1e-9));
        assertThat(FormulaPMMD.factor(DependenciaPMMD.T, mPMMD, basePMMD)).isCloseTo(250.0 * 190 / (210 * 177), within(1e-9));
        assertThat(FormulaPMMD.factor(DependenciaPMMD.N, mPMMD, basePMMD)).isCloseTo(4.0 / 3, within(1e-9));
        assertThat(FormulaPMMD.factor(DependenciaPMMD.P, new MedidasPMMD(150, 90, 85, 2), basePMMD)).isCloseTo(4.0 / 6, within(1e-9));
    }

    @Test
    void sustitucionDeTelaPMMD() {
        MaterialPMMD chenillePMMD = matPMMD("MP-003", "m", 28);
        MaterialPMMD terciopeloPMMD = matPMMD("MP-007", "m", 42);
        FichaTecnicaPMMD fPMMD = fichaPMMD(210, 92, 85, 3);
        fPMMD.agregarLinea(LineaBOMPMMD.builder().material(chenillePMMD).componente(ComponentePMMD.TAPIZADO).cantidad(10d).mermaPct(10d).dependencia(DependenciaPMMD.T).build());
        ExplosionPMMD.Resultado rPMMD = ExplosionPMMD.explotar(fPMMD, 1, Opciones.sustituir(Map.of("MP-003", "MP-007")), catalogoPMMD(Map.of("MP-007", terciopeloPMMD)));
        assertThat(rPMMD.lineas().get(0).material().getCodigo()).isEqualTo("MP-007");
        assertThat(rPMMD.costoMateriales()).isCloseTo(11 * 42, within(1e-9));

        ExplosionPMMD.Resultado sinPMMD = ExplosionPMMD.explotar(fPMMD, 1, Opciones.sustituir(Map.of("MP-003", ExplosionPMMD.QUITAR)), catalogoPMMD(Map.of()));
        assertThat(sinPMMD.lineas()).isEmpty();
    }

    @Test
    void redondeoDeCompraPMMD() {
        assertThat(UnidadCompraPMMD.redondear("pt", 12)).isEqualTo(50);
        assertThat(UnidadCompraPMMD.redondear("pt", 51)).isEqualTo(100);
        assertThat(UnidadCompraPMMD.redondear("plancha", 2.3)).isEqualTo(3);
        assertThat(UnidadCompraPMMD.redondear("m", 23.5)).isEqualTo(30);
        assertThat(UnidadCompraPMMD.redondear("unidad", 3)).isEqualTo(10);
        assertThat(UnidadCompraPMMD.redondear("caja", 1.2)).isEqualTo(2);
        assertThat(UnidadCompraPMMD.redondear("galón", 0.4)).isEqualTo(1);
        assertThat(UnidadCompraPMMD.redondear("kg", 6)).isEqualTo(10);
        assertThat(UnidadCompraPMMD.redondear("kg", 0)).isEqualTo(0);
    }

    @Test
    void empaquetadoDeEspumaConAprovechamientoMinimoPMMD() {

        var piezasPMMD = DespiecePMMD.piezas(new DespiecePMMD.Sofa(210, 92, 85, 3, false, false, true, "OP-T"));
        List<PiezaPMMD> respaldoPMMD = piezasPMMD.get(DespiecePMMD.D18_4).stream().map(pPMMD -> pPMMD.conCantidad(pPMMD.cantidad() * 2)).toList();
        CortePMMD.Resultado rPMMD = CortePMMD.optimizar(TipoCortePMMD.ESPUMA, respaldoPMMD, CortePMMD.Estrategia.MEJOR_LADO);
        assertThat(rPMMD.colocaciones()).hasSize(6);
        assertThat(rPMMD.laminas()).isEqualTo(1);
        assertThat(rPMMD.aprovechamiento()).isGreaterThanOrEqualTo(75);

        assertThat(rPMMD.colocaciones()).allMatch(cPMMD -> cPMMD.x() + cPMMD.w() <= 200.001 && cPMMD.y() + cPMMD.h() <= 100.001);

        CortePMMD.Resultado otraPMMD = CortePMMD.optimizar(TipoCortePMMD.ESPUMA, respaldoPMMD, CortePMMD.Estrategia.MEJOR_AREA);
        assertThat(otraPMMD.colocaciones()).hasSize(6);
    }

    @Test
    void telaDeTerciopeloSinRotacionPMMD() {
        var piezasPMMD = DespiecePMMD.piezas(new DespiecePMMD.Sofa(210, 92, 85, 3, false, false, TipoTelaPMMD.TERCIOPELO.permiteGiro(), "OP-T"));
        List<PiezaPMMD> telaPMMD = piezasPMMD.get(DespiecePMMD.TELA);
        assertThat(telaPMMD).allMatch(pPMMD -> !pPMMD.girable());
        assertThat(telaPMMD).anyMatch(pPMMD -> pPMMD.nombre().contains("(empalme)"));
        assertThat(telaPMMD).allMatch(pPMMD -> pPMMD.ancho() <= 140);
        CortePMMD.Resultado rPMMD = CortePMMD.optimizar(TipoCortePMMD.TELA, telaPMMD, CortePMMD.Estrategia.MEJOR_LADO);
        assertThat(rPMMD.colocaciones()).noneMatch(CortePMMD.Colocacion::girada);
        assertThat(rPMMD.colocaciones()).allMatch(cPMMD -> cPMMD.x() + cPMMD.w() <= 140.001);

        var conGiroPMMD = DespiecePMMD.piezas(new DespiecePMMD.Sofa(210, 92, 85, 3, false, false, true, "OP-T")).get(DespiecePMMD.TELA);
        assertThat(CortePMMD.optimizar(TipoCortePMMD.TELA, conGiroPMMD, CortePMMD.Estrategia.MEJOR_LADO).metros()).isLessThanOrEqualTo(rPMMD.metros() + 0.01);
    }

    @Test
    void maderaConKerfPMMD() {
        List<PiezaPMMD> pPMMD = List.of(new PiezaPMMD("Larguero", ComponentePMMD.ESTRUCTURA, 150, 0, 2, false, "OP-T"));
        CortePMMD.Resultado1D rPMMD = CortePMMD.optimizar1D(pPMMD, 300);

        assertThat(rPMMD.tablas()).hasSize(2);
        List<PiezaPMMD> qPMMD = List.of(new PiezaPMMD("Travesaño", ComponentePMMD.ESTRUCTURA, 70, 0, 4, false, "OP-T"));
        CortePMMD.Resultado1D r2PMMD = CortePMMD.optimizar1D(qPMMD, 300);
        assertThat(r2PMMD.tablas()).hasSize(1);
        assertThat(r2PMMD.tablas().get(0).sobrante()).isCloseTo(300 - 280 - 1.5, within(1e-9));
    }

    @Test
    void coberturaDeGarantiaPMMD() {
        LocalDate compraPMMD = LocalDate.of(2026, 3, 10);
        CoberturaPMMD.Resultado pataPMMD = CoberturaPMMD.evaluar("Pata floja", compraPMMD, LocalDate.of(2027, 3, 1));
        assertThat(pataPMMD.meses()).isEqualTo(12);
        assertThat(pataPMMD.cubierto()).isTrue();
        CoberturaPMMD.Resultado costuraPMMD = CoberturaPMMD.evaluar("Costura abierta", compraPMMD, LocalDate.of(2026, 10, 1));
        assertThat(costuraPMMD.meses()).isEqualTo(6);
        assertThat(costuraPMMD.cubierto()).isFalse();
        assertThat(costuraPMMD.vence()).isEqualTo(LocalDate.of(2026, 9, 10));
        assertThat(CoberturaPMMD.evaluar("Mecanismo trabado", LocalDate.of(2025, 11, 2), LocalDate.of(2026, 10, 2)).cubierto()).isTrue();
        assertThat(CoberturaPMMD.meses("Estructura quebrada")).isEqualTo(12);
        assertThat(CoberturaPMMD.meses("Espuma hundida")).isEqualTo(6);
    }

    @Test
    void igvIncluidoEnElPrecioPMMD() {
        assertThat(IgvPMMD.subtotal(new BigDecimal("2490.00"))).isEqualByComparingTo("2110.17");
        assertThat(IgvPMMD.igv(new BigDecimal("2490.00"))).isEqualByComparingTo("379.83");
        assertThat(IgvPMMD.subtotal(new BigDecimal("118.00"))).isEqualByComparingTo("100.00");
        assertThat(IgvPMMD.subtotal(new BigDecimal("118.00")).add(IgvPMMD.igv(new BigDecimal("118.00")))).isEqualByComparingTo("118.00");
    }
}
