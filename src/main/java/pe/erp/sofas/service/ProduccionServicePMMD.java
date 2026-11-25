package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;
import pe.erp.sofas.service.calculo.ExplosionPMMD;
import pe.erp.sofas.service.calculo.MedidasPMMD;
import pe.erp.sofas.service.calculo.Opciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;











@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProduccionServicePMMD {

    public static final double EPS = 1e-6;



    public record LineaMaterial(
      MaterialPMMD material, 
        String componentes, 
          double necesario, 
            double reservado,
                                
                                double disponible, 
                                  String estado, 
                                    double costo) {

        public double falta() {
            return Math.max(0, necesario - Math.max(reservado, "Disponible".equals(estado) ? necesario : reservado));
        }
    }

    public record EstadoMateriales(
      ExplosionPMMD.Resultado explosion, 
        List<LineaMaterial> lineas) {

        public long faltantes() {
            return lineas.stream().filter(lPMMD -> Set.of("Parcial", "Faltante", "Faltará", "Bajo pedido").contains(lPMMD.estado())).count();
        }

        public boolean completo() {
            return lineas.stream().allMatch(lPMMD -> "Reservado".equals(lPMMD.estado()) || "Consumido".equals(lPMMD.estado()));
        }
    }



    public record Indicador(
      String texto, 
        String variante, 
          String icono) {
    }

    public record Faltante(
      MaterialPMMD material, 
        double necesario, 
          double reservado) {

        public double falta() {
            return Math.max(0, necesario - reservado);
        }
    }

    public record ResultadoReserva(
      int reservados, 
        int lineas, 
          List<Faltante> faltantes) {

        public String texto() {
            return reservados + " materiales reservados" + (faltantes.isEmpty() ? "" : " · faltan " + faltantes.size());
        }
    }



    public enum Requisito { 
        NINGUNO, 
         AUTORIZACION, 
          CONFIRMAR_FALTANTES, 
           CONFIRMAR_CONSUMO, 
            LIBERAR }

    public record Evaluacion(
      Requisito requisito, 
        List<Faltante> faltantes) {
    }

    public record NuevaOP(
      Long productoId, 
        String nombrePersonalizado, 
          int cantidad, 
            Long clienteId, 
              Long responsableId,
                          
                          PrioridadPMMD prioridad, 
                            LocalDate inicio, 
                              LocalDate entrega, 
                                Long telaId, 
                                  MedidasPMMD medidas,
                          
                          Map<String, String> sustituciones, 
                             List<LineaExtraPMMD> extras, 
                               String pedidoCodigo,
                          
                          String retapizadoCodigo, 
                            String especificacion, 
                              EtapaPMMD etapa, 
                                BigDecimal costo,
                          
                          String fichaUid) {
    }

    public record Kpis(
      long activas, 
        long enTiempo, 
          long retrasadas, 
            long terminadasMes, 
              long sofasEnProceso, 
                long ordenesEnProceso) {
    }

    private final OrdenProduccionRepositoryPMMD repositoryPMMD;

    private final ReservaRepositoryPMMD reservaRepositoryPMMD;

    private final MaterialRepositoryPMMD materialRepositoryPMMD;

    private final ProductoRepositoryPMMD productoRepositoryPMMD;

    private final ClienteRepositoryPMMD clienteRepositoryPMMD;

    private final EmpleadoRepositoryPMMD empleadoRepositoryPMMD;

    private final TelaRepositoryPMMD telaRepositoryPMMD;

    private final PedidoRepositoryPMMD pedidoRepositoryPMMD;

    private final RetapizadoRepositoryPMMD retapizadoRepositoryPMMD;

    private final ConsumoRealRepositoryPMMD consumoRepositoryPMMD;

    private final FichaServicePMMD fichaServicePMMD;

    private final InventarioServicePMMD inventarioServicePMMD;

    private final TelaServicePMMD telaServicePMMD;



    public List<OrdenProduccionPMMD> ordenes() {
        return repositoryPMMD.findAllByOrderByCodigoAsc().stream().filter(oPMMD -> !oPMMD.getAnulada()).toList();
    }

    public List<OrdenProduccionPMMD> activas() {
        return ordenes().stream().filter(OrdenProduccionPMMD::isActiva).toList();
    }

    public OrdenProduccionPMMD op(Long idPMMD) {
        return repositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("La orden ya no existe."));
    }

    public Optional<OrdenProduccionPMMD> porCodigo(String codigoPMMD) {
        return repositoryPMMD.findByCodigo(codigoPMMD);
    }

    public String siguienteCodigo() {
        return CodigosPMMD.siguiente(repositoryPMMD.findAll(), OrdenProduccionPMMD::getCodigo, "OP-", 4);
    }

    public Kpis kpis() {
        List<OrdenProduccionPMMD> actPMMD = activas();
        long retr = actPMMD.stream().filter(OrdenProduccionPMMD::isRetrasada).count();
        LocalDate inicio = AppClockPMMD.hoy().withDayOfMonth(1);
        long term = ordenes().stream().filter(oPMMD -> oPMMD.getEtapa() == EtapaPMMD.LISTO && oPMMD.getFechaFin() != null
                && !oPMMD.getFechaFin().isBefore(inicio)).count();
        List<OrdenProduccionPMMD> procPMMD = actPMMD.stream().filter(oPMMD -> oPMMD.getEtapa().productiva()).toList();
        return new Kpis(actPMMD.size(), actPMMD.size() - retr, retr, term,
                procPMMD.stream().mapToLong(OrdenProduccionPMMD::getCantidad).sum(), procPMMD.size());
    }

    public Map<EtapaPMMD, Long> porEtapa() {
        Map<EtapaPMMD, Long> m = new EnumMap<>(EtapaPMMD.class);
        for (EtapaPMMD ePMMD : EtapaPMMD.values()) m.put(ePMMD, 0L);
        ordenes().stream().filter(oPMMD -> oPMMD.isActiva() || oPMMD.getEtapa() == EtapaPMMD.LISTO)
                .forEach(oPMMD -> m.merge(oPMMD.getEtapa(), (long) oPMMD.getCantidad(), Long::sum));
        return m;
    }

    public FichaTecnicaPMMD ficha(OrdenProduccionPMMD op) {
        return fichaServicePMMD.porUid(op.getFichaUid()).orElse(null);
    }

    public Opciones opciones(OrdenProduccionPMMD op) {
        FichaTecnicaPMMD f = ficha(op);
        MedidasPMMD m = null;
        if (op.getLargo() != null && op.getLargo() > 0 && f != null) {
            m = new MedidasPMMD(op.getLargo(), nzPMMD(op.getFondo(), f.getFondo()), nzPMMD(op.getAlto(), f.getAlto()),
                    op.getCuerpos() == null ? f.getCuerpos() : op.getCuerpos());
        }
        return new Opciones(m, op.getSustituciones(), op.getExtras(), false, 0, 0);
    }



    public ExplosionPMMD.Resultado requerimiento(OrdenProduccionPMMD op) {
        FichaTecnicaPMMD f = ficha(op);
        if (f == null) return null;
        return fichaServicePMMD.explotar(f, op.getCantidad(), opciones(op));
    }

    public double reservadoPor(OrdenProduccionPMMD op, MaterialPMMD m) {
        return reservaRepositoryPMMD.findByOpIdAndMaterialId(op.getId(), m.getId()).map(ReservaPMMD::getCantidad).orElse(0d);
    }

    public EstadoMateriales estadoMateriales(OrdenProduccionPMMD op) {
        ExplosionPMMD.Resultado r = requerimiento(op);
        if (r == null) return new EstadoMateriales(null, List.of());
        Map<String, Set<String>> componentes = new LinkedHashMap<>();
        for (ExplosionPMMD.Linea lPMMD : r.lineas()) {
            componentes.computeIfAbsent(lPMMD.material().getCodigo(), kPMMD -> new LinkedHashSet<>()).add(lPMMD.componente().nombre());
        }
        Map<Long, Double> reservasPMMD = new HashMap<>();
        reservaRepositoryPMMD.findByOpId(op.getId()).forEach(xPMMD -> reservasPMMD.put(xPMMD.getMaterial().getId(), xPMMD.getCantidad()));
        List<LineaMaterial> lineas = new ArrayList<>();
        for (ExplosionPMMD.PorMaterial pmPMMD : r.porMaterial()) {
            MaterialPMMD m = materialRepositoryPMMD.findById(pmPMMD.material().getId()).orElse(pmPMMD.material());
            double res = reservasPMMD.getOrDefault(m.getId(), 0d);
            double disp = Math.max(0, m.getStock() - (m.getReservado() - res));
            String estado;
            if (op.getEtapa() == EtapaPMMD.LISTO) estado = "Consumido";
            else if (res >= pmPMMD.cantidad() - EPS) estado = "Reservado";
            else if (Boolean.TRUE.equals(m.getBajoPedido()) && m.getStock() <= 0) estado = "Bajo pedido";
            else if (op.getEtapa() == EtapaPMMD.PENDIENTE) estado = disp >= pmPMMD.cantidad() - EPS ? "Disponible" : "Faltará";
            else estado = res > EPS ? "Parcial" : "Faltante";
            lineas.add(new LineaMaterial(m, String.join(", ", componentes.get(m.getCodigo())), pmPMMD.cantidad(), res, disp, estado, pmPMMD.costo()));
        }
        return new EstadoMateriales(r, lineas);
    }

    public Indicador indicador(OrdenProduccionPMMD op) {
        if (op.isSinFicha()) {
            return op.getAutorizacion() != null
                    ? new Indicador("Sin ficha · autorizado", "amber", "VERIFIED_USER")
                    : new Indicador("Sin ficha aprobada", "amber", "WARNING");
        }
        if (op.getEtapa() == EtapaPMMD.LISTO) return new Indicador("Materiales consumidos", "green", "DONE_ALL");
        EstadoMateriales ePMMD = estadoMateriales(op);
        long f = ePMMD.faltantes();
        if (op.getEtapa() == EtapaPMMD.PENDIENTE && !ePMMD.completo()) {
            return f == 0 ? new Indicador("Materiales disponibles", "blue", "ALL_INBOX")
                    : new Indicador(f == 1 ? "Faltará 1 material" : "Faltarán " + f + " materiales", "red", "REPORT_PROBLEM");
        }
        return f == 0 ? new Indicador("Materiales reservados", "green", "LOCK")
                : new Indicador("Faltan " + f + " material" + (f == 1 ? "" : "es"), "red", "REPORT_PROBLEM");
    }



    public List<Faltante> faltantesSiReserva(OrdenProduccionPMMD op) {
        ExplosionPMMD.Resultado r = requerimiento(op);
        if (r == null) return List.of();
        List<Faltante> f = new ArrayList<>();
        for (ExplosionPMMD.PorMaterial pmPMMD : r.porMaterial()) {
            MaterialPMMD m = materialRepositoryPMMD.findById(pmPMMD.material().getId()).orElse(pmPMMD.material());
            double res = reservadoPor(op, m);
            double disp = Math.max(0, m.getStock() - (m.getReservado() - res));
            double posible = Math.min(pmPMMD.cantidad(), disp);
            if (posible < pmPMMD.cantidad() - EPS) f.add(new Faltante(m, pmPMMD.cantidad(), posible));
        }
        return f;
    }

    public Evaluacion evaluar(OrdenProduccionPMMD op, EtapaPMMD destinoPMMD) {
        EtapaPMMD origenPMMD = op.getEtapa();
        if (destinoPMMD == origenPMMD) return new Evaluacion(Requisito.NINGUNO, List.of());
        if (origenPMMD == EtapaPMMD.LISTO) throw new NegocioExceptionPMMD("La orden " + op.getCodigo() + " ya está lista y su material fue consumido.");
        if (destinoPMMD == EtapaPMMD.LISTO) {
            if (origenPMMD == EtapaPMMD.PENDIENTE) throw new NegocioExceptionPMMD("Primero pasa la orden a una etapa de producción.");
            return new Evaluacion(Requisito.CONFIRMAR_CONSUMO, List.of());
        }
        if (destinoPMMD == EtapaPMMD.PENDIENTE) return new Evaluacion(Requisito.LIBERAR, List.of());
        if (origenPMMD == EtapaPMMD.PENDIENTE) {
            if (op.isSinFicha()) {
                return new Evaluacion(op.getAutorizacion() == null ? Requisito.AUTORIZACION : Requisito.NINGUNO, List.of());
            }
            List<Faltante> f = faltantesSiReserva(op);
            return new Evaluacion(f.isEmpty() ? Requisito.NINGUNO : Requisito.CONFIRMAR_FALTANTES, f);
        }
        return new Evaluacion(Requisito.NINGUNO, List.of());
    }



    @Transactional
    public OrdenProduccionPMMD crear(NuevaOP dPMMD) {
        if (dPMMD.cantidad() <= 0) throw new NegocioExceptionPMMD("La cantidad debe ser mayor a cero.");
        if (dPMMD.entrega() != null && dPMMD.inicio() != null && dPMMD.entrega().isBefore(dPMMD.inicio())) {
            throw new NegocioExceptionPMMD("La entrega no puede ser antes del inicio.");
        }
        ProductoPMMD pPMMD = dPMMD.productoId() == null ? null : productoRepositoryPMMD.findById(dPMMD.productoId()).orElseThrow();
        if (pPMMD == null && (dPMMD.nombrePersonalizado() == null || dPMMD.nombrePersonalizado().isBlank())) {
            throw new NegocioExceptionPMMD("Elige el modelo de sofá.");
        }
        TelaPMMD telaPMMD = dPMMD.telaId() == null ? null : telaRepositoryPMMD.findById(dPMMD.telaId()).orElseThrow();
        FichaTecnicaPMMD ficha = dPMMD.fichaUid() != null ? fichaServicePMMD.porUid(dPMMD.fichaUid()).orElse(null)
                : pPMMD == null ? null : fichaServicePMMD.aprobadaDe(pPMMD);
        Map<String, String> sustPMMD = new HashMap<>(dPMMD.sustituciones() == null ? Map.of() : dPMMD.sustituciones());
        if (telaPMMD != null && ficha != null) {
            for (String genericoPMMD : materialesTela(ficha)) sustPMMD.putIfAbsent(genericoPMMD, telaPMMD.getTipo().materialCodigo());
        }
        OrdenProduccionPMMD op = OrdenProduccionPMMD.builder()
                .codigo(siguienteCodigo()).producto(pPMMD).nombrePersonalizado(dPMMD.nombrePersonalizado())
                .cantidad(dPMMD.cantidad())
                .cliente(dPMMD.clienteId() == null ? null : clienteRepositoryPMMD.findById(dPMMD.clienteId()).orElse(null))
                .responsable(dPMMD.responsableId() == null ? null : empleadoRepositoryPMMD.findById(dPMMD.responsableId()).orElse(null))
                .prioridad(dPMMD.prioridad() == null ? PrioridadPMMD.MEDIA : dPMMD.prioridad())
                .etapa(dPMMD.etapa() == null ? EtapaPMMD.PENDIENTE : dPMMD.etapa())
                .fechaInicio(dPMMD.inicio() == null ? AppClockPMMD.hoy() : dPMMD.inicio()).fechaEntrega(dPMMD.entrega())
                .fichaUid(ficha == null ? null : ficha.getUid()).tela(telaPMMD)
                .largo(dPMMD.medidas() == null ? null : (int) Math.round(dPMMD.medidas().largo()))
                .fondo(dPMMD.medidas() == null ? null : (int) Math.round(dPMMD.medidas().fondo()))
                .alto(dPMMD.medidas() == null ? null : (int) Math.round(dPMMD.medidas().alto()))
                .cuerpos(dPMMD.medidas() == null ? null : dPMMD.medidas().cuerpos())
                .sustituciones(sustPMMD).extras(new ArrayList<>(dPMMD.extras() == null ? List.of() : dPMMD.extras()))
                .pedidoCodigo(dPMMD.pedidoCodigo()).retapizadoCodigo(dPMMD.retapizadoCodigo())
                .especificacion(dPMMD.especificacion()).build();
        op = repositoryPMMD.save(op);
        op.setCosto(dPMMD.costo() != null ? dPMMD.costo() : costoCalculado(op));
        return repositoryPMMD.save(op);
    }



    public Set<String> materialesTela(FichaTecnicaPMMD f) {
        Set<String> sPMMD = new HashSet<>();
        List<FichaTecnicaPMMD> fichasPMMD = Boolean.TRUE.equals(f.getCompuesta()) ? fichaServicePMMD.hijas(f) : List.of(f);
        for (FichaTecnicaPMMD xPMMD : fichasPMMD) {
            for (LineaBOMPMMD lPMMD : xPMMD.getLineas()) if (lPMMD.getMaterial().isTela()) sPMMD.add(lPMMD.getMaterial().getCodigo());
        }
        return sPMMD;
    }

    public BigDecimal costoCalculado(OrdenProduccionPMMD op) {
        ExplosionPMMD.Resultado r = requerimiento(op);
        if (r != null) return BigDecimal.valueOf(r.costoEstandar()).setScale(2, RoundingMode.HALF_UP);
        if (op.getProducto() != null && op.getProducto().getCosto() != null) {
            return op.getProducto().getCosto().multiply(BigDecimal.valueOf(op.getCantidad()));
        }
        return BigDecimal.ZERO;
    }



    @Transactional
    public ResultadoReserva reservar(Long opIdPMMD) {
        OrdenProduccionPMMD op = op(opIdPMMD);
        ExplosionPMMD.Resultado r = requerimiento(op);
        if (r == null) throw new NegocioExceptionPMMD("La orden no tiene ficha aprobada: no se puede reservar material.");
        if (op.getEtapa() == EtapaPMMD.LISTO) throw new NegocioExceptionPMMD("La orden ya consumió su material.");
        int reservados = 0;
        List<Faltante> faltantes = new ArrayList<>();
        List<ExplosionPMMD.PorMaterial> reqPMMD = r.porMaterial();
        Set<Long> requeridosPMMD = new HashSet<>();
        for (ExplosionPMMD.PorMaterial pmPMMD : reqPMMD) {
            MaterialPMMD m = materialRepositoryPMMD.findById(pmPMMD.material().getId()).orElseThrow();
            requeridosPMMD.add(m.getId());
            ReservaPMMD res = reservaRepositoryPMMD.findByOpIdAndMaterialId(op.getId(), m.getId()).orElse(null);
            double actualPMMD = res == null ? 0 : res.getCantidad();
            double disponible = Math.max(0, m.getStock() - (m.getReservado() - actualPMMD));
            double nuevo = Math.min(pmPMMD.cantidad(), disponible);
            double deltaPMMD = nuevo - actualPMMD;
            if (Math.abs(deltaPMMD) > EPS) {
                m.setReservado(Math.max(0, m.getReservado() + deltaPMMD));
                materialRepositoryPMMD.save(m);
                if (res == null) res = ReservaPMMD.builder().op(op).material(m).cantidad(0d).build();
                res.setCantidad(nuevo);
                if (nuevo <= EPS) reservaRepositoryPMMD.delete(res);
                else reservaRepositoryPMMD.save(res);
                inventarioServicePMMD.registrar(m, deltaPMMD > 0 ? TipoMovimientoPMMD.RESERVA : TipoMovimientoPMMD.LIBERACION,
                        Math.abs(deltaPMMD), op.getCodigo(), (deltaPMMD > 0 ? "Reserva " : "Ajuste de reserva ") + op.getProductoNombre());
            }
            if (nuevo >= pmPMMD.cantidad() - EPS) reservados++;
            else faltantes.add(new Faltante(m, pmPMMD.cantidad(), nuevo));
        }

        for (ReservaPMMD xPMMD : reservaRepositoryPMMD.findByOpId(op.getId())) {
            if (!requeridosPMMD.contains(xPMMD.getMaterial().getId())) liberarReservaPMMD(op, xPMMD);
        }
        return new ResultadoReserva(reservados, reqPMMD.size(), faltantes);
    }

    @Transactional
    public int liberar(Long opIdPMMD) {
        OrdenProduccionPMMD op = op(opIdPMMD);
        List<ReservaPMMD> reservasPMMD = reservaRepositoryPMMD.findByOpId(op.getId());
        for (ReservaPMMD r : reservasPMMD) liberarReservaPMMD(op, r);
        return reservasPMMD.size();
    }

    private void liberarReservaPMMD(OrdenProduccionPMMD op, ReservaPMMD r) {
        MaterialPMMD m = materialRepositoryPMMD.findById(r.getMaterial().getId()).orElseThrow();
        m.setReservado(Math.max(0, m.getReservado() - r.getCantidad()));
        materialRepositoryPMMD.save(m);
        inventarioServicePMMD.registrar(m, TipoMovimientoPMMD.LIBERACION, r.getCantidad(), op.getCodigo(), "Liberación " + op.getProductoNombre());
        reservaRepositoryPMMD.delete(r);
    }






    @Transactional
    public String mover(Long opIdPMMD, EtapaPMMD destinoPMMD, String autorizacionPMMD) {
        OrdenProduccionPMMD op = op(opIdPMMD);
        EtapaPMMD origenPMMD = op.getEtapa();
        if (destinoPMMD == origenPMMD) return null;
        if (destinoPMMD == EtapaPMMD.LISTO) throw new NegocioExceptionPMMD("Confirma el consumo para pasar a Listo.");
        if (origenPMMD == EtapaPMMD.LISTO) throw new NegocioExceptionPMMD("La orden ya está lista.");
        String mensajePMMD;
        if (destinoPMMD == EtapaPMMD.PENDIENTE) {
            int nPMMD = liberar(opIdPMMD);
            mensajePMMD = op.getCodigo() + " volvió a Pendiente" + (nPMMD > 0 ? " · " + nPMMD + " reservas liberadas" : "");
        } else if (origenPMMD == EtapaPMMD.PENDIENTE) {
            if (op.isSinFicha()) {
                if (op.getAutorizacion() == null) {
                    if (autorizacionPMMD == null || autorizacionPMMD.isBlank()) throw new NegocioExceptionPMMD("Se requiere autorización de Supervisor.");
                    op.setAutorizacion(autorizacionPMMD);
                }
                mensajePMMD = op.getCodigo() + " pasó a " + destinoPMMD.nombre() + " sin reservar (sin ficha · autorizado)";
            } else {
                ResultadoReserva rrPMMD = reservar(opIdPMMD);
                mensajePMMD = op.getCodigo() + " pasó a " + destinoPMMD.nombre() + " · " + rrPMMD.texto();
            }
            if (op.getFechaInicio() == null || op.getFechaInicio().isAfter(AppClockPMMD.hoy())) op.setFechaInicio(AppClockPMMD.hoy());
        } else {
            mensajePMMD = op.getCodigo() + " pasó a " + destinoPMMD.nombre();
        }
        op = op(opIdPMMD);
        op.setEtapa(destinoPMMD);
        if (op.getAutorizacion() == null && autorizacionPMMD != null && op.isSinFicha()) op.setAutorizacion(autorizacionPMMD);
        repositoryPMMD.save(op);
        actualizarPedidoPMMD(op);
        return mensajePMMD;
    }






    @Transactional
    public String completar(Long opIdPMMD, Map<Long, Double> realesPMMD) {
        OrdenProduccionPMMD op = op(opIdPMMD);
        if (op.getEtapa() == EtapaPMMD.LISTO) throw new NegocioExceptionPMMD("La orden ya está lista.");
        if (op.getEtapa() == EtapaPMMD.PENDIENTE) throw new NegocioExceptionPMMD("Primero pasa la orden a una etapa de producción.");
        ExplosionPMMD.Resultado r = requerimiento(op);
        int lineas = 0;
        if (r != null) {
            Map<Long, Double> estimadosPMMD = new HashMap<>();
            for (ExplosionPMMD.PorMaterial pmPMMD : r.porMaterial()) estimadosPMMD.put(pmPMMD.material().getId(), pmPMMD.cantidad());
            Map<Long, ReservaPMMD> reservasPMMD = new HashMap<>();
            reservaRepositoryPMMD.findByOpId(op.getId()).forEach(xPMMD -> reservasPMMD.put(xPMMD.getMaterial().getId(), xPMMD));
            for (var ePMMD : estimadosPMMD.entrySet()) {
                MaterialPMMD m = materialRepositoryPMMD.findById(ePMMD.getKey()).orElseThrow();
                ReservaPMMD res = reservasPMMD.get(m.getId());
                double reservado = res == null ? 0 : res.getCantidad();
                double realPMMD = realesPMMD != null && realesPMMD.containsKey(m.getId()) ? realesPMMD.get(m.getId()) : reservado;
                double librePMMD = Math.max(0, m.getStock() - m.getReservado());
                realPMMD = Math.max(0, Math.min(realPMMD, reservado + librePMMD));
                m.setReservado(Math.max(0, m.getReservado() - reservado));
                materialRepositoryPMMD.save(m);
                if (res != null) reservaRepositoryPMMD.delete(res);
                if (realPMMD > EPS) {
                    inventarioServicePMMD.registrar(m, TipoMovimientoPMMD.CONSUMO_OP, realPMMD, op.getCodigo(), "Consumo " + op.getProductoNombre());
                    descontarTelaPMMD(op, m, realPMMD);
                }
                consumoRepositoryPMMD.save(ConsumoRealPMMD.builder().opCodigo(op.getCodigo()).producto(op.getProductoNombre())
                        .fichaUid(op.getFichaUid()).responsable(op.getResponsable() == null ? "—" : op.getResponsable().getNombre())
                        .material(m).estimado(ePMMD.getValue()).realConsumido(realPMMD).fecha(AppClockPMMD.hoy()).build());
                lineas++;
            }
        }
        op = op(opIdPMMD);
        op.setEtapa(EtapaPMMD.LISTO);
        op.setFechaFin(AppClockPMMD.hoy());
        repositoryPMMD.save(op);
        actualizarPedidoPMMD(op);
        return op.getCodigo() + " lista" + (lineas > 0 ? " · consumo de " + lineas + " materiales registrado" : "");
    }

    private void descontarTelaPMMD(OrdenProduccionPMMD op, MaterialPMMD m, double metrosPMMD) {
        TipoTelaPMMD tipoPMMD = TipoTelaPMMD.porMaterial(m.getCodigo());
        if (tipoPMMD == null) return;
        TelaPMMD tPMMD = op.getTela() != null && op.getTela().getTipo() == tipoPMMD ? op.getTela()
                : telaServicePMMD.porTipo(tipoPMMD).stream().filter(xPMMD -> !xPMMD.getDescontinuada())
                .max(Comparator.comparingDouble(TelaPMMD::getStock)).orElse(null);
        if (tPMMD != null) telaServicePMMD.descontar(tPMMD, metrosPMMD);
    }

    @Transactional
    public String anular(Long opIdPMMD) {
        OrdenProduccionPMMD op = op(opIdPMMD);
        if (op.getEtapa() == EtapaPMMD.LISTO) throw new NegocioExceptionPMMD("No se puede anular una orden ya lista.");
        int nPMMD = liberar(opIdPMMD);
        op = op(opIdPMMD);
        op.setAnulada(true);
        repositoryPMMD.save(op);
        if (op.getPedidoCodigo() != null) {
            pedidoRepositoryPMMD.findByCodigo(op.getPedidoCodigo()).ifPresent(pPMMD -> {
                pPMMD.setOpCodigo(null);
                if (pPMMD.getEstado() == EstadoPedidoPMMD.EN_PRODUCCION) pPMMD.setEstado(EstadoPedidoPMMD.CONFIRMADO);
                pedidoRepositoryPMMD.save(pPMMD);
            });
        }
        return op.getCodigo() + " anulada" + (nPMMD > 0 ? " · " + nPMMD + " reservas liberadas" : "");
    }

    @Transactional
    public OrdenProduccionPMMD autorizar(Long opIdPMMD, String texto) {
        OrdenProduccionPMMD op = op(opIdPMMD);
        op.setAutorizacion(texto);
        return repositoryPMMD.save(op);
    }

    @Transactional
    public OrdenProduccionPMMD actualizarDatos(Long opIdPMMD, Long responsableId, PrioridadPMMD prioridad, LocalDate entrega) {
        OrdenProduccionPMMD op = op(opIdPMMD);
        if (responsableId != null) op.setResponsable(empleadoRepositoryPMMD.findById(responsableId).orElse(null));
        if (prioridad != null) op.setPrioridad(prioridad);
        if (entrega != null) op.setFechaEntrega(entrega);
        return repositoryPMMD.save(op);
    }



    private void actualizarPedidoPMMD(OrdenProduccionPMMD op) {
        if (op.getPedidoCodigo() != null) {
            pedidoRepositoryPMMD.findByCodigo(op.getPedidoCodigo()).ifPresent(pPMMD -> {
                if (op.getEtapa() == EtapaPMMD.LISTO && pPMMD.getEstado() == EstadoPedidoPMMD.EN_PRODUCCION) pPMMD.setEstado(EstadoPedidoPMMD.LISTO);
                else if (op.getEtapa().productiva() && pPMMD.getEstado() == EstadoPedidoPMMD.CONFIRMADO) pPMMD.setEstado(EstadoPedidoPMMD.EN_PRODUCCION);
                pedidoRepositoryPMMD.save(pPMMD);
            });
        }
        if (op.getRetapizadoCodigo() != null && op.getEtapa() == EtapaPMMD.LISTO) {
            retapizadoRepositoryPMMD.findByCodigo(op.getRetapizadoCodigo()).ifPresent(rtPMMD -> {
                if (rtPMMD.getEstado() == EstadoRetapizadoPMMD.EN_TALLER) {
                    rtPMMD.setEstado(EstadoRetapizadoPMMD.LISTO);
                    retapizadoRepositoryPMMD.save(rtPMMD);
                }
            });
        }
    }

    private static double nzPMMD(Integer vPMMD, Integer defPMMD) {
        return vPMMD != null && vPMMD > 0 ? vPMMD : defPMMD == null ? 0 : defPMMD;
    }
}
