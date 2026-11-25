package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoRepositoryPMMD extends JpaRepository<MovimientoPMMD, Long> {
    
    List<MovimientoPMMD> findAllByOrderByFechaDescIdDesc();

    List<MovimientoPMMD> findByMaterialIdOrderByFechaAscIdAsc(Long materialIdPMMD);
}
