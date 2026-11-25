package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.FichaTecnicaRepositoryPMMD;
import pe.erp.sofas.repository.MaterialRepositoryPMMD;
import pe.erp.sofas.repository.OrdenProduccionRepositoryPMMD;
import pe.erp.sofas.service.calculo.ExplosionPMMD;
import pe.erp.sofas.service.calculo.MedidasPMMD;
import pe.erp.sofas.service.calculo.Opciones;

import java.util.*;
import java.util.function.Consumer;






@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FichaServicePMMD {
    


    public record Edicion(
      FichaTecnicaPMMD ficha, 
        boolean nuevaVersion) {
    }



    public record FilaSimulacion(
      MaterialPMMD material, 
        ComponentePMMD componente, 
          double base, 
            double simulado) {

        public double diferencia() {
            return simulado - base;
        }
    }

    public record Simulacion(
      ExplosionPMMD.Resultado base, 
        ExplosionPMMD.Resultado simulado, 
          List<FilaSimulacion> filas) {
    }



    public record Composicion(
      String nombre, 
        double valor, 
          String color) {
    }

    private final FichaTecnicaRepositoryPMMD repositoryPMMD;

    private final MaterialRepositoryPMMD materialRepositoryPMMD;

    private final OrdenProduccionRepositoryPMMD opRepositoryPMMD;

    private final CatalogoServicePMMD catalogoPMMD;

    private final UsuarioActualPMMD usuarioPMMD;

    public List<FichaTecnicaPMMD> todas() {
        return repositoryPMMD.findAllByOrderByCodigoAscVersionDesc();
    }

    public FichaTecnicaPMMD ficha(Long idPMMD) {
        return repositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("La ficha ya no existe."));
    }

    public Optional<FichaTecnicaPMMD> porUid(String uidPMMD) {
        return uidPMMD == null ? Optional.empty() : repositoryPMMD.findByUid(uidPMMD);
    }

    public FichaTecnicaPMMD aprobada(String codigoPMMD) {
        return catalogoPMMD.fichaAprobada(codigoPMMD);
    }



    public FichaTecnicaPMMD aprobadaDe(ProductoPMMD pPMMD) {
        if (pPMMD == null) return null;
        return todas().stream().filter(fPMMD -> fPMMD.getProducto() != null && fPMMD.getProducto().getId().equals(pPMMD.getId())
                && fPMMD.getEstado() == EstadoFichaPMMD.APROBADA).findFirst().orElse(null);
    }



    public FichaTecnicaPMMD ultimaDe(ProductoPMMD pPMMD) {
        if (pPMMD == null) return null;
        return todas().stream().filter(fPMMD -> fPMMD.getProducto() != null && fPMMD.getProducto().getId().equals(pPMMD.getId()))
                .findFirst().orElse(null);
    }

    public ExplosionPMMD.Resultado explotar(FichaTecnicaPMMD fPMMD, int cantidadPMMD, Opciones opPMMD) {
        return ExplosionPMMD.explotar(fPMMD, cantidadPMMD, opPMMD, catalogoPMMD);
    }

    public ExplosionPMMD.Resultado explotar(FichaTecnicaPMMD fPMMD) {
        return explotar(fPMMD, 1, Opciones.NINGUNA);
    }

    public double costoEstandar(FichaTecnicaPMMD fPMMD) {
        return explotar(fPMMD).costoEstandar();
    }



    public List<FichaTecnicaPMMD> hijas(FichaTecnicaPMMD fPMMD) {
        return fPMMD.getHijos().stream().map(catalogoPMMD::fichaAprobada).filter(Objects::nonNull).toList();
    }

    public List<Composicion> composicion(ExplosionPMMD.Resultado rPMMD) {
        double madera = 0, espuma = 0, tela = 0, insumos = 0;
        for (ExplosionPMMD.Linea lPMMD : rPMMD.lineas()) {
            MaterialPMMD mPMMD = lPMMD.material();
            String catPMMD = mPMMD.getCategoria();
            if (catPMMD.equals("Madera") || catPMMD.equals("Tableros")) madera += lPMMD.costo();
            else if (catPMMD.equals("Espuma")) espuma += lPMMD.costo();
            else if (mPMMD.isTela() || mPMMD.getNombre().toLowerCase(Locale.ROOT).contains("guata")) tela += lPMMD.costo();
            else insumos += lPMMD.costo();
        }
        return List.of(
                new Composicion("Madera y tableros", madera, "#A47148"),
                new Composicion("Espuma", espuma, "#0EA5E9"),
                new Composicion("Tela y guata", tela, "#EC4899"),
                new Composicion("Insumos", insumos, "#F59E0B"),
                new Composicion("Mano de obra", rPMMD.costoMO(), "#2563EB"));
    }



    public List<OrdenProduccionPMMD> usos(FichaTecnicaPMMD fPMMD) {
        return opRepositoryPMMD.findAllByOrderByCodigoAsc().stream()
                .filter(oPMMD -> fPMMD.getUid().equals(oPMMD.getFichaUid()) && oPMMD.isActiva()).toList();
    }

    public Simulacion simular(FichaTecnicaPMMD fPMMD, MedidasPMMD medidasPMMD, int piezasPMMD) {
        ExplosionPMMD.Resultado base = explotar(fPMMD, piezasPMMD, Opciones.NINGUNA);
        ExplosionPMMD.Resultado sim = explotar(fPMMD, piezasPMMD, Opciones.medidas(medidasPMMD));
        List<FilaSimulacion> filas = new ArrayList<>();
        Map<String, Double> simPorMatPMMD = new HashMap<>();
        for (ExplosionPMMD.Linea lPMMD : sim.lineas()) simPorMatPMMD.merge(lPMMD.material().getCodigo() + "|" + lPMMD.componente(), lPMMD.bruta(), Double::sum);
        Map<String, FilaSimulacion> accPMMD = new LinkedHashMap<>();
        for (ExplosionPMMD.Linea lPMMD : base.lineas()) {
            String kPMMD = lPMMD.material().getCodigo() + "|" + lPMMD.componente();
            FilaSimulacion prevPMMD = accPMMD.get(kPMMD);
            double b = (prevPMMD == null ? 0 : prevPMMD.base()) + lPMMD.bruta();
            accPMMD.put(kPMMD, new FilaSimulacion(lPMMD.material(), lPMMD.componente(), b, simPorMatPMMD.getOrDefault(kPMMD, 0d)));
        }
        filas.addAll(accPMMD.values());
        return new Simulacion(base, sim, filas);
    }








    @Transactional
    public Edicion editar(Long fichaIdPMMD, Consumer<FichaTecnicaPMMD> cambioPMMD) {
        FichaTecnicaPMMD fPMMD = ficha(fichaIdPMMD);
        boolean nueva = false;
        if (!fPMMD.isEditable()) {
            fPMMD = copiarPMMD(fPMMD);
            nueva = true;
        }
        cambioPMMD.accept(fPMMD);
        return new Edicion(repositoryPMMD.save(fPMMD), nueva);
    }

    @Transactional
    public Edicion editarLinea(Long fichaIdPMMD, int indicePMMD, Consumer<LineaBOMPMMD> cambioPMMD) {
        return editar(fichaIdPMMD, fPMMD -> cambioPMMD.accept(fPMMD.getLineas().get(indicePMMD)));
    }

    @Transactional
    public Edicion agregarLinea(Long fichaIdPMMD, Long materialIdPMMD, ComponentePMMD componente, double cantidadPMMD, double mermaPMMD, DependenciaPMMD depPMMD) {
        if (materialIdPMMD == null) throw new NegocioExceptionPMMD("Elige un material.");
        if (cantidadPMMD <= 0) throw new NegocioExceptionPMMD("La cantidad debe ser mayor a cero.");
        MaterialPMMD mPMMD = materialRepositoryPMMD.findById(materialIdPMMD).orElseThrow();
        return editar(fichaIdPMMD, fPMMD -> fPMMD.agregarLinea(LineaBOMPMMD.builder().material(mPMMD).componente(componente)
                .cantidad(cantidadPMMD).mermaPct(mermaPMMD).dependencia(depPMMD).opcional(false).build()));
    }

    @Transactional
    public Edicion quitarLinea(Long fichaIdPMMD, int indicePMMD) {
        return editar(fichaIdPMMD, fPMMD -> {
            fPMMD.getLineas().remove(indicePMMD);
            for (int iPMMD = 0; iPMMD < fPMMD.getLineas().size(); iPMMD++) fPMMD.getLineas().get(iPMMD).setOrden(iPMMD);
        });
    }

    @Transactional
    public Edicion editarHoras(Long fichaIdPMMD, int indicePMMD, double horasPMMD) {
        if (horasPMMD < 0) throw new NegocioExceptionPMMD("Las horas no pueden ser negativas.");
        return editar(fichaIdPMMD, fPMMD -> fPMMD.getOperaciones().get(indicePMMD).setHoras(horasPMMD));
    }

    @Transactional
    public Edicion editarIndirectos(Long fichaIdPMMD, double pctPMMD) {
        if (pctPMMD < 0 || pctPMMD > 100) throw new NegocioExceptionPMMD("Los indirectos deben estar entre 0 y 100 %.");
        return editar(fichaIdPMMD, fPMMD -> fPMMD.setIndirectosPct(pctPMMD));
    }



    @Transactional
    public FichaTecnicaPMMD duplicar(Long fichaIdPMMD) {
        return repositoryPMMD.save(copiarPMMD(ficha(fichaIdPMMD)));
    }



    @Transactional
    public FichaTecnicaPMMD aprobar(Long fichaIdPMMD) {
        FichaTecnicaPMMD fPMMD = ficha(fichaIdPMMD);
        if (fPMMD.getEstado() == EstadoFichaPMMD.APROBADA) throw new NegocioExceptionPMMD("La ficha ya está aprobada.");
        if (!Boolean.TRUE.equals(fPMMD.getCompuesta()) && fPMMD.getLineas().isEmpty()) {
            throw new NegocioExceptionPMMD("La ficha no tiene materiales.");
        }
        for (FichaTecnicaPMMD otraPMMD : repositoryPMMD.findByCodigoOrderByVersionDesc(fPMMD.getCodigo())) {
            if (otraPMMD.getEstado() == EstadoFichaPMMD.APROBADA && !otraPMMD.getId().equals(fPMMD.getId())) {
                otraPMMD.setEstado(EstadoFichaPMMD.OBSOLETA);
                repositoryPMMD.save(otraPMMD);
            }
        }
        fPMMD.setEstado(EstadoFichaPMMD.APROBADA);
        fPMMD.setAprobadoPor(usuarioPMMD.nombre());
        fPMMD.setFechaAprobacion(AppClockPMMD.hoy());
        return repositoryPMMD.save(fPMMD);
    }

    @Transactional
    public FichaTecnicaPMMD marcarObsoleta(Long fichaIdPMMD) {
        FichaTecnicaPMMD fPMMD = ficha(fichaIdPMMD);
        if (fPMMD.getEstado() == EstadoFichaPMMD.OBSOLETA) throw new NegocioExceptionPMMD("La ficha ya está obsoleta.");
        if (!usos(fPMMD).isEmpty() && fPMMD.getEstado() == EstadoFichaPMMD.APROBADA) {

        }
        fPMMD.setEstado(EstadoFichaPMMD.OBSOLETA);
        return repositoryPMMD.save(fPMMD);
    }



    @Transactional
    public FichaTecnicaPMMD ajustarConFactores(FichaTecnicaPMMD base, Map<String, Double> factoresPMMD) {
        FichaTecnicaPMMD nueva = copiarPMMD(base);
        for (LineaBOMPMMD lPMMD : nueva.getLineas()) {
            Double facPMMD = factoresPMMD.get(lPMMD.getMaterial().getCodigo());
            if (facPMMD != null && facPMMD > 0) lPMMD.setCantidad(Math.round(lPMMD.getCantidad() * facPMMD * 1000) / 1000.0);
        }
        nueva.setNotas("Ajustada con el promedio real de consumo (" + AppClockPMMD.fmt(AppClockPMMD.hoy()) + ").");
        return repositoryPMMD.save(nueva);
    }



    @Transactional
    public FichaTecnicaPMMD crearPara(ProductoPMMD pPMMD, FichaTecnicaPMMD plantillaPMMD) {
        if (ultimaDe(pPMMD) != null) throw new NegocioExceptionPMMD("El modelo ya tiene ficha técnica.");
        String codigoPMMD = CodigosPMMD.siguiente(repositoryPMMD.findAll(), FichaTecnicaPMMD::getCodigo, "FT-", 3);
        FichaTecnicaPMMD fPMMD = FichaTecnicaPMMD.builder().codigo(codigoPMMD).version(1).uid(FichaTecnicaPMMD.uid(codigoPMMD, 1))
                .estado(EstadoFichaPMMD.BORRADOR).producto(pPMMD).nombre(pPMMD.getNombre())
                .largo(pPMMD.getLargo()).fondo(pPMMD.getFondo()).alto(pPMMD.getAlto()).cuerpos(pPMMD.getCuerpos())
                .tipoCorte(plantillaPMMD == null ? "Recto" : plantillaPMMD.getTipoCorte())
                .indirectosPct(plantillaPMMD == null ? 6d : plantillaPMMD.getIndirectosPct()).build();
        if (plantillaPMMD != null) {
            plantillaPMMD.getLineas().forEach(lPMMD -> fPMMD.agregarLinea(lPMMD.copia()));
            plantillaPMMD.getOperaciones().forEach(oPMMD -> fPMMD.agregarOperacion(oPMMD.copia()));
        } else {
            for (EtapaMOPMMD ePMMD : EtapaMOPMMD.values()) fPMMD.agregarOperacion(OperacionMOPMMD.builder().etapa(ePMMD).horas(1d).costoHora(ePMMD.costoHora()).build());
        }
        return repositoryPMMD.save(fPMMD);
    }

    private FichaTecnicaPMMD copiarPMMD(FichaTecnicaPMMD fPMMD) {
        int versionPMMD = repositoryPMMD.findByCodigoOrderByVersionDesc(fPMMD.getCodigo()).stream()
                .mapToInt(FichaTecnicaPMMD::getVersion).max().orElse(fPMMD.getVersion()) + 1;
        FichaTecnicaPMMD nPMMD = FichaTecnicaPMMD.builder()
                .codigo(fPMMD.getCodigo()).version(versionPMMD).uid(FichaTecnicaPMMD.uid(fPMMD.getCodigo(), versionPMMD))
                .estado(EstadoFichaPMMD.BORRADOR).producto(fPMMD.getProducto()).nombre(fPMMD.getNombre())
                .largo(fPMMD.getLargo()).fondo(fPMMD.getFondo()).alto(fPMMD.getAlto()).cuerpos(fPMMD.getCuerpos())
                .chaise(fPMMD.getChaise()).tipoCorte(fPMMD.getTipoCorte()).indirectosPct(fPMMD.getIndirectosPct())
                .notas(fPMMD.getNotas()).compuesta(fPMMD.getCompuesta()).hijos(new ArrayList<>(fPMMD.getHijos())).build();
        fPMMD.getLineas().forEach(lPMMD -> nPMMD.agregarLinea(lPMMD.copia()));
        fPMMD.getOperaciones().forEach(oPMMD -> nPMMD.agregarOperacion(oPMMD.copia()));
        return nPMMD;
    }
}
