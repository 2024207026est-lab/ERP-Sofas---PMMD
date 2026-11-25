package pe.erp.sofas.ui.components;

import javafx.scene.Node;
import javafx.stage.FileChooser;

import java.io.File;



public final class ArchivosPMMD {

    private static File ultimaCarpetaPMMD;

    private ArchivosPMMD() {
    }

    public static File guardar(Node anclaPMMD, String nombrePMMD, String descripcion, String extensionPMMD) {
        FileChooser fcPMMD = new FileChooser();
        fcPMMD.setTitle("Guardar " + descripcion);
        fcPMMD.setInitialFileName(nombrePMMD + "." + extensionPMMD);
        fcPMMD.getExtensionFilters().add(new FileChooser.ExtensionFilter(descripcion, "*." + extensionPMMD));
        File dirPMMD = ultimaCarpetaPMMD != null ? ultimaCarpetaPMMD : new File(System.getProperty("user.home"), "Documents");
        if (dirPMMD.isDirectory()) fcPMMD.setInitialDirectory(dirPMMD);
        File fPMMD = fcPMMD.showSaveDialog(anclaPMMD == null || anclaPMMD.getScene() == null ? null : anclaPMMD.getScene().getWindow());
        if (fPMMD != null) ultimaCarpetaPMMD = fPMMD.getParentFile();
        return fPMMD;
    }
}
