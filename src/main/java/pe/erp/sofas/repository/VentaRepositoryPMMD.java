package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepositoryPMMD extends JpaRepository<VentaPMMD, Long> {
    
    List<VentaPMMD> findAllByOrderByFechaDesc();

    List<VentaPMMD> findBySerie(String seriePMMD);
}
