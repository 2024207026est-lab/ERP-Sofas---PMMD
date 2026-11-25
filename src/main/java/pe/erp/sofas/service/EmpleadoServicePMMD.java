package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.ConsumoRealRepositoryPMMD;
import pe.erp.sofas.repository.EmpleadoRepositoryPMMD;
import pe.erp.sofas.repository.OrdenProduccionRepositoryPMMD;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpleadoServicePMMD {
    


    public record Productividad(
      EmpleadoPMMD empleado, 
        int sofasTerminados, 
          int sofasEnEtapa, 
            double tarifa, 
              double destajo) {
    }

    private final EmpleadoRepositoryPMMD repositoryPMMD;

    private final OrdenProduccionRepositoryPMMD opRepositoryPMMD;

    private final ConsumoRealRepositoryPMMD consumoRepositoryPMMD;

    private final ParametroServicePMMD parametrosPMMD;

    public List<EmpleadoPMMD> listar() {
        return repositoryPMMD.findAllByOrderByCodigoAsc();
    }

    public List<EmpleadoPMMD> taller() {
        return listar().stream().filter(e -> e.isTaller() && !"Inactivo".equals(e.getEstado())).toList();
    }

    public EmpleadoPMMD empleado(Long idPMMD) {
        return repositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("El empleado ya no existe."));
    }

    @Transactional
    public EmpleadoPMMD guardar(EmpleadoPMMD e) {
        if (e.getNombre() == null || e.getNombre().isBlank()) throw new NegocioExceptionPMMD("El nombre es obligatorio.");
        if (e.getDni() != null && !e.getDni().isBlank() && !e.getDni().matches("\\d{8}")) throw new NegocioExceptionPMMD("El DNI debe tener 8 dígitos.");
        if (e.getId() == null) e.setCodigo(CodigosPMMD.siguiente(repositoryPMMD.findAll(), EmpleadoPMMD::getCodigo, "E-", 2));
        return repositoryPMMD.save(e);
    }






    public List<Productividad> productividad() {
        LocalDate inicioPMMD = AppClockPMMD.hoy().withDayOfMonth(1);
        List<OrdenProduccionPMMD> opsPMMD = opRepositoryPMMD.findAll().stream().filter(oPMMD -> !oPMMD.getAnulada()).toList();
        List<Productividad> rPMMD = new ArrayList<>();
        for (EmpleadoPMMD e : taller()) {
            int terminadosPMMD = opsPMMD.stream().filter(oPMMD -> oPMMD.getEtapa() == EtapaPMMD.LISTO && oPMMD.getFechaFin() != null
                            && !oPMMD.getFechaFin().isBefore(inicioPMMD) && oPMMD.getResponsable() != null && oPMMD.getResponsable().getId().equals(e.getId()))
                    .mapToInt(OrdenProduccionPMMD::getCantidad).sum();
            int enEtapa = 0;
            if (e.getEtapa() != null) {
                enEtapa = opsPMMD.stream().filter(oPMMD -> oPMMD.getEtapa().ordinal() > e.getEtapa().ordinal()
                                && (oPMMD.getEtapa() != EtapaPMMD.LISTO || (oPMMD.getFechaFin() != null && !oPMMD.getFechaFin().isBefore(inicioPMMD))))
                        .mapToInt(OrdenProduccionPMMD::getCantidad).sum();

                enEtapa += historicoMesPMMD(e);
            }
            double tarifa = e.getEtapa() == null ? 0 : parametrosPMMD.tarifaDestajo(e.getEtapa());
            rPMMD.add(new Productividad(e, terminadosPMMD + historicoTerminadosPMMD(e), enEtapa, tarifa, tarifa * enEtapa));
        }
        return rPMMD;
    }



    private int historicoMesPMMD(EmpleadoPMMD e) {
        LocalDate inicioPMMD = AppClockPMMD.hoy().withDayOfMonth(1);
        return (int) consumoRepositoryPMMD.findAll().stream().filter(cPMMD -> cPMMD.getFecha() != null && !cPMMD.getFecha().isBefore(inicioPMMD))
                .map(ConsumoRealPMMD::getOpCodigo).distinct().count();
    }

    private int historicoTerminadosPMMD(EmpleadoPMMD e) {
        LocalDate inicioPMMD = AppClockPMMD.hoy().withDayOfMonth(1);
        return (int) consumoRepositoryPMMD.findAll().stream().filter(cPMMD -> cPMMD.getFecha() != null && !cPMMD.getFecha().isBefore(inicioPMMD)
                        && e.getNombre().equals(cPMMD.getResponsable())).map(ConsumoRealPMMD::getOpCodigo).distinct().count();
    }
}
