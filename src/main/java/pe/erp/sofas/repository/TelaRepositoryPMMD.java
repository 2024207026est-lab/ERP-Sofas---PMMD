package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface TelaRepositoryPMMD extends JpaRepository<TelaPMMD, Long> {
    
    Optional<TelaPMMD> findByCodigo(String codigoPMMD);

    List<TelaPMMD> findAllByOrderByCodigoAsc();

    List<TelaPMMD> findByTipo(TipoTelaPMMD tipoPMMD);
}
