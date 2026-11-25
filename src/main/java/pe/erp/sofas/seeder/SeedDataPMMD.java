package pe.erp.sofas.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.service.calculo.ExplosionPMMD;
import pe.erp.sofas.service.calculo.FormulaPMMD;
import pe.erp.sofas.service.calculo.MedidasPMMD;
import pe.erp.sofas.service.calculo.Opciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;











@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class SeedDataPMMD implements ApplicationRunner {

    private static final LocalDate HOYPMMD = AppClockPMMD.HOY;

    private final EmpresaRepositoryPMMD empresaRepositoryPMMD;

    private final ParametroRepositoryPMMD parametroRepositoryPMMD;

    private final UsuarioRepositoryPMMD usuarioRepositoryPMMD;

    private final PermisoRepositoryPMMD permisoRepositoryPMMD;

    private final ProveedorRepositoryPMMD proveedorRepositoryPMMD;

    private final MaterialRepositoryPMMD materialRepositoryPMMD;

    private final TelaRepositoryPMMD telaRepositoryPMMD;

    private final MovimientoRepositoryPMMD movimientoRepositoryPMMD;

    private final ProductoRepositoryPMMD productoRepositoryPMMD;

    private final FichaTecnicaRepositoryPMMD fichaRepositoryPMMD;

    private final ClienteRepositoryPMMD clienteRepositoryPMMD;

    private final EmpleadoRepositoryPMMD empleadoRepositoryPMMD;

    private final PedidoRepositoryPMMD pedidoRepositoryPMMD;

    private final PagoRepositoryPMMD pagoRepositoryPMMD;

    private final OrdenProduccionRepositoryPMMD opRepositoryPMMD;

    private final ReservaRepositoryPMMD reservaRepositoryPMMD;

    private final ConsumoRealRepositoryPMMD consumoRepositoryPMMD;

    private final VentaRepositoryPMMD ventaRepositoryPMMD;

    private final OrdenCompraRepositoryPMMD compraRepositoryPMMD;

    private final RetapizadoRepositoryPMMD retapizadoRepositoryPMMD;

    private final ReclamoGarantiaRepositoryPMMD reclamoRepositoryPMMD;

    private final GastoRepositoryPMMD gastoRepositoryPMMD;

    private final ResumenMensualRepositoryPMMD resumenRepositoryPMMD;

    private final CatalogoServicePMMD catalogoPMMD;

    private final FichaServicePMMD fichaServicePMMD;

    private final ProduccionServicePMMD produccionServicePMMD;

    private final RetapizadoServicePMMD retapizadoServicePMMD;

    @Value("${app.seeder.enabled:true}")
    private boolean habilitado;

    private final Map<String, ProveedorPMMD> provPMMD = new HashMap<>();

    private final Map<String, MaterialPMMD> matPMMD = new HashMap<>();

    private final Map<String, TelaPMMD> tel = new HashMap<>();

    private final Map<String, ProductoPMMD> prodPMMD = new HashMap<>();

    private final Map<String, ClientePMMD> cliPMMD = new HashMap<>();

    private final Map<String, EmpleadoPMMD> empPMMD = new HashMap<>();

    private final Map<String, OrdenProduccionPMMD> opsPMMD = new LinkedHashMap<>();


    private final List<MovimientoPMMD> movimientosPMMD = new ArrayList<>();

    @Override
    @Transactional
    public void run(ApplicationArguments argsPMMD) {
        if (!habilitado || productoRepositoryPMMD.count() > 0) return;
        log.info("Cargando datos semilla de SISTEMA ERP SOFAS Sofás…");
        empresaPMMD();
        usuariosPMMD();
        proveedores();
        materialesPMMD();
        telas();
        productosPMMD();
        fichas();
        clientes();
        empleadosPMMD();
        pedidos();
        ordenesPMMD();
        retapizadosPMMD();
        reclamosPMMD();
        consumosPMMD();
        compras();
        ventas();
        finanzas();
        stocksFinalesPMMD();
        log.info("Datos semilla cargados.");
    }



    private void empresaPMMD() {
        empresaRepositoryPMMD.save(EmpresaPMMD.builder().id(1L).razonSocial("SISTEMA ERP SOFAS S.A.C.").nombreComercial("SISTEMA ERP SOFAS Sofás")
                .ruc("20601458732").direccion("Jr. Mariano Núñez 418, Juliaca").ciudad("Juliaca, San Román, Puno")
                .telefono("051 32 1478 · 951 204 118").email("ventas@innovasur.pe").web("www.innovasur.pe").build());
        ParametroServicePMMD.MERMAS.forEach((k, vPMMD) -> paramPMMD("merma." + k, vPMMD));
        for (EtapaMOPMMD e : EtapaMOPMMD.values()) paramPMMD("costoHora." + e.name(), e.costoHora());
        paramPMMD(ParametroServicePMMD.INDIRECTOS, 6);
        paramPMMD(ParametroServicePMMD.MARGEN_OBJETIVO, 35);
        ParametroServicePMMD.DESTAJO.forEach((k, vPMMD) -> paramPMMD("destajo." + k.name(), vPMMD));
    }

    private void paramPMMD(String k, double vPMMD) {
        parametroRepositoryPMMD.save(new ParametroPMMD(k, String.valueOf(vPMMD)));
    }

    private void usuariosPMMD() {
        String hashPMMD = AuthServicePMMD.hash("innova2026");
        usuario("cmendoza", "Carlos Mendoza", "Gerente General", RolPMMD.GERENTE, hashPMMD);
        usuario("mcahuana", "Milagros Cahuana Ponce", "Administradora", RolPMMD.ADMINISTRADOR, hashPMMD);
        usuario("rvilca", "Roxana Vilca Mendoza", "Vendedora", RolPMMD.VENDEDOR, hashPMMD);
        usuario("rquispe", "Rubén Quispe Flores", "Supervisor de taller", RolPMMD.SUPERVISOR, hashPMMD);
        usuario("fchoque", "Fredy Choque Mamani", "Carpintero", RolPMMD.CARPINTERO, hashPMMD);
        usuario("yccori", "Yolanda Ccori Huamán", "Costurera", RolPMMD.COSTURERA, hashPMMD);
        usuario("emamani", "Edwin Mamani Ticona", "Tapicero", RolPMMD.TAPICERO, hashPMMD);
        usuario("larocutipa", "Luis Arocutipa Ramos", "Almacenero", RolPMMD.ALMACENERO, hashPMMD);

        Map<RolPMMD, Set<String>> verPMMD = new EnumMap<>(RolPMMD.class);
        Map<RolPMMD, Set<String>> editarPMMD = new EnumMap<>(RolPMMD.class);
        Set<String> todosPMMD = Set.of("dashboard", "modelos", "telas", "configurador", "produccion", "fichas", "mrp", "corte", "consumo",
                "pedidos", "ventas", "clientes", "posventa", "inventario", "proveedores", "compras", "personal", "finanzas", "reportes", "configuracion");
        verPMMD.put(RolPMMD.ADMINISTRADOR, todosPMMD);
        editarPMMD.put(RolPMMD.ADMINISTRADOR, todosPMMD);
        verPMMD.put(RolPMMD.VENDEDOR, Set.of("dashboard", "modelos", "telas", "configurador", "pedidos", "ventas", "clientes", "posventa"));
        editarPMMD.put(RolPMMD.VENDEDOR, Set.of("configurador", "pedidos", "ventas", "clientes", "posventa"));
        verPMMD.put(RolPMMD.SUPERVISOR, Set.of("dashboard", "modelos", "telas", "configurador", "produccion", "fichas", "mrp", "corte", "consumo", "pedidos", "posventa", "inventario", "compras", "personal", "reportes"));
        editarPMMD.put(RolPMMD.SUPERVISOR, Set.of("produccion", "fichas", "mrp", "corte", "consumo", "posventa", "inventario"));
        Set<String> tallerPMMD = Set.of("dashboard", "produccion", "fichas", "corte");
        for (RolPMMD rPMMD : List.of(RolPMMD.CARPINTERO, RolPMMD.COSTURERA, RolPMMD.TAPICERO)) {
            verPMMD.put(rPMMD, tallerPMMD);
            editarPMMD.put(rPMMD, Set.of("produccion", "corte"));
        }
        verPMMD.put(RolPMMD.ALMACENERO, Set.of("dashboard", "telas", "inventario", "mrp", "compras", "proveedores", "produccion"));
        editarPMMD.put(RolPMMD.ALMACENERO, Set.of("telas", "inventario", "compras"));
        for (RolPMMD rPMMD : RolPMMD.values()) {
            if (rPMMD == RolPMMD.GERENTE) continue;
            for (String m : todosPMMD) {
                permisoRepositoryPMMD.save(PermisoPMMD.builder().rol(rPMMD).modulo(m)
                        .ver(verPMMD.getOrDefault(rPMMD, Set.of()).contains(m)).editar(editarPMMD.getOrDefault(rPMMD, Set.of()).contains(m)).build());
            }
        }
    }

    private void usuario(String uPMMD, String nombrePMMD, String cargoPMMD, RolPMMD rolPMMD, String hashPMMD) {
        usuarioRepositoryPMMD.save(UsuarioPMMD.builder().username(uPMMD).nombre(nombrePMMD).cargo(cargoPMMD).rol(rolPMMD)
                .email(uPMMD + "@innovasur.pe").passwordHash(hashPMMD).activo(true).build());
    }



    private void proveedores() {
        p("PR-01", "Maderera Altiplano E.I.R.L.", "20448123671", "Wilfredo Mamani", "951 332 410", "Av. Huancané 1820, Juliaca", "madera, triplay, patas de madera", "Crédito 15 días", "48250");
        p("PR-02", "Telas Andinas Import S.A.C.", "20512378945", "Patricia Rojas", "054 28 4471", "Calle Mercaderes 312, Arequipa", "lino, terciopelo, bouclé", "Crédito 30 días", "36420");
        p("PR-03", "Textiles Titicaca S.R.L.", "20406654128", "Jorge Quispe", "951 778 205", "Jr. Lima 640, Puno", "chenille, cuero sintético, microfibra, guata, notex, hilo, cierres, botones", "Crédito 30 días", "61880");
        p("PR-04", "Espumas Andinas S.A.C.", "20455987613", "Rocío Cutipa", "951 600 128", "Parque Industrial Taparachi Mz C, Juliaca", "espumas, cincha, fibra siliconada", "Contado", "44310");
        p("PR-05", "Ferretería Industrial Puno", "10418827364", "Hernán Apaza", "951 451 087", "Jr. Moquegua 210, Juliaca", "grapas, clavos, patas metálicas, embalaje", "Contado", "12890");
        p("PR-06", "Pinturas y Barnices del Sur", "20447719852", "Elena Choquehuanca", "951 239 664", "Av. El Sol 455, Puno", "barniz, tinte, cola PVA, pegamento de contacto", "Crédito 15 días", "9870");
        p("PR-07", "Mecanismos del Sur S.A.C.", "20539912455", "Raúl Paredes", "054 43 9902", "Av. Parra 287, Arequipa", "mecanismos reclinables y de sofá cama, colchonetas", "Crédito 30 días", "15620");
    }

    private void p(String codigoPMMD, String rsPMMD, String rucPMMD, String contactoPMMD, String tel, String dirPMMD, String rubroPMMD, String condPMMD, String total) {
        provPMMD.put(codigoPMMD, proveedorRepositoryPMMD.save(ProveedorPMMD.builder().codigo(codigoPMMD).razonSocial(rsPMMD).ruc(rucPMMD).contacto(contactoPMMD)
                .telefono(tel).email(contactoPMMD.split(" ")[0].toLowerCase(Locale.ROOT) + "@" + rsPMMD.split(" ")[0].toLowerCase(Locale.ROOT) + ".pe")
                .direccion(dirPMMD).rubro(rubroPMMD).condicionPago(condPMMD).activo(true).totalComprado(new BigDecimal(total)).build()));
    }



    private final Map<String, Double> disponibleFinalPMMD = new LinkedHashMap<>();

    private void materialesPMMD() {
        m("MP-001", "Madera tornillo cepillado", "Madera", "pt", 4.20, 400, "Almacén A · Rack 1", "PR-01", 1350);
        m("MP-002", "Triplay 4 mm (244 × 122)", "Tableros", "plancha", 38, 10, "Almacén A · Rack 2", "PR-01", 14);
        m("MP-003", "Tela chenille", "Tapicería", "m", TipoTelaPMMD.CHENILLE.costoMetro(), 0, "Almacén B · Telas", "PR-03", -1);
        m("MP-004", "Tela cuero sintético", "Tapicería", "m", TipoTelaPMMD.CUERO.costoMetro(), 0, "Almacén B · Telas", "PR-03", -1);
        m("MP-005", "Tela microfibra antimascotas", "Tapicería", "m", TipoTelaPMMD.MICROFIBRA.costoMetro(), 0, "Almacén B · Telas", "PR-03", -1);
        m("MP-006", "Tela lino", "Tapicería", "m", TipoTelaPMMD.LINO.costoMetro(), 0, "Almacén B · Telas", "PR-02", -1);
        m("MP-007", "Tela terciopelo", "Tapicería", "m", TipoTelaPMMD.TERCIOPELO.costoMetro(), 0, "Almacén B · Telas", "PR-02", -1);
        m("MP-008", "Tela bouclé", "Tapicería", "m", TipoTelaPMMD.BOUCLE.costoMetro(), 0, "Almacén B · Telas", "PR-02", -1);
        m("MP-009", "Cincha elástica 2\"", "Tapicería", "m", 2.0, 150, "Almacén B · Estante 3", "PR-04", 260);
        m("MP-010", "Resorte zigzag", "Tapicería", "unidad", 3.5, 60, "Almacén B · Estante 3", "PR-04", 140);
        m("MP-011", "Guata 150 g", "Tapicería", "m", 4.5, 60, "Almacén B · Estante 2", "PR-03", 120);
        m("MP-012", "Notex", "Tapicería", "m", 2.2, 40, "Almacén B · Estante 2", "PR-03", 95);
        m("MP-013", "Fibra siliconada", "Tapicería", "kg", 14, 10, "Almacén B · Estante 4", "PR-04", 1.5);
        m("MP-014", "Cierre 60 cm", "Tapicería", "unidad", 1.8, 40, "Almacén B · Estante 4", "PR-03", 110);
        m("MP-015", "Hilo de tapicería", "Tapicería", "unidad", 6, 12, "Almacén B · Estante 4", "PR-03", 34);
        m("MP-016", "Botón forrado", "Tapicería", "unidad", 0.6, 100, "Almacén B · Estante 4", "PR-03", 260);
        m("MP-017", "Espuma D18 2\"", "Espuma", "plancha", 45, 8, "Almacén C · Espumas", "PR-04", 16);
        m("MP-018", "Espuma D18 4\"", "Espuma", "plancha", 85, 8, "Almacén C · Espumas", "PR-04", 14);
        m("MP-019", "Espuma D23 5\"", "Espuma", "plancha", 120, 8, "Almacén C · Espumas", "PR-04", 15);
        m("MP-020", "Espuma D30 5\"", "Espuma", "plancha", 155, 4, "Almacén C · Espumas", "PR-04", 7);
        m("MP-021", "Pata de madera cónica", "Patas", "unidad", 9, 40, "Almacén A · Rack 4", "PR-01", 86);
        m("MP-022", "Pata metálica negra", "Patas", "unidad", 23, 16, "Almacén A · Rack 4", "PR-05", 30);
        m("MP-023", "Pata metálica dorada", "Patas", "unidad", 29, 12, "Almacén A · Rack 4", "PR-05", 18);
        m("MP-024", "Cola PVA", "Insumos", "galón", 45, 3, "Almacén A · Químicos", "PR-06", 6);
        m("MP-025", "Pegamento de contacto", "Insumos", "galón", 55, 3, "Almacén A · Químicos", "PR-06", 5.5);
        m("MP-026", "Grapas 80/10", "Insumos", "caja", 18, 6, "Almacén B · Estante 1", "PR-05", 14);
        m("MP-027", "Clavos 2\"", "Insumos", "kg", 8, 5, "Almacén A · Rack 3", "PR-05", 11);
        m("MP-028", "Tinte para madera", "Insumos", "galón", 60, 2, "Almacén A · Químicos", "PR-06", 3.4);
        m("MP-029", "Barniz para patas", "Acabados", "galón", 70, 2, "Almacén A · Químicos", "PR-06", 0);
        m("MP-030", "Mecanismo reclinable manual", "Mecanismos", "unidad", 180, 3, "Almacén C · Mecanismos", "PR-07", 6);
        m("MP-031", "Mecanismo sofá cama plegable", "Mecanismos", "unidad", 320, 2, "Almacén C · Mecanismos", "PR-07", 3);
        m("MP-032", "Colchoneta 12 cm", "Mecanismos", "unidad", 140, 2, "Almacén C · Mecanismos", "PR-07", 3);
        m("MP-033", "Embalaje (stretch + esquineros)", "Embalaje", "unidad", 25, 10, "Almacén A · Despacho", "PR-05", 24);
        MaterialPMMD viscoPMMD = m("MP-034", "Espuma viscoelástica 5\"", "Espuma", "plancha", 260, 0, "Bajo pedido", "PR-04", 0);
        MaterialPMMD zocaloPMMD = m("MP-035", "Zócalo oculto de madera", "Patas", "juego", 85, 0, "Bajo pedido", "PR-01", 0);
        MaterialPMMD motorPMMD = m("MP-036", "Mecanismo reclinable eléctrico", "Mecanismos", "unidad", 520, 0, "Bajo pedido", "PR-07", 0);
        for (MaterialPMMD bPMMD : List.of(viscoPMMD, zocaloPMMD, motorPMMD)) {
            bPMMD.setBajoPedido(true);
            materialRepositoryPMMD.save(bPMMD);
        }
    }

    private MaterialPMMD m(String codigoPMMD, String nombrePMMD, String catPMMD, String unidadPMMD, double costoPMMD, double minimoPMMD, String ubicPMMD,
                       String proveedorPMMD, double disponiblePMMD) {
        MaterialPMMD xPMMD = materialRepositoryPMMD.save(MaterialPMMD.builder().codigo(codigoPMMD).nombre(nombrePMMD).categoria(catPMMD).unidad(unidadPMMD)
                .costo(costoPMMD).minimo(minimoPMMD).ubicacion(ubicPMMD).proveedor(provPMMD.get(proveedorPMMD)).stock(0d).reservado(0d).build());
        matPMMD.put(codigoPMMD, xPMMD);
        if (disponiblePMMD >= 0) disponibleFinalPMMD.put(codigoPMMD, disponiblePMMD);
        return xPMMD;
    }



    private void telas() {
        Object[][] tPMMD = {
                {TipoTelaPMMD.CHENILLE, "Gris perla", "#B9B7B1", 14}, {TipoTelaPMMD.CHENILLE, "Grafito", "#4A4D52", 27},
                {TipoTelaPMMD.CHENILLE, "Arena", "#CDB89A", 11}, {TipoTelaPMMD.CHENILLE, "Azul petróleo", "#1F4E5F", 26},
                {TipoTelaPMMD.CHENILLE, "Verde oliva", "#6B7045", 9}, {TipoTelaPMMD.CHENILLE, "Terracota", "#B5603C", 9},
                {TipoTelaPMMD.CHENILLE, "Mostaza", "#C9972E", 6}, {TipoTelaPMMD.CHENILLE, "Marfil", "#ECE4D3", 8},
                {TipoTelaPMMD.LINO, "Natural", "#D8CBB3", 19}, {TipoTelaPMMD.LINO, "Piedra", "#A59D90", 9}, {TipoTelaPMMD.LINO, "Salvia", "#9AA88E", 0},
                {TipoTelaPMMD.CUERO, "Caoba", "#6E3524", 9}, {TipoTelaPMMD.CUERO, "Cognac", "#9A5B2E", 15}, {TipoTelaPMMD.CUERO, "Negro", "#1E1E20", 13},
                {TipoTelaPMMD.TERCIOPELO, "Esmeralda", "#1F5E4B", 9}, {TipoTelaPMMD.TERCIOPELO, "Azul noche", "#1E2A4F", 10},
                {TipoTelaPMMD.TERCIOPELO, "Burdeos", "#6B1F2E", 7},
                {TipoTelaPMMD.BOUCLE, "Crema", "#EFE8DA", 12}, {TipoTelaPMMD.BOUCLE, "Avena", "#D6C7AE", 7},
                {TipoTelaPMMD.MICROFIBRA, "Gris", "#8E9196", 24}, {TipoTelaPMMD.MICROFIBRA, "Beige", "#CBB89B", 33}};
        int n = 0;
        for (Object[] rPMMD : tPMMD) {
            n++;
            TipoTelaPMMD tipoPMMD = (TipoTelaPMMD) rPMMD[0];
            String codigoPMMD = String.format("TL-%02d", n);
            String colorPMMD = (String) rPMMD[1];
            double minimoPMMD = tipoPMMD == TipoTelaPMMD.TERCIOPELO || tipoPMMD == TipoTelaPMMD.BOUCLE ? 6 : 8;
            String prefijoPMMD = switch (tipoPMMD) {
                case CHENILLE -> "CH-210";
                case LINO -> "LN-88";
                case CUERO -> "CS-450";
                case TERCIOPELO -> "TV-320";
                case BOUCLE -> "BC-115";
                case MICROFIBRA -> "MF-PET";
            };
            tel.put(codigoPMMD, telaRepositoryPMMD.save(TelaPMMD.builder().codigo(codigoPMMD).tipo(tipoPMMD).color(colorPMMD).hex((String) rPMMD[2])
                    .codigoProveedor(prefijoPMMD + " " + colorPMMD.substring(0, Math.min(3, colorPMMD.length())).toUpperCase(Locale.ROOT))
                    .stock(((Integer) rPMMD[3]).doubleValue()).minimo(minimoPMMD).build()));
        }
    }



    private void productosPMMD() {
        prPMMD("SF-001", "Sofá Moderno 3 cuerpos", "Sofá", FormaPMMD.MODERNO, "Chenille", "Gris perla", "#B9B7B1", 210, 92, 85, 3, null, 2490, 4,
                "Sofá de líneas rectas con cojines de asiento en espuma D23 de 5\" y respaldo en D18. Estructura de tornillo cepillado y patas cónicas de madera.");
        prPMMD("SF-002", "Sofá Moderno 2 cuerpos", "Sofá", FormaPMMD.MODERNO, "Chenille", "Grafito", "#4A4D52", 160, 92, 85, 2, null, 1990, 3,
                "La versión de dos cuerpos del modelo más vendido, ideal para salas pequeñas y departamentos.");
        prPMMD("SF-003", "Sillón Moderno", "Sillón", FormaPMMD.SILLON, "Chenille", "Arena", "#CDB89A", 95, 90, 85, 1, null, 1190, 2,
                "Sillón individual que combina con la línea Moderno. Brazos rectos y asiento firme.");
        prPMMD("SF-004", "Juego de sala 3-2-1 Moderno", "Juego de sala", FormaPMMD.JUEGO, "Chenille", "Grafito", "#4A4D52", 210, 92, 85, 6, "3 + 2 + 1 cuerpos", 5490, 1,
                "Juego completo: sofá de 3 cuerpos, sofá de 2 cuerpos y sillón, en la misma tela.");
        prPMMD("SF-005", "Seccional L con chaise", "Seccional", FormaPMMD.SECCIONAL_L, "Lino", "Natural", "#D8CBB3", 280, 157, 85, 3, null, 5490, 1,
                "Seccional en L con chaise reversible de 157 cm de fondo. Perfecto para salas familiares.");
        prPMMD("SF-006", "Chesterfield 3 cuerpos", "Chesterfield", FormaPMMD.CHESTERFIELD, "Cuero sintético", "Cognac", "#9A5B2E", 215, 90, 78, 3, null, 3890, 2,
                "Clásico Chesterfield con capitoné de 48 botones, brazos enrollados y resortes zigzag.");
        prPMMD("SF-007", "Sofá cama 3 cuerpos", "Sofá cama", FormaPMMD.SOFA_CAMA, "Microfibra antimascotas", "Beige", "#CBB89B", 200, 95, 88, 3, null, 2890, 0,
                "Sofá que se convierte en cama de 1.40 m con mecanismo plegable y colchoneta de 12 cm.");
        prPMMD("SF-008", "Reclinable 3 cuerpos", "Reclinable", FormaPMMD.RECLINABLE, "Cuero sintético", "Negro", "#1E1E20", 205, 98, 100, 3, null, 4290, 1,
                "Tres cuerpos reclinables con mecanismo manual independiente por cuerpo.");
        prPMMD("SF-009", "Escandinavo 2 cuerpos", "Sofá", FormaPMMD.ESCANDINAVO, "Bouclé", "Crema", "#EFE8DA", 170, 85, 80, 2, null, 2190, 3,
                "Diseño nórdico en bouclé, brazos delgados y patas de madera natural.");
        prPMMD("SF-010", "Puff cubo", "Puff", FormaPMMD.PUFF, "Chenille", "Mostaza", "#C9972E", 45, 45, 42, 1, null, 260, 12,
                "Puff tapizado de 45 cm con zócalo negro. Combina con cualquier sala.");
        prPMMD("SF-011", "Seccional U modular", "Seccional", FormaPMMD.SECCIONAL_U, "Terciopelo", "Azul noche", "#1E2A4F", 320, 220, 85, 5, null, 7490, 0,
                "Seccional en U de cinco módulos para salas amplias, lobbies y restaurantes.");
    }

    private void prPMMD(String codigoPMMD, String nombrePMMD, String catPMMD, FormaPMMD formaPMMD, String material, String colorPMMD, String hexPMMD, int l, int fPMMD, int a,
                    int cuerposPMMD, String medidasPMMD, double precioPMMD, int stockPMMD, String descPMMD) {
        prodPMMD.put(codigoPMMD, productoRepositoryPMMD.save(ProductoPMMD.builder().codigo(codigoPMMD).nombre(nombrePMMD).categoria(catPMMD).forma(formaPMMD)
                .material(material).color(colorPMMD).colorHex(hexPMMD).largo(l).fondo(fPMMD).alto(a).cuerpos(cuerposPMMD).medidasTexto(medidasPMMD)
                .precio(BigDecimal.valueOf(precioPMMD).setScale(2, RoundingMode.HALF_UP)).costo(BigDecimal.valueOf(precioPMMD * 0.6).setScale(2, RoundingMode.HALF_UP))
                .stock(stockPMMD).descripcion(descPMMD).imagenes(imagenesPorDefectoPMMD(codigoPMMD)).activo(true).build()));
    }



    private String imagenesPorDefectoPMMD(String codigoPMMD) {
        return getClass().getResource("/images/sofas/" + codigoPMMD + ".jpg") != null ? "classpath:/images/sofas/" + codigoPMMD + ".jpg" : null;
    }





    private record T(
      String material, 
        ComponentePMMD comp, 
          double cant, 
            double merma, 
              DependenciaPMMD dep, 
                boolean opcional) {
    }

    private static final List<T> PLANTILLAPMMD = List.of(
            new T("MP-001", ComponentePMMD.ESTRUCTURA, 38, 18, DependenciaPMMD.L, false),
            new T("MP-002", ComponentePMMD.ESTRUCTURA, 1.2, 8, DependenciaPMMD.LF, false),
            new T("MP-009", ComponentePMMD.ASIENTO, 22, 10, DependenciaPMMD.L, false),
            new T("MP-019", ComponentePMMD.ASIENTO, 1.6, 12, DependenciaPMMD.LF, false),
            new T("MP-018", ComponentePMMD.RESPALDO, 1.2, 12, DependenciaPMMD.LA, false),
            new T("MP-017", ComponentePMMD.BRAZOS, 0.8, 12, DependenciaPMMD.F, false),
            new T("MP-003", ComponentePMMD.TAPIZADO, 9.5, 10, DependenciaPMMD.T, false),
            new T("MP-011", ComponentePMMD.TAPIZADO, 8, 10, DependenciaPMMD.T, false),
            new T("MP-012", ComponentePMMD.TAPIZADO, 4, 10, DependenciaPMMD.LF, false),
            new T("MP-015", ComponentePMMD.TAPIZADO, 2, 5, DependenciaPMMD.F, false),
            new T("MP-013", ComponentePMMD.COJINES, 2.5, 5, DependenciaPMMD.N, false),
            new T("MP-014", ComponentePMMD.COJINES, 6, 0, DependenciaPMMD.N, false),
            new T("MP-016", ComponentePMMD.COJINES, 6, 0, DependenciaPMMD.N, true),
            new T("MP-027", ComponentePMMD.ENSAMBLAJE, 0.6, 5, DependenciaPMMD.L, false),
            new T("MP-024", ComponentePMMD.ENSAMBLAJE, 0.25, 5, DependenciaPMMD.L, false),
            new T("MP-025", ComponentePMMD.ENSAMBLAJE, 0.4, 5, DependenciaPMMD.T, false),
            new T("MP-026", ComponentePMMD.ENSAMBLAJE, 0.8, 5, DependenciaPMMD.T, false),
            new T("MP-021", ComponentePMMD.PATAS, 6, 0, DependenciaPMMD.P, false),
            new T("MP-028", ComponentePMMD.ACABADO, 0.1, 5, DependenciaPMMD.F, false),
            new T("MP-029", ComponentePMMD.ACABADO, 0.12, 5, DependenciaPMMD.F, false),
            new T("MP-033", ComponentePMMD.EMBALAJE, 1, 0, DependenciaPMMD.F, false));

    private static final double[] HORAS_BASEPMMD = {3, 8, 6, 10, 2, 1};

    private void fichas() {
        MedidasPMMD basePMMD = new MedidasPMMD(210, 92, 85, 3);

        FichaTecnicaPMMD v2PMMD = ficha("FT-001", 2, EstadoFichaPMMD.OBSOLETA, "SF-001", "Sofá Moderno 3 cuerpos", 210, 92, 85, 3, false, "Recto",
                LocalDate.of(2026, 3, 16));
        agregarPlantillaPMMD(v2PMMD, basePMMD, basePMMD, Map.of("MP-003", 10.0, "MP-001", 40.0, "MP-019", 1.7), Set.of());
        operacionesPMMD(v2PMMD, new double[]{3, 8.5, 6, 10.5, 2, 1});
        fichaRepositoryPMMD.save(v2PMMD);
        FichaTecnicaPMMD v3PMMD = ficha("FT-001", 3, EstadoFichaPMMD.APROBADA, "SF-001", "Sofá Moderno 3 cuerpos", 210, 92, 85, 3, false, "Recto",
                LocalDate.of(2026, 8, 10));
        agregarPlantillaPMMD(v3PMMD, basePMMD, basePMMD, Map.of(), Set.of());
        operacionesPMMD(v3PMMD, HORAS_BASEPMMD);
        v3PMMD.setNotas("v3: se redujo la tela de 10 a 9.5 m con el nuevo plano de corte y la madera de 40 a 38 pt.");
        fichaRepositoryPMMD.save(v3PMMD);

        simplePMMD("FT-002", "SF-002", "Sofá Moderno 2 cuerpos", 160, 92, 85, 2, Map.of(), Set.of(), Map.of(), 0, LocalDate.of(2026, 8, 10));
        simplePMMD("FT-003", "SF-003", "Sillón Moderno", 95, 90, 85, 1, Map.of(), Set.of(), Map.of(), 0, LocalDate.of(2026, 8, 10));

        FichaTecnicaPMMD juegoPMMD = ficha("FT-004", 1, EstadoFichaPMMD.APROBADA, "SF-004", "Juego de sala 3-2-1 Moderno", 210, 92, 85, 6, false, "Recto",
                LocalDate.of(2026, 8, 12));
        juegoPMMD.setCompuesta(true);
        juegoPMMD.setHijos(new ArrayList<>(List.of("FT-001", "FT-002", "FT-003")));
        juegoPMMD.setNotas("Ficha compuesta: suma las fichas aprobadas del sofá de 3, el de 2 y el sillón.");
        fichaRepositoryPMMD.save(juegoPMMD);


        FichaTecnicaPMMD secPMMD = simplePMMD("FT-005", "SF-005", "Seccional L con chaise", 280, 157, 85, 3, Map.of("MP-003", "MP-006"), Set.of(),
                Map.of("MP-001", 10.0, "MP-019", 0.6, "MP-006", 3.5, "MP-009", 8.0, "MP-011", 2.0), 25, LocalDate.of(2026, 7, 2));
        secPMMD.setChaise(true);
        secPMMD.setTipoCorte("Seccional");
        fichaRepositoryPMMD.save(secPMMD);


        FichaTecnicaPMMD chPMMD = simplePMMD("FT-006", "SF-006", "Chesterfield 3 cuerpos", 215, 90, 78, 3, Map.of("MP-003", "MP-004"), Set.of("MP-016"),
                Map.of("MP-016", 48.0, "MP-010", 14.0), 0, LocalDate.of(2026, 6, 20));
        for (LineaBOMPMMD l : chPMMD.getLineas()) if (l.getMaterial().getCodigo().equals("MP-004")) l.setCantidad(r2PMMD(l.getCantidad() * 1.2));
        chPMMD.getOperaciones().stream().filter(oPMMD -> oPMMD.getEtapa() == EtapaMOPMMD.TAPIZADO).forEach(oPMMD -> oPMMD.setHoras(oPMMD.getHoras() + 6));
        chPMMD.setTipoCorte("Chesterfield");
        chPMMD.setNotas("Cuero ×1.2 por el capitoné; 48 botones forrados.");
        fichaRepositoryPMMD.save(chPMMD);


        FichaTecnicaPMMD camaPMMD = simplePMMD("FT-007", "SF-007", "Sofá cama 3 cuerpos", 200, 95, 88, 3, Map.of("MP-003", "MP-005"), Set.of("MP-009"),
                Map.of("MP-031", 1.0, "MP-032", 1.0), 0, LocalDate.of(2026, 5, 28));
        camaPMMD.getOperaciones().stream().filter(oPMMD -> oPMMD.getEtapa() == EtapaMOPMMD.CARPINTERIA).forEach(oPMMD -> oPMMD.setHoras(oPMMD.getHoras() + 2));
        camaPMMD.setNotas("Sin cincha: el asiento apoya en el mecanismo plegable.");
        fichaRepositoryPMMD.save(camaPMMD);


        FichaTecnicaPMMD recPMMD = simplePMMD("FT-008", "SF-008", "Reclinable 3 cuerpos", 205, 98, 100, 3, Map.of("MP-003", "MP-004"), Set.of(),
                Map.of(), 0, LocalDate.of(2026, 6, 5));
        recPMMD.agregarLinea(LineaBOMPMMD.builder().material(matPMMD.get("MP-030")).componente(ComponentePMMD.MECANISMO).cantidad(3d).mermaPct(0d)
                .dependencia(DependenciaPMMD.N).opcional(false).build());
        recPMMD.getOperaciones().stream().filter(oPMMD -> oPMMD.getEtapa() == EtapaMOPMMD.CARPINTERIA).forEach(oPMMD -> oPMMD.setHoras(oPMMD.getHoras() + 4));
        recPMMD.setNotas("Un mecanismo reclinable manual por cuerpo.");
        fichaRepositoryPMMD.save(recPMMD);

        FichaTecnicaPMMD escPMMD = simplePMMD("FT-009", "SF-009", "Escandinavo 2 cuerpos", 170, 85, 80, 2, Map.of("MP-003", "MP-008"), Set.of(),
                Map.of(), 0, LocalDate.of(2026, 7, 14));
        for (LineaBOMPMMD l : escPMMD.getLineas()) if (l.getMaterial().getCodigo().equals("MP-017")) l.setCantidad(0.5);
        escPMMD.setNotas("Tela bouclé; brazos delgados.");
        fichaRepositoryPMMD.save(escPMMD);


        FichaTecnicaPMMD puffPMMD = ficha("FT-010", 1, EstadoFichaPMMD.APROBADA, "SF-010", "Puff cubo", 45, 45, 42, 1, false, "Puff", LocalDate.of(2026, 4, 8));
        lineaPMMD(puffPMMD, "MP-001", ComponentePMMD.ESTRUCTURA, 4, 18, DependenciaPMMD.L);
        lineaPMMD(puffPMMD, "MP-002", ComponentePMMD.ESTRUCTURA, 0.15, 8, DependenciaPMMD.LF);
        lineaPMMD(puffPMMD, "MP-019", ComponentePMMD.ASIENTO, 0.12, 12, DependenciaPMMD.LF);
        lineaPMMD(puffPMMD, "MP-017", ComponentePMMD.TAPIZADO, 0.25, 12, DependenciaPMMD.T);
        lineaPMMD(puffPMMD, "MP-003", ComponentePMMD.TAPIZADO, 1.6, 10, DependenciaPMMD.T);
        lineaPMMD(puffPMMD, "MP-011", ComponentePMMD.TAPIZADO, 1.2, 10, DependenciaPMMD.T);
        lineaPMMD(puffPMMD, "MP-012", ComponentePMMD.TAPIZADO, 0.3, 10, DependenciaPMMD.LF);
        lineaPMMD(puffPMMD, "MP-025", ComponentePMMD.ENSAMBLAJE, 0.05, 5, DependenciaPMMD.T);
        lineaPMMD(puffPMMD, "MP-026", ComponentePMMD.ENSAMBLAJE, 0.1, 5, DependenciaPMMD.T);
        lineaPMMD(puffPMMD, "MP-033", ComponentePMMD.EMBALAJE, 1, 0, DependenciaPMMD.F);
        operacionesPMMD(puffPMMD, new double[]{0.5, 1, 0.5, 1.5, 0.3, 0.2});
        fichaRepositoryPMMD.save(puffPMMD);


        FichaTecnicaPMMD uPMMD = ficha("FT-011", 1, EstadoFichaPMMD.BORRADOR, "SF-011", "Seccional U modular", 320, 220, 85, 5, true, "Seccional", null);
        agregarPlantillaPMMD(uPMMD, new MedidasPMMD(320, 92, 85, 5), basePMMD, Map.of(), Set.of());
        for (LineaBOMPMMD l : uPMMD.getLineas()) if (l.getMaterial().getCodigo().equals("MP-003")) l.setMaterial(matPMMD.get("MP-007"));
        lineaPMMD(uPMMD, "MP-001", ComponentePMMD.ESTRUCTURA, 20, 18, DependenciaPMMD.F);
        lineaPMMD(uPMMD, "MP-019", ComponentePMMD.ASIENTO, 1.2, 12, DependenciaPMMD.F);
        lineaPMMD(uPMMD, "MP-007", ComponentePMMD.TAPIZADO, 7, 10, DependenciaPMMD.F);
        lineaPMMD(uPMMD, "MP-010", ComponentePMMD.ASIENTO, 20, 0, DependenciaPMMD.N);
        operacionesPMMD(uPMMD, new double[]{6, 16, 11, 19, 3, 2});
        uPMMD.setNotas("Borrador: falta validar el despiece de las dos chaises.");
        fichaRepositoryPMMD.save(uPMMD);


        for (ProductoPMMD p : prodPMMD.values()) {
            FichaTecnicaPMMD fPMMD = catalogoPMMD.fichaAprobada(fichaDePMMD(p.getCodigo()));
            if (fPMMD != null) {
                double c = FormulaPMMD.redondearDecena(fichaServicePMMD.costoEstandar(fPMMD));
                p.setCosto(BigDecimal.valueOf(c).setScale(2, RoundingMode.HALF_UP));
            } else {
                p.setCosto(new BigDecimal("4620.00"));
            }
            productoRepositoryPMMD.save(p);
        }
    }

    private static String fichaDePMMD(String productoPMMD) {
        return "FT-" + productoPMMD.substring(3);
    }

    private FichaTecnicaPMMD simplePMMD(String codigoPMMD, String productoPMMD, String nombrePMMD, int l, int fPMMD, int a, int n, Map<String, String> reemplazosPMMD,
                                Set<String> quitarPMMD, Map<String, Double> extrasPMMD, double recargoMOPMMD, LocalDate aprobadaPMMD) {
        FichaTecnicaPMMD ftPMMD = ficha(codigoPMMD, 1, EstadoFichaPMMD.APROBADA, productoPMMD, nombrePMMD, l, fPMMD, a, n, false, "Recto", aprobadaPMMD);
        MedidasPMMD destinoPMMD = new MedidasPMMD(l, fPMMD > 110 ? 92 : fPMMD, a, n);
        agregarPlantillaPMMD(ftPMMD, destinoPMMD, new MedidasPMMD(210, 92, 85, 3), Map.of(), quitarPMMD);
        for (LineaBOMPMMD lbPMMD : ftPMMD.getLineas()) {
            String rPMMD = reemplazosPMMD.get(lbPMMD.getMaterial().getCodigo());
            if (rPMMD != null) lbPMMD.setMaterial(matPMMD.get(rPMMD));
        }
        extrasPMMD.forEach((m, qPMMD) -> {
            LineaBOMPMMD existentePMMD = ftPMMD.getLineas().stream().filter(xPMMD -> xPMMD.getMaterial().getCodigo().equals(m) && !xPMMD.getOpcional()).findFirst().orElse(null);
            if (existentePMMD != null && !m.equals("MP-016")) {
                existentePMMD.setCantidad(r2PMMD(existentePMMD.getCantidad() + qPMMD));
            } else {
                MaterialPMMD mmPMMD = matPMMD.get(m);
                ftPMMD.agregarLinea(LineaBOMPMMD.builder().material(mmPMMD).componente(componenteParaPMMD(mmPMMD)).cantidad(qPMMD)
                        .mermaPct(mmPMMD.isEntera() ? 0d : 5d).dependencia(mmPMMD.isEntera() ? DependenciaPMMD.F : DependenciaPMMD.L).opcional(false).build());
            }
        });
        double fmoPMMD = FormulaPMMD.factorManoObra(destinoPMMD, new MedidasPMMD(210, 92, 85, 3)) * (1 + recargoMOPMMD / 100);
        double[] hPMMD = new double[6];
        for (int iPMMD = 0; iPMMD < 6; iPMMD++) hPMMD[iPMMD] = Math.max(0.5, Math.round(HORAS_BASEPMMD[iPMMD] * fmoPMMD * 2) / 2.0);
        operacionesPMMD(ftPMMD, hPMMD);
        return fichaRepositoryPMMD.save(ftPMMD);
    }

    private static ComponentePMMD componenteParaPMMD(MaterialPMMD m) {
        return switch (m.getCategoria()) {
            case "Madera", "Tableros" -> ComponentePMMD.ESTRUCTURA;
            case "Espuma" -> ComponentePMMD.ASIENTO;
            case "Mecanismos" -> ComponentePMMD.MECANISMO;
            case "Patas" -> ComponentePMMD.PATAS;
            default -> m.getCodigo().equals("MP-016") ? ComponentePMMD.TAPIZADO : m.getCodigo().equals("MP-010") ? ComponentePMMD.ASIENTO
                    : m.isTela() ? ComponentePMMD.TAPIZADO : ComponentePMMD.TAPIZADO;
        };
    }

    private FichaTecnicaPMMD ficha(String codigoPMMD, int versionPMMD, EstadoFichaPMMD estadoPMMD, String productoPMMD, String nombrePMMD, int l, int fPMMD, int a, int n,
                               boolean chaisePMMD, String cortePMMD, LocalDate aprobadaPMMD) {
        return FichaTecnicaPMMD.builder().codigo(codigoPMMD).version(versionPMMD).uid(FichaTecnicaPMMD.uid(codigoPMMD, versionPMMD)).estado(estadoPMMD)
                .producto(prodPMMD.get(productoPMMD)).nombre(nombrePMMD).largo(l).fondo(fPMMD).alto(a).cuerpos(n).chaise(chaisePMMD).tipoCorte(cortePMMD)
                .indirectosPct(6d).aprobadoPor(aprobadaPMMD == null ? null : "Carlos Mendoza").fechaAprobacion(aprobadaPMMD).build();
    }



    private void agregarPlantillaPMMD(FichaTecnicaPMMD fPMMD, MedidasPMMD destinoPMMD, MedidasPMMD basePMMD, Map<String, Double> fijasPMMD, Set<String> quitarPMMD) {
        for (T tPMMD : PLANTILLAPMMD) {
            if (quitarPMMD.contains(tPMMD.material())) continue;
            double qPMMD = fijasPMMD.containsKey(tPMMD.material()) ? fijasPMMD.get(tPMMD.material())
                    : tPMMD.cant() * FormulaPMMD.factor(tPMMD.dep(), destinoPMMD, basePMMD);
            MaterialPMMD m = matPMMD.get(tPMMD.material());
            if (m.isEntera()) qPMMD = Math.ceil(qPMMD - 1e-9);
            fPMMD.agregarLinea(LineaBOMPMMD.builder().material(m).componente(tPMMD.comp()).cantidad(r2PMMD(qPMMD)).mermaPct(tPMMD.merma())
                    .dependencia(tPMMD.dep()).opcional(tPMMD.opcional()).build());
        }
    }

    private void lineaPMMD(FichaTecnicaPMMD fPMMD, String m, ComponentePMMD c, double qPMMD, double merma, DependenciaPMMD dPMMD) {
        fPMMD.agregarLinea(LineaBOMPMMD.builder().material(matPMMD.get(m)).componente(c).cantidad(qPMMD).mermaPct(merma).dependencia(dPMMD).opcional(false).build());
    }

    private void operacionesPMMD(FichaTecnicaPMMD fPMMD, double[] horasPMMD) {
        fPMMD.getOperaciones().clear();
        EtapaMOPMMD[] e = EtapaMOPMMD.values();
        for (int iPMMD = 0; iPMMD < e.length; iPMMD++) {
            fPMMD.agregarOperacion(OperacionMOPMMD.builder().etapa(e[iPMMD]).horas(horasPMMD[iPMMD]).costoHora(e[iPMMD].costoHora()).build());
        }
    }



    private void clientes() {
        c("C-0101", ClientePMMD.Tipo.PERSONA, "Rosa Quispe Mamani", "42315678", "951 284 330", "Jr. Moquegua 345", "Juliaca", "2024-03-12");
        c("C-0102", ClientePMMD.Tipo.PERSONA, "Juan Carlos Apaza Condori", "40128876", "973 115 902", "Av. Circunvalación 1120", "Juliaca", "2023-11-04");
        c("C-0103", ClientePMMD.Tipo.PERSONA, "Lucía Huanca Ticona", "45567123", "950 776 410", "Jr. Huancané 218", "Juliaca", "2025-01-20");
        c("C-0104", ClientePMMD.Tipo.EMPRESA, "Hotel Royal Inn Juliaca S.A.C.", "20448812345", "051 32 6655", "Jr. San Román 158", "Juliaca", "2022-08-15");
        c("C-0105", ClientePMMD.Tipo.PERSONA, "Pedro Mamani Coaquira", "41890234", "984 330 117", "Urb. La Capilla Mz B Lt 7", "Juliaca", "2024-06-02");
        c("C-0106", ClientePMMD.Tipo.PERSONA, "Carmen Flores Pari", "43678901", "951 902 667", "Jr. Lambayeque 560", "Puno", "2025-04-18");
        c("C-0107", ClientePMMD.Tipo.PERSONA, "Miguel Ángel Ccama Quispe", "46012345", "966 210 054", "Av. Independencia 890", "Juliaca", "2025-09-01");
        c("C-0108", ClientePMMD.Tipo.PERSONA, "Gladys Turpo Mamani", "44123987", "951 448 213", "Jr. Arequipa 233", "Puno", "2026-02-10");
        c("C-0109", ClientePMMD.Tipo.PERSONA, "Ronald Chura Apaza", "47234561", "974 556 801", "Av. Huancané 1450", "Juliaca", "2026-05-22");
        c("C-0110", ClientePMMD.Tipo.EMPRESA, "Restaurante El Altiplano E.I.R.L.", "20601234871", "051 36 9044", "Jr. Lima 412", "Puno", "2023-02-27");
        c("C-0111", ClientePMMD.Tipo.PERSONA, "Silvia Cutipa Larico", "42987650", "951 121 774", "Jr. 2 de Mayo 175", "Juliaca", "2026-07-08");
        c("C-0112", ClientePMMD.Tipo.EMPRESA, "I.E.P. San Martín de Porres", "20406789123", "051 32 8810", "Av. Tacna 760", "Juliaca", "2024-12-03");
    }

    private void c(String codigoPMMD, ClientePMMD.Tipo tipoPMMD, String nombrePMMD, String docPMMD, String tel, String dirPMMD, String ciudadPMMD, String fechaPMMD) {
        String emailPMMD = nombrePMMD.split(" ")[0].toLowerCase(Locale.ROOT).replace("á", "a").replace(".", "") + "." + codigoPMMD.substring(2) + "@gmail.com";
        cliPMMD.put(codigoPMMD, clienteRepositoryPMMD.save(ClientePMMD.builder().codigo(codigoPMMD).tipo(tipoPMMD).nombre(nombrePMMD).documento(docPMMD).telefono(tel)
                .email(tipoPMMD == ClientePMMD.Tipo.EMPRESA ? "compras@" + nombrePMMD.split(" ")[1].toLowerCase(Locale.ROOT).replace(".", "") + ".pe" : emailPMMD)
                .direccion(dirPMMD).ciudad(ciudadPMMD).fechaRegistro(LocalDate.parse(fechaPMMD)).build()));
    }

    private void empleadosPMMD() {
        e("E-01", "Rubén Quispe Flores", "40211873", "Supervisor de taller", "Taller · Tapizado", RolPMMD.SUPERVISOR, "951 300 412", "2014-02-03", EtapaPMMD.CONTROL_CALIDAD, "#14213D");
        e("E-02", "Fredy Choque Mamani", "41876230", "Carpintero", "Taller · Carpintería", RolPMMD.CARPINTERO, "951 412 908", "2016-05-16", EtapaPMMD.ARMADO, "#A47148");
        e("E-03", "Wilber Condori Apaza", "43902157", "Carpintero", "Taller · Carpintería", RolPMMD.CARPINTERO, "973 220 145", "2019-08-01", EtapaPMMD.CORTE_MADERA, "#C69A6D");
        e("E-04", "Yolanda Ccori Huamán", "42557310", "Costurera", "Taller · Costura", RolPMMD.COSTURERA, "951 667 203", "2015-03-09", EtapaPMMD.COSTURA, "#F59E0B");
        e("E-05", "Marleny Huanca Quispe", "45110298", "Costurera", "Taller · Costura", RolPMMD.COSTURERA, "984 102 776", "2020-10-12", EtapaPMMD.CORTE_TELA, "#EC4899");
        e("E-06", "Edwin Mamani Ticona", "41023987", "Tapicero", "Taller · Tapizado", RolPMMD.TAPICERO, "951 883 041", "2013-07-22", EtapaPMMD.TAPIZADO, "#2563EB");
        e("E-07", "Hugo Pari Calsín", "44670231", "Tapicero", "Taller · Tapizado", RolPMMD.TAPICERO, "966 340 812", "2018-01-15", EtapaPMMD.TAPIZADO, "#0EA5E9");
        e("E-08", "Nilton Apaza Cutipa", "46781203", "Tapicero", "Taller · Tapizado", RolPMMD.TAPICERO, "951 776 390", "2021-04-05", EtapaPMMD.ACABADO, "#14B8A6");
        e("E-09", "Jhon Coaquira Vilca", "48012376", "Ayudante de taller", "Taller · Carpintería", RolPMMD.CARPINTERO, "974 118 560", "2023-02-20", EtapaPMMD.CINCHADO, "#8B5CF6");
        e("E-10", "Elvis Ticona Larico", "47309812", "Ayudante de espumado", "Taller · Espumado", RolPMMD.TAPICERO, "951 230 778", "2022-09-12", EtapaPMMD.ESPUMADO, "#0EA5E9");
        e("E-11", "Roxana Vilca Mendoza", "45890123", "Vendedora", "Tienda", RolPMMD.VENDEDOR, "951 456 120", "2017-11-06", null, "#DB2777");
        e("E-12", "Katherine Flores Chambi", "48123450", "Vendedora", "Tienda", RolPMMD.VENDEDOR, "973 905 331", "2024-03-01", null, "#7C3AED");
        e("E-13", "Luis Arocutipa Ramos", "43345612", "Almacenero", "Almacén", RolPMMD.ALMACENERO, "951 558 207", "2018-06-18", null, "#64748B");
        e("E-14", "Milagros Cahuana Ponce", "44781209", "Administradora", "Administración", RolPMMD.ADMINISTRADOR, "951 220 984", "2016-01-11", null, "#16A34A");
        empleadoRepositoryPMMD.findByCodigo("E-12").ifPresent(xPMMD -> {
            xPMMD.setEstado("Vacaciones");
            empleadoRepositoryPMMD.save(xPMMD);
        });
    }

    private void e(String codigoPMMD, String nombrePMMD, String dniPMMD, String cargoPMMD, String areaPMMD, RolPMMD rolPMMD, String tel, String ingresoPMMD, EtapaPMMD etapaPMMD, String colorPMMD) {
        empPMMD.put(codigoPMMD, empleadoRepositoryPMMD.save(EmpleadoPMMD.builder().codigo(codigoPMMD).nombre(nombrePMMD).dni(dniPMMD).cargo(cargoPMMD).area(areaPMMD).rol(rolPMMD)
                .telefono(tel).ingreso(LocalDate.parse(ingresoPMMD)).estado("Activo").etapa(etapaPMMD).color(colorPMMD).build()));
    }



    private PedidoPMMD pe(String codigoPMMD, String clientePMMD, String productoPMMD, PedidoPMMD.Tipo tipoPMMD, int cant, String fechaPMMD, String entregaPMMD, double precioPMMD,
                      double adelantoPMMD, EstadoPedidoPMMD estadoPMMD, String telaPMMD, Integer l, Integer fPMMD, Integer a, String obsPMMD, String especPMMD) {
        TelaPMMD tPMMD = telaPMMD == null ? null : tel.get(telaPMMD);
        PedidoPMMD p = pedidoRepositoryPMMD.save(PedidoPMMD.builder().codigo(codigoPMMD).cliente(cliPMMD.get(clientePMMD)).producto(prodPMMD.get(productoPMMD)).tipo(tipoPMMD)
                .cantidad(cant).fecha(LocalDate.parse(fechaPMMD)).fechaEntrega(LocalDate.parse(entregaPMMD))
                .precio(BigDecimal.valueOf(precioPMMD).setScale(2, RoundingMode.HALF_UP)).adelanto(BigDecimal.valueOf(adelantoPMMD).setScale(2, RoundingMode.HALF_UP))
                .estado(estadoPMMD).tela(tPMMD).material(tPMMD == null ? null : tPMMD.getTipo().nombre()).largo(l).fondo(fPMMD).alto(a)
                .observaciones(obsPMMD).especificacion(especPMMD).build());
        if (adelantoPMMD > 0) {
            boolean total = adelantoPMMD >= precioPMMD;
            if (total && estadoPMMD == EstadoPedidoPMMD.ENTREGADO && adelantoPMMD > precioPMMD * 0.5) {
                pagoPMMD(p, LocalDate.parse(fechaPMMD), Math.round(precioPMMD * 0.5), "Transferencia", "Adelanto");
                pagoPMMD(p, LocalDate.parse(entregaPMMD), precioPMMD - Math.round(precioPMMD * 0.5), "Efectivo", "Cancelación");
            } else {
                pagoPMMD(p, LocalDate.parse(fechaPMMD), adelantoPMMD, codigoPMMD.endsWith("1") || codigoPMMD.endsWith("5") ? "Yape" : "Transferencia", "Adelanto");
            }
        }
        return p;
    }

    private void pagoPMMD(PedidoPMMD p, LocalDate fechaPMMD, double montoPMMD, String metodoPMMD, String conceptoPMMD) {
        pagoRepositoryPMMD.save(PagoPMMD.builder().cliente(p.getCliente()).referencia(p.getCodigo()).fecha(fechaPMMD)
                .monto(BigDecimal.valueOf(montoPMMD).setScale(2, RoundingMode.HALF_UP)).metodo(metodoPMMD).concepto(conceptoPMMD).build());
    }

    private void pedidos() {
        pe("PE-0237", "C-0105", "SF-002", PedidoPMMD.Tipo.ESTANDAR, 1, "2026-09-18", "2026-09-28", 1990, 1990, EstadoPedidoPMMD.ENTREGADO, "TL-06", null, null, null,
                "Entregado e instalado.", null);
        pe("PE-0238", "C-0110", "SF-010", PedidoPMMD.Tipo.ESTANDAR, 6, "2026-09-22", "2026-10-02", 1560, 1560, EstadoPedidoPMMD.ENTREGADO, "TL-12", null, null, null,
                "Seis puffs para la terraza del restaurante.", null);
        pe("PE-0239", "C-0107", "SF-009", PedidoPMMD.Tipo.PERSONALIZADO, 1, "2026-09-25", "2026-10-20", 2350, 0, EstadoPedidoPMMD.CANCELADO, "TL-19", 180, 85, 80,
                "Cliente desistió: se mudará fuera de Juliaca.", "Escandinavo a medida 180 cm");
        pe("PE-0240", "C-0109", "SF-001", PedidoPMMD.Tipo.PERSONALIZADO, 1, "2026-09-29", "2026-10-16", 2690, 1345, EstadoPedidoPMMD.LISTO, "TL-03", 220, 92, 85,
                "Coordinar entrega por la tarde.", "Sofá Moderno de 220 cm en chenille Arena");
        pe("PE-0241", "C-0101", "SF-001", PedidoPMMD.Tipo.PERSONALIZADO, 1, "2026-10-01", "2026-10-20", 2690, 1500, EstadoPedidoPMMD.EN_PRODUCCION, "TL-01", 220, 92, 85,
                "Cojines decorativos con vivo color Mostaza. Proyecto de sala: sofá + sillón.", "Sofá Moderno de 220 cm en chenille Gris perla");
        pe("PE-0242", "C-0102", "SF-005", PedidoPMMD.Tipo.PERSONALIZADO, 1, "2026-09-30", "2026-10-18", 5930, 3000, EstadoPedidoPMMD.EN_PRODUCCION, "TL-09", 280, 157, 85,
                "Chaise al lado derecho mirando de frente.", "Seccional L con chaise derecha en lino Natural");
        pe("PE-0243", "C-0104", "SF-003", PedidoPMMD.Tipo.ESTANDAR, 4, "2026-10-02", "2026-10-16", 4760, 2380, EstadoPedidoPMMD.EN_PRODUCCION, "TL-20", null, null, null,
                "Entregar en recepción del hotel; coordinar con administración.", "4 sillones para el lobby");
        pe("PE-0244", "C-0106", "SF-008", PedidoPMMD.Tipo.PERSONALIZADO, 1, "2026-09-26", "2026-10-14", 4800, 2400, EstadoPedidoPMMD.EN_PRODUCCION, "TL-14", null, null, null,
                "Mecanismo reclinable en los 3 cuerpos.", "Reclinable 3 cuerpos en cuero sintético Negro");
        pe("PE-0245", "C-0103", "SF-006", PedidoPMMD.Tipo.PERSONALIZADO, 1, "2026-10-03", "2026-10-22", 4360, 2500, EstadoPedidoPMMD.EN_PRODUCCION, "TL-13", null, null, null,
                "4.º piso sin ascensor: subir por escalera, medir el pasadizo.", "Chesterfield en cuero sintético Cognac");
        pe("PE-0246", "C-0105", "SF-004", PedidoPMMD.Tipo.PERSONALIZADO, 1, "2026-10-06", "2026-10-30", 5490, 3000, EstadoPedidoPMMD.EN_PRODUCCION, "TL-02", null, null, null,
                "Misma tela para todo el juego.", "Juego 3-2-1 en chenille Grafito");
        pe("PE-0247", "C-0112", "SF-002", PedidoPMMD.Tipo.PERSONALIZADO, 3, "2026-10-07", "2026-10-26", 5970, 2985, EstadoPedidoPMMD.EN_PRODUCCION, "TL-04", null, null, null,
                "Sala de profesores. Factura a nombre de la I.E.P.; entregar de 8 a 13 h.", "3 sofás de 2 cuerpos en chenille Azul petróleo");
        pe("PE-0248", "C-0108", "SF-003", PedidoPMMD.Tipo.PERSONALIZADO, 1, "2026-10-12", "2026-11-05", 1370, 700, EstadoPedidoPMMD.CONFIRMADO, "TL-15", null, null, null,
                "Brazos redondeados.", "Sillón en terciopelo Esmeralda");
        pe("PE-0249", "C-0111", "SF-009", PedidoPMMD.Tipo.PERSONALIZADO, 1, "2026-10-14", "2026-11-12", 2630, 0, EstadoPedidoPMMD.COTIZADO, "TL-19", null, null, null,
                "Envió fotos de su sala por WhatsApp.", "Escandinavo 2 cuerpos en bouclé Avena");
    }



    private OrdenProduccionPMMD op(String codigoPMMD, String productoPMMD, String nombrePMMD, int cant, String clientePMMD, String responsablePMMD, PrioridadPMMD prioPMMD,
                               String inicioPMMD, String entregaPMMD, EtapaPMMD etapaPMMD, String telaPMMD, String pedidoPMMD, Integer largoPMMD) {
        ProductoPMMD p = productoPMMD == null ? null : prodPMMD.get(productoPMMD);
        FichaTecnicaPMMD fPMMD = p == null ? null : fichaServicePMMD.aprobadaDe(p);
        TelaPMMD tPMMD = telaPMMD == null ? null : tel.get(telaPMMD);
        Map<String, String> sustPMMD = new HashMap<>();
        if (tPMMD != null && fPMMD != null) for (String g : produccionServicePMMD.materialesTela(fPMMD)) sustPMMD.put(g, tPMMD.getTipo().materialCodigo());
        OrdenProduccionPMMD oPMMD = OrdenProduccionPMMD.builder().codigo(codigoPMMD).producto(p).nombrePersonalizado(nombrePMMD).cantidad(cant)
                .cliente(clientePMMD == null ? null : cliPMMD.get(clientePMMD)).responsable(empPMMD.get(responsablePMMD)).prioridad(prioPMMD)
                .fechaInicio(LocalDate.parse(inicioPMMD)).fechaEntrega(LocalDate.parse(entregaPMMD)).etapa(etapaPMMD)
                .fichaUid(fPMMD == null ? null : fPMMD.getUid()).tela(tPMMD).sustituciones(sustPMMD).pedidoCodigo(pedidoPMMD)
                .largo(largoPMMD).fondo(largoPMMD == null ? null : fPMMD == null ? null : fPMMD.getFondo()).alto(largoPMMD == null ? null : fPMMD == null ? null : fPMMD.getAlto())
                .cuerpos(largoPMMD == null ? null : fPMMD == null ? null : fPMMD.getCuerpos()).build();
        oPMMD = opRepositoryPMMD.save(oPMMD);
        oPMMD.setCosto(produccionServicePMMD.costoCalculado(oPMMD));
        oPMMD = opRepositoryPMMD.save(oPMMD);
        opsPMMD.put(codigoPMMD, oPMMD);
        if (pedidoPMMD != null) pedidoRepositoryPMMD.findByCodigo(pedidoPMMD).ifPresent(xPMMD -> {
            xPMMD.setOpCodigo(codigoPMMD);
            pedidoRepositoryPMMD.save(xPMMD);
        });
        return oPMMD;
    }

    private void ordenesPMMD() {
        op("OP-1019", "SF-001", null, 1, "C-0101", "E-06", PrioridadPMMD.MEDIA, "2026-10-05", "2026-10-20", EtapaPMMD.TAPIZADO, "TL-01", "PE-0241", 220);
        op("OP-1020", "SF-005", null, 1, "C-0102", "E-04", PrioridadPMMD.ALTA, "2026-10-03", "2026-10-18", EtapaPMMD.COSTURA, "TL-09", "PE-0242", null);
        op("OP-1021", "SF-003", null, 4, "C-0104", "E-01", PrioridadPMMD.ALTA, "2026-10-04", "2026-10-16", EtapaPMMD.CONTROL_CALIDAD, "TL-20", "PE-0243", null);
        op("OP-1022", "SF-008", null, 1, "C-0106", "E-10", PrioridadPMMD.ALTA, "2026-09-29", "2026-10-14", EtapaPMMD.ESPUMADO, "TL-14", "PE-0244", null);
        op("OP-1023", "SF-006", null, 1, "C-0103", "E-08", PrioridadPMMD.MEDIA, "2026-10-06", "2026-10-22", EtapaPMMD.ACABADO, "TL-13", "PE-0245", null);
        op("OP-1024", "SF-007", null, 3, null, "E-02", PrioridadPMMD.BAJA, "2026-10-08", "2026-10-28", EtapaPMMD.ARMADO, "TL-21", null, null);
        op("OP-1025", "SF-004", null, 1, "C-0105", "E-03", PrioridadPMMD.MEDIA, "2026-10-13", "2026-10-30", EtapaPMMD.CORTE_MADERA, "TL-02", "PE-0246", null);
        OrdenProduccionPMMD retapPMMD = op("OP-1026", null, "Retapizado · Sofá 2 cuerpos (Silvia Cutipa)", 1, "C-0111", "E-05", PrioridadPMMD.MEDIA,
                "2026-10-12", "2026-10-19", EtapaPMMD.CORTE_TELA, "TL-10", null, null);
        retapPMMD.setRetapizadoCodigo("RT-032");
        retapPMMD.setAutorizacion("Retapizado RT-032 · no requiere ficha");
        retapPMMD.setEspecificacion("Retapizado 160 × 88 × 82 cm · espuma hundida · lino Piedra");
        retapPMMD.setCosto(new BigDecimal("640.00"));
        opRepositoryPMMD.save(retapPMMD);
        op("OP-1027", "SF-002", null, 3, "C-0112", "E-09", PrioridadPMMD.MEDIA, "2026-10-09", "2026-10-26", EtapaPMMD.CINCHADO, "TL-04", "PE-0247", null);
        op("OP-1028", "SF-003", null, 1, null, "E-06", PrioridadPMMD.BAJA, "2026-10-19", "2026-11-03", EtapaPMMD.PENDIENTE, "TL-15", null, null);
        OrdenProduccionPMMD listoPMMD = op("OP-1029", "SF-001", null, 1, "C-0109", "E-07", PrioridadPMMD.MEDIA, "2026-09-30", "2026-10-16", EtapaPMMD.LISTO, "TL-03", "PE-0240", 220);
        listoPMMD.setFechaFin(LocalDate.of(2026, 10, 13));
        opRepositoryPMMD.save(listoPMMD);
        OrdenProduccionPMMD uPMMD = op("OP-1030", "SF-011", null, 1, "C-0110", "E-03", PrioridadPMMD.ALTA, "2026-10-14", "2026-11-05", EtapaPMMD.CORTE_MADERA, "TL-16", null, null);
        uPMMD.setAutorizacion("Autorizado por Rubén Quispe Flores (Supervisor) el 14/10/2026: ficha FT-011 en revisión.");
        opRepositoryPMMD.save(uPMMD);


        Map<String, Double> parcialPMMD = Map.of("OP-1024|MP-002", 1.0, "OP-1027|MP-013", 3.0, "OP-1025|MP-029", 1.0);
        for (OrdenProduccionPMMD oPMMD : opsPMMD.values()) {
            if (!oPMMD.getEtapa().productiva() || oPMMD.isSinFicha()) continue;
            ExplosionPMMD.Resultado rPMMD = produccionServicePMMD.requerimiento(oPMMD);
            for (ExplosionPMMD.PorMaterial pmPMMD : rPMMD.porMaterial()) {
                MaterialPMMD m = matPMMD.get(pmPMMD.material().getCodigo());
                Double faltaFracPMMD = parcialPMMD.get(oPMMD.getCodigo() + "|" + m.getCodigo());
                double cant = pmPMMD.cantidad();
                if (faltaFracPMMD != null) cant = m.getCodigo().equals("MP-029") ? 0 : Math.max(0, cant - faltaFracPMMD);
                cant = Math.ceil(cant * 100 - 1e-6) / 100.0;
                if (cant <= 0) continue;
                reservaRepositoryPMMD.save(ReservaPMMD.builder().op(oPMMD).material(m).cantidad(cant).build());
                m.setReservado(m.getReservado() + cant);
                materialRepositoryPMMD.save(m);
                movimientoRepositoryPMMD.save(MovimientoPMMD.builder().material(m).fecha(LocalDateTime.of(oPMMD.getFechaInicio(), LocalTime.of(8, 30)))
                        .tipo(TipoMovimientoPMMD.RESERVA).cantidad(cant).referencia(oPMMD.getCodigo()).concepto("Reserva " + oPMMD.getProductoNombre())
                        .usuario("rquispe").build());
            }
        }
    }



    private void retapizadosPMMD() {
        rtPMMD("RT-030", "C-0107", "Sofá 3 cuerpos antiguo, tela floreada gastada", 3, 200, 90, 85, "Buena", "Hundida", "TL-06",
                "2026-09-20", "2026-10-05", "2026-10-12", EstadoRetapizadoPMMD.ENTREGADO, null);
        rtPMMD("RT-031", "C-0108", "Sillón bergère heredado", 1, 85, 80, 95, "Regular", "Cambio total", "TL-17",
                "2026-10-09", null, null, EstadoRetapizadoPMMD.COTIZADO, null);
        rtPMMD("RT-032", "C-0111", "Sofá 2 cuerpos color vino, espuma hundida", 2, 160, 88, 82, "Buena", "Hundida", "TL-10",
                "2026-10-06", "2026-10-11", "2026-10-19", EstadoRetapizadoPMMD.EN_TALLER, "OP-1026");
        rtPMMD("RT-033", "C-0109", "Seccional en L de 3 cuerpos con brazo roto", 3, 240, 90, 85, "Requiere refuerzo", "Cambio total", "TL-20",
                "2026-10-13", "2026-10-17", null, EstadoRetapizadoPMMD.RECOJO_PROGRAMADO, null);
        rtPMMD("RT-034", "C-0101", "Sillón de la sala de Rosa (proyecto sofá + sillón)", 1, 90, 85, 88, "Buena", "Buena", null,
                "2026-10-15", null, null, EstadoRetapizadoPMMD.POR_COTIZAR, null);
    }

    private void rtPMMD(String codigoPMMD, String clientePMMD, String descPMMD, int cuerposPMMD, int l, int fPMMD, int a, String estrPMMD, String espPMMD, String telaPMMD,
                    String registroPMMD, String recojoPMMD, String entregaPMMD, EstadoRetapizadoPMMD estadoPMMD, String op) {
        TelaPMMD tPMMD = telaPMMD == null ? null : tel.get(telaPMMD);
        RetapizadoPMMD rPMMD = RetapizadoPMMD.builder().codigo(codigoPMMD).cliente(cliPMMD.get(clientePMMD)).descripcion(descPMMD).cuerpos(cuerposPMMD).largo(l).fondo(fPMMD).alto(a)
                .estructura(estrPMMD).espuma(espPMMD).tela(tPMMD).fechaRegistro(LocalDate.parse(registroPMMD))
                .fechaRecojo(recojoPMMD == null ? null : LocalDate.parse(recojoPMMD)).fechaEntrega(entregaPMMD == null ? null : LocalDate.parse(entregaPMMD))
                .estado(estadoPMMD).opCodigo(op).build();
        if (tPMMD != null) {
            RetapizadoServicePMMD.Cotizacion c = retapizadoServicePMMD.cotizar(rPMMD);
            rPMMD.setMetros(r2PMMD(c.metrosTelaPMMD()));
            rPMMD.setTotal(BigDecimal.valueOf(c.precio()).setScale(2, RoundingMode.HALF_UP));
        }
        retapizadoRepositoryPMMD.save(rPMMD);
        if (estadoPMMD == EstadoRetapizadoPMMD.ENTREGADO && rPMMD.getTotal() != null) {
            pagoRepositoryPMMD.save(PagoPMMD.builder().cliente(rPMMD.getCliente()).referencia(codigoPMMD).fecha(rPMMD.getFechaEntrega()).monto(rPMMD.getTotal())
                    .metodo("Efectivo").concepto("Retapizado").build());
        }
    }

    private void reclamosPMMD() {
        grPMMD("GR-012", "C-0105", "SF-002", "2026-03-10", "Pata floja", "Una pata trasera se mueve al sentarse.", "2026-09-14",
                EstadoReclamoPMMD.RESUELTO, "Taco interno despegado; se reencoló y atornilló.", "Reparación en domicilio", 18, "2026-09-16");
        grPMMD("GR-013", "C-0101", "SF-003", "2026-02-15", "Costura abierta", "Se abrió la costura del cojín de asiento.", "2026-10-06",
                EstadoReclamoPMMD.EN_DIAGNOSTICO, "Revisando si es desgaste por uso o falla de hilo.", "Por definir", 0, null);
        grPMMD("GR-014", "C-0104", "SF-001", "2026-06-20", "Espuma hundida", "Dos cojines del lobby se hundieron.", "2026-10-13",
                EstadoReclamoPMMD.ABIERTO, null, "Por definir", 0, null);
        grPMMD("GR-015", "C-0106", "SF-008", "2025-11-02", "Mecanismo trabado", "El cuerpo izquierdo no vuelve a su posición.", "2026-10-02",
                EstadoReclamoPMMD.EN_REPARACION, "Resorte del mecanismo vencido; se cambia el mecanismo completo.", "Reemplazo del mecanismo", 180, null);
        grPMMD("GR-016", "C-0110", "SF-010", "2026-01-05", "Decoloración de tela", "Los puffs de la terraza perdieron color.", "2026-09-22",
                EstadoReclamoPMMD.RECHAZADO, "Exposición directa al sol en uso comercial; fuera de cobertura.", "No procede", 0, "2026-09-24");
    }

    private void grPMMD(String codigoPMMD, String clientePMMD, String productoPMMD, String compraPMMD, String fallaPMMD, String descPMMD, String fechaPMMD, EstadoReclamoPMMD estadoPMMD,
                    String diagPMMD, String solPMMD, double costoPMMD, String cierrePMMD) {
        reclamoRepositoryPMMD.save(ReclamoGarantiaPMMD.builder().codigo(codigoPMMD).cliente(cliPMMD.get(clientePMMD)).producto(prodPMMD.get(productoPMMD))
                .fechaCompra(LocalDate.parse(compraPMMD)).falla(fallaPMMD).descripcion(descPMMD).fecha(LocalDate.parse(fechaPMMD)).estado(estadoPMMD)
                .diagnostico(diagPMMD).solucion(solPMMD).costo(BigDecimal.valueOf(costoPMMD).setScale(2, RoundingMode.HALF_UP))
                .fechaCierre(cierrePMMD == null ? null : LocalDate.parse(cierrePMMD)).build());
    }



    private void consumosPMMD() {
        Object[][] histPMMD = {
                {"OP-1010", "SF-001", 1, "E-02", "2026-09-08", 0.03},
                {"OP-1011", "SF-002", 2, "E-06", "2026-09-12", -0.06},
                {"OP-1012", "SF-007", 1, "E-02", "2026-09-16", 0.10},
                {"OP-1013", "SF-005", 1, "E-07", "2026-09-19", 0.06},
                {"OP-1014", "SF-003", 2, "E-08", "2026-09-23", -0.05},
                {"OP-1015", "SF-001", 2, "E-06", "2026-09-26", -0.04},
                {"OP-1016", "SF-006", 1, "E-07", "2026-09-30", 0.12},
                {"OP-1017", "SF-009", 1, "E-08", "2026-10-03", -0.03},
                {"OP-1018", "SF-002", 1, "E-02", "2026-10-07", 0.05}};
        Random rndPMMD = new Random(2026);
        for (Object[] hPMMD : histPMMD) {
            ProductoPMMD p = prodPMMD.get((String) hPMMD[1]);
            FichaTecnicaPMMD fPMMD = fichaServicePMMD.aprobadaDe(p);
            EmpleadoPMMD e = empPMMD.get((String) hPMMD[3]);
            double desvioOpPMMD = (double) hPMMD[5] + (e.getCodigo().equals("E-02") ? 0.03 : 0);
            ExplosionPMMD.Resultado rPMMD = fichaServicePMMD.explotar(fPMMD, (Integer) hPMMD[2], Opciones.NINGUNA);
            LocalDate fechaPMMD = LocalDate.parse((String) hPMMD[4]);
            for (ExplosionPMMD.PorMaterial pmPMMD : rPMMD.porMaterial()) {
                MaterialPMMD m = pmPMMD.material();
                double porMaterialPMMD = switch (m.getCategoria()) {
                    case "Madera" -> 0.03;
                    case "Espuma" -> 0.02;
                    case "Tableros" -> 0.01;
                    default -> m.isTela() ? 0.015 : 0;
                };
                double ruidoPMMD = (rndPMMD.nextDouble() - 0.5) * 0.04;
                double realPMMD = m.isEntera() ? pmPMMD.cantidad() : r2PMMD(pmPMMD.cantidad() * (1 + desvioOpPMMD + porMaterialPMMD + ruidoPMMD));
                consumoRepositoryPMMD.save(ConsumoRealPMMD.builder().opCodigo((String) hPMMD[0]).producto(p.getNombre()).fichaUid(fPMMD.getUid())
                        .responsable(e.getNombre()).material(m).estimado(r2PMMD(pmPMMD.cantidad())).realConsumido(realPMMD).fecha(fechaPMMD).build());
                if (!fechaPMMD.isBefore(LocalDate.of(2026, 10, 1)) && realPMMD > 0) {
                    fisicoPMMD(m, LocalDateTime.of(fechaPMMD, LocalTime.of(17, 0)), TipoMovimientoPMMD.CONSUMO_OP, realPMMD, (String) hPMMD[0], "Consumo " + p.getNombre(), "rquispe");
                }
            }
        }

        OrdenProduccionPMMD listoPMMD = opsPMMD.get("OP-1029");
        ExplosionPMMD.Resultado rPMMD = produccionServicePMMD.requerimiento(listoPMMD);
        for (ExplosionPMMD.PorMaterial pmPMMD : rPMMD.porMaterial()) {
            MaterialPMMD m = pmPMMD.material();
            double realPMMD = m.isEntera() ? pmPMMD.cantidad() : r2PMMD(pmPMMD.cantidad() * (m.getCategoria().equals("Espuma") ? 1.05 : 1.02));
            consumoRepositoryPMMD.save(ConsumoRealPMMD.builder().opCodigo("OP-1029").producto(listoPMMD.getProductoNombre()).fichaUid(listoPMMD.getFichaUid())
                    .responsable(listoPMMD.getResponsable().getNombre()).material(m).estimado(r2PMMD(pmPMMD.cantidad())).realConsumido(realPMMD)
                    .fecha(LocalDate.of(2026, 10, 13)).build());
            fisicoPMMD(m, LocalDateTime.of(2026, 10, 13, 16, 40), TipoMovimientoPMMD.CONSUMO_OP, realPMMD, "OP-1029", "Consumo " + listoPMMD.getProductoNombre(), "rquispe");
        }
        movimientoRepositoryPMMD.save(MovimientoPMMD.builder().material(matPMMD.get("MP-019")).fecha(LocalDateTime.of(2026, 10, 13, 16, 50))
                .tipo(TipoMovimientoPMMD.RETAZO).cantidad(0.3).referencia("OP-1029").concepto("Retazo reutilizable · 0.6 m² de espuma D23")
                .usuario("rquispe").build());
    }



    private void fisicoPMMD(MaterialPMMD m, LocalDateTime fechaPMMD, TipoMovimientoPMMD tipoPMMD, double cant, String refPMMD, String conceptoPMMD, String usuario) {
        movimientosPMMD.add(MovimientoPMMD.builder().material(m).fecha(fechaPMMD).tipo(tipoPMMD).cantidad(cant).referencia(refPMMD).concepto(conceptoPMMD).usuario(usuario).build());
    }



    private OrdenCompraPMMD ocPMMD(String codigoPMMD, String proveedorPMMD, String fechaPMMD, EstadoCompraPMMD estadoPMMD, String origenPMMD, boolean nuevaPMMD, String recepcionPMMD,
                           Object[][] itemsPMMD) {
        OrdenCompraPMMD oPMMD = OrdenCompraPMMD.builder().codigo(codigoPMMD).proveedor(provPMMD.get(proveedorPMMD)).fecha(LocalDate.parse(fechaPMMD))
                .fechaEntrega(LocalDate.parse(fechaPMMD).plusDays(5)).estado(estadoPMMD).origen(origenPMMD).nueva(nuevaPMMD)
                .fechaRecepcion(recepcionPMMD == null ? null : LocalDate.parse(recepcionPMMD)).build();
        for (Object[] itPMMD : itemsPMMD) {
            MaterialPMMD m = matPMMD.get((String) itPMMD[0]);
            TelaPMMD tPMMD = itPMMD.length > 2 ? tel.get((String) itPMMD[2]) : null;
            oPMMD.agregar(ItemCompraPMMD.builder().material(m).tela(tPMMD).cantidad(((Number) itPMMD[1]).doubleValue())
                    .precioUnitario(BigDecimal.valueOf(m.getCosto()).setScale(2, RoundingMode.HALF_UP)).build());
            if (estadoPMMD == EstadoCompraPMMD.RECIBIDA && !m.isTela()) {
                fisicoPMMD(m, LocalDateTime.of(oPMMD.getFechaRecepcion(), LocalTime.of(11, 15)), TipoMovimientoPMMD.ENTRADA, ((Number) itPMMD[1]).doubleValue(),
                        codigoPMMD, "Recepción " + oPMMD.getProveedor().getRazonSocial(), "larocutipa");
            } else if (estadoPMMD == EstadoCompraPMMD.RECIBIDA) {
                fisicoPMMD(m, LocalDateTime.of(oPMMD.getFechaRecepcion(), LocalTime.of(11, 20)), TipoMovimientoPMMD.ENTRADA, ((Number) itPMMD[1]).doubleValue(),
                        codigoPMMD, "Rollo " + tPMMD.getColor() + " " + tPMMD.getCodigo(), "larocutipa");
            }
        }
        return compraRepositoryPMMD.save(oPMMD);
    }

    private void compras() {
        ocPMMD("OC-0317", "PR-01", "2026-10-09", EstadoCompraPMMD.RECIBIDA, "OP-1024", false, "2026-10-14",
                new Object[][]{{"MP-002", 12}, {"MP-001", 300}, {"MP-021", 40}});
        ocPMMD("OC-0318", "PR-04", "2026-10-01", EstadoCompraPMMD.RECIBIDA, "MRP", false, "2026-10-06",
                new Object[][]{{"MP-019", 10}, {"MP-018", 8}, {"MP-017", 6}, {"MP-009", 100}});
        ocPMMD("OC-0319", "PR-03", "2026-10-04", EstadoCompraPMMD.RECIBIDA, "Manual", false, "2026-10-09",
                new Object[][]{{"MP-011", 60}, {"MP-015", 20}, {"MP-014", 40}, {"MP-003", 30, "TL-02"}});
        ocPMMD("OC-0320", "PR-06", "2026-10-13", EstadoCompraPMMD.APROBADA, "MRP", false, null,
                new Object[][]{{"MP-029", 3}, {"MP-028", 2}});
        ocPMMD("OC-0321", "PR-04", "2026-10-14", EstadoCompraPMMD.BORRADOR, "OP-1027", false, null,
                new Object[][]{{"MP-013", 15}});
        ocPMMD("OC-0322", "PR-02", "2026-10-15", EstadoCompraPMMD.BORRADOR, "Calculadora", true, null,
                new Object[][]{{"MP-006", 20, "TL-11"}, {"MP-008", 10, "TL-19"}});
        ocPMMD("OC-0323", "PR-05", "2026-10-08", EstadoCompraPMMD.CANCELADA, "Manual", false, null,
                new Object[][]{{"MP-026", 10}});
        fisicoPMMD(matPMMD.get("MP-026"), LocalDateTime.of(2026, 10, 7, 9, 10), TipoMovimientoPMMD.SALIDA, 2, "VALE-118", "Salida a mantenimiento de engrapadoras", "larocutipa");
        fisicoPMMD(matPMMD.get("MP-027"), LocalDateTime.of(2026, 10, 10, 18, 0), TipoMovimientoPMMD.AJUSTE, -0.5, "INV-10", "Ajuste por inventario físico", "larocutipa");
    }



    private void ventas() {

        Object[][] vPMMD = {
                {1, "10:20", null, "B", "Efectivo", 0, new Object[][]{{"SF-001", 1, "TL-01"}}},
                {1, "16:45", "C-0107", "B", "Yape", 0, new Object[][]{{"SF-010", 2, "TL-07"}}},
                {2, "11:05", null, "B", "Tarjeta", 0, new Object[][]{{"SF-007", 1, "TL-21"}}},
                {3, "12:30", "C-0110", "F", "Transferencia", 0, new Object[][]{{"SF-011", 1, "TL-16"}}},
                {3, "17:10", null, "B", "Efectivo", 70, new Object[][]{{"SF-002", 1, "TL-02"}}},
                {4, "10:40", null, "B", "Plin", 0, new Object[][]{{"SF-003", 1, "TL-03"}}},
                {4, "15:55", "C-0108", "B", "Tarjeta", 0, new Object[][]{{"SF-006", 1, "TL-13"}}},
                {5, "11:30", null, "B", "Efectivo", 0, new Object[][]{{"SF-001", 1, "TL-02"}, {"SF-010", 2, "TL-07"}}},
                {6, "16:20", null, "B", "Transferencia", 290, new Object[][]{{"SF-004", 1, "TL-02"}}},
                {7, "10:15", "C-0102", "B", "Yape", 0, new Object[][]{{"SF-009", 1, "TL-18"}}},
                {7, "18:05", null, "B", "Tarjeta", 0, new Object[][]{{"SF-007", 1, "TL-21"}}},
                {8, "12:00", null, "B", "Efectivo", 0, new Object[][]{{"SF-001", 1, "TL-03"}}},
                {8, "17:35", null, "B", "Mixto", 0, new Object[][]{{"SF-005", 1, "TL-09"}}},
                {9, "11:50", null, "B", "Tarjeta", 0, new Object[][]{{"SF-002", 1, "TL-20"}}},
                {9, "16:10", "C-0112", "F", "Transferencia", 400, new Object[][]{{"SF-011", 0, null}, {"SF-007", 1, "TL-21"}, {"SF-003", 1, "TL-20"}}},
                {10, "10:30", null, "B", "Efectivo", 0, new Object[][]{{"SF-001", 1, "TL-01"}}},
                {10, "13:20", null, "B", "Yape", 0, new Object[][]{{"SF-010", 2, "TL-05"}}},
                {11, "11:40", "C-0109", "B", "Tarjeta", 0, new Object[][]{{"SF-008", 1, "TL-14"}}},
                {11, "17:00", null, "B", "Efectivo", 200, new Object[][]{{"SF-005", 1, "TL-10"}}},
                {12, "10:05", null, "B", "Plin", 0, new Object[][]{{"SF-002", 1, "TL-08"}}},
                {12, "16:30", null, "B", "Tarjeta", 0, new Object[][]{{"SF-001", 1, "TL-02"}}},
                {13, "11:10", "C-0105", "B", "Transferencia", 0, new Object[][]{{"SF-006", 1, "TL-12"}}},
                {13, "15:40", null, "B", "Efectivo", 0, new Object[][]{{"SF-009", 1, "TL-19"}}},
                {14, "10:50", null, "B", "Tarjeta", 600, new Object[][]{{"SF-004", 1, "TL-01"}}},
                {14, "12:45", null, "B", "Yape", 0, new Object[][]{{"SF-007", 1, "TL-20"}}},
                {14, "17:20", "C-0103", "B", "Efectivo", 0, new Object[][]{{"SF-001", 1, "TL-04"}, {"SF-003", 1, "TL-04"}}},
                {15, "09:40", null, "B", "Tarjeta", 0, new Object[][]{{"SF-001", 1, "TL-01"}, {"SF-002", 1, "TL-02"}}}};
        int boletaPMMD = 1240, facturaPMMD = 318;
        for (Object[] xPMMD : vPMMD) {
            boolean facPMMD = "F".equals(xPMMD[3]);
            VentaPMMD ventaPMMD = VentaPMMD.builder().comprobante(facPMMD ? VentaPMMD.Comprobante.FACTURA : VentaPMMD.Comprobante.BOLETA)
                    .serie(facPMMD ? "F001" : "B001").numero(facPMMD ? ++facturaPMMD : ++boletaPMMD)
                    .fecha(LocalDateTime.of(LocalDate.of(2026, 10, (Integer) xPMMD[0]), LocalTime.parse((String) xPMMD[1])))
                    .cliente(xPMMD[2] == null ? null : cliPMMD.get((String) xPMMD[2])).vendedor(((Integer) xPMMD[0]) % 2 == 0 ? "Roxana Vilca Mendoza" : "Katherine Flores Chambi")
                    .build();
            BigDecimal brutoPMMD = BigDecimal.ZERO;
            for (Object[] itPMMD : (Object[][]) xPMMD[6]) {
                int cant = (Integer) itPMMD[1];
                if (cant == 0) continue;
                ProductoPMMD p = prodPMMD.get((String) itPMMD[0]);
                ventaPMMD.agregar(ItemVentaPMMD.builder().producto(p).cantidad(cant).precioUnitario(p.getPrecio()).telaCodigo((String) itPMMD[2]).build());
                brutoPMMD = brutoPMMD.add(p.getPrecio().multiply(BigDecimal.valueOf(cant)));
            }
            BigDecimal descPMMD = BigDecimal.valueOf((Integer) xPMMD[5]).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = brutoPMMD.subtract(descPMMD);
            ventaPMMD.setDescuento(descPMMD);
            ventaPMMD.setTotal(total);
            String metodoPMMD = (String) xPMMD[4];
            if (metodoPMMD.equals("Mixto")) {
                BigDecimal a = new BigDecimal("2000.00");
                metodoPMMD = "Mixto (Efectivo S/ 2,000.00 + Yape S/ " + String.format(Locale.US, "%,.2f", total.subtract(a)) + ")";
            }
            if (metodoPMMD.equals("Efectivo")) {
                BigDecimal recibidoPMMD = total.setScale(-2, RoundingMode.UP);
                ventaPMMD.setRecibido(recibidoPMMD);
                ventaPMMD.setVuelto(recibidoPMMD.subtract(total));
            }
            ventaPMMD.setMetodoPago(metodoPMMD);
            ventaRepositoryPMMD.save(ventaPMMD);
        }
    }



    private void finanzas() {
        int[][] histPMMD = {{2025, 11, 62400, 48900, 25}, {2025, 12, 98700, 61200, 39}, {2026, 1, 54300, 45800, 21}, {2026, 2, 51800, 43900, 20},
                {2026, 3, 58600, 47200, 23}, {2026, 4, 61900, 49700, 24}, {2026, 5, 70400, 52300, 28}, {2026, 6, 66200, 51100, 26},
                {2026, 7, 79500, 57800, 32}, {2026, 8, 72300, 55400, 29}, {2026, 9, 75800, 56900, 30}};
        for (int[] hPMMD : histPMMD) {
            resumenRepositoryPMMD.save(ResumenMensualPMMD.builder().anio(hPMMD[0]).mes(hPMMD[1]).ventas(BigDecimal.valueOf(hPMMD[2])).egresos(BigDecimal.valueOf(hPMMD[3])).sofas(hPMMD[4]).build());
        }
        g("2026-10-01", "Alquiler", "Alquiler de taller y tienda · octubre", 3500);
        g("2026-10-05", "Servicios", "Luz, agua e internet", 950);
        g("2026-10-08", "Transporte", "Fletes de entregas en Juliaca y Puno", 1200);
        g("2026-10-10", "Publicidad", "Campaña en redes sociales", 800);
        g("2026-10-15", "Planilla", "Planilla 1.ª quincena de octubre", 16800);
    }

    private void g(String fechaPMMD, String catPMMD, String conceptoPMMD, double montoPMMD) {
        gastoRepositoryPMMD.save(GastoPMMD.builder().fecha(LocalDate.parse(fechaPMMD)).categoria(catPMMD).concepto(conceptoPMMD)
                .monto(BigDecimal.valueOf(montoPMMD).setScale(2, RoundingMode.HALF_UP)).build());
    }








    private void stocksFinalesPMMD() {
        for (MaterialPMMD m : matPMMD.values()) {
            MaterialPMMD frescoPMMD = materialRepositoryPMMD.findById(m.getId()).orElseThrow();
            double stockPMMD;
            TipoTelaPMMD tipoPMMD = TipoTelaPMMD.porMaterial(frescoPMMD.getCodigo());
            if (tipoPMMD != null) {
                stockPMMD = telaRepositoryPMMD.findByTipo(tipoPMMD).stream().filter(tPMMD -> !tPMMD.getDescontinuada()).mapToDouble(TelaPMMD::getStock).sum();
            } else if (Boolean.TRUE.equals(frescoPMMD.getBajoPedido())) {
                stockPMMD = 0;
            } else {
                stockPMMD = r2PMMD(frescoPMMD.getReservado() + disponibleFinalPMMD.getOrDefault(frescoPMMD.getCodigo(), frescoPMMD.getMinimo() * 2));
            }
            double posterioresPMMD = movimientosPMMD.stream().filter(xPMMD -> xPMMD.getMaterial().getCodigo().equals(frescoPMMD.getCodigo()))
                    .mapToDouble(MovimientoPMMD::getEfecto).sum();
            double inicialPMMD = r2PMMD(stockPMMD - posterioresPMMD);
            if (inicialPMMD < 0) {
                stockPMMD = r2PMMD(stockPMMD - inicialPMMD);
                inicialPMMD = 0;
            }
            frescoPMMD.setStock(stockPMMD);
            materialRepositoryPMMD.save(frescoPMMD);
            if (inicialPMMD > 0 || stockPMMD > 0) {
                movimientoRepositoryPMMD.save(MovimientoPMMD.builder().material(frescoPMMD).fecha(LocalDateTime.of(2026, 10, 1, 8, 0))
                        .tipo(TipoMovimientoPMMD.ENTRADA).cantidad(inicialPMMD).referencia("INV-OCT").concepto("Saldo inicial de octubre")
                        .usuario("larocutipa").build());
            }
        }
        for (MovimientoPMMD xPMMD : movimientosPMMD) {
            xPMMD.setMaterial(materialRepositoryPMMD.findById(xPMMD.getMaterial().getId()).orElseThrow());
            movimientoRepositoryPMMD.save(xPMMD);
        }

        Map<Long, Double> saldoPMMD = new HashMap<>();
        List<MovimientoPMMD> todosPMMD = new ArrayList<>(movimientoRepositoryPMMD.findAll());
        todosPMMD.sort(Comparator.comparing(MovimientoPMMD::getFecha).thenComparing(MovimientoPMMD::getId));
        for (MovimientoPMMD xPMMD : todosPMMD) {
            double sPMMD = saldoPMMD.getOrDefault(xPMMD.getMaterial().getId(), 0d) + xPMMD.getEfecto();
            saldoPMMD.put(xPMMD.getMaterial().getId(), sPMMD);
            xPMMD.setSaldo(r2PMMD(sPMMD));
        }
        movimientoRepositoryPMMD.saveAll(todosPMMD);
    }

    private static double r2PMMD(double vPMMD) {
        return Math.round(vPMMD * 100.0) / 100.0;
    }
}
