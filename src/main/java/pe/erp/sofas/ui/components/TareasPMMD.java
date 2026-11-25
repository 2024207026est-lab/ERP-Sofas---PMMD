package pe.erp.sofas.ui.components;

import javafx.concurrent.Task;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.core.OverlaysPMMD;
import pe.erp.sofas.core.ToastPMMD;

import java.util.function.Consumer;
import java.util.function.Supplier;



public final class TareasPMMD {

    private TareasPMMD() {
    }

    public static <T> void ejecutar(OverlaysPMMD overlaysPMMD, ToastPMMD toastPMMD, String mensajePMMD, Supplier<T> trabajo, Consumer<T> alTerminarPMMD) {
        OverlaysPMMD.Handle esperaPMMD = overlaysPMMD.cargando(mensajePMMD);
        Task<T> t = new Task<>() {

            @Override
            protected T call() {
                return trabajo.get();
            }
        };
        t.setOnSucceeded(ePMMD -> {
            esperaPMMD.cerrar();
            alTerminarPMMD.accept(t.getValue());
        });
        t.setOnFailed(ePMMD -> {
            esperaPMMD.cerrar();
            Throwable ex = t.getException();
            while (ex != null && !(ex instanceof NegocioExceptionPMMD) && ex.getCause() != null) ex = ex.getCause();
            toastPMMD.error(ex instanceof NegocioExceptionPMMD ? ex.getMessage() : "No se pudo completar: " + (ex == null ? "error" : ex.getMessage()));
            if (!(ex instanceof NegocioExceptionPMMD) && t.getException() != null) {
                org.slf4j.LoggerFactory.getLogger(TareasPMMD.class).error("Tarea fallida", t.getException());
            }
        });
        Thread hiloPMMD = new Thread(t, "innova-tarea");
        hiloPMMD.setDaemon(true);
        hiloPMMD.start();
    }
}
