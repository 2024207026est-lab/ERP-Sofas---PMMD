package pe.erp.sofas.core;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.model.RolPMMD;
import pe.erp.sofas.model.UsuarioPMMD;
import pe.erp.sofas.service.UsuarioActualPMMD;



@Component
@Lazy
@RequiredArgsConstructor
public class SessionPMMD {

    private final UsuarioActualPMMD usuarioActualPMMD;

    private UsuarioPMMD usuario;

    public UsuarioPMMD usuario() {
        return usuario;
    }

    public void iniciar(UsuarioPMMD usuario) {
        this.usuario = usuario;
        usuarioActualPMMD.fijar(usuario.getUsername(), usuario.getNombre(), usuario.getRol());
    }

    public void cerrar() {
        this.usuario = null;
    }

    public String nombre() {
        return usuario == null ? "Carlos Mendoza" : usuario.getNombre();
    }

    public String cargo() {
        return usuario == null ? "Gerente General" : usuario.getCargo();
    }

    public RolPMMD rol() {
        return usuario == null ? RolPMMD.GERENTE : usuario.getRol();
    }

    public String username() {
        return usuario == null ? "cmendoza" : usuario.getUsername();
    }
}
