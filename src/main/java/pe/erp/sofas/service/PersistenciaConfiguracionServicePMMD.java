package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.model.ConfiguracionGuardadaPMMD;
import pe.erp.sofas.model.UsuarioPMMD;
import pe.erp.sofas.repository.ConfiguracionGuardadaRepositoryPMMD;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersistenciaConfiguracionServicePMMD {

    private final ConfiguracionGuardadaRepositoryPMMD repoPMMD;

    public ConfiguracionGuardadaPMMD guardar(UsuarioPMMD usuarioPMMD, ConfigSofaPMMD configPMMD, String nombrePMMD, String descripcionPMMD) {
        ConfiguracionGuardadaPMMD guardadaPMMD = ConfiguracionGuardadaPMMD.fromConfigSofa(configPMMD, usuarioPMMD, nombrePMMD, descripcionPMMD);
        return repoPMMD.save(guardadaPMMD);
    }

    public ConfigSofaPMMD cargar(Long idPMMD, UsuarioPMMD usuarioPMMD) {
        ConfiguracionGuardadaPMMD guardadaPMMD = repoPMMD.findByIdAndUsuario(idPMMD, usuarioPMMD)
            .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));
        return guardadaPMMD.toConfigSofa();
    }

    public Optional<ConfiguracionGuardadaPMMD> obtener(Long idPMMD, UsuarioPMMD usuarioPMMD) {
        return repoPMMD.findByIdAndUsuario(idPMMD, usuarioPMMD);
    }

    public List<ConfiguracionGuardadaPMMD> listarPorUsuario(UsuarioPMMD usuarioPMMD) {
        return repoPMMD.findByUsuarioOrderByFechaCreacionDesc(usuarioPMMD);
    }

    public List<ConfiguracionGuardadaPMMD> listarFavoritos(UsuarioPMMD usuarioPMMD) {
        return repoPMMD.findByUsuarioAndEsFavoritoTrueOrderByFechaCreacionDesc(usuarioPMMD);
    }

    public List<ConfiguracionGuardadaPMMD> listarPorEtiqueta(UsuarioPMMD usuarioPMMD, String etiquetaPMMD) {
        return repoPMMD.findByUsuarioAndEtiquetaOrderByFechaCreacionDesc(usuarioPMMD, etiquetaPMMD);
    }

    public ConfiguracionGuardadaPMMD marcarFavorito(Long idPMMD, UsuarioPMMD usuarioPMMD, boolean esFavoritoPMMD) {
        ConfiguracionGuardadaPMMD guardadaPMMD = repoPMMD.findByIdAndUsuario(idPMMD, usuarioPMMD)
            .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));
        guardadaPMMD.setEsFavorito(esFavoritoPMMD);
        return repoPMMD.save(guardadaPMMD);
    }

    public ConfiguracionGuardadaPMMD actualizar(Long idPMMD, UsuarioPMMD usuarioPMMD, String nombrePMMD, String descripcionPMMD, String etiquetaPMMD) {
        ConfiguracionGuardadaPMMD guardadaPMMD = repoPMMD.findByIdAndUsuario(idPMMD, usuarioPMMD)
            .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));
        guardadaPMMD.setNombre(nombrePMMD);
        guardadaPMMD.setDescripcion(descripcionPMMD);
        guardadaPMMD.setEtiqueta(etiquetaPMMD);
        return repoPMMD.save(guardadaPMMD);
    }

    public void eliminar(Long idPMMD, UsuarioPMMD usuarioPMMD) {
        ConfiguracionGuardadaPMMD guardadaPMMD = repoPMMD.findByIdAndUsuario(idPMMD, usuarioPMMD)
            .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));
        repoPMMD.delete(guardadaPMMD);
    }

    public long contarPorUsuario(UsuarioPMMD usuarioPMMD) {
        return repoPMMD.countByUsuario(usuarioPMMD);
    }
}
