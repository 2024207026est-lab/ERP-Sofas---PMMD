package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.MaterialRepositoryPMMD;
import pe.erp.sofas.repository.MovimientoRepositoryPMMD;
import pe.erp.sofas.repository.TelaRepositoryPMMD;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioServicePMMD {

    public static final List<String> UNIDADES = List.of("pt", "plancha", "m", "unidad", "caja", "galón", "kg", "juego", "par");

    private final MaterialRepositoryPMMD materialRepositoryPMMD;

    private final MovimientoRepositoryPMMD movimientoRepositoryPMMD;

    private final TelaRepositoryPMMD telaRepositoryPMMD;

    private final UsuarioActualPMMD usuarioPMMD;

    public List<MaterialPMMD> materiales() {
        return materialRepositoryPMMD.findAllByOrderByCodigoAsc();
    }

    public MaterialPMMD material(Long idPMMD) {
        return materialRepositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("El material ya no existe."));
    }

    public MaterialPMMD material(String codigoPMMD) {
        return materialRepositoryPMMD.findByCodigo(codigoPMMD).orElseThrow(() -> new NegocioExceptionPMMD("No existe el material " + codigoPMMD + "."));
    }

    public List<MovimientoPMMD> movimientos() {
        return movimientoRepositoryPMMD.findAllByOrderByFechaDescIdDesc();
    }

    public List<MovimientoPMMD> kardex(Long materialIdPMMD) {
        return movimientoRepositoryPMMD.findByMaterialIdOrderByFechaAscIdAsc(materialIdPMMD);
    }



    public List<MaterialPMMD> bajoMinimo() {
        return materiales().stream().filter(MaterialPMMD::isBajoMinimo)
                .sorted(Comparator.comparingDouble(mPMMD -> mPMMD.getDisponible() / Math.max(0.01, mPMMD.getMinimo())))
                .toList();
    }

    public List<MaterialPMMD> criticos() {
        return materiales().stream().filter(MaterialPMMD::isCritico).toList();
    }

    public double valorTotal() {
        return materiales().stream().mapToDouble(MaterialPMMD::getValor).sum();
    }

    public long movimientosDelMes() {
        LocalDate inicioPMMD = AppClockPMMD.hoy().withDayOfMonth(1);
        return movimientoRepositoryPMMD.findAll().stream().filter(mPMMD -> !mPMMD.getFecha().toLocalDate().isBefore(inicioPMMD)).count();
    }

    public String siguienteCodigo() {
        return CodigosPMMD.siguiente(materialRepositoryPMMD.findAll(), MaterialPMMD::getCodigo, "MP-", 3);
    }

    @Transactional
    public MaterialPMMD guardarMaterial(MaterialPMMD material) {
        if (material.getNombre() == null || material.getNombre().isBlank()) throw new NegocioExceptionPMMD("El nombre del material es obligatorio.");
        if (material.getCosto() == null || material.getCosto() < 0) throw new NegocioExceptionPMMD("Ingresa un costo unitario válido.");
        if (material.getStock() < 0 || material.getMinimo() < 0) throw new NegocioExceptionPMMD("Las cantidades no pueden ser negativas.");
        boolean nuevoPMMD = material.getId() == null;
        if (nuevoPMMD) material.setCodigo(siguienteCodigo());
        double inicialPMMD = material.getStock();
        if (nuevoPMMD) material.setStock(0d);
        MaterialPMMD gPMMD = materialRepositoryPMMD.save(material);
        if (nuevoPMMD && inicialPMMD > 0) registrar(gPMMD, TipoMovimientoPMMD.ENTRADA, inicialPMMD, "INI-" + gPMMD.getCodigo(), "Inventario inicial");
        return gPMMD;
    }






    @Transactional
    public MovimientoPMMD movimientoManual(Long materialIdPMMD, TipoMovimientoPMMD tipoPMMD, double cantidadPMMD, Long telaIdPMMD,
                                       String referenciaPMMD, String motivoPMMD) {
        if (tipoPMMD != TipoMovimientoPMMD.ENTRADA && tipoPMMD != TipoMovimientoPMMD.SALIDA && tipoPMMD != TipoMovimientoPMMD.AJUSTE) {
            throw new NegocioExceptionPMMD("Movimiento no permitido.");
        }
        if (tipoPMMD == TipoMovimientoPMMD.AJUSTE ? cantidadPMMD == 0 : cantidadPMMD <= 0) throw new NegocioExceptionPMMD("La cantidad debe ser mayor a cero.");
        MaterialPMMD mPMMD = material(materialIdPMMD);
        if (mPMMD.isTela()) {
            if (telaIdPMMD == null) throw new NegocioExceptionPMMD("Elige el color de la tela.");
            TelaPMMD tPMMD = telaRepositoryPMMD.findById(telaIdPMMD).orElseThrow();
            double efectoPMMD = tipoPMMD == TipoMovimientoPMMD.SALIDA ? -cantidadPMMD : cantidadPMMD;
            if (tPMMD.getStock() + efectoPMMD < -1e-9) throw new NegocioExceptionPMMD("El color " + tPMMD.getColor() + " solo tiene " + fmt(tPMMD.getStock()) + " m.");
            tPMMD.setStock(tPMMD.getStock() + efectoPMMD);
            telaRepositoryPMMD.save(tPMMD);
            motivoPMMD = (motivoPMMD == null || motivoPMMD.isBlank() ? tipoPMMD.nombre() : motivoPMMD) + " · " + tPMMD.getColor() + " " + tPMMD.getCodigo();
        }
        return registrar(mPMMD, tipoPMMD, cantidadPMMD, referenciaPMMD, motivoPMMD);
    }






    @Transactional
    public MovimientoPMMD registrar(MaterialPMMD material, TipoMovimientoPMMD tipoPMMD, double cantidadPMMD, String referenciaPMMD, String conceptoPMMD) {
        double saldoPMMD = material.getStock();
        double efectoPMMD = switch (tipoPMMD) {
            case ENTRADA -> cantidadPMMD;
            case SALIDA, CONSUMO_OP -> -cantidadPMMD;
            case AJUSTE -> cantidadPMMD;
            default -> 0;
        };
        if (saldoPMMD + efectoPMMD < -1e-9) {
            throw new NegocioExceptionPMMD("Stock insuficiente: hay " + fmt(saldoPMMD) + " " + material.getUnidad() + " de " + material.getNombre() + ".");
        }
        if (tipoPMMD == TipoMovimientoPMMD.SALIDA && saldoPMMD + efectoPMMD < material.getReservado() - 1e-9) {
            throw new NegocioExceptionPMMD("No se puede sacar material reservado para producción: disponible "
                    + fmt(material.getDisponible()) + " " + material.getUnidad() + ".");
        }
        material.setStock(Math.max(0, saldoPMMD + efectoPMMD));
        materialRepositoryPMMD.save(material);
        return movimientoRepositoryPMMD.save(MovimientoPMMD.builder()
                .material(material).fecha(LocalDateTime.of(AppClockPMMD.hoy(), AppClockPMMD.HORA)).tipo(tipoPMMD)
                .cantidad(cantidadPMMD).saldo(material.getStock())
                .referencia(referenciaPMMD == null || referenciaPMMD.isBlank() ? "—" : referenciaPMMD.trim())
                .concepto(conceptoPMMD == null || conceptoPMMD.isBlank() ? tipoPMMD.nombre() : conceptoPMMD.trim())
                .usuario(usuarioPMMD.username()).build());
    }

    public static String fmt(double n) {
        return n == Math.floor(n) ? String.valueOf((long) n) : String.format(java.util.Locale.US, "%.2f", n);
    }
}
