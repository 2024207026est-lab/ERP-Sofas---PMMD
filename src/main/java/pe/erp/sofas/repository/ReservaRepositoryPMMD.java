package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepositoryPMMD extends JpaRepository<ReservaPMMD, Long> {
    
    List<ReservaPMMD> findByOpId(Long opIdPMMD);

    Optional<ReservaPMMD> findByOpIdAndMaterialId(Long opIdPMMD, Long materialIdPMMD);

    List<ReservaPMMD> findByMaterialId(Long materialIdPMMD);
}
