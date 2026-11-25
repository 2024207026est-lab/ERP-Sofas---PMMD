package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.CarritoPMMD;
import pe.erp.sofas.model.UsuarioPMMD;

import java.util.Optional;

@Repository
public interface CarritoRepositoryPMMD extends JpaRepository<CarritoPMMD, Long> {
    
    Optional<CarritoPMMD> findByUsuario(UsuarioPMMD usuarioPMMD);

    void deleteByUsuario(UsuarioPMMD usuarioPMMD);
}
