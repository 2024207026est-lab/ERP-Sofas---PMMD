package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.model.ConfigSofaPMMD.*;
import pe.erp.sofas.service.calculo.ExplosionPMMD;
import pe.erp.sofas.service.calculo.FormulaPMMD;
import pe.erp.sofas.service.calculo.MedidasPMMD;
import pe.erp.sofas.service.calculo.Opciones;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;







@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguradorServicePMMD {

    public static final double FACTOR_PRECIO = 1.55;

    public static final String TELA_GENERICA = "MP-003";

    public static final String D23 = "MP-019";

    public static final String PATA_MADERA = "MP-021";

    public static final String CINCHA = "MP-009";

    public static final String MADERA = "MP-001";

    public static final String GUATA = "MP-011";

    public static final String FIBRA = "MP-013";

    public static final String BOTON = "MP-016";

    public static final String RECLINABLE = "MP-030";

    public static final String CAMA = "MP-031";

    public static final String COLCHONETA = "MP-032";

    public static final String ELECTRICO = "MP-036";



    public record Linea(
      MaterialPMMD material, 
        String componente, 
          double cantidad, 
            double costo, 
              String estado) {
    }

    public record Cotizacion(
      ConfigSofaPMMD config, 
        FichaTecnicaPMMD ficha, 
          ExplosionPMMD.Resultado explosion, 
            List<Linea> lineas,
                             
                             double costoMateriales, 
                               double horasMO, 
                                 double costoMO, 
                                   double indirectos, 
                                     double costo,
                             
                             double precio, 
                               int dias, 
                                 boolean bajoPedido, 
                                   boolean critico, 
                                     TelaPMMD tela) {

        public double margenPct() {
            return precio == 0 ? 0 : (precio - costo) * 100 / precio;
        }



        public String referencia() {
            return ficha == null ? "sin ficha aprobada" : "ficha " + ficha.getEtiqueta();
        }
    }

    private final FichaServicePMMD fichaServicePMMD;

    private final CatalogoServicePMMD catalogoPMMD;

    private final TelaServicePMMD telaServicePMMD;

    private final ProductoServicePMMD productoServicePMMD;

    private final PedidoServicePMMD pedidoServicePMMD;

    private final ProduccionServicePMMD produccionServicePMMD;



    public ConfigSofaPMMD defecto(Modelo m) {
        return switch (m) {
            case MODERNO -> new ConfigSofaPMMD(m, 3, Chaise.NINGUNA, 92, tela("TL-01"), Brazo.RECTO, Patas.MADERA,
                    AcabadoMadera.NATURAL, Espuma.D23, 2, tela("TL-07"), Mecanismo.FIJO);
            case SECCIONAL_L -> new ConfigSofaPMMD(m, 3, Chaise.DERECHA, 92, tela("TL-09"), Brazo.RECTO, Patas.METAL_NEGRO,
                    AcabadoMadera.NATURAL, Espuma.D23, 4, tela("TL-10"), Mecanismo.FIJO);
            case SILLON -> new ConfigSofaPMMD(m, 1, Chaise.NINGUNA, 92, tela("TL-15"), Brazo.REDONDEADO, Patas.MADERA,
                    AcabadoMadera.NOGAL, Espuma.D23, 0, tela("TL-15"), Mecanismo.FIJO);
            case CHESTERFIELD -> new ConfigSofaPMMD(m, 3, Chaise.NINGUNA, 92, tela("TL-13"), Brazo.REDONDEADO, Patas.MADERA,
                    AcabadoMadera.WENGUE, Espuma.D23, 0, tela("TL-13"), Mecanismo.FIJO);
            case SOFA_CAMA -> new ConfigSofaPMMD(m, 3, Chaise.NINGUNA, 92, tela("TL-21"), Brazo.RECTO, Patas.MADERA,
                    AcabadoMadera.NOGAL, Espuma.D23, 2, tela("TL-20"), Mecanismo.CAMA);
            case ESCANDINAVO -> new ConfigSofaPMMD(m, 2, Chaise.NINGUNA, 92, tela("TL-18"), Brazo.DELGADO, Patas.MADERA,
                    AcabadoMadera.NATURAL, Espuma.D23, 2, tela("TL-19"), Mecanismo.FIJO);
        };
    }

    private Long tela(String codigoPMMD) {
        return telaServicePMMD.porCodigo(codigoPMMD).map(TelaPMMD::getId).orElse(null);
    }

    private final Map<Modelo, BigDecimal> desdeCachePMMD = new java.util.concurrent.ConcurrentHashMap<>();



    public BigDecimal desde(Modelo m) {
        return desdeCachePMMD.computeIfAbsent(m, xPMMD -> {
            try {
                ConfigSofaPMMD c = defecto(xPMMD);
                ConfigSofaPMMD minimoPMMD = c.conCojines(0, null).conCuerpos(xPMMD == Modelo.SECCIONAL_L ? 2 : xPMMD == Modelo.SILLON ? 1 : Math.max(1, c.cuerpos() - 1));
                return BigDecimal.valueOf(cotizar(minimoPMMD).precio());
            } catch (RuntimeException ePMMD) {
                return productoServicePMMD.porCodigo(xPMMD.producto()).map(ProductoPMMD::getPrecio).orElse(BigDecimal.ZERO);
            }
        });
    }

    public static String fichaBase(int cuerposPMMD) {
        return cuerposPMMD <= 1 ? "FT-003" : cuerposPMMD == 2 ? "FT-002" : "FT-001";
    }

    public Cotizacion cotizar(ConfigSofaPMMD c) {
        FichaTecnicaPMMD ficha = fichaServicePMMD.aprobada(fichaBase(c.cuerpos()));
        if (ficha == null) throw new NegocioExceptionPMMD("No hay ficha aprobada " + fichaBase(c.cuerpos()) + " para cotizar.");
        TelaPMMD tela = c.telaId() == null ? null : telaServicePMMD.tela(c.telaId());
        TelaPMMD telaCojinesPMMD = c.telaCojinesId() == null ? tela : telaServicePMMD.tela(c.telaCojinesId());
        String telaMat = tela == null ? TELA_GENERICA : tela.getTipo().materialCodigo();
        String espumaMat = c.espuma().material() == null ? D23 : c.espuma().material();

        Map<String, String> sust = new HashMap<>();
        sust.put(TELA_GENERICA, telaMat);
        if (c.espuma().material() != null) sust.put(D23, c.espuma().material());
        if (c.patas() == Patas.ZOCALO) sust.put(PATA_MADERA, ExplosionPMMD.QUITAR);
        else if (c.patas().material() != null) sust.put(PATA_MADERA, c.patas().material());
        if (c.mecanismo() == Mecanismo.CAMA) sust.put(CINCHA, ExplosionPMMD.QUITAR);

        List<LineaExtraPMMD> extras = new ArrayList<>();
        double recargoMO = 0, moFija = 0;
        if (c.conChaise()) {
            extras.add(new LineaExtraPMMD(MADERA, ComponentePMMD.ESTRUCTURA.name(), 10d));
            extras.add(new LineaExtraPMMD(espumaMat, ComponentePMMD.ASIENTO.name(), 0.6));
            extras.add(new LineaExtraPMMD(telaMat, ComponentePMMD.TAPIZADO.name(), 3.5));
            if (c.mecanismo() != Mecanismo.CAMA) extras.add(new LineaExtraPMMD(CINCHA, ComponentePMMD.ASIENTO.name(), 8d));
            extras.add(new LineaExtraPMMD(GUATA, ComponentePMMD.TAPIZADO.name(), 2d));
            recargoMO += 25;
        }
        if (c.cojines() > 0) {
            String telaCoj = telaCojinesPMMD == null ? telaMat : telaCojinesPMMD.getTipo().materialCodigo();
            extras.add(new LineaExtraPMMD(telaCoj, ComponentePMMD.COJINES.name(), 0.6 * c.cojines()));
            extras.add(new LineaExtraPMMD(FIBRA, ComponentePMMD.COJINES.name(), 0.3 * c.cojines()));
        }
        switch (c.mecanismo()) {
            case RECLINABLE_MANUAL -> extras.add(new LineaExtraPMMD(RECLINABLE, ComponentePMMD.MECANISMO.name(), (double) c.cuerpos()));
            case RECLINABLE_ELECTRICO -> extras.add(new LineaExtraPMMD(ELECTRICO, ComponentePMMD.MECANISMO.name(), (double) c.cuerpos()));
            case CAMA -> {
                extras.add(new LineaExtraPMMD(CAMA, ComponentePMMD.MECANISMO.name(), 1d));
                extras.add(new LineaExtraPMMD(COLCHONETA, ComponentePMMD.MECANISMO.name(), 1d));
            }
            default -> {
            }
        }
        if (c.mecanismo() != Mecanismo.FIJO) moFija += 40;
        if (c.modelo() == Modelo.CHESTERFIELD) {
            extras.add(new LineaExtraPMMD(BOTON, ComponentePMMD.TAPIZADO.name(), 48d));
            extras.add(new LineaExtraPMMD(telaMat, ComponentePMMD.TAPIZADO.name(), 2.2));
            moFija += 54;
        }
        if (c.patas() == Patas.ZOCALO) extras.add(new LineaExtraPMMD("MP-035", ComponentePMMD.PATAS.name(), 1d));

        MedidasPMMD medidas = new MedidasPMMD(c.largo(), c.profundidad(), c.alto(), c.cuerpos());
        Opciones opPMMD = new Opciones(medidas, sust, extras, false, recargoMO, moFija);
        ExplosionPMMD.Resultado r = fichaServicePMMD.explotar(ficha, 1, opPMMD);

        List<Linea> lineas = new ArrayList<>();
        boolean bajoPedido = c.patas().bajoPedido() || c.espuma().bajoPedido() || c.mecanismo().bajoPedido();
        boolean critico = false;
        Map<String, String> compPorMatPMMD = new HashMap<>();
        for (ExplosionPMMD.Linea lPMMD : r.lineas()) compPorMatPMMD.merge(lPMMD.material().getCodigo(), lPMMD.componente().nombre(), (aPMMD, bPMMD) -> aPMMD.contains(bPMMD) ? aPMMD : aPMMD + ", " + bPMMD);
        for (ExplosionPMMD.PorMaterial pmPMMD : r.porMaterial()) {
            MaterialPMMD m = pmPMMD.material();
            String estado;
            if (Boolean.TRUE.equals(m.getBajoPedido()) && m.getStock() <= 0) {
                estado = "Bajo pedido";
                bajoPedido = true;
            } else if (m.isTela() && tela != null && m.getCodigo().equals(tela.getTipo().materialCodigo())) {
                double metrosColorPMMD = tela.getStock();
                estado = metrosColorPMMD >= pmPMMD.cantidad() ? "Suficiente" : metrosColorPMMD > 0 ? "Parcial" : "Sin stock";
            } else {
                double dispPMMD = m.getDisponible();
                estado = dispPMMD >= pmPMMD.cantidad() ? "Suficiente" : dispPMMD > 0 ? "Parcial" : "Sin stock";
            }
            if (estado.equals("Parcial") || estado.equals("Sin stock")) critico = true;
            lineas.add(new Linea(m, compPorMatPMMD.getOrDefault(m.getCodigo(), ""), pmPMMD.cantidad(), pmPMMD.costo(), estado));
        }
        double costo = r.costoEstandar();
        double precio = FormulaPMMD.redondearDecena(costo * FACTOR_PRECIO);
        int dias = (int) Math.ceil(r.horasMO() / 8.0 + 3) + (bajoPedido ? 4 : 0) + (critico ? 5 : 0);
        return new Cotizacion(c, ficha, r, lineas, r.costoMateriales(), r.horasMO(), r.costoMO(), r.indirectos(), costo,
                precio, dias, bajoPedido, critico, tela);
    }



    public String especificacion(ConfigSofaPMMD c) {
        TelaPMMD tPMMD = c.telaId() == null ? null : telaServicePMMD.tela(c.telaId());
        TelaPMMD tcPMMD = c.telaCojinesId() == null ? null : telaServicePMMD.tela(c.telaCojinesId());
        StringBuilder sbPMMD = new StringBuilder();
        sbPMMD.append(c.modelo().nombre()).append(" · ").append(c.cuerpos()).append(c.cuerpos() == 1 ? " cuerpo" : " cuerpos");
        if (c.conChaise()) sbPMMD.append(" · chaise ").append(c.chaise().nombre().toLowerCase(Locale.ROOT));
        sbPMMD.append(" · ").append(c.largo()).append(" × ").append(c.conChaise() ? c.profundidad() + 65 : c.profundidad()).append(" × ").append(c.alto()).append(" cm");
        sbPMMD.append(" · ").append(tPMMD == null ? "tela según ficha" : tPMMD.getNombre());
        sbPMMD.append(" · brazo ").append(c.brazo().nombre().toLowerCase(Locale.ROOT));
        sbPMMD.append(" · patas ").append(c.patas().nombre().toLowerCase(Locale.ROOT));
        if (c.patas() == Patas.MADERA) sbPMMD.append(" (").append(c.acabado().nombre().toLowerCase(Locale.ROOT)).append(")");
        sbPMMD.append(" · ").append(c.espuma().nombre());
        if (c.cojines() > 0) sbPMMD.append(" · ").append(c.cojines()).append(" cojines").append(tcPMMD == null ? "" : " " + tcPMMD.getColor());
        if (c.mecanismo() != Mecanismo.FIJO) sbPMMD.append(" · ").append(c.mecanismo().nombre().toLowerCase(Locale.ROOT));
        return sbPMMD.toString();
    }



    public ProductoPMMD productoPara(ConfigSofaPMMD c) {
        String codigoPMMD = switch (c.modelo()) {
            case MODERNO -> c.cuerpos() <= 1 ? "SF-003" : c.cuerpos() == 2 ? "SF-002" : "SF-001";
            default -> c.modelo().producto();
        };
        return productoServicePMMD.porCodigo(codigoPMMD).orElseThrow();
    }



    public ProduccionServicePMMD.NuevaOP nuevaOP(ConfigSofaPMMD c, Cotizacion cotPMMD, Long clienteId, Long responsableId, LocalDate entrega,
                                             String pedidoCodigo) {
        ProductoPMMD pPMMD = productoPara(c);
        Opciones opPMMD = opciones(c);
        Map<String, String> sust = new HashMap<>(opPMMD.sust());
        return new ProduccionServicePMMD.NuevaOP(pPMMD.getId(), c.modelo().nombre() + " " + c.cuerpos() + (c.cuerpos() == 1 ? " cuerpo" : " cuerpos") + " · a medida",
                1, clienteId, responsableId, PrioridadPMMD.MEDIA, AppClockPMMD.hoy(), entrega, c.telaId(),
                new MedidasPMMD(c.largo(), c.profundidad(), c.alto(), c.cuerpos()), sust, opPMMD.ext(), pedidoCodigo, null,
                especificacion(c), EtapaPMMD.PENDIENTE, BigDecimal.valueOf(cotPMMD.costo()).setScale(2, java.math.RoundingMode.HALF_UP),
                cotPMMD.ficha().getUid());
    }



    public Opciones opciones(ConfigSofaPMMD c) {
        Cotizacion cotPMMD = cotizar(c);
        ExplosionPMMD.Resultado r = cotPMMD.explosion();
        Map<String, String> sust = new HashMap<>();
        List<LineaExtraPMMD> extras = new ArrayList<>();
        for (ExplosionPMMD.Linea lPMMD : r.lineas()) {
            if (lPMMD.extra()) extras.add(new LineaExtraPMMD(lPMMD.material().getCodigo(), lPMMD.componente().name(), lPMMD.base()));
        }
        TelaPMMD tela = cotPMMD.tela();
        sust.put(TELA_GENERICA, tela == null ? TELA_GENERICA : tela.getTipo().materialCodigo());
        if (c.espuma().material() != null) sust.put(D23, c.espuma().material());
        if (c.patas() == Patas.ZOCALO) sust.put(PATA_MADERA, ExplosionPMMD.QUITAR);
        else if (c.patas().material() != null) sust.put(PATA_MADERA, c.patas().material());
        if (c.mecanismo() == Mecanismo.CAMA) sust.put(CINCHA, ExplosionPMMD.QUITAR);
        double recargo = c.conChaise() ? 25 : 0;
        double fija = (c.mecanismo() != Mecanismo.FIJO ? 40 : 0) + (c.modelo() == Modelo.CHESTERFIELD ? 54 : 0);
        return new Opciones(new MedidasPMMD(c.largo(), c.profundidad(), c.alto(), c.cuerpos()), sust, extras, false, recargo, fija);
    }



    @Transactional
    public PedidoPMMD crearPedido(ConfigSofaPMMD c, Long clienteId, LocalDate entrega, BigDecimal adelanto, String metodo,
                              String observaciones, boolean crearOPPMMD, Long responsableId) {
        if (clienteId == null) throw new NegocioExceptionPMMD("Elige el cliente.");
        Cotizacion cotPMMD = cotizar(c);
        ProductoPMMD pPMMD = productoPara(c);
        PedidoPMMD pedPMMD = pedidoServicePMMD.crear(new PedidoServicePMMD.NuevoPedido(clienteId, pPMMD.getId(), PedidoPMMD.Tipo.PERSONALIZADO, 1,
                c.largo(), c.conChaise() ? c.profundidad() + 65 : c.profundidad(), c.alto(), c.telaId(),
                entrega == null ? AppClockPMMD.hoy().plusDays(cotPMMD.dias()) : entrega,
                BigDecimal.valueOf(cotPMMD.precio()), adelanto, metodo, observaciones, c.serializar(), especificacion(c)));
        if (crearOPPMMD) {
            if (adelanto != null && adelanto.compareTo(pedPMMD.getPrecio().multiply(new BigDecimal("0.5"))) >= 0) {
                pedPMMD = pedidoServicePMMD.avanzarA(pedPMMD.getId(), EstadoPedidoPMMD.CONFIRMADO);
            }
            OrdenProduccionPMMD opPMMD = produccionServicePMMD.crear(nuevaOP(c, cotPMMD, clienteId, responsableId, pedPMMD.getFechaEntrega(), pedPMMD.getCodigo()));
            pedPMMD = pedidoServicePMMD.vincularOP(pedPMMD.getId(), opPMMD.getCodigo());
        }
        return pedPMMD;
    }
}
