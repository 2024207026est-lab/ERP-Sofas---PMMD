package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.UsuarioPMMD;
import pe.erp.sofas.repository.UsuarioRepositoryPMMD;






@Service
@RequiredArgsConstructor
public class AuthServicePMMD {

    public static final String USUARIO_DEMO = "cmendoza";

    private final UsuarioRepositoryPMMD repositoryPMMD;

    private final UsuarioActualPMMD usuarioActualPMMD;

    @Value("${app.auth.modo-prototipo:false}")
    private boolean modoPrototipo;

    @Transactional
    public UsuarioPMMD login(String usernamePMMD, String passwordPMMD) {
        String userPMMD = usernamePMMD == null ? "" : usernamePMMD.trim();
        if (userPMMD.isEmpty() || passwordPMMD == null || passwordPMMD.isEmpty()) throw new NegocioExceptionPMMD("Ingresa tu usuario y contraseña.");
        var encontradoPMMD = repositoryPMMD.findByUsernameIgnoreCase(userPMMD);
        UsuarioPMMD uPMMD;
        if (modoPrototipo) {
            uPMMD = encontradoPMMD.filter(UsuarioPMMD::getActivo).orElseGet(() -> repositoryPMMD.findByUsernameIgnoreCase(USUARIO_DEMO).orElseThrow());
        } else {
            uPMMD = encontradoPMMD.orElseThrow(() -> new NegocioExceptionPMMD("Usuario o contraseña incorrectos."));
            if (!uPMMD.getActivo()) throw new NegocioExceptionPMMD("El usuario está desactivado.");
            if (!verificar(passwordPMMD, uPMMD.getPasswordHash())) throw new NegocioExceptionPMMD("Usuario o contraseña incorrectos.");
        }
        uPMMD.setUltimoAcceso(AppClockPMMD.hoy());
        usuarioActualPMMD.fijar(uPMMD.getUsername(), uPMMD.getNombre(), uPMMD.getRol());
        return repositoryPMMD.save(uPMMD);
    }



    public UsuarioPMMD autorizar(String usernamePMMD, String passwordPMMD) {
        UsuarioPMMD uPMMD = repositoryPMMD.findByUsernameIgnoreCase(usernamePMMD == null ? "" : usernamePMMD.trim())
                .orElseThrow(() -> new NegocioExceptionPMMD("Usuario no encontrado."));
        if (!uPMMD.getRol().autoriza()) throw new NegocioExceptionPMMD(uPMMD.getNombre() + " no puede autorizar: se requiere un Supervisor.");
        if (!verificar(passwordPMMD == null ? "" : passwordPMMD, uPMMD.getPasswordHash())) throw new NegocioExceptionPMMD("Contraseña de autorización incorrecta.");
        return uPMMD;
    }

    public java.util.List<UsuarioPMMD> autorizadores() {
        return repositoryPMMD.findAll().stream().filter(uPMMD -> uPMMD.getActivo() && uPMMD.getRol().autoriza()).toList();
    }

    public static String hash(String passwordPMMD) {
        return BCrypt.hashpw(passwordPMMD, BCrypt.gensalt(10));
    }

    public static boolean verificar(String passwordPMMD, String hash) {
        try {
            return hash != null && BCrypt.checkpw(passwordPMMD, hash);
        } catch (IllegalArgumentException ePMMD) {
            return false;
        }
    }
}
