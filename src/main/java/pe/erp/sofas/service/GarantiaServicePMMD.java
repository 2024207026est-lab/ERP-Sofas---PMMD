package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.EstadoReclamoPMMD;
import pe.erp.sofas.model.ReclamoGarantiaPMMD;
import pe.erp.sofas.repository.ClienteRepositoryPMMD;
import pe.erp.sofas.repository.ProductoRepositoryPMMD;
import pe.erp.sofas.repository.ReclamoGarantiaRepositoryPMMD;
import pe.erp.sofas.service.calculo.CoberturaPMMD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GarantiaServicePMMD {

    public static final int SLA_DIAS = 7;

    public record Kpis(
      long abiertos, 
        double pctCubiertos, 
          double diasPromedio, 
            BigDecimal costo) {
    }

    private final ReclamoGarantiaRepositoryPMMD repositoryPMMD;

    private final ClienteRepositoryPMMD clienteRepositoryPMMD;

    private final ProductoRepositoryPMMD productoRepositoryPMMD;

    public List<ReclamoGarantiaPMMD> listar() {
        return repositoryPMMD.findAllByOrderByCodigoDesc();
    }

    public ReclamoGarantiaPMMD reclamo(Long idPMMD) {
        return repositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("El reclamo ya no existe."));
    }

    public long abiertos() {
        return listar().stream().filter(rPMMD -> !rPMMD.getEstado().cerrado()).count();
    }

    public CoberturaPMMD.Resultado cobertura(ReclamoGarantiaPMMD rPMMD) {
        return CoberturaPMMD.evaluar(rPMMD.getFalla(), rPMMD.getFechaCompra(), rPMMD.getFecha());
    }



    public long sla(ReclamoGarantiaPMMD rPMMD) {
        LocalDate finPMMD = rPMMD.getFechaCierre() == null ? AppClockPMMD.hoy() : rPMMD.getFechaCierre();
        return ChronoUnit.DAYS.between(rPMMD.getFecha(), finPMMD);
    }

    public boolean slaVencido(ReclamoGarantiaPMMD rPMMD) {
        return !rPMMD.getEstado().cerrado() && sla(rPMMD) > SLA_DIAS;
    }

    public Kpis kpis() {
        List<ReclamoGarantiaPMMD> lPMMD = listar();
        long cubiertosPMMD = lPMMD.stream().filter(rPMMD -> cobertura(rPMMD).cubierto()).count();
        double dias = lPMMD.stream().filter(rPMMD -> rPMMD.getFechaCierre() != null).mapToLong(this::sla).average().orElse(0);
        BigDecimal costo = lPMMD.stream().map(rPMMD -> rPMMD.getCosto() == null ? BigDecimal.ZERO : rPMMD.getCosto()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Kpis(abiertos(), lPMMD.isEmpty() ? 0 : cubiertosPMMD * 100.0 / lPMMD.size(), dias, costo);
    }

    public String siguienteCodigo() {
        return CodigosPMMD.siguiente(repositoryPMMD.findAll(), ReclamoGarantiaPMMD::getCodigo, "GR-", 3);
    }

    @Transactional
    public ReclamoGarantiaPMMD registrar(Long clienteIdPMMD, Long productoIdPMMD, LocalDate fechaCompraPMMD, String fallaPMMD, String descripcionPMMD) {
        if (clienteIdPMMD == null) throw new NegocioExceptionPMMD("Elige el cliente.");
        if (fallaPMMD == null) throw new NegocioExceptionPMMD("Elige el tipo de falla.");
        if (fechaCompraPMMD == null || fechaCompraPMMD.isAfter(AppClockPMMD.hoy())) throw new NegocioExceptionPMMD("Revisa la fecha de compra.");
        return repositoryPMMD.save(ReclamoGarantiaPMMD.builder().codigo(siguienteCodigo())
                .cliente(clienteRepositoryPMMD.findById(clienteIdPMMD).orElseThrow())
                .producto(productoIdPMMD == null ? null : productoRepositoryPMMD.findById(productoIdPMMD).orElse(null))
                .fechaCompra(fechaCompraPMMD).falla(fallaPMMD).descripcion(descripcionPMMD).fecha(AppClockPMMD.hoy())
                .estado(EstadoReclamoPMMD.ABIERTO).solucion("Por definir").build());
    }

    @Transactional
    public ReclamoGarantiaPMMD actualizar(Long idPMMD, String diagnosticoPMMD, String solucionPMMD, BigDecimal costo, EstadoReclamoPMMD estadoPMMD) {
        ReclamoGarantiaPMMD rPMMD = reclamo(idPMMD);
        if (costo != null && costo.signum() < 0) throw new NegocioExceptionPMMD("El costo no puede ser negativo.");
        rPMMD.setDiagnostico(diagnosticoPMMD);
        rPMMD.setSolucion(solucionPMMD);
        rPMMD.setCosto(costo == null ? BigDecimal.ZERO : costo);
        if (estadoPMMD != null) {
            if (estadoPMMD.cerrado() && !rPMMD.getEstado().cerrado()) rPMMD.setFechaCierre(AppClockPMMD.hoy());
            if (!estadoPMMD.cerrado()) rPMMD.setFechaCierre(null);
            rPMMD.setEstado(estadoPMMD);
        }
        return repositoryPMMD.save(rPMMD);
    }
}
