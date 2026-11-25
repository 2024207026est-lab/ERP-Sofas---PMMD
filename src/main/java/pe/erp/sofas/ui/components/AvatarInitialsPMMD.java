package pe.erp.sofas.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;



public class AvatarInitialsPMMD extends StackPane {

    public AvatarInitialsPMMD(String nombre, double size, String variantePMMD) {
        Label lPMMD = new Label(iniciales(nombre));
        lPMMD.getStyleClass().add("avatar-text");
        lPMMD.setStyle("-fx-font-size: " + Math.round(size * 0.38) + "px;");
        getChildren().add(lPMMD);
        getStyleClass().addAll("avatar", "avatar-" + variantePMMD);
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
    }



    public static AvatarInitialsPMMD conColor(String nombre, double size, String colorPMMD) {
        AvatarInitialsPMMD aPMMD = new AvatarInitialsPMMD(nombre, size, "custom");
        aPMMD.setStyle("-fx-background-color: " + colorPMMD + ";");
        return aPMMD;
    }

    public static String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "?";
        String[] pPMMD = nombre.replaceAll("[^\\p{L} ]", " ").trim().split("\\s+");
        if (pPMMD.length == 1) return pPMMD[0].substring(0, Math.min(2, pPMMD[0].length())).toUpperCase();
        return ("" + pPMMD[0].charAt(0) + pPMMD[1].charAt(0)).toUpperCase();
    }
}
