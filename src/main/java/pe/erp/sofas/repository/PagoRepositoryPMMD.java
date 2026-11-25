package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepositoryPMMD extends JpaRepository<PagoPMMD, Long> {
    
    List<PagoPMMD> findByClienteIdOrderByFechaDesc(Long clienteIdPMMD);

    List<PagoPMMD> findAllByOrderByFechaDesc();
}
