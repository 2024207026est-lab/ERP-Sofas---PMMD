package pe.erp.sofas.core;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;






@Component
@Lazy
public class NavigatorPMMD {
    


    public interface Host {
        
        void mostrar(ModuloPMMD moduloPMMD, IntentPMMD intentPMMD, boolean registrarHistorialPMMD);

        boolean regresar();

        void refrescarContadores();

        void cerrarSesion();
    }

    private Host host;

    public void registrar(Host host) {
        this.host = host;
    }

    public void goTo(String moduloPMMD) {
        goTo(moduloPMMD, IntentPMMD.NONE);
    }

    public void goTo(String moduloPMMD, IntentPMMD intentPMMD) {
        if (host != null) host.mostrar(ModuloPMMD.porClave(moduloPMMD), intentPMMD, true);
    }

    public boolean back() {
        return host != null && host.regresar();
    }



    public void cerrarSesion() {
        if (host != null) host.cerrarSesion();
    }



    public void datosCambiaron() {
        if (host != null) host.refrescarContadores();
    }
}
