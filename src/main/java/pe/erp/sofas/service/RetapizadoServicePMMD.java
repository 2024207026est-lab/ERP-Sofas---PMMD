package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;
import pe.erp.sofas.service.calculo.ExplosionPMMD;
import pe.erp.sofas.service.calculo.FormulaPMMD;
import pe.erp.sofas.service.calculo.MedidasPMMD;
import pe.erp.sofas.service.calculo.Opciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;








@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetapizadoServicePMMD {

    public static final double COSTO_HORA = 9;

    public record LineaCot(
      String concepto, 
        MaterialPMMD material, 
          double cantidad, 
            double costo) {
    }

    public record Cotizacion(
      List<LineaCot> lineas, 
        double metrosTelaPMMD, 
          double horas, 
            double costoMO, 
              double costo,
                             
                             double precio, 
                               String aviso, 
                                 FichaTecnicaPMMD ficha) {

        public double costoMateriales() {
            return lineas.stream().mapToDouble(LineaCot::costo).sum();
        }
    }

    public record Kpis(
      long activos, 
        long enTaller, 
          long recojos, 
            BigDecimal ingresosMes) {
    }

    private final RetapizadoRepositoryPMMD repositoryPMMD;

    private final ClienteRepositoryPMMD clienteRepositoryPMMD;

    private final TelaRepositoryPMMD telaRepositoryPMMD;

    private final EmpleadoRepositoryPMMD empleadoRepositoryPMMD;

    private final PagoRepositoryPMMD pagoRepositoryPMMD;

    private final FichaServicePMMD fichaServicePMMD;

    private final ProduccionServicePMMD produccionServicePMMD;

    public List<RetapizadoPMMD> listar() {
        return repositoryPMMD.findAllByOrderByCodigoDesc();
    }

    public RetapizadoPMMD retapizado(Long idPMMD) {
        return repositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("El retapizado ya no existe."));
    }

    public Kpis kpis() {
        List<RetapizadoPMMD> lPMMD = listar();
        LocalDate inicioPMMD = AppClockPMMD.hoy().withDayOfMonth(1);
        BigDecimal ingresos = lPMMD.stream().filter(rPMMD -> rPMMD.getEstado() == EstadoRetapizadoPMMD.ENTREGADO && rPMMD.getFechaEntrega() != null
                && !rPMMD.getFechaEntrega().isBefore(inicioPMMD) && rPMMD.getTotal() != null).map(RetapizadoPMMD::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Kpis(lPMMD.stream().filter(rPMMD -> rPMMD.getEstado() != EstadoRetapizadoPMMD.ENTREGADO).count(),
                lPMMD.stream().filter(rPMMD -> rPMMD.getEstado() == EstadoRetapizadoPMMD.EN_TALLER).count(),
                lPMMD.stream().filter(rPMMD -> rPMMD.getEstado() == EstadoRetapizadoPMMD.RECOJO_PROGRAMADO).count(), ingresos);
    }

    public static String fichaPorCuerpos(int cuerpos) {
        return cuerpos <= 1 ? "FT-003" : cuerpos == 2 ? "FT-002" : "FT-001";
    }

    public Cotizacion cotizar(int cuerpos, double largoPMMD, double fondoPMMD, double altoPMMD, String estructuraPMMD, String espumaPMMD, TelaPMMD telaPMMD) {
        FichaTecnicaPMMD f = fichaServicePMMD.aprobada(fichaPorCuerpos(cuerpos));
        if (f == null) throw new NegocioExceptionPMMD("No hay ficha aprobada para cotizar.");
        Map<String, String> sust = new HashMap<>();
        if (telaPMMD != null) {
            for (LineaBOMPMMD lPMMD : f.getLineas()) if (lPMMD.getMaterial().isTela()) sust.put(lPMMD.getMaterial().getCodigo(), telaPMMD.getTipo().materialCodigo());
        }
        MedidasPMMD m = new MedidasPMMD(largoPMMD <= 0 ? f.getLargo() : largoPMMD, fondoPMMD <= 0 ? f.getFondo() : fondoPMMD, altoPMMD <= 0 ? f.getAlto() : altoPMMD, cuerpos);
        ExplosionPMMD.Resultado rPMMD = fichaServicePMMD.explotar(f, 1, new Opciones(m, sust, List.of(), false, 0, 0));
        double factorEspumaPMMD = switch (espumaPMMD == null ? "Buena" : espumaPMMD) {
            case "Hundida" -> 0.5;
            case "Cambio total" -> 1;
            default -> 0;
        };
        double factorMaderaPMMD = switch (estructuraPMMD == null ? "Buena" : estructuraPMMD) {
            case "Regular" -> 0.1;
            case "Requiere refuerzo" -> 0.3;
            default -> 0;
        };
        List<LineaCot> lineas = new ArrayList<>();
        double metros = 0;
        Map<String, double[]> accPMMD = new LinkedHashMap<>();
        Map<String, MaterialPMMD> matsPMMD = new HashMap<>();
        Map<String, String> conceptosPMMD = new HashMap<>();
        for (ExplosionPMMD.Linea lPMMD : rPMMD.lineas()) {
            MaterialPMMD matPMMD = lPMMD.material();
            String catPMMD = matPMMD.getCategoria();
            double factorPMMD;
            String concepto;
            if (matPMMD.isTela()) {
                factorPMMD = 1;
                concepto = "Tela nueva";
            } else if (catPMMD.equals("Espuma")) {
                factorPMMD = factorEspumaPMMD;
                concepto = "Espuma (" + (espumaPMMD == null ? "Buena" : espumaPMMD.toLowerCase(Locale.ROOT)) + ")";
            } else if (catPMMD.equals("Madera") || catPMMD.equals("Tableros")) {
                factorPMMD = factorMaderaPMMD;
                concepto = "Refuerzo de estructura";
            } else if (lPMMD.componente() == ComponentePMMD.TAPIZADO || lPMMD.componente() == ComponentePMMD.COJINES || catPMMD.equals("Insumos")) {
                factorPMMD = 1;
                concepto = "Guata, notex e insumos";
            } else {
                continue;
            }
            if (factorPMMD == 0) continue;
            double cantPMMD = lPMMD.bruta() * factorPMMD;
            if (matPMMD.isEntera()) cantPMMD = Math.ceil(cantPMMD - 1e-9);
            if (matPMMD.isTela()) metros += cantPMMD;
            double[] aPMMD = accPMMD.computeIfAbsent(matPMMD.getCodigo(), kPMMD -> new double[2]);
            aPMMD[0] += cantPMMD;
            aPMMD[1] += cantPMMD * matPMMD.getCosto();
            matsPMMD.put(matPMMD.getCodigo(), matPMMD);
            conceptosPMMD.put(matPMMD.getCodigo(), concepto);
        }
        accPMMD.forEach((codigoPMMD, aPMMD) -> lineas.add(new LineaCot(conceptosPMMD.get(codigoPMMD), matsPMMD.get(codigoPMMD), aPMMD[0], aPMMD[1])));
        double horasTapizadoPMMD = horas(f, EtapaMOPMMD.TAPIZADO) + horas(f, EtapaMOPMMD.COSTURA);
        double horas = (horasTapizadoPMMD * 1.2 + horas(f, EtapaMOPMMD.ACABADO)) * FormulaPMMD.factorManoObra(m, MedidasPMMD.de(f));
        double mo = horas * COSTO_HORA;
        double costo = lineas.stream().mapToDouble(LineaCot::costo).sum() + mo;
        double precio = FormulaPMMD.redondearDecena(costo * 1.06 * 1.45);
        String aviso = null;
        if (telaPMMD != null && telaPMMD.getStock() < metros) {
            aviso = "La tela " + telaPMMD.getNombre() + " no alcanza: hay " + InventarioServicePMMD.fmt(telaPMMD.getStock()) + " m y se necesitan "
                    + InventarioServicePMMD.fmt(FormulaPMMD.r2(metros)) + " m.";
        }
        return new Cotizacion(lineas, metros, horas, mo, costo, precio, aviso, f);
    }

    private static double horas(FichaTecnicaPMMD f, EtapaMOPMMD ePMMD) {
        return f.getOperaciones().stream().filter(oPMMD -> oPMMD.getEtapa() == ePMMD).mapToDouble(OperacionMOPMMD::getHoras).sum();
    }

    public Cotizacion cotizar(RetapizadoPMMD rPMMD) {
        return cotizar(rPMMD.getCuerpos(), nzPMMD(rPMMD.getLargo()), nzPMMD(rPMMD.getFondo()), nzPMMD(rPMMD.getAlto()), rPMMD.getEstructura(), rPMMD.getEspuma(), rPMMD.getTela());
    }

    @Transactional
    public RetapizadoPMMD crear(Long clienteIdPMMD, int cuerpos, String descripcionPMMD, Integer largoPMMD, Integer fondoPMMD, Integer altoPMMD,
                            String estructuraPMMD, String espumaPMMD, LocalDate recojoPMMD, Long telaIdPMMD, String fotoPMMD) {
        if (clienteIdPMMD == null) throw new NegocioExceptionPMMD("Elige el cliente.");
        if (descripcionPMMD == null || descripcionPMMD.isBlank()) throw new NegocioExceptionPMMD("Describe el sofá del cliente.");
        if (cuerpos < 1 || cuerpos > 4) throw new NegocioExceptionPMMD("Los cuerpos deben estar entre 1 y 4.");
        TelaPMMD telaPMMD = telaIdPMMD == null ? null : telaRepositoryPMMD.findById(telaIdPMMD).orElseThrow();
        RetapizadoPMMD rPMMD = RetapizadoPMMD.builder()
                .codigo(CodigosPMMD.siguiente(repositoryPMMD.findAll(), RetapizadoPMMD::getCodigo, "RT-", 3))
                .cliente(clienteRepositoryPMMD.findById(clienteIdPMMD).orElseThrow()).descripcion(descripcionPMMD.trim())
                .cuerpos(cuerpos).largo(largoPMMD).fondo(fondoPMMD).alto(altoPMMD).estructura(estructuraPMMD).espuma(espumaPMMD)
                .tela(telaPMMD).fechaRegistro(AppClockPMMD.hoy()).fechaRecojo(recojoPMMD).foto(fotoPMMD)
                .estado(telaPMMD == null ? EstadoRetapizadoPMMD.POR_COTIZAR : EstadoRetapizadoPMMD.COTIZADO).build();
        if (telaPMMD != null) {
            Cotizacion cPMMD = cotizar(rPMMD);
            rPMMD.setMetros(FormulaPMMD.r2(cPMMD.metrosTelaPMMD()));
            rPMMD.setTotal(BigDecimal.valueOf(cPMMD.precio()).setScale(2, RoundingMode.HALF_UP));
        }
        return repositoryPMMD.save(rPMMD);
    }



    @Transactional
    public String avanzar(Long idPMMD) {
        RetapizadoPMMD rPMMD = retapizado(idPMMD);
        EstadoRetapizadoPMMD sigPMMD = rPMMD.getEstado().siguiente();
        switch (sigPMMD) {
            case COTIZADO -> {
                if (rPMMD.getTela() == null) throw new NegocioExceptionPMMD("Elige la tela nueva para cotizar.");
                Cotizacion cPMMD = cotizar(rPMMD);
                rPMMD.setMetros(FormulaPMMD.r2(cPMMD.metrosTelaPMMD()));
                rPMMD.setTotal(BigDecimal.valueOf(cPMMD.precio()).setScale(2, RoundingMode.HALF_UP));
            }
            case RECOJO_PROGRAMADO -> {
                if (rPMMD.getFechaRecojo() == null || rPMMD.getFechaRecojo().isBefore(AppClockPMMD.hoy())) rPMMD.setFechaRecojo(AppClockPMMD.hoy().plusDays(2));
            }
            case EN_TALLER -> {
                EmpleadoPMMD costureraPMMD = empleadoRepositoryPMMD.findAll().stream()
                        .filter(ePMMD -> ePMMD.getEtapa() == EtapaPMMD.CORTE_TELA || ePMMD.getEtapa() == EtapaPMMD.COSTURA).findFirst().orElse(null);
                OrdenProduccionPMMD opPMMD = produccionServicePMMD.crear(new ProduccionServicePMMD.NuevaOP(null,
                        "Retapizado · " + rPMMD.getDescripcion(), 1, rPMMD.getCliente().getId(), costureraPMMD == null ? null : costureraPMMD.getId(),
                        PrioridadPMMD.MEDIA, AppClockPMMD.hoy(), AppClockPMMD.hoy().plusDays(6), rPMMD.getTela() == null ? null : rPMMD.getTela().getId(),
                        null, Map.of(), List.of(), null, rPMMD.getCodigo(), "Retapizado " + rPMMD.getMedidas(), EtapaPMMD.CORTE_TELA,
                        rPMMD.getTotal() == null ? null : rPMMD.getTotal().divide(new BigDecimal("1.537"), 2, RoundingMode.HALF_UP), null));
                produccionServicePMMD.autorizar(opPMMD.getId(), "Retapizado " + rPMMD.getCodigo() + " · no requiere ficha");
                rPMMD.setOpCodigo(opPMMD.getCodigo());
                rPMMD.setFechaEntrega(AppClockPMMD.hoy().plusDays(6));
            }
            case ENTREGADO -> {
                rPMMD.setFechaEntrega(AppClockPMMD.hoy());
                if (rPMMD.getTotal() != null) {
                    pagoRepositoryPMMD.save(PagoPMMD.builder().cliente(rPMMD.getCliente()).referencia(rPMMD.getCodigo()).fecha(AppClockPMMD.hoy())
                            .monto(rPMMD.getTotal()).metodo("Efectivo").concepto("Retapizado").build());
                }
            }
            default -> {
            }
        }
        if (sigPMMD == rPMMD.getEstado()) throw new NegocioExceptionPMMD("El retapizado ya fue entregado.");
        rPMMD.setEstado(sigPMMD);
        repositoryPMMD.save(rPMMD);
        return rPMMD.getCodigo() + " → " + sigPMMD.nombre() + (sigPMMD == EstadoRetapizadoPMMD.EN_TALLER ? " · " + rPMMD.getOpCodigo() + " creada en Corte de tela" : "");
    }

    @Transactional
    public RetapizadoPMMD fijarFoto(Long idPMMD, String rutaPMMD) {
        RetapizadoPMMD rPMMD = retapizado(idPMMD);
        rPMMD.setFoto(rutaPMMD);
        return repositoryPMMD.save(rPMMD);
    }

    private static double nzPMMD(Integer vPMMD) {
        return vPMMD == null ? 0 : vPMMD;
    }
}
