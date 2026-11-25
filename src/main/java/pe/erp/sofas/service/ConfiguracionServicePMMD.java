package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.EmpresaRepositoryPMMD;
import pe.erp.sofas.repository.PermisoRepositoryPMMD;
import pe.erp.sofas.repository.UsuarioRepositoryPMMD;

import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguracionServicePMMD {

    private final EmpresaRepositoryPMMD empresaRepositoryPMMD;

    private final UsuarioRepositoryPMMD usuarioRepositoryPMMD;

    private final PermisoRepositoryPMMD permisoRepositoryPMMD;

    public EmpresaPMMD empresa() {
        return empresaRepositoryPMMD.findById(1L).orElseGet(() -> EmpresaPMMD.builder().id(1L).razonSocial("SISTEMA ERP SOFAS S.A.C.").build());
    }

    @Transactional
    public EmpresaPMMD guardarEmpresa(EmpresaPMMD ePMMD) {
        if (ePMMD.getRazonSocial() == null || ePMMD.getRazonSocial().isBlank()) throw new NegocioExceptionPMMD("La razón social es obligatoria.");
        if (ePMMD.getRuc() == null || !ePMMD.getRuc().matches("\\d{11}")) throw new NegocioExceptionPMMD("El RUC debe tener 11 dígitos.");
        ePMMD.setId(1L);
        return empresaRepositoryPMMD.save(ePMMD);
    }

    public List<UsuarioPMMD> usuarios() {
        return usuarioRepositoryPMMD.findAll();
    }

    @Transactional
    public UsuarioPMMD guardarUsuario(UsuarioPMMD uPMMD, String passwordPMMD) {
        if (uPMMD.getUsername() == null || !uPMMD.getUsername().matches("[a-zA-Z0-9._]{3,30}")) throw new NegocioExceptionPMMD("Usuario de 3 a 30 caracteres, sin espacios.");
        if (uPMMD.getNombre() == null || uPMMD.getNombre().isBlank()) throw new NegocioExceptionPMMD("El nombre es obligatorio.");
        boolean duplicadoPMMD = usuarios().stream().anyMatch(xPMMD -> xPMMD.getUsername().equalsIgnoreCase(uPMMD.getUsername()) && !xPMMD.getId().equals(uPMMD.getId()));
        if (duplicadoPMMD) throw new NegocioExceptionPMMD("El usuario " + uPMMD.getUsername() + " ya existe.");
        if (uPMMD.getId() == null && (passwordPMMD == null || passwordPMMD.length() < 6)) throw new NegocioExceptionPMMD("La contraseña debe tener al menos 6 caracteres.");
        if (passwordPMMD != null && !passwordPMMD.isEmpty()) uPMMD.setPasswordHash(AuthServicePMMD.hash(passwordPMMD));
        return usuarioRepositoryPMMD.save(uPMMD);
    }

    public List<PermisoPMMD> permisos(RolPMMD rolPMMD) {
        return permisoRepositoryPMMD.findByRol(rolPMMD);
    }

    @Transactional
    public PermisoPMMD fijarPermiso(RolPMMD rolPMMD, String moduloPMMD, boolean verPMMD, boolean editarPMMD) {
        PermisoPMMD pPMMD = permisoRepositoryPMMD.findByRolAndModulo(rolPMMD, moduloPMMD).orElseGet(() -> PermisoPMMD.builder().rol(rolPMMD).modulo(moduloPMMD).build());
        pPMMD.setVer(verPMMD || editarPMMD);
        pPMMD.setEditar(editarPMMD);
        return permisoRepositoryPMMD.save(pPMMD);
    }

    public boolean puedeVer(RolPMMD rolPMMD, String moduloPMMD) {
        if (rolPMMD == RolPMMD.GERENTE) return true;
        return permisoRepositoryPMMD.findByRolAndModulo(rolPMMD, moduloPMMD).map(PermisoPMMD::getVer).orElse(false);
    }
}
