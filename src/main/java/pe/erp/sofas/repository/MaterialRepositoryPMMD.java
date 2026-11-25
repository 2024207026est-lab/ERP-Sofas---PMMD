package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepositoryPMMD extends JpaRepository<MaterialPMMD, Long> {
    
    Optional<MaterialPMMD> findByCodigo(String codigoPMMD);

    List<MaterialPMMD> findAllByOrderByCodigoAsc();
}
