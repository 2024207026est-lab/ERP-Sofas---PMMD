package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisoRepositoryPMMD extends JpaRepository<PermisoPMMD, Long> {
    
    List<PermisoPMMD> findByRol(RolPMMD rolPMMD);

    Optional<PermisoPMMD> findByRolAndModulo(RolPMMD rolPMMD, String moduloPMMD);
}
