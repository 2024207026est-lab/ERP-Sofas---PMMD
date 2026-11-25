package pe.erp.sofas.ui.components;



public class ToggleSwitchPMMD extends atlantafx.base.controls.ToggleSwitch {

    public ToggleSwitchPMMD(boolean seleccionadoPMMD) {
        setSelected(seleccionadoPMMD);
        getStyleClass().add("is-switch");
    }

    public ToggleSwitchPMMD(String textoPMMD, boolean seleccionadoPMMD) {
        super(textoPMMD);
        setSelected(seleccionadoPMMD);
        getStyleClass().add("is-switch");
    }
}
