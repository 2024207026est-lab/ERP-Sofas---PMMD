package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.MaterialRepositoryPMMD;
import pe.erp.sofas.service.calculo.*;

import java.util.*;






@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CorteServicePMMD {
    


    public record Fuente(
      OrdenProduccionPMMD op, 
        FichaTecnicaPMMD ficha) {

        public String codigo() {
            return op != null ? op.getCodigo() : ficha.getEtiqueta();
        }

        public String titulo() {
            return op != null ? op.getCodigo() + " · " + op.getProductoNombre() + " ×" + op.getCantidad()
                    : ficha.getEtiqueta() + " · " + ficha.getNombre() + " (1 unidad)";
        }
    }



    public record MaterialCorte(
      MaterialPMMD material, 
        TipoCortePMMD tipo, 
          List<PiezaPMMD> piezas) {

        public int numeroPiezas() {
            return piezas.stream().mapToInt(PiezaPMMD::cantidad).sum();
        }
    }

    private final ProduccionServicePMMD produccionServicePMMD;

    private final FichaServicePMMD fichaServicePMMD;

    private final MaterialRepositoryPMMD materialRepositoryPMMD;

    private final InventarioServicePMMD inventarioServicePMMD;

    public List<Fuente> fuentes() {
        List<Fuente> lPMMD = new ArrayList<>();
        produccionServicePMMD.activas().stream().filter(o -> !o.isSinFicha()).forEach(o -> lPMMD.add(new Fuente(o, null)));
        fichaServicePMMD.todas().stream().filter(f -> f.getEstado() == EstadoFichaPMMD.APROBADA).forEach(f -> lPMMD.add(new Fuente(null, f)));
        return lPMMD;
    }



    public List<MaterialCorte> materiales(Fuente f, boolean conjuntoPMMD) {
        Map<String, MaterialCorte> mPMMD = new LinkedHashMap<>();
        agregarPMMD(mPMMD, f);
        if (conjuntoPMMD && f.op() != null) {
            Set<String> matsPMMD = mPMMD.keySet();
            for (OrdenProduccionPMMD otra : produccionServicePMMD.activas()) {
                if (otra.getId().equals(f.op().getId()) || otra.isSinFicha()) continue;
                Map<String, MaterialCorte> o = new LinkedHashMap<>();
                agregarPMMD(o, new Fuente(otra, null));
                for (var ePMMD : o.entrySet()) {
                    if (!matsPMMD.contains(ePMMD.getKey())) continue;
                    MaterialCorte basePMMD = mPMMD.get(ePMMD.getKey());
                    List<PiezaPMMD> piezas = new ArrayList<>(basePMMD.piezas());
                    piezas.addAll(ePMMD.getValue().piezas());
                    mPMMD.put(ePMMD.getKey(), new MaterialCorte(basePMMD.material(), basePMMD.tipo(), piezas));
                }
            }
        }
        return new ArrayList<>(mPMMD.values());
    }

    private void agregarPMMD(Map<String, MaterialCorte> accPMMD, Fuente f) {
        FichaTecnicaPMMD ficha = f.op() != null ? produccionServicePMMD.ficha(f.op()) : f.ficha();
        if (ficha == null) return;
        int cantidad = f.op() != null ? f.op().getCantidad() : 1;
        Map<String, String> sustPMMD = f.op() != null ? f.op().getSustituciones() : Map.of();
        TipoTelaPMMD tipoTelaPMMD = f.op() != null && f.op().getTela() != null ? f.op().getTela().getTipo() : null;
        List<FichaTecnicaPMMD> partesPMMD = Boolean.TRUE.equals(ficha.getCompuesta()) ? fichaServicePMMD.hijas(ficha) : List.of(ficha);
        for (FichaTecnicaPMMD partePMMD : partesPMMD) {
            DespiecePMMD.Sofa sPMMD = sofaPMMD(partePMMD, f.op(), tipoTelaPMMD, f.codigo());
            for (var ePMMD : DespiecePMMD.piezas(sPMMD).entrySet()) {
                String codigo = sustPMMD.getOrDefault(ePMMD.getKey(), ePMMD.getKey());
                if (ExplosionPMMD.QUITAR.equals(codigo)) continue;
                if (ePMMD.getKey().equals(DespiecePMMD.TELA) && tipoTelaPMMD == null) codigo = telaDeFichaPMMD(partePMMD, ePMMD.getKey());
                MaterialPMMD mat = materialRepositoryPMMD.findByCodigo(codigo).orElse(null);
                if (mat == null) continue;
                TipoCortePMMD tipo = TipoCortePMMD.paraCategoria(mat.getCategoria(), mat.getCodigo());
                if (tipo == null) continue;
                List<PiezaPMMD> piezas = new ArrayList<>();
                boolean giroTelaPMMD = tipo != TipoCortePMMD.TELA || TipoTelaPMMD.porMaterial(mat.getCodigo()).permiteGiro();
                for (PiezaPMMD pPMMD : ePMMD.getValue()) piezas.add(pPMMD.conCantidad(pPMMD.cantidad() * cantidad).conGiro(pPMMD.girable() && giroTelaPMMD));
                MaterialCorte prevPMMD = accPMMD.get(codigo);
                if (prevPMMD != null) piezas.addAll(0, prevPMMD.piezas());
                accPMMD.put(codigo, new MaterialCorte(mat, tipo, piezas));
            }
        }
    }



    private String telaDeFichaPMMD(FichaTecnicaPMMD f, String defectoPMMD) {
        return f.getLineas().stream().map(LineaBOMPMMD::getMaterial).filter(MaterialPMMD::isTela).map(MaterialPMMD::getCodigo)
                .findFirst().orElse(defectoPMMD);
    }

    private DespiecePMMD.Sofa sofaPMMD(FichaTecnicaPMMD f, OrdenProduccionPMMD op, TipoTelaPMMD telaPMMD, String origen) {
        boolean usarMedidasPMMD = op != null && op.getLargo() != null && op.getLargo() > 0 && !Boolean.TRUE.equals(f.getCompuesta());
        double L = usarMedidasPMMD ? op.getLargo() : f.getLargo();
        double F = usarMedidasPMMD && op.getFondo() != null ? op.getFondo() : f.getFondo();
        double A = usarMedidasPMMD && op.getAlto() != null ? op.getAlto() : f.getAlto();
        int n = usarMedidasPMMD && op.getCuerpos() != null ? op.getCuerpos() : f.getCuerpos();
        boolean chaise = Boolean.TRUE.equals(f.getChaise()) || (op != null && op.getExtras().stream()
                .anyMatch(xPMMD -> "MP-001".equals(xPMMD.getMaterialCodigo()) && xPMMD.getCantidad() >= 10));
        if (chaise && !Boolean.TRUE.equals(f.getChaise())) F = F + 65;
        boolean giro = telaPMMD == null || telaPMMD.permiteGiro();
        if (telaPMMD == null) {
            String tPMMD = telaDeFichaPMMD(f, DespiecePMMD.TELA);
            TipoTelaPMMD ttPMMD = TipoTelaPMMD.porMaterial(tPMMD);
            giro = ttPMMD == null || ttPMMD.permiteGiro();
        }
        return new DespiecePMMD.Sofa(L, F, A, n, chaise, "Puff".equals(f.getTipoCorte()), giro, origen);
    }



    public double metrosPlano(FichaTecnicaPMMD ficha, int cantidad, TipoTelaPMMD tipo) {
        List<FichaTecnicaPMMD> partesPMMD = Boolean.TRUE.equals(ficha.getCompuesta()) ? fichaServicePMMD.hijas(ficha) : List.of(ficha);
        List<PiezaPMMD> piezas = new ArrayList<>();
        for (FichaTecnicaPMMD partePMMD : partesPMMD) {
            DespiecePMMD.Sofa sPMMD = sofaPMMD(partePMMD, null, tipo, partePMMD.getEtiqueta());
            for (PiezaPMMD pPMMD : DespiecePMMD.piezas(sPMMD).getOrDefault(DespiecePMMD.TELA, List.of())) piezas.add(pPMMD.conCantidad(pPMMD.cantidad() * cantidad));
        }
        if (piezas.isEmpty()) return 0;
        return CortePMMD.optimizar(TipoCortePMMD.TELA, piezas, CortePMMD.Estrategia.MEJOR_LADO).metros();
    }



    public double estimadoFicha(Fuente f, MaterialPMMD mPMMD) {
        ExplosionPMMD.Resultado rPMMD = f.op() != null ? produccionServicePMMD.requerimiento(f.op()) : fichaServicePMMD.explotar(f.ficha());
        if (rPMMD == null) return 0;
        return rPMMD.porMaterial().stream().filter(pPMMD -> pPMMD.material().getCodigo().equals(mPMMD.getCodigo()))
                .mapToDouble(ExplosionPMMD.PorMaterial::cantidad).sum();
    }

    public CortePMMD.Resultado optimizar(MaterialCorte mcPMMD, CortePMMD.Estrategia estrategiaPMMD) {
        return CortePMMD.optimizar(mcPMMD.tipo(), mcPMMD.piezas(), estrategiaPMMD);
    }

    public CortePMMD.Resultado1D optimizarMadera(MaterialCorte mcPMMD) {
        return CortePMMD.optimizar1D(mcPMMD.piezas(), TipoCortePMMD.MADERA.ancho());
    }



    public static double usado(MaterialCorte mcPMMD, CortePMMD.Resultado rPMMD) {
        return switch (mcPMMD.tipo()) {
            case TELA -> rPMMD.metros();
            default -> rPMMD.laminas();
        };
    }



    @Transactional
    public void guardarRetazo(MaterialPMMD mPMMD, double cantidad, String referenciaPMMD, String detallePMMD) {
        if (cantidad <= 0) throw new NegocioExceptionPMMD("No hay sobrante reutilizable para guardar.");
        MaterialPMMD frescoPMMD = materialRepositoryPMMD.findById(mPMMD.getId()).orElseThrow();
        inventarioServicePMMD.registrar(frescoPMMD, TipoMovimientoPMMD.RETAZO, cantidad, referenciaPMMD, "Retazo reutilizable · " + detallePMMD);
    }
}
