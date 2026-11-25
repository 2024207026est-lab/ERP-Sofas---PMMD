package pe.erp.sofas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.erp.sofas.model.ConfiguracionGuardadaPMMD;
import pe.erp.sofas.model.UsuarioPMMD;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionGuardadaRepositoryPMMD extends JpaRepository<ConfiguracionGuardadaPMMD, Long> {
    
    List<ConfiguracionGuardadaPMMD> findByUsuarioOrderByFechaCreacionDesc(UsuarioPMMD usuarioPMMD);

    List<ConfiguracionGuardadaPMMD> findByUsuarioAndEsFavoritoTrueOrderByFechaCreacionDesc(UsuarioPMMD usuarioPMMD);

    Optional<ConfiguracionGuardadaPMMD> findByIdAndUsuario(Long idPMMD, UsuarioPMMD usuarioPMMD);

    List<ConfiguracionGuardadaPMMD> findByUsuarioAndEtiquetaOrderByFechaCreacionDesc(UsuarioPMMD usuarioPMMD, String etiquetaPMMD);

    long countByUsuario(UsuarioPMMD usuarioPMMD);
}
