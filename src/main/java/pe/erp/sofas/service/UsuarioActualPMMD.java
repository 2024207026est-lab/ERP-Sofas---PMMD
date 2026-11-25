package pe.erp.sofas.service;

import org.springframework.stereotype.Component;
import pe.erp.sofas.model.RolPMMD;



@Component
public class UsuarioActualPMMD {

    private String username = "sistema";

    private String nombre = "Sistema";

    private RolPMMD rol = RolPMMD.GERENTE;

    public void fijar(String username, String nombre, RolPMMD rol) {
        this.username = username;
        this.nombre = nombre;
        this.rol = rol;
    }

    public String username() {
        return username;
    }

    public String nombre() {
        return nombre;
    }

    public RolPMMD rol() {
        return rol;
    }
}
