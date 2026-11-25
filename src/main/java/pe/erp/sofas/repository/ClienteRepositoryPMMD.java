package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepositoryPMMD extends JpaRepository<ClientePMMD, Long> {
    
    Optional<ClientePMMD> findByCodigo(String codigoPMMD);

    List<ClientePMMD> findAllByOrderByCodigoAsc();
}
